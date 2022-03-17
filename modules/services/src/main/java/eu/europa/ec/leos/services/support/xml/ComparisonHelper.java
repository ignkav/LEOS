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

import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.support.xml.domain.Element;
import eu.europa.ec.leos.services.support.xml.domain.FragmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.AKNBODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.AKOMANTOSO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CITATIONS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CONCLUSIONS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREAMBLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITALS;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.hasChildTextNode;

public class ComparisonHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ComparisonHelper.class);

    public static Element buildElement(Node node, Map<String, Integer> hmIndex, Map<String, Element> elementsMap) {
        String tagName = node.getNodeName();
        String tagContent = node.getTextContent();
        String tagId = XmlUtils.getId(node);

        hmIndex.put(tagName, hmIndex.get(tagName) == null ? 1 : (hmIndex.get(tagName)) + 1);
        Integer nodeIndex = hmIndex.get(tagName);

        boolean hasText = hasChildTextNode(node);
//        String innerText = getTextForSimilarityMatch(tagId, tagName, node);
        String innerText = ""; //TODO potential bug. Need to find a solution during CN implementation

        List<Element> children = new ArrayList<>();
        List<Node> childrenNodes = XmlUtils.getChildren(node);
        for (int i = 0; i < childrenNodes.size(); i++) {
            Node childNode = childrenNodes.get(i);
            Element childElement = buildElement(childNode, hmIndex, elementsMap);
            children.add(childElement);
        }

        if (tagId == null) {
            tagId = tagName.concat(nodeIndex.toString());
        }
//        System.out.println("Adding " + tagName + " - " + tagId + " with " + children.size() + " children");
        Element element = new Element(node, tagId, tagName, tagContent, nodeIndex, hasText, innerText, children);
        elementsMap.put(tagId, element);
        return element;
    }

    //tags which occur only one time in xml and contains lot of text.We avoid these tags for text similarity search by not setting the full text
    private static final List<String> excludedTags = Arrays.asList(BILL, PREFACE, AKOMANTOSO, PREAMBLE, AKNBODY, CONCLUSIONS, RECITALS, CITATIONS);

    private static String getTextForSimilarityMatch(String tagId, String tagName, Node node) {
        String innerText = "";
        //optimization block.. do not generate complete content in cases content matching is not performed.
        if (tagId == null
                && !excludedTags.contains(tagName.toLowerCase())) {
            innerText = XmlUtils.nodeToString(node);
            innerText = XmlHelper.removeEnclosingTags(innerText, AKOMANTOSO);
        }
        return innerText;
    }

    public static String getElementFragmentAsString(Node node, Element element) {
        String content = getFragment(node, element, FragmentType.ELEMENT);
        return content;
    }

    public static String getContentFragmentAsString(Node node, Element element) {
        String content = getFragment(node, element, FragmentType.CONTENT);
        return content;
    }

    public static boolean isElementContentEqual(ContentComparatorContext context) {
        if(context.getOldElement() != null & context.getOldElement() != null){
            return ((Node) context.getOldElement().getNavigationIndex()).isEqualNode((Node) context.getNewElement().getNavigationIndex());
        } else {
            return false;
        }
    }

    private static String getFragment(Node node, Element element, FragmentType fragmentType) {
        Node child = (Node) element.getNavigationIndex();
        String fragmentElementContent;
        if (fragmentType.equals(FragmentType.ELEMENT)) {
            fragmentElementContent = XmlUtils.nodeToString(child);
        } else if (fragmentType.equals(FragmentType.CONTENT)) {
            fragmentElementContent = XmlUtils.getContentNodeAsXmlFragment(child);
        } else {
            throw new IllegalArgumentException("Unknown fragment type: " + fragmentType);
        }
        return fragmentElementContent;
    }

    public static String getChangedElementContent(Node newContentNode, Element newElement, String attrName, String startTagValueForAddedElementFromAncestor) {
        return null;
    }

    public static String getNonIgnoredChangedElementContent(Node newContentNode, Element newElement, String attrName, String startTagValueForAddedElementFromAncestor) {
        return null;
    }

    public static void appendNonIgnoredChangedElementsContent(Boolean aTrue, StringBuilder leftResultBuilder, StringBuilder rightResultBuilder, Node newContentNode, Element newElement) {

    }
}
