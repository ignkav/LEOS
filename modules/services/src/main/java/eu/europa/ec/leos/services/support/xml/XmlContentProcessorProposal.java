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

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLAUSE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLONED_CREATION_DATE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLONED_PROPOSAL_REF;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLONED_STATUS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLONED_TARGET_USER;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INTRO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_DELETABLE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_MOVED_LABEL_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITAL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.getDateAsXml;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.getXMLFormatDate;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.addAttribute;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createNodeFromXmlFragment;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getChildren;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getId;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.importNodeInDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.insertOrUpdateAttributeValue;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.nodeToByteArray;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.nodeToString;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.removeAttribute;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.setId;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.updateXMLIDAttributesInElementContent;
import static eu.europa.ec.leos.util.LeosDomainUtil.getLeosDateFromString;
import static eu.europa.ec.leos.util.LeosDomainUtil.wrapXmlFragment;

@Service
@Instance(instances = {InstanceType.OS, InstanceType.COMMISSION})
public class XmlContentProcessorProposal extends XmlContentProcessorImpl {

    private static final Logger LOG = LoggerFactory.getLogger(XmlContentProcessorProposal.class);

    @Autowired
    private PackageService packageService;
    @Autowired
    private CloneContext cloneContext;

    @Override
    public byte[] cleanSoftActions(byte[] xmlContent) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    public Node buildTocItemContent(List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, Map<TocItem, List<TocItem>> tocRules,
                                    Document document, Node node, TableOfContentItemVO tocVo, User user) {
        String tagName = tocVo.getTocItem().getAknTag().value();
        LOG.debug("buildTocItemContent for tocItemName '{}', tocItemId '{}', nodeName '{}', nodeId '{}', children {}", tagName, tocVo.getId(), node.getNodeName(), getId(node), tocVo.getChildItemsView().size());

        // 1. Get the corresponding node from the XML, or create a new one using the template
        node = getNode(document, tocVo);

        // 2. Store the node details in temp variables
        Node numNode = XmlContentProcessorHelper.extractOrBuildNumElement(document, node, tocVo);
        Node headingNode = XmlContentProcessorHelper.extractOrBuildHeaderElement(document, node, tocVo);
        Node introNode = XmlUtils.getFirstChild(node, INTRO);  //recitals intro
        List<Node> childrenNode = XmlContentProcessorHelper.extractLevelNonTocItems(tocItems, tocRules, node, tocVo);

        // 3. clean the node and build it again.
        node.setTextContent("");
        updateDepthAttribute(tocVo, node);
        appendChildIfNotNull(numNode, node);
        appendChildIfNotNull(headingNode, node);
        appendChildIfNotNull(introNode, node);

        // 4. Propagate to children
        for (TableOfContentItemVO child : tocVo.getChildItemsView()) {
            Node newChild = buildTocItemContent(tocItems, numberingConfigs, tocRules, document, node, child, user);
            LOG.debug("buildTocItemContent adding {} '{}' as child of {}", newChild.getNodeName(), getId(newChild), node.getNodeName());
            XmlUtils.addChild(newChild, node);
        }

        appendChildrenIfNotNull(childrenNode, node); // only for part of the body which is not configured in structure.xml, like CLAUSE tag

        if (isClonedProposal()) {
            if (SoftActionType.MOVE_TO.equals(tocVo.getSoftActionAttr())) {
                updateXMLIDAttributesInElementContent(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, false);
            } else if (SoftActionType.DELETE.equals(tocVo.getSoftActionAttr())) {
                updateXMLIDAttributesInElementContent(node, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, false);
            }
            processSoftElements(node, tocVo, user);
        }
        return node;
    }

    private void processSoftElements(Node node, TableOfContentItemVO tocVo, User user) {
        XmlUtils.addAttribute(node, LEOS_ORIGIN_ATTR, tocVo.getOriginAttr());
        String moveId;
        if (tocVo.getSoftActionAttr() != null) {
            switch (tocVo.getSoftActionAttr()) {
                case MOVE_TO:
                    moveId = tocVo.getSoftMoveTo();
                    break;
                case MOVE_FROM:
                    moveId = tocVo.getSoftMoveFrom();
                    break;
                default:
                    moveId = null;
            }

            updateSoftInfo(node, tocVo.getSoftActionAttr(), tocVo.isSoftActionRoot(), user, tocVo.getOriginAttr(), moveId, tocVo.getTocItem());
        }
    }

    public static void updateSoftInfo(Node node, SoftActionType action, Boolean isSoftActionRoot, User user, String originAttrValue, String moveId, TocItem tocItem) {
        if (originAttrValue == null) {
            return;
        }

        if (action != null) {
            addAttribute(node, LEOS_SOFT_ACTION_ATTR, action.getSoftAction());
        } else {
            removeAttribute(node, LEOS_SOFT_ACTION_ATTR);
        }

        if (action != null) {
            switch (action) {
                case DELETE:
                    insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
                    removeAttribute(node, LEOS_SOFT_MOVED_LABEL_ATTR);
                    removeAttribute(node, LEOS_SOFT_MOVE_FROM);
                    removeAttribute(node, LEOS_SOFT_MOVE_TO);
                    break;
                case MOVE_TO:
                    insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_TO, moveId);
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_FROM, null);
                    break;
                case MOVE_FROM:
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_TO, null);
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_FROM, moveId);
                    break;
                case UNDELETE:
                    restoreOldId(node);
                    if (tocItem.getAknTag().value() != ARTICLE) {
                        insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, null);
                        insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, null);
                    }
                    if (tocItem.getAknTag().value() == CLAUSE) {
                        insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, true);
                        insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, true);
                    }
                    break;
                default:
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVED_LABEL_ATTR, null);
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_FROM, null);
                    insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_TO, null);
            }
            insertOrUpdateAttributeValue(node, LEOS_SOFT_USER_ATTR, user != null ? user.getLogin() : null);
            insertOrUpdateAttributeValue(node, LEOS_SOFT_DATE_ATTR, getDateAsXml());
        } else {
            insertOrUpdateAttributeValue(node, LEOS_SOFT_USER_ATTR, null);
            insertOrUpdateAttributeValue(node, LEOS_SOFT_DATE_ATTR, null);

            insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVED_LABEL_ATTR, null);
            insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_FROM, null);
            insertOrUpdateAttributeValue(node, LEOS_SOFT_MOVE_TO, null);
        }
        if(isSoftActionRoot != null) {
            insertOrUpdateAttributeValue(node, LEOS_SOFT_ACTION_ROOT_ATTR, isSoftActionRoot);
        }
    }

    protected static void restoreOldId(Node node) {
        String id = getId(node);
        if (id.contains(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)) {
            id = id.replace("deleted_", EMPTY_STRING);
            setId(node, id);
        }
    }


    private Node getNode(Document document, TableOfContentItemVO tocVo) {
        String tagName = tocVo.getTocItem().getAknTag().value();
        Node node;
        if (tocVo.getVtdIndex() != null) {
            node = (Node) tocVo.getVtdIndex();
            node = importNodeInDocument(document, node);
        } else {
            if (tocVo.getChildItemsView().isEmpty()) {
                String childNode = XmlHelper.getTemplate(tocVo.getTocItem(), tocVo.getNumber(), tocVo.getHeading(), messageHelper);
                node = createNodeFromXmlFragment(document, childNode.getBytes(UTF_8), false);
            } else {
                String childNode = XmlHelper.getTemplate(tagName);
                node = createNodeFromXmlFragment(document, childNode.getBytes(UTF_8), false);
//                node = XmlUtils.createElement(document, tagName, IdGenerator.generateId(tagName.substring(0, 3), 7), "");
//                if (isClonedProposal()) {
//                    startTag = updateOriginAttribute(startTagStr.getBytes(UTF_8), tableOfContentItemVO.getOriginAttr());
//                } else {
//                    startTag = startTagStr.getBytes(UTF_8);
//                }
            }
        }
        return node;
    }

    private void appendChildIfNotNull(Node childNode, Node node) {
        if (childNode != null) {
            node.appendChild(childNode);
        }
    }

    private void appendChildrenIfNotNull(List<Node> childrenNode, Node node) {
        for (int i = 0; i < childrenNode.size(); i++) {
            appendChildIfNotNull(childrenNode.get(i), node);
        }
    }

    private void updateDepthAttribute(TableOfContentItemVO tocVo, Node node) {
        if (tocVo.getItemDepth() > 0) {
            addAttribute(node, LEOS_DEPTH_ATTR, String.valueOf(tocVo.getItemDepth()));
        }
    }

    @Override
    protected Pair<byte[], Element> buildSplittedElementPair(byte[] xmlContent, Element splitElement) {
        if (splitElement == null) {
            return null;
        }
        return new Pair<>(xmlContent, splitElement);
    }

    @Override
    public Element getMergeOnElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        throw new IllegalStateException("Operation non implemented for this instance");
    }

    @Override
    public byte[] mergeElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        throw new IllegalStateException("Operation non implemented for this instance");
    }

    @Override
    public boolean needsToBeIndented(String elementContent) {
        return false;
    }

    @Override
    public byte[] indentElement(byte[] xmlContent, String elementName, String elementId, String elementContent, List<TableOfContentItemVO> toc) throws IllegalArgumentException {
        return xmlContent;
    }

    @Override
    public byte[] removeElementByTagNameAndId(byte[] xmlContent, String tagName, String elementId) {
        if(isClonedProposal()) {
            Element element = getElementById(xmlContent, elementId);
            if (element == null) {
                return xmlContent;
            }
            return removeElementByTagNameAndId(xmlContent, element, LS);
        }
        return deleteElementByTagNameAndId(xmlContent, tagName, elementId);
    }

    @Override
    protected byte[] doRemoveElementByTagNameAndId(byte[] xmlContent, final String elementId, final String originElementId) {
        Node document = XmlUtils.createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, elementId);
        Map<String, String> attributes = XmlUtils.getAttributes(node);
        if (isProposalElement(attributes) && !isSoftMovedFrom(attributes)) {
            return replaceElementByTagNameAndId(xmlContent, softDeleteElement(node, false), node.getNodeName(), elementId);
        } else if (isProposalElement(attributes) && isSoftMovedFrom(attributes)) {
            if (!originElementId.equals(elementId)) {
                xmlContent = softDeleteMovedToElement(xmlContent, node, originElementId);
            }
            xmlContent = softDeleteMovedToElement(xmlContent, node, elementId);
            return deleteElementByTagNameAndId(xmlContent, node.getNodeName(), elementId);
        } else {
            return deleteElementByTagNameAndId(xmlContent, node.getNodeName(), elementId);
        }
    }

    @Override
    public void removeElement(Node node) {
        XmlUtils.deleteElement(node);
    }

    @Override
    public String getUpdatedContent(Node node) {
        return XmlUtils.nodeToString(node);
    }

    @Override
    public void specificInstanceXMLPostProcessing(Node node) {
        if (isClonedProposal()) {
            removeTempIdAttributeIfExists(node);
            updateSoftMoveLabelAttribute(node, LEOS_SOFT_MOVE_TO);
            updateSoftMoveLabelAttribute(node, LEOS_SOFT_MOVE_FROM);
            updateNewElements(node, CITATION);
            updateNewElements(node, RECITAL);
            updateNewElements(node, ARTICLE);
            updateNewElements(node, PREFACE);
            updateNewElements(node, MAIN_BODY);
            updateNewElements(node, LEVEL);
            updateNewElements(node, PARAGRAPH);
        }
    }

    private void updateNewElements(Node node, String elementTagName) {
        NodeList nodeList = XmlUtils.getElementsByName(node, elementTagName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            modifySubElement(nodeList.item(i), LS);
        }
    }

    @Override
    public List<CloneProposalMetadataVO> getClonedProposalsMetadataVO(String proposalId, String legDocumentName) {
        //TODO move this first block using packageService in the service layer.
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
        List<XmlDocument> documents = packageService.findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, true);
        List<CloneProposalMetadataVO> clonedProposalsMetadataVO = new ArrayList<>();
        byte[] xmlContent = documents.stream()
                .filter(xmlDocument -> xmlDocument.getCategory().equals(LeosCategory.PROPOSAL))
                .findAny().get().getContent().get().getSource().getBytes();

        String xPath = "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legDocumentName + "\"]";
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getFirstElementByXPath(document, xPath);

        if (node != null) {
            List<Node> clonedList = getChildren(node, CLONED_PROPOSAL_REF);
            for (int i = 0; i < clonedList.size(); i++) {
                Node cloned = clonedList.get(i);
                String targetUser = XmlUtils.getChildContent(cloned, CLONED_TARGET_USER);
                String creationDate = XmlUtils.getChildContent(cloned, CLONED_CREATION_DATE);
                String status = XmlUtils.getChildContent(cloned, CLONED_STATUS);

                CloneProposalMetadataVO cloneProposalMetadataVO = new CloneProposalMetadataVO();
                cloneProposalMetadataVO.setTargetUser(targetUser);
                cloneProposalMetadataVO.setCreationDate(getLeosDateFromString(creationDate));
                cloneProposalMetadataVO.setRevisionStatus(status);
                clonedProposalsMetadataVO.add(cloneProposalMetadataVO);
            }
        }
        clonedProposalsMetadataVO.sort(Comparator.comparing(CloneProposalMetadataVO::getCreationDate).reversed());
        return clonedProposalsMetadataVO;
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }

    @Override
    public Pair<byte[], String> updateSoftMovedAttributes(byte[] xmlContent, String elementContent) {
        Pair<byte[], String> result = new Pair<>(xmlContent, new String()); //default result
        Document fragment = createDocument(wrapXmlFragment(elementContent).getBytes(StandardCharsets.UTF_8));
        NodeList softMovedNodes = XmlUtils.getElementsByXPath(fragment, String.format("//*[@%s]", LEOS_SOFT_MOVE_FROM));

        Document document = createDocument(xmlContent);
        for (int nodeIdx = 0; nodeIdx < softMovedNodes.getLength(); nodeIdx++) {
            Node node = softMovedNodes.item(nodeIdx);
            String idAttrVal = XmlUtils.getAttributeValue(node, XMLID);
            if(idAttrVal != null && idAttrVal.indexOf("temp_") != -1) {
                String updatedIdAttrVal = idAttrVal.replace("temp_", EMPTY_STRING);
                String xPath = "//*[@xml:id = '" + updatedIdAttrVal + "']";
                NodeList sourceNodes = XmlUtils.getElementsByXPath(fragment, xPath);
                if(sourceNodes != null && sourceNodes.getLength() > 0) {
                    insertSoftMovedAttributes(updatedIdAttrVal, sourceNodes);
                    result = new Pair<>(new byte[0], nodeToString(fragment.getFirstChild().getFirstChild()));
                } else {
                    sourceNodes = XmlUtils.getElementsByXPath(document, xPath);
                    if(sourceNodes != null && sourceNodes.getLength() > 0) {
                        insertSoftMovedAttributes(updatedIdAttrVal, sourceNodes);
                        result = new Pair<>(nodeToByteArray(document), new String());
                    }
                }
            }
        }
        return result;
    }

    private void insertSoftMovedAttributes(String updatedIdAttrVal, NodeList sourceNodes) {
        for (int srcNodeIdx = 0; srcNodeIdx < sourceNodes.getLength(); srcNodeIdx++) {
            Node sourceNode = sourceNodes.item(srcNodeIdx);
            String sourceNodeIdVal = XmlUtils.getAttributeValue(sourceNode, XMLID);
            XmlUtils.addAttribute(sourceNode, XMLID, SOFT_MOVE_PLACEHOLDER_ID_PREFIX + sourceNodeIdVal);
            XmlUtils.addAttribute(sourceNode, LEOS_SOFT_ACTION_ATTR, SoftActionType.MOVE_TO.getSoftAction());
            XmlUtils.addAttribute(sourceNode, LEOS_SOFT_USER_ATTR, securityContext.getUserName());
            XmlUtils.addAttribute(sourceNode, LEOS_SOFT_DATE_ATTR, getXMLFormatDate());
            XmlUtils.addAttribute(sourceNode, LEOS_SOFT_ACTION_ROOT_ATTR, "true");
            XmlUtils.addAttribute(sourceNode, LEOS_SOFT_MOVE_TO, updatedIdAttrVal);

            //Add leos:editable=false to make this element read-only inside CKE
            XmlUtils.addAttribute(sourceNode, LEOS_EDITABLE_ATTR, "false");
        }
    }

    private void removeTempIdAttributeIfExists(Node node) {
        String xPath = "//*[starts-with(@xml:id,'temp_')]";
        NodeList nodes = XmlUtils.getElementsByXPath(node, xPath);
        for(int idx = 0; idx < nodes.getLength(); idx++) {
            Node tempIdNode = nodes.item(idx);
            String tempIdNodeVal = XmlUtils.getAttributeValue(tempIdNode, XMLID);
            if (tempIdNodeVal != null && tempIdNodeVal.indexOf("temp_") != -1) {
                String updatedIdAttrVal = tempIdNodeVal.replace("temp_", EMPTY_STRING);
                XmlUtils.addAttribute(tempIdNode, XMLID, updatedIdAttrVal);
            }
        }
    }
}