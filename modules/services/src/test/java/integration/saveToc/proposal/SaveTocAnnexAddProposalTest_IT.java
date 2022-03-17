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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static integration.saveToc.TocVOCreateProposalUtils.createChapter;
import static integration.saveToc.TocVOCreateProposalUtils.createLevel;
import static integration.saveToc.TocVOCreateProposalUtils.createPart;
import static integration.saveToc.TocVOCreateProposalUtils.createSection;
import static integration.saveToc.TocVOCreateProposalUtils.createTitle;
import static integration.saveToc.TocVOCreateUtils.getElementById;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SaveTocAnnexAddProposalTest_IT extends SaveTocAnnexProposalTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocAnnexAddProposalTest_IT.class);

    @Test
    public void test_buildTocFromXML() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX, "annex_with1Level.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentAnnex(xmlInput);
        TableOfContentItemVO article = getElementById(toc, "level_1");
        assertNotNull(article);
    }

    @Test
    public void test_add__level() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX, "annex_with1Level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_EC, "test_add__level__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentAnnex(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "mainBody");
        TableOfContentItemVO level = createLevel("new_level_1", 2);
        body.addChildItem(level);

        // When
        byte[] xmlResult = processSaveTocAnnex(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void test_add__part_title_chapter_section_level() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX, "annex_with1Level.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_EC, "test_add__part_title_chapter_section_level_pointUntilIndent__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentAnnex(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "mainBody");
        TableOfContentItemVO level1 = getElementById(toc, "level_1");
        level1.setHeading("New level heading");

        TableOfContentItemVO newPart = createPart("new_part_1", 0);
        TableOfContentItemVO newTitle = createTitle("new_title_1", 0);
        TableOfContentItemVO newChapter = createChapter("new_chapter_1", 0);
        TableOfContentItemVO newSection = createSection("new_section_1", 0);
        TableOfContentItemVO level = createLevel("new_level_1", 2);

        newPart.addChildItem(newTitle);
        newTitle.addChildItem(newChapter);
        newChapter.addChildItem(newSection);
        newSection.addChildItem(level);
        body.addChildItem(newPart);

        // When
        byte[] xmlResult = processSaveTocAnnex(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void test_update__annexElements() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_EC, "test_add__part_title_chapter_section_level_pointUntilIndent__expected.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_ANNEX_EC, "test_update__annexElements__expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentAnnex(xmlInput);

        TableOfContentItemVO level1 = getElementById(toc, "level_1");
        level1.setHeading(null);

        TableOfContentItemVO part = getElementById(toc, "par_M0cfqI");
        part.setNumber("I");
        part.setHeading(null);

        TableOfContentItemVO section = getElementById(toc, "sec_WEoPGW");
        section.setNumber("Section 1");
        section.setHeading("new heading");

        // When
        byte[] xmlResult = processSaveTocAnnex(xmlInput, toc);

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

}
