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
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BODY;
import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJohnTestUser;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class XmlContentProcessorProposal_createDocumentWithNewTocTest extends XmlContentProcessorTest {

    @InjectMocks
    private XmlTableOfContentHelper xmlTableOfContentHelper = Mockito.spy(new XmlTableOfContentHelperImpl());
    @InjectMocks
    private XmlContentProcessorImpl xmlContentProcessor = new XmlContentProcessorProposal();

    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-EC.xml";
    }

    @Test
    public void test_createDocumentContentWithNewTocList() {
        byte[] xmlDocument = TestUtils.getFileContent(FILE_PREFIX + "/test_createDocumentContentWithNewTocList.xml");
        List<TableOfContentItemVO> tocList = xmlTableOfContentHelper.buildTableOfContent(BILL, xmlDocument, TocMode.NOT_SIMPLIFIED);

        byte[] xmlResult = xmlContentProcessor.createDocumentContentWithNewTocList(tocList, xmlDocument, getJohnTestUser());

        byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX + "/test_createDocumentContentWithNewTocList_expected.xml");
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_oldContainedutf8() {
        String xml = "<akomaNtoso><bill><body>" + "<article xml:id=\"art486\">" +
                "<num>Article 486 <placeholder>[…]</placeholder></num>" +
                "<heading><content><p>1ste article</p></content></heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</body></bill></akomaNtoso>";
        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        List<TableOfContentItemVO> articleVOs = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486 <placeholder>[…]</placeholder>",
                null, "<content><p>1ste article</p></content>", null, null, null, 4, null);

        articleVOs.add(art1);

        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        bodyVO.addAllChildItems(articleVOs);
        tableOfContentItemVOList.add(bodyVO);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(UTF_8), getJohnTestUser());

        assertThat(new String(result, UTF_8), is(xml));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_oldContainedEscapedXML() {
        String xml = "<akomaNtoso><bill><body>" + "<article xml:id=\"art486\">" +
                "<num>Article 486 <placeholder>[…]</placeholder></num>" +
                "<heading><content><p>1ste article</p></content></heading>" +
                "<alinea xml:id=\"art486-aln1\">bla amounts and L &lt; K bla</alinea>" +
                "</article>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        List<TableOfContentItemVO> articleVOs = new ArrayList<TableOfContentItemVO>();

        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486 <placeholder>[…]</placeholder>", null,
                "<content><p>1ste article</p></content>", null, null, null, 4, null);

        articleVOs.add(art1);

        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        bodyVO.addAllChildItems(articleVOs);
        tableOfContentItemVOList.add(bodyVO);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(UTF_8), getJohnTestUser());

        assertThat(new String(result, UTF_8), is(xml));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_2newAreAddedAtSameOffset() {
        String xml = "<akomaNtoso><bill><body>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading><content><p>1ste article</p></content></heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" +
                "<article xml:id=\"art489\">" +
                "<num>Article 489</num>" +
                "<heading>4th article</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla 4</alinea>" +
                "</article>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        List<TableOfContentItemVO> articleVOs = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", null, null,
                null, 4, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, null, null, "487 added", null, "2de article added", null, null,
                null, null, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, null, null, "488 added", null, "3th article added", null, null,
                null, null, null);
        TableOfContentItemVO art4 = new TableOfContentItemVO(tocItemArticle, "art489", null, " 489", null, "4th article", null, null,
                null, 17, null);

        articleVOs.add(art1);
        articleVOs.add(art2);
        articleVOs.add(art3);
        articleVOs.add(art4);

        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        bodyVO.addAllChildItems(articleVOs);
        tableOfContentItemVOList.add(bodyVO);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill><body>" + "<article xml:id=\"art486\">" + "<num>Article 486</num>" + "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" + "</article>" +
                "<article xml:id=\".+\"><num leos:editable=\"false\">Article 487 added</num>" +
                "<heading>2de article added</heading><paragraph xml:id=\".+-par1\"><num>1.</num>" + "<content><p>Text...</p></content></paragraph></article>" +
                "<article xml:id=\".+\"><num leos:editable=\"false\">Article 488 added</num>" +
                "<heading>3th article added</heading><paragraph xml:id=\".+-par1\"><num>1.</num>" + "<content><p>Text...</p></content></paragraph></article>" +
                "<article xml:id=\"art489\">" + "<num>Article 489</num>" + "<heading>4th article</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla 4</alinea>" + "</article>" + "</body></bill></akomaNtoso>";
        Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher(new String(result));

        assertThat(expected + " should be found in " + new String(result), matcher.find(), is(true));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_articlesAreRemoved() {
        String xml = "<akomaNtoso><bill><preface id =\"1\"><p>preface</p></preface>" + "<body><article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art488\">" +
                "<num>Article 488</num>" +
                "<heading>3th article</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla</alinea>" +
                "</article>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        List<TableOfContentItemVO> articleVOs = new ArrayList<TableOfContentItemVO>();

        TableOfContentItemVO pref = new TableOfContentItemVO(tocItemPreface, "1", null, null, null, null, null, null, null, 3, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article became 1the", null,
                null, null, 20, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, "art488", null, "Article 488", null, "3th article became 2the", null,
                null, null, 31, null);
        tableOfContentItemVOList.add(pref);
        articleVOs.add(art2);
        articleVOs.add(art3);

        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 8, null);
        bodyVO.addAllChildItems(articleVOs);
        tableOfContentItemVOList.add(bodyVO);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill><preface id =\"1\"><p>preface</p></preface>" + "<body><article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article became 1the</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art488\">" +
                "<num>Article 488</num>" +
                "<heading>3th article became 2the</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla</alinea>" +
                "</article>" + "</body></bill></akomaNtoso>";
        assertThat(new String(result), is(expected));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_numAndHeadingAreAdded() {
        String xml = "<akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" + "</section>" + "</body></bill></akomaNtoso>";
        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        List<TableOfContentItemVO> articleVOs = new ArrayList<TableOfContentItemVO>();

        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, "Section 1", null, "Paragraphs", null, null, null, 4, null);

        articleVOs.add(sec1);

        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        bodyVO.addAllChildItems(articleVOs);
        tableOfContentItemVOList.add(bodyVO);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" + "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "</section>" +
                "</body></bill></akomaNtoso>";
        assertThat(new String(result), is(expected));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_articlesMovedFromSection() {
        String xml = "<akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" + "<num>Section 1</num>" + "<heading >Paragraphs</heading>" +
                "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<section xml:id=\"sect2\">" + "<num>Section 2</num>" + "<heading >Paragraphs</heading>" +
                "<article xml:id=\"art488\">" +
                "<num>Article 488</num>" +
                "<heading>3th article</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, "Section 1", null, "Paragraphs", null, null, null, 4, null);
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", null, null,
                null, 11, null);
        TableOfContentItemVO sec2 = new TableOfContentItemVO(tocItemSection, "sect2", null, "Section 2", null, "Paragraphs", null, null, null, 33, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article", null, null,
                null, 22, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, "art488", null, "Article 488", null, "3th article", null, null,
                null, 40, null);

        bodyVO.addChildItem(sec1);
        sec1.addChildItem(art1);
        bodyVO.addChildItem(sec2);
        sec2.addChildItem(art2);
        sec2.addChildItem(art3);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" + "<num>Section 1</num>" + "<heading>Paragraphs</heading>" +
                "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<section xml:id=\"sect2\">" + "<num>Section 2</num>" + "<heading>Paragraphs</heading>" +
                "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art488\">" +
                "<num>Article 488</num>" +
                "<heading>3th article</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";
        assertThat(new String(result), is(expected));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_allArticlesRemovedFromSection() {
        String xml = "<akomaNtoso><bill>" + "<body><section xml:id=\"sect1\">" + "<num>Section 1</num>" + "<heading>Paragraphs</heading>" +
                "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art487\">" +
                "<num class=\"ArticleNumber\">Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<section xml:id=\"sect2\">" + "<num>Section 2</num>" + "<heading>Paragraphs</heading>" +
                "<article xml:id=\"art488\">" +
                "<num class=\"ArticleNumber\">Article 488</num>" +
                "<heading>3th article</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, "Section 1", null, "Paragraphs", null, null, null, 4, null);
        TableOfContentItemVO sec2 = new TableOfContentItemVO(tocItemSection, "sect2", null, "Section 2", null, "Paragraphs", null, null, null, 35, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article",
                null, null, null, 22, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, "art488", null, "Article 488", null, "3th article",
                null, null, null, 42, null);

        bodyVO.addChildItem(sec1);
        bodyVO.addChildItem(sec2);
        sec2.addChildItem(art2);
        sec2.addChildItem(art3);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" + "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "</section>" +
                "<section xml:id=\"sect2\">" + "<num>Section 2</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art488\">" +
                "<num>Article 488</num>" +
                "<heading>3th article</heading>" +
                "<alinea xml:id=\"art488-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";
        assertThat(new String(result), is(expected));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_sectionAdded() {
        String xml = "<!--This AkomaNtoso document was created via a LegisWrite export.--><akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" +
                "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 4, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, "Section 1", null, "Paragraphs", null, null, null, 5, null);
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", null, null,
                null, 12, null);
        TableOfContentItemVO sec2 = new TableOfContentItemVO(tocItemSection, null, null, "Section 2", null, null, null, null, null, null, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article",
                null, null, null, 23, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, null, null, "488", null, "3th article", null, null, null, null, null);

        bodyVO.addChildItem(sec1);
        sec1.addChildItem(art1);
        bodyVO.addChildItem(sec2);
        sec2.addChildItem(art2);
        sec2.addChildItem(art3);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<!--This AkomaNtoso document was created via a LegisWrite export.--><akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" +
                "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art486\">" + "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<section xml:id=\".+\">" + "<num>Section 2</num>" + "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" +
                "<article xml:id=\".+\"><num leos:editable=\"false\">Article 488</num>" +
                "<heading>3th article</heading><paragraph xml:id=\".+-par1\"><num>1.</num>" +
                "<content><p>Text...</p></content></paragraph></article>" + "</section>" + "</body></bill></akomaNtoso>";

        Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher(new String(result));

        assertThat(expected + " should be found in " + new String(result), matcher.find(), is(true));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_sectionAddedWithHeaderAndNumberTagsPreserved() {
        String xml = "<!--This AkomaNtoso document was created via a LegisWrite export.--><akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" +
                "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art486\">" +
                "<num class=\"numClass\">Article 486</num>" +
                "<heading class=\"hdgClass\">1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art487\">" +
                "<num class=\"ArticleNumber\">Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 4, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, "Section 1", null, "Paragraphs", 8, 10, null, 5, null);
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", 15, 19,
                null, 12, null);
        TableOfContentItemVO sec2 = new TableOfContentItemVO(tocItemSection, null, null, "Section 2", null, null, null, null, null, null, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article",
                30, 34, null, 27, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, null, null, "488", null, "3th article", null, null, null, null, null);

        bodyVO.addChildItem(sec1);
        sec1.addChildItem(art1);
        bodyVO.addChildItem(sec2);
        sec2.addChildItem(art2);
        sec2.addChildItem(art3);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<!--This AkomaNtoso document was created via a LegisWrite export.--><akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" +
                "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art486\">" +
                "<num class=\"numClass\">Article 486</num>" +
                "<heading class=\"hdgClass\">1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<section xml:id=\".+\">" + "<num>Section 2</num>" + "<article xml:id=\"art487\">" +
                "<num class=\"ArticleNumber\">Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\".+\"><num leos:editable=\"false\">Article 488</num>" +
                "<heading>3th article</heading><paragraph xml:id=\".+-par1\"><num>1.</num>" +
                "<content><p>Text...</p></content></paragraph></article>" + "</section>" + "</body></bill></akomaNtoso>";
        Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher(new String(result));

        assertThat(expected + " should be found in " + new String(result), matcher.find(), is(true));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_articleAddedAndHcontainerAtTheEnd() {
        String xml = "<!--This AkomaNtoso document was created via a LegisWrite export.-->" +
                "<akomaNtoso xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\" xmlns:leos=\"urn:eu:europa:ec:leos\">" +
                "<bill><body>" + "<article xml:id=\"art486\"> <num leos:editable=\"false\">Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<hcontainer><content><p>test</p></content>" + "</hcontainer>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 8, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO artNew = new TableOfContentItemVO(tocItemArticle, null, null, "485", null, "0ste article", null, null, null, null, null);
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", null,
                null, null, 9, null);

        bodyVO.addChildItem(artNew);
        bodyVO.addChildItem(art1);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<!--This AkomaNtoso document was created via a LegisWrite export.--><akomaNtoso xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\" xmlns:leos=\"urn:eu:europa:ec:leos\"><bill><body>"

                + "<article xml:id=\".+\"><num leos:editable=\"false\">Article 485</num>" +
                "<heading>0ste article</heading><paragraph xml:id=\".+-par1\"><num>1.</num>" +
                "<content><p>Text...</p></content></paragraph></article>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<hcontainer><content><p>test</p></content>" + "</hcontainer>" + "</body></bill></akomaNtoso>";
        Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher(new String(result));

        assertThat(expected + " should be found in " + new String(result), matcher.find(), is(true));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_sectionMoved() {
        String xml = "<!--This AkomaNtoso document was created via a LegisWrite export.--><akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" +
                "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<section xml:id=\"Section 2\">" + "<num>Section 2</num>" + "<article xml:id=\"art487\">" +
                "<num class=\"ArticleNumber\">Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 4, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, "Section 1", null, "Paragraphs", null, null, null, 5, null);
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", null, null,
                null, 12, null);
        TableOfContentItemVO sec2 = new TableOfContentItemVO(tocItemSection, "Section 2", null, "Section 2", null, null, null, null, null, 23, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article",
                null, null, null, 28, null);

        bodyVO.addChildItem(sec1);
        sec1.addChildItem(sec2);
        sec2.addChildItem(art2);
        sec1.addChildItem(art1);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<!--This AkomaNtoso document was created via a LegisWrite export.--><akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" +
                "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<section xml:id=\"Section 2\">" + "<num>Section 2</num>" +
                "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</section>"

                + "</body></bill></akomaNtoso>";
        assertThat(new String(result), is(expected));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_sectionHasNoNumOrHeading() {
        String xml = "<akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, null, null, null, null, null, null, 4, null);
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", null, null,
                null, 7, null);
        TableOfContentItemVO sec2 = new TableOfContentItemVO(tocItemSection, null, null, "Section 2", null, "Paragraphs", null, null, null, null, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article", null, null,
                null, 18, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, null, null, "488", null, "3th article", null, null, null, null, null);

        bodyVO.addChildItem(sec1);
        sec1.addChildItem(art1);
        bodyVO.addChildItem(sec2);
        sec2.addChildItem(art2);
        sec2.addChildItem(art3);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill><body>" + "<section xml:id=\"sect1\">" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "<section xml:id=\".+\">" + "<num>Section 2</num>" + "<heading>Paragraphs</heading>" +
                "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\".+\"><num leos:editable=\"false\">Article 488</num>" +
                "<heading>3th article</heading><paragraph xml:id=\".+-par1\"><num>1.</num>" +
                "<content><p>Text...</p></content></paragraph></article>" + "</section>" + "</body></bill></akomaNtoso>";
        Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher(new String(result));

        assertThat(expected + " should be found in " + new String(result), matcher.find(), is(true));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_sectionAddedin3levelBill() {
        String xml = "<akomaNtoso><bill><body>" + "<part xml:id=\"part1\">" + "<num>Part 1</num>" + "<heading>part1</heading>" + "<section xml:id=\"sect1\">" +
                "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\"art487\">" +
                "<num class=\"ArticleNumber\">Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</part>" + "</body></bill></akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO bodyVO = new TableOfContentItemVO(tocItemBody, BODY, null, null, null, null, null, null, null, 3, null);
        tableOfContentItemVOList.add(bodyVO);

        TableOfContentItemVO part1 = new TableOfContentItemVO(tocItemPart, "part1", null, "Part 1", null, "part1", null, null, null, 4, null);
        TableOfContentItemVO sec1 = new TableOfContentItemVO(tocItemSection, "sect1", null, "Section 1", null, "Paragraphs", null, null, null, 11, null);
        TableOfContentItemVO art1 = new TableOfContentItemVO(tocItemArticle, "art486", null, "Article 486", null, "1ste article", null, null,
                null, 18, null);
        TableOfContentItemVO part2 = new TableOfContentItemVO(tocItemPart, null, null, "Part 2", null, "part2", null, null, null, null, null);
        TableOfContentItemVO sec2 = new TableOfContentItemVO(tocItemSection, null, null, "Section 2", null, "Paragraphs", null, null, null, null, null);
        TableOfContentItemVO art2 = new TableOfContentItemVO(tocItemArticle, "art487", null, "Article 487", null, "2de article",
                null, null, null, 29, null);
        TableOfContentItemVO art3 = new TableOfContentItemVO(tocItemArticle, null, null, "488", null, "3th article", null, null, null, null, null);

        bodyVO.addChildItem(part1);
        part1.addChildItem(sec1);
        sec1.addChildItem(art1);
        bodyVO.addChildItem(part2);
        part2.addChildItem(sec2);
        sec2.addChildItem(art2);
        sec2.addChildItem(art3);

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill><body>" + "<part xml:id=\"part1\">" + "<num>Part 1</num>" + "<heading>part1</heading>" +
                "<section xml:id=\"sect1\">" + "<num>Section 1</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "</section>" + "</part>" + "<part xml:id=\".+\">" + "<num>Part 2</num>" + "<heading>part2</heading>" +
                "<section xml:id=\".+\">" + "<num>Section 2</num>" + "<heading>Paragraphs</heading>" + "<article xml:id=\"art487\">" +
                "<num>Article 487</num>" +
                "<heading>2de article</heading>" +
                "<alinea xml:id=\"art487-aln1\">bla bla</alinea>" +
                "</article>" + "<article xml:id=\".+\"><num leos:editable=\"false\">Article 488</num>" +
                "<heading>3th article</heading><paragraph xml:id=\".+-par1\"><num>1.</num>" +
                "<content><p>Text...</p></content></paragraph></article>" + "</section>" + "</part>" + "</body></bill></akomaNtoso>";
        Pattern pattern = Pattern.compile(expected);
        Matcher matcher = pattern.matcher(new String(result));

        assertThat(expected + " should be found in " + new String(result), matcher.find(), is(true));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_when_chapterHasNoChildrenAndBodyHasHcontainer() {
        String xml = "<akomaNtoso><bill>" +
                "<body id =\"body1\">" +
                "<part xml:id=\"part1\">" +
                "<num>Part I</num>" +
                "<heading>LEOS (Proof-Of-Concept)</heading>" +
                "<title xml:id=\"titl1\">" +
                "<num>Title I</num>" +
                "<heading>Example Document</heading>" +
                "<chapter xml:id=\"chap1\">" +
                "</chapter>" +
                "</title>" +
                "</part>" +
                "<hcontainer name=\"application\" xml:id=\"app1\">" +
                "<content xml:id=\"con\">" +
                "<p class=\"DirectApplication\">This Regulation shall be binding in its entirety and directly applicable in all Member States.</p>" +
                "<p class=\"DirectApplication\">(A possible extension of the direct application.)</p>" +
                "</content>" +
                "</hcontainer>" +
                "</body>" +
                "</bill>" +
                "</akomaNtoso>";

        List<TableOfContentItemVO> tableOfContentItemVOList = new ArrayList<TableOfContentItemVO>();
        TableOfContentItemVO body1 = new TableOfContentItemVO(tocItemBody, "body1", null, null, null, null, null, null, null, 3, null);
        TableOfContentItemVO part1 = new TableOfContentItemVO(tocItemPart, "part1", null, "Part I", null, "LEOS (Proof-Of-Concept)", null,
                null, null, 6, null);
        TableOfContentItemVO title1 = new TableOfContentItemVO(tocItemTitle, "titl1", null, "Title I", null, "Example Document", null, null,
                null, 13, null);
        TableOfContentItemVO ch1 = new TableOfContentItemVO(tocItemChapter, "chap1", null, null, null, null, null, null, null, 20, null);

        tableOfContentItemVOList.add(body1);
        body1.addChildItem(part1);
        part1.addChildItem(title1);
        title1.addChildItem(ch1);
        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(tableOfContentItemVOList, xml.getBytes(), getJohnTestUser());

        String expected = "<akomaNtoso><bill>" +
                "<body id =\"body1\">" +
                "<part xml:id=\"part1\">" +
                "<num>Part I</num>" +
                "<heading>LEOS (Proof-Of-Concept)</heading>" +
                "<title xml:id=\"titl1\">" +
                "<num>Title I</num>" +
                "<heading>Example Document</heading>" +
                "<chapter xml:id=\"chap1\">" +
                "</chapter>" +
                "</title>" +
                "</part>" +
                "<hcontainer name=\"application\" xml:id=\"app1\">" +
                "<content xml:id=\"con\">" +
                "<p class=\"DirectApplication\">This Regulation shall be binding in its entirety and directly applicable in all Member States.</p>" +
                "<p class=\"DirectApplication\">(A possible extension of the direct application.)</p>" +
                "</content>" +
                "</hcontainer>" +
                "</body>" +
                "</bill>" +
                "</akomaNtoso>";

        assertThat(new String(result), is(expected));
    }

    @Ignore
    @Test
    public void test_mergeTableOfContentIntoDocument_should_returnUpdatedByteArray_when_NoBillFound() {
        String xml = "<!--This AkomaNtoso document was created via a LegisWrite export.-->" + "<akomaNtoso>" +
                "<blabla>" + "<article xml:id=\"art486\">" +
                "<num>Article 486</num>" +
                "<heading>1ste article</heading>" +
                "<alinea xml:id=\"art486-aln1\">bla bla</alinea>" +
                "</article>" + "<hcontainer><content><p>test</p></content>" + "</hcontainer>" + "</blabla></akomaNtoso>";

        byte[] result = xmlContentProcessor.createDocumentContentWithNewTocList(Collections.<TableOfContentItemVO>emptyList(), xml.getBytes(),
                getJohnTestUser());

        assertThat(new String(result), is(xml));
    }

}
