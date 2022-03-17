/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.services.support.xml;

import eu.europa.ec.leos.domain.vo.ElementMatchVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.parser.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.AKOMANTOSO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.nodeToByteArray;

public class SearchEngineImpl implements SearchEngine {

    private static List<String> tagsToExclude = Arrays.asList("meta", "authorialNote");
    private static List<String> customInlineTags = Arrays.asList("authorialNote", "signature", "placeholder", "omissis", "date", "mref");

    private String searchableString;
    private List<Index> indexesForString;
    private Map<String, Element> elementsById;

    public SearchEngineImpl(byte[] content) {
        Document document = createDocument(content);
        indexContent(document);
    }

    public static SearchEngineImpl forContent(byte[] xmlContent) {
        return new SearchEngineImpl(xmlContent);
    }

    static boolean lastCharIsWhitespace(StringBuilder sb) {
        return sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ';
    }

    /**
     * Indexes the content. It does a depth first transversal of the xml content hierarchy.
     * During a visit to a node, it gathers the content text if the node is of known html type.
     */
    private void indexContent(Document document) {
        List<Element> elements = new ArrayList<>();

        Node root = XmlUtils.getFirstElementByName(document, AKOMANTOSO);
        visitNode(root, elements);

        createSearchableString(elements);
    }

    private int visitNode(Node node, List<Element> elements) {
        String tagName = node.getNodeName();
        if (tagsToExclude.contains(tagName)) {
            return 0;
        }

        boolean hasText = XmlUtils.hasChildTextNode(node);
        if (hasText) {
            return visitNodeWithText(node, elements);
        } else {
            return visitElementNode(node, elements);
        }
    }

    private int visitNodeWithText(Node node, List<Element> elements) {
        int textStartIndex = 0;

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            int contentLength;
            Node childNode = nodeList.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
                String content = childNode.getTextContent();
                addElementNode(node, elements, content, textStartIndex);
                contentLength = content.length();
            } else {
                contentLength = visitNode(childNode, elements);
            }
            textStartIndex += contentLength;
        }

        return textStartIndex;
    }

    private int visitElementNode(Node node, List<Element> elements) {
        addElementNode(node, elements, "", 0);
        int textStartIndex = 0;

        List<Node> nodeList = XmlUtils.getChildren(node);
        for (int i = 0; i < nodeList.size(); i++) {
            textStartIndex += visitNode(nodeList.get(i), elements);
        }

        return textStartIndex;
    }

    private void addElementNode(Node node, List<Element> elements, String content, int textStartIndex) {
        String tag = node.getNodeName();
        String xmlIdAttribute = XmlUtils.getAttributeValue(node, XMLID);
        String elementId = xmlIdAttribute != null ? xmlIdAttribute : tag + "_generated";

        Element element = new Element(
                elementId,
                content,
                XmlContentProcessorImpl.isEditableElement(node),
                tag,
                textStartIndex);
        elements.add(element);
    }

    private void createSearchableString(List<Element> elements) {
        indexesForString = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        elementsById = new HashMap<>();

        // Loop to extract the text content of all the elements.
        // They will be concatenated so that later a regular string search can be done.

        elements.forEach(el -> {
            if (((!Tag.isKnownTag(el.tag) && !customInlineTags.contains(el.tag)) // unknown tags are considered block tags and space is inserted after them
            || (Tag.valueOf(el.tag).isBlock() && el.startIndexOfText <= 0) // if block tag is containing any text
            || el.tag.equals("br"))) {
                if (!lastCharIsWhitespace(sb) && !el.content.startsWith(" ")) {
                    sb.append(' ');
                    // for the empty character
                    indexesForString.add(new Index(null, -1));
                }
            }

            sb.append(el.content);

            // For each character inside the content, make Index indicating the index value and position
            // of the character
            // Ex: a text of 4 chars will add 4 to the list for 0,1,2,3 positions
            for (int i = 0; i < el.content.length(); i++) {
                indexesForString.add(new Index(el.elementId, i + el.startIndexOfText));
            }

            // Preserve the element to make a reference to future if it repeats
            if (elementsById.containsKey(el.elementId)) {
                elementsById.get(el.elementId).content += el.content;
            } else {
                elementsById.put(el.elementId, el);
            }
        });
        searchableString = sb.toString();
    }

    /**
     * Searches the text in the content xml.
     * It first finds the matching text and the positional indicies. Then using the indicies,
     * it looks up in the index map build during indexing step
     *
     * @param searchText   search term
     * @param isMatchCase  if search is case sensitive
     * @param isWholeWords if search is for whole word(s)
     * @return List of matching objects
     */
    @Override
    public List<SearchMatchVO> searchText(String searchText, boolean isMatchCase, boolean isWholeWords) {
        List<SearchMatchVO> searchMatchedElements = new ArrayList<>();
        StringBuilder patternText = new StringBuilder();
        String quotedText = Pattern.quote(searchText);
        if (!isMatchCase) {
            patternText.append("(?i)");
        }
        if (isWholeWords) {
            patternText.append("\\b").append(quotedText).append("\\b");
        } else {
            patternText.append(quotedText);
        }
        Matcher matcher = Pattern.compile(patternText.toString()).matcher(searchableString);
        while (matcher.find()) {

            List<ElementMatchVO> matchedElements = new ArrayList<>();
            for (int i = matcher.start(); i < matcher.end(); i++) {
                Index idx = indexesForString.get(i);
                if (StringUtils.isEmpty(idx.elementId)) {
                    // do not include manually added spaces in the result
                    continue;
                }
                Element element = elementsById.get(idx.elementId);
                if (element.content.trim().length() == 0) {
                    // do not include blanks in the result
                    continue;
                }

                ElementMatchVO elementMatchVO = null;
                if (matchedElements.size() > 0) {
                    ElementMatchVO lastElement = matchedElements.get(matchedElements.size() - 1);
                    if (lastElement.getElementId().equals(element.elementId)) {
                        elementMatchVO = lastElement;
                    } else {
                        elementMatchVO = new ElementMatchVO(element.elementId, idx.indexInTag, element.isEditable);
                        matchedElements.add(elementMatchVO);
                    }
                } else {
                    elementMatchVO = new ElementMatchVO(element.elementId, idx.indexInTag, element.isEditable);
                    matchedElements.add(elementMatchVO);
                }

                elementMatchVO.setMatchEndIndex(idx.indexInTag + 1);
            }

            // calculate the isReplaceable based on the attributes of the matched elements and if the matched elements are cross-tags
            if (matchedElements.size() > 0) {
                searchMatchedElements.add(new SearchMatchVO(matchedElements, calculateReplaceble(matchedElements)));
            }
        }
        return searchMatchedElements;
    }

    private boolean calculateReplaceble(List<ElementMatchVO> matchedElements) {
        boolean replaceable = true;
        for (ElementMatchVO elementMatchVO : matchedElements) {
            if (!elementMatchVO.isEditable()) {
                replaceable = false;
                break;
            }
        }
        return replaceable;
    }

    @Override
    public byte[] replace(byte[] docContent, List<SearchMatchVO> searchMatchVOs, String searchText, String replaceText, boolean removeEmptyTags) {
        Document document = createDocument(docContent);
        int replacedContentDiffLength = replaceText.length() - searchText.length();
        for (int i = 0; i < searchMatchVOs.size(); i++) {
            SearchMatchVO smVO = searchMatchVOs.get(i);
            replace(document, smVO, replaceText, true);
            // update positions of elements that have replaced texts
            List<String> elementIdsReplaced = smVO.getMatchedElements().stream().map(ElementMatchVO::getElementId).collect(Collectors.toList());
            for (int j = i + 1; j < searchMatchVOs.size(); j++) {
                SearchMatchVO matchesSVO = searchMatchVOs.get(j);
                matchesSVO.getMatchedElements().stream()
                        .filter(matchedElement -> elementIdsReplaced.contains(matchedElement.getElementId())
                                && (matchedElement.getMatchStartIndex() > 0))
                        .forEach(matchedElement -> {
                            matchedElement.setMatchStartIndex(matchedElement.getMatchStartIndex() + replacedContentDiffLength);
                            matchedElement.setMatchEndIndex(matchedElement.getMatchEndIndex() + replacedContentDiffLength);
                        });
            }
        }
        return nodeToByteArray(document);
    }

    /**
     * replaces the given text
     * 1. if the entire text to replace is inside an inline tag, then the replaced text will be inside the inline tag
     * 2. if only part of the text to replace is inside an inline tag, then that part will be replaced with empty string.
     *        If that inline tag becomes empty then it should be removed ideally
     * 3.  authorialNote tags will be preserved
     *
     * @param document
     * @param smVO
     * @param replaceText
     * @param removeEmptyTags
     *
     */
    private void replace(Document document, SearchMatchVO smVO, String replaceText, boolean removeEmptyTags) {
        if (smVO.isReplaceable()) {
            if (StringUtils.isEmpty(replaceText)) {
                replaceText = "";
            }
            int startIndex = 0, replaceLength = replaceText.length();
            List<String> replaceTextSegments = new ArrayList<>();
            // split the search text into as many segments as elementmatchVOs.
            // At the same time, also make corresponding replace segments for a search segment (criteria is length)
            for (ElementMatchVO eVO : smVO.getMatchedElements()) {
                // length of the search text segment present in the element
                int lenMatchInsideElement = eVO.getMatchEndIndex() - eVO.getMatchStartIndex();

                // add the length of the matched search segment to the new start index to get end index
                int endIndexLocal = startIndex + lenMatchInsideElement;

                String replaceSubText;

                // calculate corresponding replace segment
                if (startIndex < replaceLength) {
                    if (endIndexLocal < replaceLength) {
                        replaceSubText = replaceText.substring(startIndex,
                                endIndexLocal);
                    } else {
                        replaceSubText = replaceText.substring(startIndex);
                    }
                } else {
                    replaceSubText = "";
                }
                replaceTextSegments.add(replaceSubText);

                startIndex = endIndexLocal;

            }
            // when search string segments < replace string
            if (startIndex < replaceLength) {
                replaceTextSegments.set(replaceTextSegments.size() - 1,
                        replaceTextSegments.get(replaceTextSegments.size() - 1).concat(replaceText.substring(startIndex)));
            }

            Map<String, List<Integer>> matchedElementsChildContentLength = new HashMap<>();
            for (ElementMatchVO eVO : smVO.getMatchedElements()) {
                if (!matchedElementsChildContentLength.containsKey(eVO.getElementId())) {
                    matchedElementsChildContentLength.put(eVO.getElementId(),
                            getChildElementsContentLength(document, eVO.getElementId()));
                }
            }

            Set<String> emptyElementSet = new HashSet<>();
            for (int i = 0; i < smVO.getMatchedElements().size(); i++) {
                ElementMatchVO eVO = smVO.getMatchedElements().get(i);
                boolean emptyTag = replaceContent(document, eVO, replaceTextSegments.get(i), removeEmptyTags,
                        matchedElementsChildContentLength.get(eVO.getElementId()));
                if (emptyTag) {
                    emptyElementSet.add(eVO.getElementId());
                }
            }

            removeEmptyElementsAndParents(document, emptyElementSet);
        }
    }

    private List<Integer> getChildElementsContentLength(Document document, String elementId) {
        List<Integer> childNodesContentLength = new ArrayList<>();
        Node node = XmlUtils.getElementById(document, elementId);
        if (node != null) {
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                childNodesContentLength.add(node.getTextContent().length());
            }
        }
        return childNodesContentLength;
    }

    private boolean replaceContent(Document document, ElementMatchVO eVO, String replaceSegmentGlobal, boolean removeEmptyTags,
                                   List<Integer> matchedElementChildContentLength) {
        boolean containsNonEmptyElement = false;
        boolean containsEmptyTextElement = false;

        Node node = XmlUtils.getElementById(document, eVO.getElementId());
        if (node != null) {
            int index = 0;
            int startEVO = eVO.getMatchStartIndex();
            int endEVO = eVO.getMatchEndIndex();
            String replaceSegment = replaceSegmentGlobal;
            Node lastUpdatedNode = null;

            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                int length = matchedElementChildContentLength.get(i);
                int indexAtEndOfContent = index + length;

                if ((node.getNodeType() == Node.TEXT_NODE) && (index < endEVO && indexAtEndOfContent > startEVO)) {

                    String content = node.getTextContent();
                    int minReplaceSegmentLength = Math.min(replaceSegment.length(), indexAtEndOfContent - startEVO);

                    // replace from startEVO till length
                    String updatedContent = content.substring(0, startEVO - index) + replaceSegment.substring(0,
                            minReplaceSegmentLength);

                    // tail part: if the search segment part falls within the content, then simply append the remaing content
                    if (indexAtEndOfContent > endEVO && endEVO > startEVO) {
                        // when there exist search segment that has been replaced somewhere in the middle i.e, startEVO>0
                        // but there is still some content left towards the end i.e, length > endEVO
                        updatedContent += content.substring(endEVO - Math.min(startEVO, index));
                    }

                    // record an update to be executed later
                    // in case the content is empty and as a result the tag as well, then it can also be deleted
                    if (removeEmptyTags) {
                        if ((startEVO == 0) && StringUtils.isEmpty(updatedContent)) {
                            containsEmptyTextElement = true;
                        } else if (!StringUtils.isEmpty(updatedContent)) {
                            containsNonEmptyElement = true;
                        }
                    }

                    node.setTextContent(StringEscapeUtils.escapeXml10(updatedContent));
                    lastUpdatedNode = node;

                    replaceSegment = replaceSegment.substring(minReplaceSegmentLength);
                    startEVO = indexAtEndOfContent;
                } else {
                    containsNonEmptyElement = true;
                }
                index += length;
            }

            // In case if the replace segment is too large and still some part was remaining,
            // simple append at the end.
            if (lastUpdatedNode != null && replaceSegment.length() > 0) {
                lastUpdatedNode.setTextContent(lastUpdatedNode.getTextContent() +
                        StringEscapeUtils.escapeXml10(replaceSegment));
            }
        }

        return containsEmptyTextElement && !containsNonEmptyElement;
    }

    private void removeEmptyElementsAndParents(Document document, Set<String> elementIds) {
        for (String elementId : elementIds) {
            Node node = XmlUtils.getElementById(document, elementId);
            if (node != null) {
                while ((node.getParentNode() != null) && XmlHelper.INLINE_ELEMENTS.contains(node.getParentNode().getNodeName()) &&
                        StringUtils.isEmpty(node.getParentNode().getTextContent())) {
                    node = node.getParentNode();
                }
                XmlUtils.deleteElement(node);
            }
        }
    }

    private static class Element {
        String elementId;
        String content;
        boolean isEditable;
        String tag;
        int startIndexOfText;

        Element(String elementId, String content, boolean isEditable, String tag, int startIndexOfText) {
            this.elementId = elementId;
            this.content = content;
            this.isEditable = isEditable;
            this.tag = tag;
            this.startIndexOfText = startIndexOfText;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Element that = (Element) o;
            return Objects.equals(elementId, that.elementId) && Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(elementId, tag);
        }

        @Override
        public String toString() {
            return "Element{" +
                    "elementId='" + elementId + '\'' +
                    ", content='" + content + '\'' +
                    ", isEditable=" + isEditable +
                    ", tag='" + tag + '\'' +
                    ", startIndexOfText='" + startIndexOfText + '\'' +
                    '}';
        }
    }

    private static class Index {
        String elementId;
        int indexInTag;

        public Index(String elementId, int indexInTag) {
            this.elementId = elementId;
            this.indexInTag = indexInTag;
        }

        @Override
        public String toString() {
            return "Index{" +
                    "elementId='" + elementId + '\'' +
                    ", indexInTag=" + indexInTag +
                    '}';
        }
    }
}
