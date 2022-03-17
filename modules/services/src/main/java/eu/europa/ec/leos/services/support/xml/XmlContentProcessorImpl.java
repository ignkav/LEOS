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

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.content.ReferenceLabelService;
import eu.europa.ec.leos.services.support.IdGenerator;
import eu.europa.ec.leos.services.support.xml.ref.Ref;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Provider;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.TableOfContentHelper.getTocElementById;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftOrigin;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.isElementInToc;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.AUTHORIAL_NOTE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.HEADING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.HREF;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_DELETABLE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_REF;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_REF_BROKEN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_MOVED_LABEL_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL_NUM_SEPARATOR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MARKER_ATTRIBUTE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.META;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MREF;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NON_BREAKING_SPACE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.REF;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.STATUS_IGNORED_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.WHITESPACE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XML_SHOW_AS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.determinePrefixForChildren;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.getEditableAttribute;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.getSubstringAvoidingTags;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.getXMLFormatDate;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.normalizeNewText;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.removeAllNameSpaces;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.removeSelfClosingElements;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.skipNodeAndChildren;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.skipNodeOnly;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.wrapXPathWithQuotes;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.addAttribute;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.addSibling;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createNodeFromXmlFragment;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeValue;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getChildContent;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getChildren;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getContentByTagName;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getFirstElementByName;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getId;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getNextSibling;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getParentId;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.importNodeInDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.nodeToByteArray;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.nodeToString;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.nodeToStringSimple;

public abstract class XmlContentProcessorImpl implements XmlContentProcessor {
    static final Logger LOG = LoggerFactory.getLogger(XmlContentProcessorImpl.class);
    protected static final String NBSP = "\u00a0";

    @Autowired
    protected ReferenceLabelService referenceLabelService;
    @Autowired
    protected MessageHelper messageHelper;
    @Autowired
    protected Provider<StructureContext> structureContextProvider;
    @Autowired
    protected XmlTableOfContentHelper xmlTableOfContentHelper;
    @Autowired
    protected SecurityContext securityContext;
    @Autowired
    protected XPathCatalog xPathCatalog;

    @Override
    public byte[] createDocumentContentWithNewTocList(List<TableOfContentItemVO> tableOfContentItemVOs, byte[] content, User user) {
        LOG.trace("Start building the document content for the new toc list");
        long startTime = System.currentTimeMillis();
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        List<NumberingConfig> numberingConfigs = structureContextProvider.get().getNumberingConfigs();
        Map<TocItem, List<TocItem>> tocRules = structureContextProvider.get().getTocRules();

        Document document = createDocument(content);
        for (TableOfContentItemVO tocVo : tableOfContentItemVOs) {
            Node node = navigateToTocElement(tocVo, document);
            node.setTextContent("");
            LOG.trace("Build content for parent TOC item '{}', node '{}'", tocVo.getTocItem().getAknTag().value(), node.getNodeName());
            Node newBody = buildTocItemContent(tocItems, numberingConfigs, tocRules, document, node, tocVo, user);
            newBody = importNodeInDocument(document, newBody);
            XmlUtils.replaceElement(newBody, node);
        }

//        LOG.trace("new doc \n{}", XmlUtils.nodeToString(document));
        LOG.trace("Build the document content for the new toc list completed in {} ms", (System.currentTimeMillis() - startTime));
        return nodeToByteArray(document);
    }

    private Node navigateToFirstTocElement(List<TableOfContentItemVO> tableOfContentItemVOs, Node document) {
        Node node = null;
        if (!tableOfContentItemVOs.isEmpty()) {
            TableOfContentItemVO firstTocVO = tableOfContentItemVOs.listIterator().next();
            node = getFirstElementByName(document, firstTocVO.getTocItem().getAknTag().value());
        }
        return node;
    }

    private Node navigateToTocElement(TableOfContentItemVO tocVo, Node document) {
        Node node = getFirstElementByName(document, tocVo.getTocItem().getAknTag().value());
        return node;
    }

    protected abstract Node buildTocItemContent(List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, Map<TocItem, List<TocItem>> tocRules,
                                                Document document, Node node, TableOfContentItemVO tocVo, User user);

    @Override
    public String getElementValue(byte[] xmlContent, String xPath, boolean namespaceEnabled) {
        Document document = createDocument(xmlContent, namespaceEnabled);
        Node node = XmlUtils.getFirstElementByXPath(document, xPath, namespaceEnabled);
        String elementValue = null;
        if (node != null) {
            elementValue = node.getTextContent();
        }
        return elementValue;
    }

    @Override
    public boolean evalXPath(byte[] xmlContent, String xPath, boolean namespaceEnabled) {
        Document document = createDocument(xmlContent, namespaceEnabled);
        return XmlUtils.evalXPath(document, xPath, namespaceEnabled);
    }

    @Override
    public int getElementCountByXpath(byte[] xmlContent, String xPath, boolean namespaceEnabled){
        Document document = createDocument(xmlContent, namespaceEnabled);
        return XmlUtils.getElementCountByXpath(document, xPath, namespaceEnabled);
    }

    @Override
    public String getDocType(byte[] xmlContent, boolean namespaceEnabled) {
        Document document = createDocument(xmlContent, namespaceEnabled);
        Node node = XmlUtils.getFirstElementByXPath(document, xPathCatalog.getXPathDocType(), true);
        String docType = null;
        if (node != null) {
            docType = XmlUtils.getAttributeValue(node, XML_SHOW_AS);
        }
        return docType;
    }

    @Override
    public byte[] removeElement(byte[] xmlContent, String xPath, boolean namespaceEnabled) {
        Document document = createDocument(xmlContent, namespaceEnabled);
        XmlUtils.deleteElementsByXPath(document, xPath, namespaceEnabled);
        return nodeToByteArray(document);
    }

    @Override
    public byte[] insertElement(byte[] xmlContent, String xPath, boolean namespaceEnabled, String newContent) {
        Document document = createDocument(xmlContent, namespaceEnabled);
        Node node = XmlUtils.getFirstElementByXPath(document, xPath, namespaceEnabled);
        if (node != null) {
            Node newNode = XmlUtils.createNodeFromXmlFragment(document, newContent.getBytes(UTF_8), false);
            addSibling(newNode, node, false);
        }
        return nodeToByteArray(document);
    }

    @Override
    public byte[] replaceElement(byte[] xmlContent, String xPath, boolean namespaceEnabled, String newContent) {
        Document document = createDocument(xmlContent, namespaceEnabled);  //TODO remove the boolean, always coming as true
        Node node = XmlUtils.getFirstElementByXPath(document, xPath, namespaceEnabled);
        if (node != null) {
            node = XmlUtils.replaceElement(node, newContent);
            xmlContent = nodeToByteArray(node);
        }
        return xmlContent;
    }

    @Override
    public byte[] replaceElementByTagNameAndId(byte[] xmlContent, String newContent, String tagName, String idAttributeValue) {
        //TODO remove tagName from the logic. Everywhere in this file. Check all the methods
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, idAttributeValue);
        if (node != null) {
            node = XmlUtils.replaceElement(node, newContent);
            xmlContent = nodeToByteArray(node);
            //TODO refactor doXMLPostProcessing to work with Node in input too. To increase performance
            xmlContent = doXMLPostProcessing(xmlContent);
        }
        return xmlContent;
    }

    @Override
    public byte[] insertElementByTagNameAndId(byte[] xmlContent, String elementTemplate, String tagName, String idAttributeValue, boolean before) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, idAttributeValue);
        if (node != null) {
            Node newNode = XmlUtils.createNodeFromXmlFragment(document, elementTemplate.getBytes(UTF_8), false);
            XmlUtils.addSibling(newNode, node, before);
        }
        return nodeToByteArray(document);
    }

    @Override
    public String getElementByNameAndId(byte[] xmlContent, String tagName, String idAttributeValue) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementByNameAndId(document, tagName, idAttributeValue);
        String elementAsString = null;
        if (node != null) {
            elementAsString = nodeToString(node);
            elementAsString = removeAllNameSpaces(elementAsString);
        }
        return elementAsString;
    }

    @Override
    public String getElementAttributeValueByNameAndId(byte[] xmlContent, String attributeName, String tagName, String idAttributeValue) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementByNameAndId(document, tagName, idAttributeValue);
        String attrVal = "false";
        if (node != null) {
            String nodeAttrVal = getAttributeValue(node, attributeName);
            if (nodeAttrVal != null) {
                attrVal = nodeAttrVal;
            }
        }
        return attrVal;
    }

    @Override
    public Element getParentElement(byte[] xmlContent, String tagName, String idAttributeValue) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, idAttributeValue);
        Element element = null;
        if (node != null) {
            element = getParentElement(node);
        }
        return element;
    }

    private Element getParentElement(Node node) {
        Element element = null;
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            String elementTagName = parentNode.getNodeName();
            String parentId = getAttributeValue(parentNode, XMLID);
            if (parentId == null) {
                parentId = "";
            }
            String elementFragment = nodeToString(parentNode);
            element = new Element(parentId, elementTagName, elementFragment);
        }
        return element;
    }

    @Override
    public Element getSiblingElement(byte[] xmlContent, String tagName, String idAttributeValue, List<String> elementTags, boolean before) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, idAttributeValue);
        Element element = null;
        if (node != null) {
            element = getSiblingElement(node, elementTags, before);
        }
        return element;
    }

    protected Element getSiblingElement(Node node, List<String> elementTags, boolean before) {
        Element element = null;
        Node sibling;
        while ((sibling = XmlUtils.getSibling(node, before)) != null && element == null) {
            String elementTagName = sibling.getNodeName();
            if (elementTags.contains(elementTagName) || elementTags.isEmpty()) {
                String elementId = getId(sibling) != null ? getId(sibling) : "";
                String elementFragment = nodeToString(sibling);
                element = new Element(elementId, elementTagName, elementFragment);
            }
        }
        return element;
    }

    @Override
    public Element getChildElement(byte[] xmlContent, String tagName, String idAttributeValue, List<String> elementTags, int position) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, idAttributeValue);
        Element element = null;
        if (node != null) {
            List<Node> nodeList = getChildren(node);
            int childProcessed = 0;
            String elementTagName;
            for (int i = 0; i < nodeList.size(); i++) {
                node = nodeList.get(i);
                if (childProcessed < position) {
                    elementTagName = node.getNodeName();
                    if (elementTags.contains(elementTagName) || elementTags.isEmpty()) {
                        childProcessed++;
                        if (childProcessed == position) {
                            String elementId = getId(node) != null ? getId(node) : "";
                            String elementFragment = nodeToString(node);
                            element = new Element(elementId, elementTagName, elementFragment);
                        }
                    }
                }
            }
        }
        return element;
    }

    @Override
    public List<Map<String, String>> getElementsAttributesByPath(byte[] xmlContent, String xPath) {
        List<Map<String, String>> elementAttributesList = new ArrayList<>();
        Document document = createDocument(xmlContent);
        NodeList elements = XmlUtils.getElementsByXPath(document, xPath);
        for (int i = 0; i < elements.getLength(); i++) {
            Node element = elements.item(i);
            elementAttributesList.add(XmlUtils.getAttributes(element));
        }
        return elementAttributesList;
    }

    @Override
    public Map<String, String> getElementAttributesByPath(byte[] xmlContent, String xPath, boolean namespaceEnabled) {
        Map<String, String> attributes = new HashMap<>();
        Document document = createDocument(xmlContent, namespaceEnabled);
        Node element = XmlUtils.getFirstElementByXPath(document, xPath, namespaceEnabled);
        if (element != null) {
            attributes = XmlUtils.getAttributes(element);
        }
        return attributes;
    }

    @Override
    public String getElementContentFragmentByPath(byte[] xmlContent, String xPath, boolean namespaceEnabled) {
        Document document = createDocument(xmlContent, namespaceEnabled);
        Node element = XmlUtils.getFirstElementByXPath(document, xPath, namespaceEnabled);
        if (element != null) {
            return nodeToString(element);
        }
        return null;
    }

    @Override
    public String getElementFragmentByPath(byte[] xmlContent, String xPath, boolean namespaceEnabled) {
        Document document = createDocument(xmlContent, namespaceEnabled);
        Node element = XmlUtils.getFirstElementByXPath(document, xPath, namespaceEnabled);
        if (element != null) {
            return element.getTextContent(); // or remove open/close tag from nodeToString(element);
        }
        return null;
    }

    @Override
    public byte[] setAttributeForAllChildren(byte[] xmlContent, String parentTag, List<String> elementTags, String attributeName, String value) {
        Document document = createDocument(xmlContent);
        NodeList nodeList = XmlUtils.getElementsByName(document, parentTag);
        for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
            Node node = nodeList.item(nodeIndex);
            List<Node> children = getChildren(node);
            for (int childIndex = 0; childIndex < children.size(); childIndex++) {
                setAttribute(children.get(childIndex), elementTags, attributeName, value);
            }
        }
        return nodeToByteArray(document);
    }

    private static void setAttribute(Node node, List<String> elementTags, String attrName, String attrValue) {
        String tagName = node.getNodeName();
        if (tagName.equals(META)) {
            return;
        }

        if (elementTags.contains(tagName) || elementTags.isEmpty()) {
            String val = getAttributeValue(node, attrName);
            if (val != null) {
                LOG.trace("Attribute {} already exists. Updating the value to {}", attrName, attrValue);
            }
            XmlUtils.addAttribute(node, attrName, String.valueOf(attrValue));
        }

        List<Node> children = getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            setAttribute(children.get(i), elementTags, attrName, attrValue);
        }
    }

    @Override
    public byte[] doXMLPostProcessing(byte[] xmlContent) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Document document = doXmlPostProcessingCommon(xmlContent);

        specificInstanceXMLPostProcessing(document);
        long postProcessingTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        LOG.trace("Finished XML post processing: doXMLPostProcessing at {}ms",
                postProcessingTime, (System.currentTimeMillis() - postProcessingTime));
        return nodeToByteArray(document);
    }

    private Document doXmlPostProcessingCommon(byte[] xmlContent) {
        long startTime = System.currentTimeMillis();
        Document document = createDocument(xmlContent);

        // Inject Ids
        Stopwatch stopwatch = Stopwatch.createStarted();
        injectTagIdsInNode(document.getDocumentElement(), IdGenerator.DEFAULT_PREFIX);
        long injectIdTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        // modify Authnote markers
        modifyAuthorialNoteMarkers(document, 1);
        long authNoteTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        // update refs
        updateReferences(document);
        long mrefUpdateTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

        LOG.trace("Finished doXMLPostProcessing: Ids Injected at {}ms, authNote Renumbering at {}ms, mref udpated at {}ms, Total time elapsed {}ms",
                injectIdTime, authNoteTime, mrefUpdateTime, (System.currentTimeMillis() - startTime));
        return document;
    }

    public abstract void specificInstanceXMLPostProcessing(Node node);

    protected String modifySubElement(Node node, String parentOrigin) {
        String originAttr = getAttributeValue(node, LEOS_ORIGIN_ATTR);
        if (originAttr == null) {
            originAttr = parentOrigin;
        }

        if (originAttr.equals(parentOrigin)) {
            XmlUtils.addAttribute(node, LEOS_ORIGIN_ATTR, originAttr);

            String softAction = getAttributeValue(node, LEOS_SOFT_ACTION_ATTR);
            if (softAction == null) {
                XmlUtils.addAttribute(node, LEOS_SOFT_ACTION_ATTR, SoftActionType.ADD.getSoftAction());
                XmlUtils.addAttribute(node, LEOS_SOFT_USER_ATTR, securityContext.getUserName());
                XmlUtils.addAttribute(node, LEOS_SOFT_DATE_ATTR, getXMLFormatDate());
            }
        }
        return originAttr;
    }

    private void injectTagIdsInNode(Node node, String idPrefix) {
        String tagName = node.getNodeName();
        if (skipNodeAndChildren(tagName)) {// skipping node processing along with children
            return;
        }

        String idAttrValue = null;
        if (!skipNodeOnly(tagName)) {// do not update id for this tag
            idAttrValue = updateNodeWithId(node, idPrefix);
        }

        idPrefix = determinePrefixForChildren(tagName, idAttrValue, idPrefix);
        List<Node> children = getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            injectTagIdsInNode(children.get(i), idPrefix);
        }
    }

    private String updateNodeWithId(Node node, String idPrefix) {
        String idAttrValue = getAttributeValue(node, XMLID);
        if (idAttrValue == null || idAttrValue.isEmpty()) {
            idAttrValue = IdGenerator.generateId(idPrefix, 7);
            XmlUtils.addAttribute(node, XMLID, idAttrValue);
        }
        return idAttrValue;
    }

    private void modifyAuthorialNoteMarkers(Node node, int markerNumber) {
        NodeList nodeList = XmlUtils.getElementsByName(node, AUTHORIAL_NOTE);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            XmlUtils.addAttribute(child, MARKER_ATTRIBUTE, Integer.toString(markerNumber++));
        }
    }

    @Override
    public byte[] updateReferences(byte[] xmlContent) {
        Document document = createDocument(xmlContent);
        if (updateReferences(document)) {
            return nodeToByteArray(document);
        } else {
            return null;
        }
    }

    boolean updateReferences(Document document) {
        boolean updated = false;
        String sourceRef = getContentByTagName(document, LEOS_REF);
        NodeList nodeList = XmlUtils.getElementsByName(document, MREF);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            String childXml = XmlUtils.getContentNodeAsXmlFragment(child);
            List<Ref> refs = findReferences(child, sourceRef);
            String updatedMrefContent;
            if (!refs.isEmpty()) {
                Result<String> labelResult = referenceLabelService.generateLabel(refs, sourceRef, getParentId(child), nodeToByteArray(document));
                if (labelResult.isOk()) {
                    updatedMrefContent = labelResult.get();
                    if (!updatedMrefContent.replaceAll("\\s+", "").equals(childXml.replaceAll("\\s+", ""))) {
                        child = XmlUtils.addContentToNode(document, child, updatedMrefContent);
                        String brokenRefAttr = XmlUtils.getAttributeValue(child, LEOS_REF_BROKEN_ATTR);
                        if (brokenRefAttr != null) {
                            XmlUtils.removeAttribute(document, LEOS_REF_BROKEN_ATTR);
                        }
                        updated = true;
                    }
                } else {
                    XmlUtils.addAttribute(child, LEOS_REF_BROKEN_ATTR, "true");
                    updated = true;
                }
            }
        }
        return updated;
    }

    private List<Ref> findReferences(Node node, String documentRefSource) {
        List<Ref> refs = new ArrayList<>();
        NodeList nodeList = XmlUtils.getElementsByName(node, REF);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            refs.add(getRefElement(child, documentRefSource));
        }
        return refs;
    }

    private Ref getRefElement(Node node, String documentRef) {
        String id = getAttributeValue(node, XMLID);
        String href = getAttributeValue(node, HREF);
        String origin = getAttributeValue(node, LEOS_ORIGIN_ATTR);
        if (href != null) {
            String[] hrefMixedArr = href.split("/");
            if (hrefMixedArr.length > 1) {
                documentRef = hrefMixedArr[0];
                href = hrefMixedArr[1];
            } else {
                href = hrefMixedArr[0];
            }
        }
        String refVal = node.getTextContent();
        return new Ref(id, href, documentRef, origin, refVal);
    }

    @Override
    public byte[] replaceTextInElement(byte[] xmlContent, String origText, String newText, String elementId, int startOffset, int endOffset) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, elementId);
        byte[] newElement = null;
        if (node != null) {
            String elementContent = nodeToString(node);
            StringBuilder eltContent = new StringBuilder(elementContent);
            ImmutableTriple<String, Integer, Integer> result = getSubstringAvoidingTags(elementContent, startOffset, endOffset);
            String matchingText = result.left;
            if (matchingText.replace(NON_BREAKING_SPACE, WHITESPACE).equals(StringEscapeUtils.escapeXml10(origText.replace(NON_BREAKING_SPACE, WHITESPACE)))) {
                eltContent.replace(result.middle, result.right, StringEscapeUtils.escapeXml10(normalizeNewText(origText, newText)));
                Node newNode = XmlUtils.createNodeFromXmlFragment(document, eltContent.toString().getBytes(UTF_8), false);
                XmlUtils.replaceElement(newNode, node);
                newElement = nodeToByteArray(document);
            } else {
                LOG.debug("Text not matching {}, original text:{}, matched text:{}", elementId, origText, matchingText);
            }
        }
        return newElement;
    }

    @Override
    public byte[] appendElementToTag(byte[] xmlContent, String tagName, String newContent, boolean asFirstChild) {
        Document document = createDocument(xmlContent);
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            throw new IllegalArgumentException("No tag found with name " + tagName);
        }

        Node newNode = createNodeFromXmlFragment(document, newContent.getBytes(UTF_8));
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (asFirstChild) {
                XmlUtils.addFirstChild(newNode, node);
            } else {
                XmlUtils.addLastChild(newNode, node);
            }
        }
        return nodeToByteArray(document);
    }

    @Override
    public byte[] insertDepthAttribute(byte[] xmlContent, String tagName, String elementId) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, elementId);
        if (tagName.equals(NUM)) {
            tagName = XmlUtils.getParentTagName(node);
            elementId = XmlUtils.getParentId(node);
        }

        NodeList nodeList = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            int depth = getElementDepth(node, elementId);
            addAttribute(node, LEOS_DEPTH_ATTR, String.valueOf(depth));
        }
        return nodeToByteArray(document);
    }

    @Override
    public byte[] searchAndReplaceText(byte[] xmlContent, String searchText, String replaceText) {
        Document document = createDocument(xmlContent);
        String xPath = String.format("//*[contains(lower-case(text()), %s)]", wrapXPathWithQuotes(searchText.toLowerCase()));
        NodeList nodeList = XmlUtils.getElementsByXPath(document, xPath);
        boolean found = false;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            String content = child.getTextContent();
            if (content != null && isEditableElement(child)) {
                String updatedContent = content.replaceAll("(?i)" + Pattern.quote(searchText), Matcher.quoteReplacement(replaceText));
                child.setTextContent(StringEscapeUtils.escapeXml10(updatedContent));
                found = true;
            }
        }

        if (found) { //update content only if any change happened
            xmlContent = nodeToByteArray(document);
        }
        return xmlContent;
    }

    public static boolean isEditableElement(Node node) {
        Validate.isTrue(node != null, "Node can not be null");
        Validate.isTrue(node.getParentNode() != null, "Parent Node can not be null");
        EditableAttributeValue editableAttrVal = getEditableAttributeForNode(node);
        node = node.getParentNode();
        while (EditableAttributeValue.UNDEFINED.equals(editableAttrVal) && node != null) {
            editableAttrVal = getEditableAttributeForNode(node);
            node = node.getParentNode();
        }
        return Boolean.parseBoolean(editableAttrVal.name());
    }

    private static EditableAttributeValue getEditableAttributeForNode(Node node) {
        Map<String, String> attrs = XmlUtils.getAttributes(node);
        String tagName = node.getNodeName();
        String attrVal = attrs.get(LEOS_EDITABLE_ATTR);
        return getEditableAttribute(tagName, attrVal);
    }

    @Override
    public Element getElementById(byte[] xmlContent, String idAttributeValue) {
        Validate.isTrue(idAttributeValue != null, "Id can not be null");
        Document document = createDocument(xmlContent);
        Element element = null;
        Node node = XmlUtils.getElementById(document, idAttributeValue);
        if (node != null) {
            String nodeString = nodeToStringSimple(node);
            element = new Element(idAttributeValue, node.getNodeName(), nodeString);
        }
        return element;
    }

    @Override
    public List<String> getAncestorsIdsForElementId(byte[] xmlContent, String idAttributeValue) {
        Validate.isTrue(idAttributeValue != null, "Id can not be null");
        LinkedList<String> ancestorsIds = new LinkedList<String>();

        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, idAttributeValue);
        if (node == null) {
            String errorMsg = String.format("Element with id: %s does not exists.", idAttributeValue);
            LOG.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        while ((node = node.getParentNode()) != null) {
            String idValue = getAttributeValue(node, XMLID);
            if (idValue != null) {
                ancestorsIds.addFirst(idValue);
            }
        }
        return ancestorsIds;
    }

    @Override
    public byte[] removeElements(byte[] xmlContent, String xpath, int levelsToRemove) {
        Document document = createDocument(xmlContent);
        NodeList nodeList = XmlUtils.getElementsByXPath(document, xpath);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node parent = node.getParentNode();
            for (int level = 0; level < levelsToRemove; level++) {
                node = parent; // go up in node levels
                parent = parent.getParentNode();
            }
            parent.removeChild(node);
            LOG.debug("Removed nodeName '{}' with id '{}' ", node.getNodeName(), getId(node));
        }
        return nodeToByteArray(document);
    }

    @Override
    public byte[] removeElements(byte[] xmlContent, String xpath) {
        return removeElements(xmlContent, xpath, 0);
    }

    @Override
    public String doImportedElementPreProcessing(String xmlContent, String elementType) {
        xmlContent = StringUtils.normalizeSpace(xmlContent);
        Document document = createDocument(xmlContent.getBytes(StandardCharsets.UTF_8));
        Node node = document.getFirstChild();
        String idPrefix = "imp_" + XmlUtils.getId(node);
        String newIdAttrValue = IdGenerator.generateId(idPrefix, 7);
        addAttribute(node, XMLID, newIdAttrValue);
        String updatedElement = nodeToString(node);
        updatedElement = removeSelfClosingElements(updatedElement);
        return updatedElement;
    }

    @Override
    public Element getTocElement(final byte[] xmlContent, final String idAttributeValue, final List<TableOfContentItemVO> toc, final List<String> tagNames) {
        Element currentElement = getElementById(xmlContent, idAttributeValue);
        if (isElementInToc(currentElement, toc)) {
            return currentElement;
        } else {
            Element childElement = getChildElement(xmlContent, currentElement.getElementTagName(), currentElement.getElementId(), tagNames, 1);
            if (childElement != null) {
                currentElement = childElement;
            }
        }

        while (currentElement != null && !isElementInToc(currentElement, toc)) {
            currentElement = getParentElement(xmlContent, currentElement.getElementTagName(), currentElement.getElementId());
        }
        return currentElement;
    }

    @Override
    public String getElementIdByPath(byte[] xmlContent, String xPath) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getFirstElementByXPath(document, xPath);
        if (node == null) {
            throw new IllegalArgumentException("Didn't found a node in xpath: " + xPath + ", namespace: true");
        }
        return XmlUtils.getAttributeValue(node, XMLID);
    }

    @Override
    public String removeEmptyHeading(String newContent) {
        Document document = createDocument(newContent.getBytes(StandardCharsets.UTF_8), false);
        XmlUtils.addLeosNamespace(document);
        NodeList headingList = XmlUtils.getElementsByName(document, HEADING);
        String contentOrigin = getAttributeValue(document, LEOS_ORIGIN_ATTR);
        boolean removed = false;
        for (int i = 0; i < headingList.getLength(); i++) {
            Node heading = headingList.item(i);
            String headingValue = heading.getTextContent();
            if (headingValue != null && headingValue.replaceAll(NBSP, "").trim().isEmpty()) {
                if (CN.equals(contentOrigin)) {
                    XmlUtils.deleteElement(heading);
                } else {
                    removeElement(heading);
                }
                removed = true;
            }
        }
        if (removed) {
            if (CN.equals(contentOrigin)) {
                newContent = XmlUtils.nodeToString(document);
            } else {
                newContent = getUpdatedContent(document);
            }
        }
        return newContent;
    }

    protected abstract String getUpdatedContent(Node node);

    protected abstract void removeElement(Node node);

    @Override
    public LevelItemVO getLevelItemVo(byte[] xmlContent, String elementId, String elementTagName) {
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, elementId);
        LevelItemVO levelItemVo = new LevelItemVO();
        if (node != null) {
            if (NUM.equals(elementTagName)) {
                node = node.getParentNode();
                if (node == null) {
                    throw new IllegalStateException("Element " + elementId + "is not NUM of a Level node.");
                }
            }
            int depth = getElementDepth(node, elementId);
            levelItemVo = createLevelItemVO(elementId, node, depth);
        }
        return levelItemVo;
    }

    private static int getElementDepth(Node node, String elementId) {
        int depth = 0;
        Node numNode = getFirstChild(node, NUM);
        if (numNode != null) {
            String elementNumber = numNode.getTextContent();
            if (elementNumber.contains(".")) {
                String[] levelArr = StringUtils.split(elementNumber, LEVEL_NUM_SEPARATOR);
                depth = levelArr.length;
            } else {
                depth = calculateDepthForNewElement(node, elementId);
            }
        }
        return depth;
    }

    private static int calculateDepthForNewElement(Node node, String elementId) {
        int depth = 0;
        node = XmlUtils.getElementById(node, elementId);
        if (node != null) {
            depth = getElementDepth(node, elementId);
        }
        return depth;
    }

    private LevelItemVO createLevelItemVO(String elementId, Node node, int depth) {
        LevelItemVO levelItemVo = new LevelItemVO();
        levelItemVo.setId(elementId);
        levelItemVo.setLevelDepth(depth);
        levelItemVo.setLevelNum(getChildContent(node, NUM));
        levelItemVo.setOrigin(getAttributeValue(node, LEOS_ORIGIN_ATTR));

        while ((node = getNextSibling(node, LEVEL)) != null) {
            int nextDepth = getElementDepth(node, elementId);
            if (nextDepth - depth == 1) { // If next sibling depth is > current level depth then add it as a child
                String siblingElementId = getId(node);
                if (siblingElementId != null) {
                    LevelItemVO childItemVO = createLevelItemVO(siblingElementId, node, nextDepth);
                    levelItemVo.addChildLevelItemVO(childItemVO);
                } else {
                    throw new IllegalStateException("Invalid XML element without Id exists");
                }
            } else if (nextDepth <= depth) {
                break;
            }
        }
        return levelItemVo;
    }

    @Override
    public byte[] updateRefsWithRefOrigin(byte[] xmlContent, String ref, String refOrigin) {
        Document document = createDocument(xmlContent);
        NodeList nodeList = XmlUtils.getElementsByXPath(document, REF);
        boolean flag = false;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            String href = getAttributeValue(child, HREF);
            if (href != null) {
                int index = href.indexOf('/');
                if (index >= 0) {
                    String refXml = href.substring(0, index);
                    if (refXml.equals(refOrigin)) {
                        String newRef = ref + href.substring(index);
                        XmlUtils.addAttribute(child, HREF, newRef);
                        flag = true;
                    }
                }
            }
        }

        if (flag) { //update only if changed
            xmlContent = nodeToByteArray(document);
        }
        return xmlContent;
    }

    @Override
    public byte[] updateDepthAttribute(byte[] xmlContent) {
        return xmlContent;
    }

    @Override
    public byte[] insertAffectedAttributeIntoParentElements(byte[] xmlContent, String idAttributeValue) {
        return xmlContent;
    }

    @Override
    public List<Element> getElementsByTagName(byte[] xmlContent, List<String> elementTags, boolean withContent) {
        Document document = createDocument(xmlContent);
        List<Element> elements = new ArrayList<>();
        for (String elementTag : elementTags) {
            NodeList nodeList = XmlUtils.getElementsByXPath(document, elementTag);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                String id = getId(child);
                if (id != null) {
                    elements.add(new Element(id, child.getNodeName(), withContent ? nodeToString(document) : null));
                }
            }
        }
        return elements;
    }

    @Override
    public byte[] ignoreNotSelectedElements(byte[] xmlContent, List<String> rootElements, List<String> elementIds) {
        List<String> ancestorIds = getAncestorsIdsForElements(xmlContent, elementIds);
        Document document = createDocument(xmlContent);
        for (String rootElement : rootElements) {
            Node node = XmlUtils.getFirstElementByName(document, rootElement);
            if (node != null) {
                ignoreNotSelectedElement(node, elementIds, ancestorIds);
            }
        }
        return nodeToByteArray(document);
    }

    private List<String> getAncestorsIdsForElements(byte[] xmlContent, List<String> elementIds) {
        List<String> ancestorIds = new ArrayList<>();
        elementIds.stream().forEach(elementId -> {
            ancestorIds.addAll(this.getAncestorsIdsForElementId(xmlContent, elementId));
        });
        return ancestorIds.stream().distinct().collect(Collectors.toList());
    }

    private void ignoreNotSelectedElement(Node node, List<String> elementIds, List<String> ancestorIds) {
        String tagName = node.getNodeName();
        String elementId = getId(node);
        if (elementId != null) {
            if (elementIds.contains(elementId) || tagName.equals(NUM) || tagName.equals(HEADING)) {
                return;
            } else if (!ancestorIds.contains(elementId)) {
                addAttribute(node, STATUS_IGNORED_ATTR, STATUS_IGNORED_ATTR);
                return;
            }
        }
        List<Node> children = getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            ignoreNotSelectedElement(children.get(i), elementIds, ancestorIds);
        }
    }

    @Override
    public abstract List<CloneProposalMetadataVO> getClonedProposalsMetadataVO(String proposalId, String legDocumentName);

    protected byte[] deleteElementByTagNameAndId(byte[] xmlContent, String tagName, String idAttributeValue) {
        Document document = createDocument(xmlContent);
        XmlUtils.deleteElementById(document, idAttributeValue);
        return nodeToByteArray(document);
    }

    protected void updateSoftMoveLabelAttribute(Node documentNode, String attr) {
        String sourceDocumentRef = getContentByTagName(documentNode, LEOS_REF);
        NodeList nodeList = XmlUtils.getElementsByXPath(documentNode, String.format("//*[@%s]", attr));
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Result<String> labelResult = referenceLabelService.generateSoftMoveLabel(getRefFromSoftMovedElt(node, attr),
                    XmlUtils.getParentId(node), nodeToByteArray(documentNode), attr, sourceDocumentRef);
            if (labelResult != null && labelResult.isOk()) {
                XmlUtils.addAttribute(node, LEOS_SOFT_MOVED_LABEL_ATTR, labelResult.get());
            }
        }
    }

    private Ref getRefFromSoftMovedElt(Node node, String attr) {
        String id = XmlUtils.getId(node);
        String href = XmlUtils.getAttributeValue(node, attr);
        String origin = XmlUtils.getAttributeValue(node, LEOS_ORIGIN_ATTR);
        return new Ref(id, href, null, origin);
    }

    protected Element getSiblingOfParentElement(byte[] xmlContent, String tagName, String id) {
        LOG.trace("getSiblingOfParentElement for node {} with id {}", tagName, id);
        Element element = null;
        Document document = createDocument(xmlContent);
        Node node = XmlUtils.getElementById(document, id);
        if (node != null) {
            Node parent = node.getParentNode();
            if (parent != null) {
                element = getSiblingElement(parent, Collections.emptyList(), false);
            }
        }
        return element;
    }

    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        Element splitElement;
        if (Arrays.asList(SUBPARAGRAPH, SUBPOINT).contains(tagName) || (PARAGRAPH.equals(tagName) && !content.contains("<" + SUBPARAGRAPH + ">"))) {
            splitElement = getSiblingElement(xmlContent, tagName, idAttributeValue, Collections.emptyList(), false);
        } else if (LEVEL.equals(tagName)) {
            return null;
        } else if (CONTENT.equals(tagName)) {
            splitElement = getSiblingOfParentElement(xmlContent, CONTENT, idAttributeValue);
        } else {
            splitElement = getChildElement(xmlContent, tagName, idAttributeValue, Arrays.asList(SUBPARAGRAPH, SUBPOINT), 2);
        }

        return buildSplittedElementPair(xmlContent, splitElement);
    }

    protected byte[] removeElementByTagNameAndId(byte[] xmlContent, Element element, String currentOrigin) {
        String tagName = element.getElementTagName();
        String elementId = element.getElementId();
        String content = element.getElementFragment();
        Map<String, String> attributes = getElementAttributesByPath(content.getBytes(XmlHelper.UTF_8),
                "/" + tagName, false);
        String originElementId = elementId;
        Element parentElement = getParentElement(xmlContent, tagName, elementId);
        // Needs to build hierarchy from the parent to have a consistent hierarchy
        TableOfContentItemVO tocItem = getElementHierarchyFromToc(parentElement.getElementId(), xmlContent);
        // If the element has been transformed it needs to be reverted to its original layout before deleting
        if (isSoftTransformed(attributes)) {
            String originalId = elementId.substring(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX.length());
            xmlContent = restoreTransformedElement(xmlContent, originalId, elementId, tagName);
            tocItem = getElementHierarchyFromToc(originalId, xmlContent);
            elementId = originalId;
        } else if ((parentElement.getElementTagName().equalsIgnoreCase(PARAGRAPH) && tagName.equalsIgnoreCase(SUBPARAGRAPH)
                && tocItem.getChildItems().get(0).getId().equals(elementId))
                || (tagName.equalsIgnoreCase(SUBPOINT) && (parentElement.getElementTagName().equalsIgnoreCase(INDENT) || (tocItem.getChildItems().size() > 0  && tocItem.getChildItems().get(0).getId().equals(elementId))))
                || tagName.equalsIgnoreCase(CONTENT)) {
            // Cases when the deleted element should be the wrapping element
            elementId = parentElement.getElementId();
        } else {
            Optional<TableOfContentItemVO> item = getTocElementById(elementId, tocItem);
            if (item.isPresent()) {
                tocItem = item.get();
            }
        }
        if ((hasTocItemSoftOrigin(tocItem, currentOrigin) && hasChildrenWithOriginAttr(tocItem, EC))
                || (hasTocItemSoftOrigin(tocItem, EC) && (hasChildrenWithOriginAttr(tocItem, currentOrigin) ||
                hasSoftActionChildrenAttr(tocItem, SoftActionType.MOVE_FROM)))) {
            for (TableOfContentItemVO child : tocItem.getChildItems()) {
                Element childElement = getElementById(xmlContent, child.getId());
                if (!child.getId().equals(originElementId)) {
                    xmlContent = removeElementByTagNameAndId(xmlContent, childElement, currentOrigin);
                } else {
                    for (TableOfContentItemVO grandChild : child.getChildItems()) {
                        Element grandChildElement = getElementById(xmlContent, grandChild.getId());
                        xmlContent = removeElementByTagNameAndId(xmlContent, grandChildElement, currentOrigin);
                    }
                }
            }
        }
        return doRemoveElementByTagNameAndId(xmlContent, elementId, originElementId);
    }

    private TableOfContentItemVO getElementHierarchyFromToc(final String elementId, final byte[] xmlContent) {
        final List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        final Map<TocItem, List<TocItem>> tocRules = structureContextProvider.get().getTocRules();
        return XmlContentProcessorHelper.buildTableOfContentFromNodeId(tocItems, tocRules, elementId, xmlContent, TocMode.SIMPLIFIED);
    }

    protected boolean isSoftTransformed(final Map<String, String> attributes) {
        return ((attributes.get(LEOS_SOFT_ACTION_ATTR) != null) && attributes.get(LEOS_SOFT_ACTION_ATTR).equals(SoftActionType.TRANSFORM.getSoftAction()));
    }

    protected byte[] restoreTransformedElement(byte[] xmlContent, final String destinationId, final String originId, final String originTagName) {
        Validate.isTrue(destinationId !=null,"destinationId can not be null");
        Validate.isTrue(originId !=null,"originId can not be null");
        try
        {
            Node document = XmlUtils.createDocument(xmlContent);
            String contentXPath = "//*[@xml:id = '" + originId + "']/akn:content";
            Node contentNode = XmlUtils.getFirstElementByXPath(document, contentXPath);
            if (contentNode != null) {
                String originContent = nodeToString(contentNode);
                String tagNameXPath = "//*[@xml:id = '" + destinationId + "']/" + originTagName;
                Node tagNode = XmlUtils.getFirstElementByXPath(document, tagNameXPath);
                if (tagNode != null) {
                    String destinationContentId = XmlUtils.getAttributeValue(tagNode, XMLID);
                    String tagName = tagNode.getNodeName();
                    xmlContent = replaceElementByTagNameAndId(xmlContent, originContent, tagName, destinationContentId);
                }
            }
        } catch(Exception e) {
            LOG.error("Unexpected error occurred while restoring element in restoreTransformedElement", e);
        }
        return xmlContent;
    }

    private boolean hasChildrenWithOriginAttr(final TableOfContentItemVO tocItem, final String originValue) {
        for (TableOfContentItemVO childTocItem : tocItem.getChildItems()) {
            if (hasTocItemSoftOrigin(childTocItem, originValue)) {
                return true;
            } else {
                if (hasChildrenWithOriginAttr(childTocItem, originValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasSoftActionChildrenAttr(final TableOfContentItemVO tocItem, final SoftActionType actionType) {
        for (TableOfContentItemVO childTocItem : tocItem.getChildItems()) {
            if (hasTocItemSoftAction(childTocItem, actionType)) {
                return true;
            } else {
                if (hasSoftActionChildrenAttr(childTocItem, actionType)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isProposalElement(Map<String, String> attributes) {
        return ((attributes.get(LEOS_ORIGIN_ATTR) != null) && attributes.get(LEOS_ORIGIN_ATTR).equals(EC));
    }

    protected boolean isSoftMovedFrom(Map<String, String> attributes) {
        return ((attributes.get(LEOS_SOFT_ACTION_ATTR) != null) && attributes.get(LEOS_SOFT_ACTION_ATTR).equals(SoftActionType.MOVE_FROM.getSoftAction()));
    }

    protected String softDeleteElement(Node node, boolean replacePrefix) {
        XmlUtils.insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
        XmlUtils.insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
        updateSoftAttributes(SoftActionType.DELETE, node);

        XmlUtils.removeAttribute(node, LEOS_SOFT_MOVED_LABEL_ATTR);
        XmlUtils.removeAttribute(node, LEOS_SOFT_MOVE_TO);

        XmlUtils.updateXMLIDAttributesInElementContent(node, SOFT_DELETE_PLACEHOLDER_ID_PREFIX, replacePrefix);
        return XmlUtils.nodeToString(node);
    }

    protected void updateSoftAttributes(SoftActionType softAction, Node node) {
        if(softAction != null) {
            XmlUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_ACTION_ATTR, softAction.getSoftAction());
        }
        XmlUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_ACTION_ROOT_ATTR, Boolean.TRUE.toString());
        XmlUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_USER_ATTR, securityContext.getUserName());
        XmlUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_DATE_ATTR, getXMLFormatDate());
    }

    protected byte[] softDeleteMovedToElement(byte[] xmlContent, Node node, final String movedFromElementId) {
        String movedId = SOFT_MOVE_PLACEHOLDER_ID_PREFIX + movedFromElementId;
        return replaceElementByTagNameAndId(xmlContent, softDeleteElement(node, true), node.getNodeName(), movedId);
    }

    protected abstract byte[] doRemoveElementByTagNameAndId(byte[] xmlContent, String elementId, String originElementId);
    protected abstract Pair<byte[], Element> buildSplittedElementPair(byte[] xmlContent, Element splitElement);

    @Override
    public Pair<byte[], String> updateSoftMovedAttributes(byte[] xmlContent, String elementContent) {
        return new Pair(null, null);
    }

    @Override
    public boolean isAnnexComparisonRequired(byte[] contentBytes) {
        Document document = createDocument(contentBytes);
        Node node = XmlUtils.getFirstChild(document, MAIN_BODY);
        String origin = XmlUtils.getAttributeValue(node, LEOS_ORIGIN_ATTR);
        return EC.equals(origin);
    }
}
