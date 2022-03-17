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
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.services.validation.handlers.AkomantosoXsdValidator;
import eu.europa.ec.leos.test.support.LeosTest;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.AKOMANTOSO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CHAPTER;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.removeAllNameSpaces;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeValue;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static eu.europa.ec.leos.util.LeosDomainUtil.calculateLeftPadd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class XmlUtilsTest extends LeosTest {

    @Autowired
    protected AkomantosoXsdValidator akomantosoXsdValidator = new AkomantosoXsdValidator();

    protected final static String FILE_PREFIX = "/xmlUtils";
    private static int countItems = 0;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_PATH", "eu/europa/ec/leos/xsd");
        ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_NAME", "akomantoso30.xsd");
        akomantosoXsdValidator.initXSD();
    }

    public static void fullScanTags(Node node) {
        fullScanTags(node, 0);
    }

    public static Node fullScanTags(Node root, int deep) {
        deep = printChild(root, deep);
        Node current = printSiblings(root, deep--);
        return current;
    }

    private static int printChild(Node root, int deep) {
        NodeList nodeList = root.getChildNodes();
        Node current;
        for (int i = 0; i < nodeList.getLength(); i++) {
            current = nodeList.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                deep++;
                countItems++;
                System.out.println(calculateLeftPadd(deep, "\t") + current.getNodeName());
                current = fullScanTags(current, deep);
                if (current == null) {
                    deep--;
                    break;
                }
            }
        }
        return deep;
    }

    private static Node printSiblings(Node root, int deep) {
        Node current = root.getNextSibling();
        while (current != null) {
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                countItems++;
                System.out.println(calculateLeftPadd(deep, "\t") + current.getNodeName());
                current = fullScanTags(current, deep);
                if (current == null) {
                    break;
                }
            }
            current = current.getNextSibling();
        }
        return current;
    }

    @Ignore
    @Test
    public void test_printFullXml() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        Document document = XmlUtils.createDocument(fileContent);
        fullScanTags(document);
    }

    @Test
    public void test_getNodeAsXmlFragment() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        String expected = new String(fileContent, UTF_8);
        Node node = XmlUtils.createDocument(fileContent);
        String str = XmlUtils.nodeToStringSimple(node);
        str = squeezeXmlAndRemoveAllNS(str);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, str);
    }

    @Test
    public void test_getNodeAsXmlFragment_withMultipleRootNodes() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill_withMultipleRootNodes.xml");
        byte[] fileContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/bill_withMultipleRootNodes_expected.xml");

        Node node = XmlUtils.createDocument(fileContent);
        String nodeAsString = XmlUtils.nodeToStringSimple(node);
        String expected = new String(fileContentExpected, UTF_8);
        nodeAsString = squeezeXmlAndRemoveAllNS(nodeAsString);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, nodeAsString);
    }

    @Test
    public void test_parseDocument_thenSaveWithoutAddingAnything_shouldNotChange() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        Document document = createDocument(fileContent);

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        assertTrue(akomantosoXsdValidator.validate(actualResult));

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult, UTF_8)));
    }

    @Test
    public void test_parseDocument_withSpecialCharacters() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article_withSpecialCharacters.xml");
        Document document = createDocument(fileContent);
        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        String actualResultStr = removeAllNameSpaces(new String(actualResult, UTF_8));

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/article_withSpecialCharactersAfterSave.xml");
        String expectedStr = removeAllNameSpaces(new String(expected, UTF_8));
        assertEquals(squeezeXmlAndRemoveAllNS(expectedStr), squeezeXmlAndRemoveAllNS(actualResultStr));

        // back to initial file
        document = createDocument(expected);

        actualResult = XmlUtils.nodeToByteArray(document);
        actualResultStr = removeAllNameSpaces(new String(actualResult, UTF_8));

        expected = TestUtils.getFileContent(FILE_PREFIX + "/article_withSpecialCharactersAfterSave.xml");
        expectedStr = removeAllNameSpaces(new String(expected, UTF_8));
        assertEquals(squeezeXmlAndRemoveAllNS(expectedStr), squeezeXmlAndRemoveAllNS(actualResultStr));
    }

    @Test
    public void test_parseDocument_withLeosAttributes_whenNamespacePresentInDocument_nsEnabled() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        Document document = createDocument(fileContent);

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));

        // using Node
        Node current = XmlUtils.getElementById(document, "art_1");
        XmlUtils.addLeosNamespace(current);
        actualResult = XmlUtils.nodeToByteArray(document);
        expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_parseDocument_withLeosAttributes_whenNamespacePresentInDocument_nsNotEnabled() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        Document document = createDocument(fileContent, false);

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));

        // using Node
        Node current = XmlUtils.getElementById(document, "art_1", false);
        XmlUtils.addLeosNamespace(current);
        actualResult = XmlUtils.nodeToByteArray(document);
        expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_parseDocument_withLeosAttributes_whenNamespaceNotPresentInDocument_nsEnabled() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes_noNamespace.xml");
        String fileContentAsStr = XmlHelper.addLeosNamespace(new String(fileContent, UTF_8)); // need to add namespace in the xml before processing
        Document document = createDocument(fileContentAsStr.getBytes());

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));

        // using Node
        Node current = XmlUtils.getElementById(document, "art_1");
        actualResult = XmlUtils.nodeToByteArray(current);
        expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_parseDocument_withLeosAttributes_whenNamespaceNotPresentInDocument_nsNotEnabled() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes_noNamespace.xml");
        Document document = createDocument(fileContent, false);
        XmlUtils.addLeosNamespace(document);

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));

        // using Node
        Node current = XmlUtils.getElementById(document, "art_1", false);
        actualResult = XmlUtils.nodeToByteArray(current);
        expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_addXmlFragment_withLeosAttributes_middle() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art485");

        // New Article
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        Node newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);

        // Add Sibling
        newNode = XmlUtils.addSibling(newNode, current, false);
        assertNotNull(newNode);

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_addXmlFragment_withLeosAttributes_middle_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));

        // with Namespace false
        document = createDocument(fileContent, false);
        current = XmlUtils.getElementById(document, "art485", false);
        newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);
        newNode = XmlUtils.addSibling(newNode, current, false);
        assertNotNull(newNode);
        actualResult = XmlUtils.nodeToByteArray(document);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_addXmlFragment_top() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art485");

        // New Article
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/article1.xml");
        Node newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);

        // Add Sibling
        newNode = XmlUtils.addSibling(newNode, current, true);
        assertNotNull(newNode);

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_addXmlFragment_top_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_addXmlFragment_bottom() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art486");

        // New Article
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        Node newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);

        // Add Sibling
        newNode = XmlUtils.addSibling(newNode, current, false);
        assertNotNull(newNode);

        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_addXmlFragment_bottom_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_addChild_last() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art485");

        // New Child
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/paragraph1.xml");
        Node newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);

        // Add Sibling
        newNode = XmlUtils.addChild(newNode, current);
        assertNotNull(newNode);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles_addParagraphAsChild.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(XmlUtils.nodeToString(document)));
    }

    @Test
    public void test_addChild_first() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art485");

        // New Child
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/paragraph1.xml");
        Node newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);

        // Add Sibling
        newNode = XmlUtils.addFirstChild(newNode, current);
        assertNotNull(newNode);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles_addParagraphAsFirstChild.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(XmlUtils.nodeToString(document)));
    }

    @Test
    public void test_deleteArticle() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);

        // Delete Node
        Node deleted = XmlUtils.deleteElementById(document, "art486");
        assertNotNull(deleted);
        byte[] actualResult = XmlUtils.nodeToByteArray(document);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_deleteArticle_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_deleteArticle_byXPath() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);

        // Delete Node
        List<Node> deleted = XmlUtils.deleteElementsByXPath(document, "//*[@xml:id = 'art486']", true);
        assertFalse(deleted.isEmpty());
        byte[] actualResult = XmlUtils.nodeToByteArray(document);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_deleteArticle_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_deleteAllArticles_byXPath() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        boolean namespaceEnabled = true;
        Document document = createDocument(fileContent, namespaceEnabled);

        // Delete Node
        List<Node> deleted = XmlUtils.deleteElementsByXPath(document, "//akn:article", namespaceEnabled);
        assertFalse(deleted.isEmpty());
        byte[] actualResult = XmlUtils.nodeToByteArray(document);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_deleteAllArticles_byXPath.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));

        namespaceEnabled = false;
        document = createDocument(fileContent, namespaceEnabled);
        deleted = XmlUtils.deleteElementsByXPath(document, "//article", namespaceEnabled);
        assertFalse(deleted.isEmpty());
        actualResult = XmlUtils.nodeToByteArray(document);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_replaceArticle() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art486");

        // New Updated Article
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/article1.xml");
        Node newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);

        // Replace Articles
        Node newArticle = XmlUtils.replaceElement(newNode, current);
        assertNotNull(newArticle);
        byte[] actualResult = XmlUtils.nodeToByteArray(document);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceArticle_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_replaceArticle_asXmlFragment() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art486");
        // New Updated Article
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/article1.xml");
        // Replace Articles
        Node newArticle = XmlUtils.replaceElement(current, new String(xmlFragment));
        assertNotNull(newArticle);
        byte[] actualResult = XmlUtils.nodeToByteArray(document);
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceArticle_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_replaceArticle_withLeosAttributes() {
        // Existing Tree
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art486");

        // New Updated Article
        byte[] xmlFragment = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        Node newNode = XmlUtils.createNodeFromXmlFragment(document, xmlFragment);

        // Replace Articles
        Node newArticle = XmlUtils.replaceElement(newNode, current);
        assertNotNull(newArticle);
        byte[] actualResult = XmlUtils.nodeToByteArray(document);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_replaceArticle_withLeosAttributes_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(actualResult)));
    }

    @Test
    public void test_getAttribute() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "cit_2");

        String val = XmlUtils.getAttributeValue(current, XMLID);
        assertEquals("cit_2", val);

        val = XmlUtils.getAttributeValue(current, LEOS_EDITABLE_ATTR);
        assertEquals("true", val);

        Boolean isEditable = XmlUtils.getAttributeValueAsBoolean(current, LEOS_EDITABLE_ATTR);
        assertEquals(true, isEditable);

        SoftActionType softAction = XmlUtils.getAttributeForSoftAction(current, LEOS_SOFT_ACTION_ATTR);
        assertEquals(SoftActionType.MOVE_TO, softAction);

        val = XmlUtils.getAttributeValueForElementId(document, "cit_2", LEOS_EDITABLE_ATTR);
        assertEquals("true", val);
    }

    @Test
    public void test_removeAttribute() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article1.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art_1");

        boolean removed = XmlUtils.removeAttribute(current, XMLID);
        assertTrue(removed);
        String newNode = XmlUtils.nodeToString(current);
        assertFalse(newNode.contains("xml:id=\"art_1\""));

        removed = XmlUtils.removeAttribute(current, "notPresentAttribute");
        assertFalse(removed);
    }

    @Test
    public void test_removeAttribute_leos() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementById(document, "art_1");

        XmlUtils.removeAttribute(current, LEOS_ORIGIN_ATTR);
        XmlUtils.removeAttribute(current, LEOS_EDITABLE_ATTR);
        String newNode = XmlUtils.nodeToString(current);
        assertFalse(newNode.contains(LEOS_ORIGIN_ATTR));
        assertFalse(newNode.contains(LEOS_EDITABLE_ATTR));
    }

    @Test
    public void test_removeAllAttributes() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        byte[] expectedContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1_noIds.xml");

        Document document = createDocument(fileContent);
        Node node = XmlUtils.removeAllAttributes(document, XMLID, true);
        String result = XmlUtils.nodeToString(node);

        String expected = new String(expectedContent, UTF_8);
        expected = squeezeXmlAndRemoveAllNS(expected);
        result = squeezeXmlAndRemoveAllNS(result);
        assertEquals(expected, result);
    }

    @Test
    public void test_evalXPath() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles_addParagraphAsChild.xml");
        Document document = createDocument(fileContent);

        boolean found = XmlUtils.evalXPath(document.getFirstChild(), "//*[@xml:id = 'par1_new']");
        assertTrue(found);

        found = XmlUtils.evalXPath(document.getFirstChild(), "fakePath");
        assertFalse(found);

        document = createDocument(fileContent, false);
        found = XmlUtils.evalXPath(document.getFirstChild(), " //*[@id = 'par1_new']", false);
        assertTrue(found);
    }

    @Test
    public void test_getElementByNameAndId() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getElementByNameAndId(document, ARTICLE, null);
        assertNotNull(current);
        assertEquals("art485", getAttributeValue(current, XMLID));

        current = XmlUtils.getElementByNameAndId(document, ARTICLE, "art486");
        assertNotNull(current);
        assertEquals("art486", getAttributeValue(current, XMLID));

        current = XmlUtils.getElementByNameAndId(document, CHAPTER, null);
        assertNull(current);
    }

    @Test
    public void test_getElementById_noNamespaceInXml() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/article1.xml");
        Document document = createDocument(fileContent);
        String idDocument = XmlUtils.getId(document);
        assertNull(idDocument);

        Node node = document.getFirstChild(); // getId() works only on the node
        String idNode = XmlUtils.getId(node);
        assertNotNull(idNode);
    }

    @Test
    public void test_getElementByName() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles.xml");
        Document document = createDocument(fileContent);
        Node current = XmlUtils.getFirstElementByName(document, ARTICLE);
        assertNotNull(current);

        current = XmlUtils.getFirstElementByName(document, "wrongTagName");
        assertNull(current);
    }

    @Test
    public void test_getElementsByName() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        Document document = createDocument(fileContent);

        NodeList allElements = XmlUtils.getElementsByName(document, AKOMANTOSO);
        assertEquals(1, allElements.getLength());

        allElements = XmlUtils.getElementsByName(document, ARTICLE);
        assertEquals(5, allElements.getLength());
    }

    @Test
    public void test_getChildren() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        Document document = createDocument(fileContent);

        Node partNode = XmlUtils.getElementById(document, "akn_part_htJBP6");
        List<Node> children = XmlUtils.getChildren(partNode, ARTICLE);

        assertEquals(2, children.size());
    }

    @Test
    public void test_getElementByXPath() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles_addParagraphAsChild.xml");
        boolean namespaceAware = false;
        Document document = createDocument(fileContent, namespaceAware);
        String xPath = "//*[@id = 'par1_new']";

        Node current = XmlUtils.getFirstElementByXPath(document.getFirstChild(), xPath, namespaceAware);
        assertNotNull(current);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/paragraph1.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(expected)));

        namespaceAware = true;
        document = createDocument(fileContent, namespaceAware);
        xPath = "//*[@xml:id = 'par1_new']";
        current = XmlUtils.getFirstElementByXPath(document.getFirstChild(), xPath, namespaceAware);
        assertNotNull(current);

        expected = TestUtils.getFileContent(FILE_PREFIX + "/paragraph1.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(new String(expected)));
    }

    @Test
    public void test_getElementByXPath_fromModifiedDom() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        Document document = createDocument(fileContent);
        String xPath = "/akn:akomaNtoso//akn:meta";

        Node current = XmlUtils.getFirstElementByXPath(document, xPath);
        assertNotNull(current);

        Node newNode = XmlUtils.createElementWithAknNS(document, "newNode", "new added node");
        XmlUtils.addLastChild(newNode, current);

        xPath = "/akn:akomaNtoso//akn:meta/akn:newNode";
        current = XmlUtils.getFirstElementByXPath(document, xPath);
        assertNotNull(current);
    }

    @Test
    public void test_getElementByXPath_withNamespace() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/part_2articles_addParagraphAsChild.xml");
        boolean namespaceAware = true;
        Document document = createDocument(fileContent, namespaceAware);
        final String xPath = "//*[@xml:id = 'par1_new']";

        Node current = XmlUtils.getFirstElementByXPath(document.getFirstChild(), xPath, namespaceAware);
        assertNotNull(current);
        String newNode = XmlUtils.nodeToString(current);
        newNode = removeAllNameSpaces(newNode);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/paragraph1.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(expected, UTF_8)), squeezeXmlAndRemoveAllNS(newNode));
    }

    @Test
    public void test_getElementByComplexXPath() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill1.xml");
        boolean namespaceAware = true;
        Document document = createDocument(fileContent, namespaceAware);
        String xPath = "//akn:article[@xml:id = 'art_1']";

        Node current = XmlUtils.getFirstElementByXPath(document, xPath, namespaceAware);
        assertNotNull(current);
        String currentAsString = XmlUtils.nodeToString(current);
        currentAsString = removeAllNameSpaces(currentAsString);

        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/article1_withLeosAttributes.xml");
        String expectedAsString = new String(expected, UTF_8);
        expectedAsString = removeAllNameSpaces(expectedAsString);
        assertEquals(squeezeXmlAndRemoveAllNS(expectedAsString), squeezeXmlAndRemoveAllNS(currentAsString));

        xPath = "//akn:part/akn:article[last()]";
        current = XmlUtils.getFirstElementByXPath(document, xPath, namespaceAware);
        assertNotNull(current);
        String id = XmlUtils.getAttributeValue(current, XMLID);
        assertEquals("art_2", id);

        xPath = "/akn:akomaNtoso/akn:bill/akn:body/akn:part/akn:num";
        current = XmlUtils.getFirstElementByXPath(document, xPath, namespaceAware);
        assertNotNull(current);
        id = XmlUtils.getAttributeValue(current, XMLID);
        assertEquals("akn_BE55oX", id);
    }

    @Test
    public void test_getContentAsXmlFragment() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/paragraph1.xml");
        Document document = createDocument(fileContent);
        Node node = document.getFirstChild();
        long start = System.currentTimeMillis();
        String str = XmlUtils.getContentNodeAsXmlFragment(node);
        long end = System.currentTimeMillis();
        System.out.println("computed in  " + (end - start) + "ms. Output:\n" + str);
    }

    @Test
    public void test_add_htmlXml_content_to_node() {
        byte[] fileContent = "<content></content>".getBytes(UTF_8);
        Document document = createDocument(fileContent);
        Node node = document.getFirstChild();
        String contentAsString = "Article <span class=\"old\">13</span><span class=\"new\">14</span>";

        List<org.jsoup.nodes.Node> externalNodes = new LinkedList<>(Parser.parseXmlFragment(contentAsString, ""));
        long end = System.currentTimeMillis();

        long start = System.currentTimeMillis();
        for (org.jsoup.nodes.Node jsoupNode : externalNodes) {
            if (jsoupNode instanceof TextNode) {
                TextNode jTextNode = (TextNode) jsoupNode;
                node.setTextContent(jTextNode.text());
            } else {
                Element jElement = (Element) jsoupNode;
                Node w3cNode = XmlUtils.createElement(node.getOwnerDocument(), jElement.nodeName(), jElement.text());
                List<Attribute> attrs = jElement.attributes().asList();
                for (Attribute attr : attrs) {
                    XmlUtils.addAttribute(w3cNode, attr.getKey(), attr.getValue());
                }
                node.appendChild(w3cNode);
            }
        }
//        String fakeNodeStr = "<fake>Article <span class=\"old\">13</span><span class=\"new\">14</span></fake>";
//        Node fakeNode = XmlUtils.createNodeFromXmlFragment(node.getOwnerDocument(), fakeNodeStr.getBytes(UTF_8));
//        XmlUtils.copyContent(fakeNode, node);
        System.out.println("computed in  " + (end - start) + "ms. Output:\n" + XmlUtils.nodeToString(node));
    }
}
