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

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.TocItemUtils;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.ELEMENTS_TO_HIDE_CONTENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.HEADING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INTRO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ID_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_TYPE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_LIST_TYPE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_TRANS_FROM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL_NUM_SEPARATOR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.extractNumber;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.trimmedXml;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.addAttribute;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createElement;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeForSoftAction;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeForType;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeValue;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeValueAsBoolean;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeValueAsGregorianCalendar;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeValueAsInteger;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getNextSibling;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getParentTagName;

public class XmlContentProcessorHelper {

    private static final Logger LOG = LoggerFactory.getLogger(XmlContentProcessorHelper.class);

    public static List<TableOfContentItemVO> getAllChildTableOfContentItems(Node node, List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, TocMode mode) {
        List<TableOfContentItemVO> itemVOList = new ArrayList<>();
        Node child;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                addTocItemVoToList(tocItems, tocRules, child, itemVOList, mode);
            }
        }
        return itemVOList;
    }

    private static void addTocItemVoToList(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node, List<TableOfContentItemVO> itemVOList, TocMode mode) {
        TableOfContentItemVO tableOfContentItemVO = buildTableOfContentsItemVO(tocItems, node);
        if (tableOfContentItemVO != null) {
            List<TableOfContentItemVO> itemVOChildrenList = getAllChildTableOfContentItems(node, tocItems, tocRules, mode);
            if ((!TocMode.SIMPLIFIED_CLEAN.equals(mode) || (TocMode.SIMPLIFIED_CLEAN.equals(mode) && tableOfContentItemVO.getTocItem().isDisplay()))
                    && shouldItemBeAddedToToc(tocItems, tocRules, node, tableOfContentItemVO.getTocItem())) {
                if (TocMode.SIMPLIFIED.equals(mode) || TocMode.SIMPLIFIED_CLEAN.equals(mode)) {
                    if (getTagValueFromTocItemVo(tableOfContentItemVO).equals(LIST) && !itemVOList.isEmpty()) {
                        tableOfContentItemVO = itemVOList.get(itemVOList.size() - 1);
                        tableOfContentItemVO.addAllChildItems(itemVOChildrenList);
                        return;
                    } else if (Arrays.asList(PARAGRAPH, POINT, INDENT, LEVEL).contains(getTagValueFromTocItemVo(tableOfContentItemVO))) {
                        boolean isFirstCrossHeading  = !itemVOChildrenList.isEmpty() ? CROSSHEADING.equals(getTagValueFromTocItemVo(itemVOChildrenList.get(0))) : false;
                        if ((itemVOChildrenList.size() > 1) && (itemVOChildrenList.get(0).getChildItems().isEmpty())) {
                            tableOfContentItemVO.setId(itemVOChildrenList.get(0).getId());
                            if(!isFirstCrossHeading) itemVOChildrenList.remove(0);
                        } else if (itemVOChildrenList.size() == 1) {
                            tableOfContentItemVO.setId(itemVOChildrenList.get(0).getId());
                            if(!isFirstCrossHeading) itemVOChildrenList = itemVOChildrenList.get(0).getChildItems();
                        }
                    }
                }
                itemVOList.add(tableOfContentItemVO);
                tableOfContentItemVO.addAllChildItems(itemVOChildrenList);
            } else if (tableOfContentItemVO.getParentItem() != null) {
                tableOfContentItemVO.getParentItem().addAllChildItems(itemVOChildrenList);
            } else {
                itemVOChildrenList.forEach(childItem -> itemVOList.add(childItem));
            }
        }
    }

    public static TableOfContentItemVO buildTableOfContentsItemVO(List<TocItem> tocItems, Node node) {
        String tagName = node.getNodeName();
        TocItem tocItem = TocItemUtils.getTocItemByName(tocItems, tagName);

        if (tocItem == null) {
            // unsupported tag name
            return null;
        }

        String elementId = getAttributeValue(node, XMLID);
        String originAttr = getAttributeValue(node, LEOS_ORIGIN_ATTR);
        SoftActionType softActionAttr = getAttributeForSoftAction(node, LEOS_SOFT_ACTION_ATTR);
        Boolean isSoftActionRoot = getAttributeValueAsBoolean(node, LEOS_SOFT_ACTION_ROOT_ATTR);
        String softUserAttr = getAttributeValue(node, LEOS_SOFT_USER_ATTR);
        GregorianCalendar softDateAttr = getAttributeValueAsGregorianCalendar(node, LEOS_SOFT_DATE_ATTR);
        String softMovedFrom = getAttributeValue(node, LEOS_SOFT_MOVE_FROM);
        String softMovedTo = getAttributeValue(node, LEOS_SOFT_MOVE_TO);
        String softTransFrom = getAttributeValue(node, LEOS_SOFT_TRANS_FROM);

        // get the indent attributes
        IndentedItemType indentOriginType = getAttributeForType(node, LEOS_INDENT_ORIGIN_TYPE_ATTR, IndentedItemType.class);
        Integer indentOriginDepth = getAttributeValueAsInteger(node, LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR);
        String indentOriginNumValue = getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ATTR);
        String indentOriginNumId = getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ID_ATTR);
        String indentOriginNumOrigin = getAttributeValue(node, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR);

        // get the num
        String number = null;
        String originNumAttr = null;
        String numId = null;
        SoftActionType numSoftActionAttribute = null;
        Node numNode = getFirstChild(node, NUM);

        if (numNode != null) {
            originNumAttr = getAttributeValue(numNode, LEOS_ORIGIN_ATTR);
            numId = getAttributeValue(numNode, XMLID);
            numSoftActionAttribute = getAttributeForType(numNode, LEOS_SOFT_ACTION_ATTR, SoftActionType.class);
            number = extractNumber(numNode.getTextContent());
        }

        String listTypeValue = getAttributeValue(node, LEOS_LIST_TYPE_ATTR);

        // get the heading
        String heading = null;
        String originHeadingAttr = null;
        SoftActionType headingSoftActionAttribute = null;
        Node headingNode = getFirstChild(node, HEADING);
        if (headingNode != null) {
            heading = trimmedXml(headingNode.getTextContent());
            originHeadingAttr = getAttributeValue(headingNode, LEOS_ORIGIN_ATTR);
            headingSoftActionAttribute = getAttributeForType(headingNode, LEOS_SOFT_ACTION_ATTR, SoftActionType.class);
        }

        String list = null;
        Node listNode = getFirstChild(node, LIST);
        if (listNode != null) {
            list = trimmedXml(listNode.getTextContent());
        }

        int elementDepth = calculateElementDepth(node, tocItem);

        //get the content
        String content = extractContentForTocItemsExceptNumAndHeadingAndIntro(node, tagName);
        content = trimmedXml(content);
//        content = removeNS(content);

        // build the table of content item and return it
        return new TableOfContentItemVO(tocItem, elementId, originAttr, number, originNumAttr, heading, originHeadingAttr, null,
                null, null, node, list, null, content,
                softActionAttr, isSoftActionRoot, softUserAttr, softDateAttr, softMovedFrom, softMovedTo, softTransFrom, false,
                numSoftActionAttribute, headingSoftActionAttribute, elementDepth,
                numId, indentOriginType, indentOriginDepth, indentOriginNumId, indentOriginNumValue, indentOriginNumOrigin, listTypeValue);
    }

    private static int calculateElementDepth(Node node, TocItem tocItem) {
        int elementDepth;
        if (tocItem.isHigherElement() != null && tocItem.isHigherElement()) {
            elementDepth = calculateDepthForHigherElement(node);
        } else {
            elementDepth = getElementDepth(node);
        }
        return elementDepth;
    }

    private static int calculateDepthForHigherElement(Node node) {
        int childLevelDepth = -1;
        List<Node> children = XmlUtils.getChildren(node, LEVEL);
        for (int i = 0; i < children.size(); i++) {
            childLevelDepth = getElementDepth(children.get(i));
            if (childLevelDepth != -1) {
                break;
            }
        }

        if (childLevelDepth == -1) {
            Node levelNode = XmlUtils.getPrevSibling(node, LEVEL);
            if (levelNode != null) {
                childLevelDepth = getElementDepth(node);
            } else {
                childLevelDepth = 1;
            }
        }
        return childLevelDepth;
    }

    private static int getElementDepth(Node node) {
        int depth = 0;
        Node numNode = getFirstChild(node, NUM);
        if (numNode != null) {
            String elementNumber = numNode.getTextContent();
            if (elementNumber.contains(".")) {
                String[] levelArr = StringUtils.split(elementNumber, LEVEL_NUM_SEPARATOR);
                depth = levelArr.length;
            }
        }
        return depth;
    }

    private static boolean shouldItemBeAddedToToc(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node, TocItem tocItem) {
        boolean addItemToToc = false;
        if (tocItem.isRoot()) {
            addItemToToc = tocItem.isDisplay();
        } else {
            TocItem parentTocItem = TocItemUtils.getTocItemByName(tocItems, getParentTagName(node));
            if ((parentTocItem != null) && (tocRules.get(parentTocItem) != null)) {
                addItemToToc = tocRules.get(parentTocItem).contains(tocItem);
            }
        }
        return addItemToToc;
    }

    public static String getTagValueFromTocItemVo(TableOfContentItemVO tableOfContentItemVO) {
        return tableOfContentItemVO.getTocItem().getAknTag().value();
    }

    private static String extractContentForTocItemsExceptNumAndHeadingAndIntro(Node node, String elementName) {
        if (!ELEMENTS_TO_HIDE_CONTENT.contains(elementName)) {
            Node current = XmlUtils.getFirstChild(node, HEADING);
            if (current == null) {
                current = XmlUtils.getFirstChild(node, NUM);
            }
            if (current == null) {
                current = XmlUtils.getFirstChild(node, INTRO);
            }
            if (current != null) {
                current = getNextSibling(current);
                if (current == null) {
                    return StringUtils.EMPTY;
                }
                return current.getTextContent();
            } else {
                return node.getTextContent();
            }
        } else {
            return "";
        }
    }

    public static Node extractOrBuildNumElement(Document document, Node node, TableOfContentItemVO tocVo) {
        Node numNode = null;
        if (tocVo.getNumber() != null) {
            String newNum = createNumContent(tocVo);
            numNode = XmlUtils.getFirstChild(node, NUM);
            if (numNode != null) {
                numNode.setTextContent(newNum);
            } else {
                numNode = createElement(document, NUM, newNum);
            }
            if (!EC.equals(tocVo.getOriginNumAttr())) { //TODO temp solution of not setting origin only for LS
                addAttribute(numNode, LEOS_ORIGIN_ATTR, tocVo.getOriginNumAttr());
            }
        }
        return numNode;
    }

    private static String createNumContent(TableOfContentItemVO tocVo) {
        StringBuilder item = new StringBuilder(StringUtils.capitalize(tocVo.getTocItem().getAknTag().value()));
        String newNum = trimmedXml(tocVo.getNumber());
        if (tocVo.getTocItem().isNumWithType()) {
            newNum = item + " " + newNum;
        }
        return newNum;
    }

    public static Node extractOrBuildHeaderElement(Document document, Node node, TableOfContentItemVO tocVo) {
        Node headingNode = null;
        if (!StringUtils.isEmpty(tocVo.getHeading())) {
            String newHeading = tocVo.getHeading();
            headingNode = XmlUtils.getFirstChild(node, HEADING);
            if (headingNode != null) {
                headingNode.setTextContent(newHeading);
            } else {
                headingNode = XmlUtils.createElementAsLastChildOfNode(document, node, HEADING, newHeading);
            }
        }
        return headingNode;
    }

    public static List<Node> extractLevelNonTocItems(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node, TableOfContentItemVO tocVo) {
        List<Node> childrenToAppend = new ArrayList<>();
        List<Node> children = XmlUtils.getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            Node remainingNode = extractNonTocItemExceptNumAndHeadingAndIntro(tocItems, tocRules, children.get(i));
            if (remainingNode != null) {
                childrenToAppend.add(remainingNode);
            }
        }

        if (tocVo.isUndeleted()) {
//            contentTag = updateXMLIDAttributesInElementContent(contentTag, "", true); //TODO
        }
        return childrenToAppend;
    }

    private static Node extractNonTocItemExceptNumAndHeadingAndIntro(List<TocItem> tocItems, Map<TocItem, List<TocItem>> tocRules, Node node) {
        String tagName = node.getNodeName();
        TocItem tocItem = TocItemUtils.getTocItemByName(tocItems, tagName);
        if ((tocItem == null || !shouldItemBeAddedToToc(tocItems, tocRules, node, tocItem)) &&
                (!tagName.equals(NUM) && !tagName.equals(HEADING) && !tagName.equals(INTRO))) {
            return node;
        }
        return null;
    }

    public static TableOfContentItemVO buildTableOfContentFromNodeId(final List<TocItem> tocItems, final Map<TocItem, List<TocItem>> tocRules, final String startingNodeId, final byte[] xmlContent, final TocMode mode) {
        LOG.trace("Start building the table of content from node id {}", startingNodeId);
        long startTime = System.currentTimeMillis();
        TableOfContentItemVO itemVO = null;
        List<TableOfContentItemVO> itemVOList;
        try {
            Node document = XmlUtils.createDocument(xmlContent);
            String xPath = "//*[@xml:id = '" + startingNodeId + "']";
            Node node = XmlUtils.getFirstElementByXPath(document, xPath);
            if (node != null) {
                itemVO = buildTableOfContentsItemVO(tocItems, node);
                itemVOList = getAllChildTableOfContentItems(node, tocItems, tocRules, mode);
                itemVO.addAllChildItems(itemVOList);
            }
        } catch (Exception e) {
            LOG.error("Unable to build the Table of content item list", e);
            throw new RuntimeException("Unable to build the Table of content item list", e);
        }

        LOG.trace("Build table of content from node completed in {} ms", (System.currentTimeMillis() - startTime));
        return itemVO;
    }
}
