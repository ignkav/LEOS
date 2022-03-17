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

import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import io.atlassian.fugue.Pair;

import java.util.List;
import java.util.Map;

public interface XmlContentProcessor {

    String getElementValue(byte[] xmlContent, String xPath, boolean namespaceEnabled);

    byte[] removeElement(byte[] xmlContent, String xPath, boolean namespaceEnabled);

    byte[] insertElement(byte[] xmlContent, String xPath, boolean namespaceEnabled, String newContent);

    byte[] replaceElement(byte[] xmlContent, String xPath, boolean namespaceEnabled, String newContent);

    List<String> getAncestorsIdsForElementId(byte[] xmlContent, String idAttributeValue);

    String getElementByNameAndId(byte[] xmlContent, String tagName, String idAttributeValue);

    String getElementAttributeValueByNameAndId(byte[] xmlContent, String attributeName, String tagName, String idAttributeValue);

    List<Map<String, String>> getElementsAttributesByPath(byte[] xmlContent, String xPath);

    Map<String, String> getElementAttributesByPath(byte[] xmlContent, String xPath, boolean namespaceEnabled);

    String getElementContentFragmentByPath(byte[] xmlContent, String xPath, boolean namespaceEnabled);

    String getElementFragmentByPath(byte[] xmlContent, String xPath, boolean namespaceEnabled);

    byte[] replaceElementByTagNameAndId(byte[] xmlContent, String newContent, String tagName, String idAttributeValue);

    byte[] removeElementByTagNameAndId(byte[] xmlContent, String tagName, String idAttributeValue);

    byte[] insertElementByTagNameAndId(byte[] xmlContent, String articleTemplate, String tagName, String idAttributeValue, boolean before);

    byte[] createDocumentContentWithNewTocList(List<TableOfContentItemVO> tableOfContentItemVOs, byte[] content, User user);

    byte[] appendElementToTag(byte[] xmlContent, String tagName, String newContent, boolean asFirstChild);

    byte[] doXMLPostProcessing(byte[] xmlContent);

    byte[] cleanSoftActions(byte[] xmlContent);

    byte[] updateReferences(byte[] xmlContent) throws Exception;

    Pair<byte[], String> updateSoftMovedAttributes(byte[] xmlContent, String elementContent);

    /**
     * Finds the first element with the id,if there are others, XML is incorrect
     *
     * @param xmlContent
     * @param idAttributeValue id Attribute value
     * @return complete Tag or null
     */
    Element getElementById(byte[] xmlContent, String idAttributeValue);

    /**
     * removes all elements selected by xpath supplied
     *
     * @param xmlContent
     * @param xpath      to select elements
     * @return updated xml
     */
    byte[] removeElements(byte[] xmlContent, String xpath);

    /**
     * removes all elements selected by xpath supplied, Also it removes parent of selected elements specified by parentsToRemove
     *
     * @param xmlContent
     * @param xpath           xpath to select elements
     * @param parentsToRemove number of parents to remove
     * @return updated xml
     */
    byte[] removeElements(byte[] xmlContent, String xpath, int parentsToRemove);

    /**
     * searches the {@param searchText} and replace it with the {@param replaceText}.
     *
     * @param xmlContent
     * @param searchText
     * @param replaceText
     * @return: On success returns updated content. On failure returns null.
     */
    byte[] searchAndReplaceText(byte[] xmlContent, String searchText, String replaceText);

    /**
     * returns a map with new article id as key and the updated content.
     *
     * @param content
     * @param elementType
     * @return updated content
     */
    String doImportedElementPreProcessing(String content, String elementType);

    /**
     * returns the id of the last element based on the xPath present in the xml
     *
     * @param xmlContent
     * @param xPath
     * @return elementId
     */
    String getElementIdByPath(byte[] xmlContent, String xPath);

    /**
     * searches the {@param origText} in the element {@param elementId} and replace it with the {@param newText}.
     *
     * @param xmlContent
     * @param origText
     * @param newText
     * @param elementId
     * @param startOffset
     * @param endOffset
     * @return: On success returns updated content. On failure throws exception.
     */
    byte[] replaceTextInElement(byte[] xmlContent, String origText, String newText, String elementId, int startOffset, int endOffset);

    /**
     * adding and attribute {@param attributeName} on all children of an XML element {@param parentTag}.
     *
     * @param xmlContent
     * @param parentTag
     * @param attributeName
     * @param value
     * @return: On success returns updated content. On failure throws exception.
     */
    byte[] setAttributeForAllChildren(byte[] xmlContent, String parentTag, List<String> elementTags, String attributeName, String value) throws Exception;

    /**
     * get the parent element id given a child id attribute value
     *
     * @param xmlContent
     * @param tagName
     * @param idAttributeValue
     * @return
     */
    Element getParentElement(byte[] xmlContent, String tagName, String idAttributeValue);

    /**
     * get the sibling element given an element id attribute value, tag name, considering only tag elements provided and before/after element
     *
     * @param xmlContent
     * @param tagName
     * @param idAttributeValue
     * @param elementTags
     * @param before
     * @return
     */
    Element getSiblingElement(byte[] xmlContent, String tagName, String idAttributeValue, List<String> elementTags, boolean before);

    /**
     * get the child element at specified position given an element id attribute value, tag name, considering only tag elements provided and position
     *
     * @param xmlContent
     * @param tagName
     * @param idAttributeValue
     * @param elementTags
     * @param position
     * @return
     */
    Element getChildElement(byte[] xmlContent, String tagName, String idAttributeValue, List<String> elementTags, int position);

    /**
     * get element from the given document if a split operation is already performed over element passed as argument
     *
     * @param xmlContent
     * @param content
     * @param tagName
     * @param idAttributeValue
     * @return
     */
    Pair<byte[], Element> getSplittedElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) throws Exception;

    /**
     * get element from the given document if a merge operation is performed over element passed as argument
     *
     * @param xmlContent
     * @param content
     * @param tagName
     * @param idAttributeValue
     * @return
     */
    Element getMergeOnElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) throws Exception;

    /**
     * get element from the given document if a merge operation is performed over element passed as argument
     *
     * @param xmlContent
     * @param idAttributeValue
     * @param toc
     * @param tagNames
     * @return
     */
    Element getTocElement(final byte[] xmlContent, final String idAttributeValue, final List<TableOfContentItemVO> toc, final List<String> tagNames);

    /**
     * Merge element from the given document with sibling or parent element
     *
     * @param xmlContent
     * @param content
     * @param tagName
     * @param idAttributeValue
     * @return
     */
    byte[] mergeElement(byte[] xmlContent, String content, String tagName, String idAttributeValue);

    boolean needsToBeIndented(String elementContent);

    /**
     * Merge element from the given document with sibling or parent element
     *
     * @param xmlContent
     * @param elementName
     * @param elementId
     * @param elementContent
     * @param toc
     * @return
     */
    byte[] indentElement(byte[] xmlContent, String elementName, String elementId, String elementContent, List<TableOfContentItemVO> toc) throws IllegalArgumentException;

    String removeEmptyHeading(String newContent);

    byte[] insertDepthAttribute(byte[] xmlContent, String tagName, String elementId);

    LevelItemVO getLevelItemVo(byte[] xmlContent, String elementId, String elementTagName) throws Exception;

    byte[] updateDepthAttribute(byte[] xmlContent) throws Exception;

    byte[] updateRefsWithRefOrigin(byte[] source, String ref, String refOrigin);

    byte[] insertAffectedAttributeIntoParentElements(byte[] xmlContent, String idAttributeValue);

    boolean evalXPath(byte[] xmlContent, String xPath, boolean namespaceEnabled);

    int getElementCountByXpath(byte[] xmlContent, String xPath, boolean namespaceEnabled);

    String getDocType(byte[] xmlContent, boolean namespaceEnabled);

    List<Element> getElementsByTagName(byte[] xmlContent, List<String> elementTags, boolean withContent);

    byte[] ignoreNotSelectedElements(byte[] xmlContent, List<String> rootElements, List<String> elementIds);

    List<CloneProposalMetadataVO> getClonedProposalsMetadataVO(String proposalId, String legDocumentName);

    boolean isAnnexComparisonRequired(byte[] contentBytes);
}
