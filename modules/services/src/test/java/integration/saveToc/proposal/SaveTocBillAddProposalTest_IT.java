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
package integration.saveToc.proposal;

import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import integration.saveToc.TocVOCreateProposalUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static integration.saveToc.TocVOCreateUtils.getElementById;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SaveTocBillAddProposalTest_IT extends SaveTocBillProposalTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillAddProposalTest_IT.class);

    @Test
    public void test_buildTocFromXML() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "bill_with1Article_ec.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);
        TableOfContentItemVO article = getElementById(toc, "art_1");
        assertNotNull(article);
    }

    @Test
    public void test_add__article() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "bill_with1Article_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_add__article__expected_ec.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);
        assertTrue(toc.size() > 0);
        TableOfContentItemVO newArticle = TocVOCreateProposalUtils.createArticle("new_art_1", 0);
        TableOfContentItemVO body = getElementById(toc, "body");
        body.addChildItem(newArticle);

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void test_add__3Articles() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "bill_with1Article_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_add__3articles__expected_ec.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        body.addChildItem(0, TocVOCreateProposalUtils.createArticle("new_art_1", 0));
        body.addChildItem(0, TocVOCreateProposalUtils.createArticle("new_art_2", 0));
        body.addChildItem(TocVOCreateProposalUtils.createArticle("new_art_3", 0));

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void test_add__onExistingPartChapter__section() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with1Part1Chapter1Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_add__onExistingPartChapter__section__expected_ec.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO chapter = getElementById(toc, "chapter_1");
        TableOfContentItemVO newSection = TocVOCreateProposalUtils.createSection("new_section_1", 0);
        TableOfContentItemVO newArticle = TocVOCreateProposalUtils.createArticle("new_art_1", 0);
        newSection.addChildItem(newArticle);
        chapter.addChildItem(newSection);

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void test_update__article() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "bill_with1Article_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_update__article__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO art1 = getElementById(toc, "art_1");
        art1.setNumber("2");
        art1.setHeading("new heading");

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

}
