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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static integration.saveToc.TocVOCreateLegalServiceUtils.createMoveFromElement;
import static integration.saveToc.TocVOCreateLegalServiceUtils.createMoveToElement;
import static integration.saveToc.TocVOCreateUtils.getElementById;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SaveTocBillMoveProposalTest_IT extends SaveTocBillProposalTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillMoveProposalTest_IT.class);

    @Test
    public void test_from_part_title_chapter_section_article__move_section() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_move_from_part_title_chapter_section_article__to_section.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_move_from_part_title_chapter_section_article__to_section_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);
        assertTrue(toc.size() > 0);
        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO section = getElementById(toc, "section");
        section.getParentItem().removeChildItem(section);
        body.addChildItem(2, section);

        // When
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Ignore
    @Test
    public void test_clone_proposal_move__article_top_outsideChapter() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "bill_with2Article.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_CN, "test_clone_proposal_move__article_top__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        System.out.println(toc);

        TableOfContentItemVO body = getElementById(toc, "body");
        TableOfContentItemVO chapter = getElementById(toc, "chapter_1");

        TableOfContentItemVO originalArticle = getElementById(toc, "art_2");
        TableOfContentItemVO moveToArticle = createMoveToElement(originalArticle);
        TableOfContentItemVO moveFromArticle = createMoveFromElement(originalArticle);

        chapter.getChildItems().remove(originalArticle);
        chapter.getChildItems().add(moveToArticle);
        body.getChildItems().add(0, moveFromArticle);

        when(cloneContext.isClonedProposal()).thenReturn(true);
        // Do the actual call
        byte[] xmlResult = processSaveTocBill(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

}