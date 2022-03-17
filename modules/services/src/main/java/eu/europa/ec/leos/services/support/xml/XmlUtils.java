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

import eu.europa.ec.leos.model.action.SoftActionType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jaxen.dom.DOMXPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XPathV1Catalog.NAMESPACE_AKN_NAME;
import static eu.europa.ec.leos.services.support.xml.XPathV1Catalog.NAMESPACE_AKN_URI;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLOSE_END_TAG;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLOSE_TAG;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.ID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INLINE_ELEMENTS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.OPEN_END_TAG;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.OPEN_TAG;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_ACTIONS_PREFIXES;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.convertStringDateToCalendar;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.escapeXml;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.findString;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.removeSelfClosingElements;

public class XmlUtils {

    private static final Logger LOG = LoggerFactory.getLogger(XmlUtils.class);

    public static Document createDocument(byte[] xmlContent, boolean namespaceEnabled) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(namespaceEnabled);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            Document doc = builder.parse(new ByteArrayInputStream(xmlContent));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create create document ", e);
        }
    }

    public static Document createDocument(byte[] xmlContent) {
        return createDocument(xmlContent, true);
    }

    public static Node createNodeFromXmlFragment(Document document, byte[] xmlFragment) {
        return createNodeFromXmlFragment(document, xmlFragment, true);
    }

    public static Node createNodeFromXmlFragment(Document document, byte[] xmlFragment, boolean namespaceEnabled) {
        Document externalDoc = createDocument(xmlFragment, namespaceEnabled);
        Node externalNode = externalDoc.getDocumentElement();
        return document.importNode(externalNode, true);
    }

    public static Node addContentToNode(Document document, Node node, String newContent) {
        return addContentToNode(document, node, newContent, true);
    }

    public static Node addContentToNode(Document document, Node node, String newContent, boolean removeExisting) {
        if (removeExisting) {
            node.setTextContent("");
        }
        String nodeAsString = nodeToString(node);
        nodeAsString = removeSelfClosingElements(nodeAsString);
        String openTagAndActualContent = findString(nodeAsString, "<mref(.|\\S|\\n)*?>");
        String closeTagStr = findString(nodeAsString, "<\\/\\S+?>$");
        String newNodeXml = openTagAndActualContent + newContent + closeTagStr;
        Node newNode = createNodeFromXmlFragment(document, newNodeXml.getBytes(UTF_8));
        replaceElement(newNode, node);
        return newNode;
    }

    public static byte[] nodeToByteArray(Node node) {
        return nodeToStringWithTransformer(node).getBytes(UTF_8);
    }

    public static String nodeToString(Node node) {
        return nodeToStringWithTransformer(node);
    }

    /**
     * This method performs better that nodeToStringSimple() for normal/big documents.
     * For small fragments nodeToStringSimple() performs better
     */
    public static String nodeToStringWithTransformer(Node node) {
        StringWriter sw = new StringWriter();
        StreamResult output = new StreamResult(sw);
        saveNodeToOutput(node, output);
        return sw.toString();
    }

    private static void saveNodeToOutput(Node node, StreamResult output) {
        try {
            final Source input = new DOMSource(node);
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(input, output);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot save Node to output", e);
        }
    }

    public static byte[] nodeToByteArraySimple(Node node) {
        return nodeToStringSimple(node).getBytes(UTF_8);
    }

    public static String getContentNodeAsXmlFragment(Node node) {
        String xmlContent = nodeToStringSimple(node);
        return XmlHelper.removeEnclosingTags(xmlContent);
    }

    /**
     * Skips all XML headers and print only the real XML root <akomaNtoso>.
     * This method performs better that nodeToStringWithTransformer() for small contents.
     * For normal/big documents use nodeToStringWithTransformer.
     */
    public static String nodeToStringSimple(Node node) {
        StringBuffer sb = new StringBuffer();
        if (node != null) {
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                node = node.getFirstChild();
            }
            buildNodeAsString(node, sb);
            if (sb.toString().length() == 0) {
                LOG.warn("Potential error!!! The structure of the XML does not contain only one single root <akomaNtoso>. Other XML headers are present.");
                buildNodeAsString(getNextSibling(node), sb);
            }
        }
        return sb.toString();
    }

    private static String buildNodeAsString(Node node, StringBuffer sb) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            sb.append(OPEN_TAG + node.getNodeName());  // sb: <tagName
            NamedNodeMap attributesMap = node.getAttributes();
            if (attributesMap != null) {
                for (int i = 0; i < attributesMap.getLength(); i++) {
                    Node attr = attributesMap.item(i);
                    sb.append(" " + attr.getNodeName() + "=\"" + attr.getTextContent() + "\"");  // sb: <tagName atr="attrVal"
                }
            }
            if (node.hasChildNodes()) {
                sb.append(CLOSE_TAG);// sb: <tagName atr="attrVal">
            } else {
                sb.append(CLOSE_END_TAG);// sb: <tagName atr="attrVal"/>
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            sb.append(escapeXml(node.getTextContent()));  // append text content. sb:  <tagName atr="attrVal"> nodeValue
        } else {
//            throw new IllegalStateException("Check full XML trying to parse. Node type not handled!");
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            buildNodeAsString(nodeList.item(i), sb); //propagate to children
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.hasChildNodes()) {
                sb.append(OPEN_END_TAG + node.getNodeName() + CLOSE_TAG); // sb:  <tagName atr="attrVal">nodeValue</tagName>
            } // else is a self closed tag
        }
        return sb.toString();
    }

    public static Node getElementById(Node node, String elementId) {
        return getElementById(node, elementId, true);
    }

    public static Node getElementById(Node node, String elementId, boolean namespaceEnabled) {
        String attrId = namespaceEnabled ? XMLID : ID;
        // Preferred to use XPath for finding elements by ID.
        // In case you want to use API method getElementById(elementId) rules has to be set to Xerces to indicate which from the parameters will be considered as ID.
        NodeList nodes = getElementsByXPath(node, String.format("//*[@%s = '%s']", attrId, elementId), namespaceEnabled);
        if (nodes.getLength() == 0) {
            if (namespaceEnabled) { //try without namespace.
                // TODO Is a bad design! Actually we shouldn't be in a situation when we load the DOM tree with namespace enabled
                // while  we keep treating the ID attribute without namespace. Is happening in comparison when converting the
                // files in transformerService.formatToHtml()
                nodes = getElementsByXPath(node, String.format("//*[@%s = '%s']", ID, elementId), false);
            }
            if (nodes.getLength() == 0) {
                return null;
            }
        }

        if (nodes.getLength() > 1) {
            LOG.warn("Strange situation! Found more than 1 element with the same ID '{}', returning the first one ", elementId);
        }
        return nodes.item(0);
    }

    public static Node getElementByNameAndId(Node node, String tagName, String elementId) {
        NodeList nodeList = getElementsByName(node, tagName);
        if (elementId == null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        //TODO consider to remove the old logic in favor of: getElementById(node, elementId);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            String childId = getAttributeValue(child, XMLID);
            if (elementId.equals(childId)) {
                return child;
            }
        }
        return null;
    }

    public static Node getFirstElementByXPath(Node node, String xPath) {
        NodeList nodeList = getElementsByXPath(node, xPath);
        return nodeList.item(0);
    }

    public static Node getFirstElementByXPath(Node node, String xPath, boolean namespaceEnabled) {
        NodeList nodeList = getElementsByXPath(node, xPath, namespaceEnabled);
        return nodeList.item(0);
    }

    public static int getElementCountByXpath(Node node, String xPath, boolean namespaceEnabled) {
        NodeList list =  getElementsByXPath(node, xPath, namespaceEnabled);
        return list.getLength();
    }

    public static NodeList getElementsByXPath(Node node, String xPath) {
        return getElementsByXPath(node, xPath, true);
    }

    public static NodeList getElementsByXPath(Node node, String xPath, boolean namespaceEnabled) {
        try {
            XPath xPathParser = XPathFactory.newInstance().newXPath();
            if (namespaceEnabled) {
                xPathParser.setNamespaceContext(getSimpleNamespaceContext());
            }
            NodeList nodes = (NodeList) xPathParser.evaluate(xPath, node, XPathConstants.NODESET);
            return nodes;
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Cannot find xpath " + xPath);
        }
    }

    public static boolean evalXPathJaxen(Node node, String xPath) {
        try {
            DOMXPath myXPath = new DOMXPath(xPath);
            String myContent = myXPath.stringValueOf(node);
            if (!StringUtils.isEmpty(myContent))
                return true;
            else {
                return false;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot find xpath " + xPath);
        }
    }

    public static boolean evalXPath(Node node, String xPath) {
        return evalXPath(node, xPath, true);
    }

    public static boolean evalXPath(Node node, String xPath, boolean namespaceEnabled) {
        boolean elementFound = false;
        try {
            XPath xPathParser = XPathFactory.newInstance().newXPath();
            if (namespaceEnabled) {
                xPathParser.setNamespaceContext(getSimpleNamespaceContext());
            }
            NodeList nodes = (NodeList) xPathParser.evaluate(xPath, node, XPathConstants.NODESET);
            if (nodes != null && nodes.getLength() > 0) {
                elementFound = true;
            }
        } catch (XPathExpressionException e) {
            elementFound = false;
        }

        return elementFound;
    }

    private static SimpleNamespaceContext getSimpleNamespaceContext() {
        SimpleNamespaceContext nsc = new SimpleNamespaceContext();
        nsc.bindNamespaceUri("xml", "http://www.w3.org/XML/1998/namespace");
        nsc.bindNamespaceUri("leos", "urn:eu:europa:ec:leos");
        nsc.bindNamespaceUri(NAMESPACE_AKN_NAME, NAMESPACE_AKN_URI); //fake to trick the parser for the default ns
        return nsc;
    }

    public static Node getFirstElementByName(Node node, String elementName) {
        NodeList nodeList = getElementsByName(node, elementName);
        return nodeList.item(0);
    }

    public static NodeList getElementsByName(Node node, String elementName) {
        NodeList nodeList;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            nodeList = ((Element) node).getElementsByTagName(elementName);
        } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
            nodeList = ((Document) node).getElementsByTagName(elementName);
        } else {
            throw new IllegalArgumentException("Cannot get elements of type " + elementName + " inside node " + node.getNodeName());
        }
        return nodeList;
    }

    public static Node deleteElement(Node node) {
        return node.getParentNode().removeChild(node);
    }

    public static Node deleteElementById(Node node, String elementId) {
        node = getElementById(node, elementId);
        return node.getParentNode().removeChild(node);
    }

    public static List<Node> deleteElementsByXPath(Node node, String xPath) {
        return deleteElementsByXPath(node, xPath, true);
    }

    public static List<Node> deleteElementsByXPath(Node node, String xPath, boolean namespaceEnabled) {
        List<Node> deletedNodes = new ArrayList<>();
        NodeList nodeList = getElementsByXPath(node, xPath, namespaceEnabled);
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getParentNode() != null) {
                deletedNodes.add(nodeList.item(i).getParentNode().removeChild(nodeList.item(i)));
            }
        }
        return deletedNodes;
    }

    public static Node replaceElement(Node newNode, Node oldNode) {
        Validate.notNull(newNode, "New node cannot be null!");
        Validate.notNull(newNode, "Old node cannot be null!");
        Validate.notNull(oldNode.getParentNode(), "Parent of Old Node '" + oldNode.getNodeName() + "' cannot be null!");
        return oldNode.getParentNode().replaceChild(newNode, oldNode);
    }

    public static Node replaceElement(Node node, String newContent) {
        Node fakeNodeWithNewContent = createNodeFromXmlFragment(node.getOwnerDocument(), ("<fake>" + newContent + "</fake>").getBytes(UTF_8), false);
        NodeList fakeNodeChildNodes = fakeNodeWithNewContent.getChildNodes();
        for (int i = fakeNodeChildNodes.getLength() - 1; i >= 0 ; i--) {
            addSibling(fakeNodeChildNodes.item(i), node, false);
        }
        deleteElement(node);
        return node.getOwnerDocument();
    }

    public static Node importNodeInDocument(Document document, Node node) {
        return document.importNode(node, true);
    }

    public static Node getSibling(Node node, boolean before) {
        Node sibling;
        if (before) {
            sibling = getPrevSibling(node);
        } else {
            sibling = getNextSibling(node);
        }
        return sibling;
    }

    public static Node getNextSibling(Node node) {
        while ((node = node.getNextSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
        }
        return node;
    }

    public static Node getNextSibling(Node node, String elementName) {
        while ((node = node.getNextSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(elementName)) {
                break;
            }
        }
        return node;
    }

    public static Node getPrevSibling(Node node) {
        while ((node = node.getPreviousSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
        }
        return node;
    }

    public static Node getPrevSibling(Node node, String elementName) {
        while ((node = node.getPreviousSibling()) != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE && elementName.equals(node.getNodeName())) {
                break;
            }
        }
        return node;
    }

    public static Node addSibling(Node newNode, Node node, boolean before) {
        Validate.notNull(node, "Node cannot be null!");
        Validate.notNull(node.getParentNode(), "Node do not have a parent!");

        final Node parentNode = node.getParentNode();

        if (before) {
            newNode = parentNode.insertBefore(newNode, node);
        } else {
            node = getNextSibling(node);
            if (node != null) {
                newNode = parentNode.insertBefore(newNode, node);
            } else {
                newNode = parentNode.appendChild(newNode);
            }
        }
        return newNode;
    }

    public static Node addChild(Node newNode, Node node) {
        return node.appendChild(newNode);
    }

    public static Node addFirstChild(Node newNode, Node node) {
        Node firstChild = getFirstChild(node);
        return firstChild != null ? addSibling(newNode, firstChild, true) : addChild(newNode, node);
    }

    public static Node addLastChild(Node newNode, Node node) {
        node = node.getLastChild();
        return addSibling(newNode, node, false);
    }

    public static Element addLeosNamespace(Node node) {
        return addAttribute(node, "xmlns:leos", "urn:eu:europa:ec:leos");
    }

    public static Element addAttribute(Node node, String attrName, String attrValue) {
        Validate.notNull(node, "Node cannot be null!");
        Element element;
        if (node instanceof Document) {
            Document document = (Document) node;
            element = document.getDocumentElement();
        } else if (node instanceof Element) {
            element = (Element) node;
        } else {
            throw new IllegalArgumentException("Not handled!");
        }

        if (attrValue != null) {
            element.setAttribute(attrName, attrValue);
        } else {
            element.removeAttribute(attrName);
        }

        return element;
    }

    public static boolean removeAttribute(Node node, String attName) {
        boolean flag = false;
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            throw new IllegalArgumentException("Node is not of type Element");
        }
        Element element = (Element) node;
        if (element.hasAttribute(attName)) {
            element.removeAttribute(attName);
            flag = true;
        }
        return flag;
    }

    public static Node removeAllAttributes(Node node, String attrName, boolean namespaceEnabled) {
        String xPath = String.format("//*[@%s]", attrName);
        NodeList nodeList = getElementsByXPath(node, xPath, namespaceEnabled);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node eachNode = nodeList.item(i);
            removeAttribute(eachNode, attrName);
        }
        return node;
    }

    public static String getAttributeValueForElementId(Node node, String elementId, String attrName) {
        node = getElementById(node, elementId);
        NamedNodeMap attributesMap = node.getAttributes();
        Node nodeAttribute = attributesMap.getNamedItem(attrName);
        String attrVal = null;
        if (nodeAttribute != null) {
            attrVal = nodeAttribute.getTextContent();
        }
        return attrVal;
    }

    public static String getAttributeValue(Node node, String attrName) {
        String attrVal = null;
        NamedNodeMap attributesMap = node.getAttributes();
        if (attributesMap != null) {
            Node nodeAttribute = attributesMap.getNamedItem(attrName);
            if (nodeAttribute != null) {
                attrVal = nodeAttribute.getTextContent();
            }
        }
        return attrVal;
    }

    public static boolean containsAttribute(Node node, String attrName) {
        if (node != null) {
            String attrVal = getAttributeValue(node, attrName);
            if (!StringUtils.isEmpty(attrVal)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> getAttributes(Node node) {
        Map<String, String> attrs = new HashMap<>();
        NamedNodeMap attributesMap = node.getAttributes();
        if (attributesMap != null) {
            for (int i = 0; i < attributesMap.getLength(); i++) {
                Node nodeAttribute = attributesMap.item(i);
                attrs.put(nodeAttribute.getNodeName(), nodeAttribute.getTextContent());
            }
        }
        return attrs;
    }

    public static boolean checkAttributeValue(Node node, String attribute, String attributeValue) {
        String value = getAttributeValue(node, attribute);
        if (value != null && value.equals(attributeValue)) {
            return true;
        }
        return false;
    }

    public static Integer getAttributeValueAsInteger(Node node, String attrName) {
        String attrVal = getAttributeValue(node, attrName);
        return attrVal != null ? Integer.valueOf(attrVal) : null;
    }

    public static Boolean getAttributeValueAsBoolean(Node node, String attrName) {
        String attrVal = getAttributeValue(node, attrName);
        return attrVal != null ? Boolean.valueOf(attrVal) : null;
    }

    // Works only if name and value are of the  same value.
    public static <T extends Enum<T>> T getAttributeForType(Node node, String attrName, Class<T> enumClass) {
        String attrValue = getAttributeValue(node, attrName);
        return !StringUtils.isEmpty(attrValue) ? Enum.valueOf(enumClass, attrValue) : null;
    }

    public static SoftActionType getAttributeForSoftAction(Node node, String attrName) {
        String attrValue = getAttributeValue(node, attrName);
        return !StringUtils.isEmpty(attrValue) ? SoftActionType.of(attrValue) : null;
    }

    public static GregorianCalendar getAttributeValueAsGregorianCalendar(Node node, String attrName) {
        String attrVal = getAttributeValue(node, attrName);
        return convertStringDateToCalendar(attrVal);
    }

    public static Node getFirstChild(Node node) {
        Node firstChild = null;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                firstChild = node;
                break;
            }
        }
        return firstChild;
    }

    public static Node getFirstChild(Node node, String elementName) {
        Node firstChild = null;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (elementName.equals(node.getNodeName())) {
                    firstChild = node;
                    break;
                }
            }
        }
        return firstChild;
    }

    public static List<Node> getChildren(Node node, List<String> elementsName) {
        List<Node> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && elementsName.contains(node.getNodeName())) {
                children.add(node);
            }
        }
        return children;
    }

    public static List<Node> getChildren(Node node, String elementName) {
        return getChildren(node, Arrays.asList(elementName));
    }

    public static List<Node> getChildren(Node node) {
        List<Node> children = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                children.add(node);
            }
        }
        return children;
    }

    public static String getChildContent(Node node, String childTagName) {
        String number = null;
        Node child = getFirstChild(node, childTagName);
        if (child != null) {
            number = child.getTextContent();
        }
        return number;
    }

    public static String getParentTagName(Node node) {
        String elementTagName = null;
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            elementTagName = parentNode.getNodeName();
        }
        return elementTagName;
    }

    public static String getParentId(Node node) {
        String id = "";
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            id = getAttributeValue(parentNode, XMLID);
        }
        return id;
    }

    public static String getId(Node node) {
        String id = getAttributeValue(node, XMLID);
        if (id == null) {
            id = getAttributeValue(node, ID);
        }
        return id;
    }

    public static Node setId(Node node, String id) {
        return addAttribute(node, XMLID, id);
    }

    public static String getContentByTagName(Node node, String tagName) {
        Node element = getFirstElementByName(node, tagName);
        String content = null;
        if (element != null) {
            content = element.getTextContent();
        }
        return content;
    }

    public static Node createElementAsFirstChildOfNode(Document document, Node node, String elementName, String elementContent) {
        Node newNode = createElement(document, elementName, elementContent);
        newNode = addFirstChild(newNode, node);
        return newNode;
    }

    public static Node createElementAsLastChildOfNode(Document document, Node node, String elementName, String elementContent) {
        Node newNode = createElement(document, elementName, elementContent);
        node.appendChild(newNode);
        return newNode;
    }

    public static Element createElementWithAknNS(Document document, String elementName, String elementContent) {
        Element element = document.createElementNS(NAMESPACE_AKN_URI, elementName);
        element.setTextContent(elementContent);
        return element;
    }

    public static Element createElement(Document document, String elementName, String elementContent) {
        Element element = document.createElement(elementName);
        element.setTextContent(elementContent);
        return element;
    }

    public static Element createElement(Document document, String elementName, String elementId, String content) {
        Element element = document.createElement(elementName);

        Attr attr = document.createAttribute(XMLID);
        attr.setValue(elementId);
        element.setAttributeNode(attr);

        element.appendChild(document.createTextNode(content));
        return element;
    }

    private static String getSoftActionPrefix(String id) {
        return SOFT_ACTIONS_PREFIXES.stream()
                .filter(softActionPrefix -> id.contains(softActionPrefix))
                .findFirst()
                .orElse(null);
    }

    public static void updateXMLIDAttributesInElementContent(Node node, String newValuePrefix, boolean replacePrefix) {
        String id = getId(node);
        String newId = id;
        String softActionPrefix = getSoftActionPrefix(id);
        if (!INLINE_ELEMENTS.contains(node.getNodeName())) {
            if (replacePrefix && softActionPrefix != null) {
                newId = id.replace(softActionPrefix, newValuePrefix);
            } else if (newValuePrefix != null && !newValuePrefix.equals(softActionPrefix)) {
                newId = newValuePrefix + id;
            }
        }
        addAttribute(node, XMLID, newId);
        List<Node> children = getChildren(node);
        for (int i = 0; i < children.size(); i++) {
            updateXMLIDAttributesInElementContent(children.get(i), newValuePrefix, replacePrefix);
        }
    }

    public static void insertOrUpdateAttributeValue(Node node, String attrName, boolean attrValue) {
        Validate.notNull(attrName, "Attribute name should not be null");
        addAttribute(node, attrName, attrValue + "");
    }

    public static void insertOrUpdateAttributeValue(Node node, String attrName, String attrValue) {
        String currentAttrValue = getAttributeValue(node, attrName);
        if (currentAttrValue != null) {
            addAttribute(node, attrName, currentAttrValue + " " + attrValue);
        } else {
            addAttribute(node, attrName, attrValue);
        }
    }

    public static String updateElementAttribute(String content, String attrName, String attrValue) {
        if (content.isEmpty()) {
            return content;
        }
        byte[] elementContent = content.getBytes(Charset.forName("UTF-8"));
//        if (attrValue != null && attrName != null) {
//            VTDNav fragmentNavigator = buildXMLNavigator(content);
//            int oldClassValIndex = fragmentNavigator.getAttrVal(attrName);
//            XMLModifier fragmentModifier = new XMLModifier(fragmentNavigator);
//            if (oldClassValIndex >= 0 && !fragmentNavigator.toRawString(oldClassValIndex).isEmpty()) {
//                fragmentModifier.updateToken(oldClassValIndex, attrValue.concat(" ").concat(fragmentNavigator.toRawString(oldClassValIndex)));
//            } else {
//                fragmentModifier.insertAttribute(" ".concat(attrName).concat("=\"").concat(attrValue).concat("\""));
//            }
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            fragmentModifier.output(os);
//            elementContent = os.toByteArray();
//            os.close();
//        }
        return new String(elementContent, Charset.forName("UTF-8"));
    }

    public static boolean toBeSkippedForNumbering(Node node) {
        String elementActionType = getAttributeValue(node, LEOS_SOFT_ACTION_ATTR);
        return elementActionType != null &&
                (elementActionType.equals(SoftActionType.MOVE_TO.getSoftAction()) ||
                        elementActionType.equals(SoftActionType.DELETE.getSoftAction()));
    }

    public static boolean hasChildTextNode(Node node) {
        return evalXPathJaxen(node, "text()[normalize-space()][string-length() > 0]");
    }

    /**
     * If the Node is already in the Tree, appendChild() moves it from source to target. That's why we are using '0' index instead of 'i'
     *
     * @param source Node from where we want to copy the list of children
     * @param target Node to which we want to bring the list of children
     * @return target node
     */
    public static Node copyContent(Node source, Node target) {
        NodeList nodeList = source.getChildNodes();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            Node node = nodeList.item(0); //always the first child
            target.appendChild(node);
        }
        return target;
    }

}