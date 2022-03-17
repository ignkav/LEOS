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
import eu.europa.ec.leos.vo.toc.TocItemUtils;
import eu.europa.ec.leos.vo.toc.TocItemVOBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PART;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREFACE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class XmlTOCHelperAnnexProposalTest extends XmlTableOfContentHelperTest {

    @Override
    protected void getStructureFile() {
        docTemplate = "SG-017";
        configFile = "/structure-test-annex-EC.xml";
    }

//    @Test
//    public void test_buildTableOfContent_compareWithVtd() {
//        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/annex_basic.xml");
//
//        List<TableOfContentItemVO> TOC = tableOfContentHelper.buildTableOfContent(DOC, fileContent, TocMode.NOT_SIMPLIFIED);
//        assertThat(TOC, is(notNullValue()));
//        assertThat(TOC.size(), is(2));
//
//        List<TableOfContentItemVO> expectedTOC = xmlTableOfContentHelperVtd.buildTableOfContent(DOC, fileContent, TocMode.NOT_SIMPLIFIED);
////        System.out.println(expectedTOC);
//
//        compareTOCs(expectedTOC, TOC, true);
//    }

    @Test
    public void test_buildTableOfContent() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/annex_basic.xml");

        List<TableOfContentItemVO> TOC = tableOfContentHelper.buildTableOfContent(DOC, fileContent, TocMode.NOT_SIMPLIFIED);
        assertThat(TOC, is(notNullValue()));
        assertThat(TOC.size(), is(2));

        List<TableOfContentItemVO> expectedTOC = buildTOCProgrammatically();

        compareTOCs(expectedTOC, TOC, false);
    }

    private List<TableOfContentItemVO> buildTOCProgrammatically() {
        TableOfContentItemVO preface = buildSingleTOCVo("preface", PREFACE, null, null, null, null, 0, null, "");
        TableOfContentItemVO mainBody = TocItemVOBuilder.getBuilder()
                .withId("mainBody")
                .withContent("")
                .withTocItem(
                        TocItemUtils.getTocItemByName(tocItems, MAIN_BODY)
                )
                .withChild(
                        buildSingleTOCVo("level_1", LEVEL, null, "1.", "level_1_num", null, 1, null, "Without heading - only short content")
                )
                .withChild(
                        TocItemVOBuilder.getBuilder()
                                .withId("level_2")
                                .withTocItem(
                                        TocItemUtils.getTocItemByName(tocItems, LEVEL)
                                )
                                .withHeading("Level with List")
                                .withNumber("2.")
                                .withElementNumberId("level_2_num")
                                .withList("(a) Point (a) (b) Point (b) (c) Point (c) (i) Point (i) (ii) Point (ii) (1) Point (1) (2) Point (2) - first indent - second indent - third indent (4) Point (4)")
                                .withItemDepth(1)
                                .withContent("Level 2 first sub")
                                .build()

                )
                .withChild(
                        TocItemVOBuilder.getBuilder()
                                .withTocItem(
                                        TocItemUtils.getTocItemByName(tocItems, PART)
                                )
                                .withId("part_1")
                                .withHeading("Part heading.")
                                .withNumber("I")
                                .withElementNumberId("part_1_num")
                                .withContent("Paragraph1, sub1 content Paragraph1, sub2 content (a) Point (a) content (i) Point (i) content")
                                .withItemDepth(1)
                                .withChild(
                                        buildSingleTOCVo("par_1", PARAGRAPH, null, null, null, null, 0, "(a) Point (a) content (i) Point (i) content", "Paragraph1, sub1 content Paragraph1, sub2 content (a) Point (a) content (i) Point (i) content")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3", LEVEL, "Heading for 3.", "3.", "level_3_num", null, 1, null, "Content for 3.")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3_1", LEVEL, null, "3.1.", "level_3_1_num", null, 2, null, "Content for 3.1")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3_1_1", LEVEL, null, "3.1.1.", "level_3_1_1_num", null, 3, null, "Content for 3.1.1")
                                )
                                .withChild(
                                        buildSingleTOCVo("level_3_1_2", LEVEL, null, "3.1.2.", "level_3_1_2_num", null, 3, null, "Content for 3.1.2")
                                )

                                .build()
                )
                .build();

        return Arrays.asList(preface, mainBody);
    }

    private TableOfContentItemVO buildSingleTOCVo(String id, String aknTag, String heading, String number, String numberId, String origin, int depth, String list, String content) {
        return TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(
                        TocItemUtils.getTocItemByName(tocItems, aknTag)
                )
                .withContent(content)
                .withHeading(heading)
                .withNumber(number)
                .withElementNumberId(numberId)
                .withOriginAttr(origin)
                .withList(list)
                .withVtdIndex(null)
                .withNumTagIndex(null)
                .withHeadingTagIndex(null)
                .withIntroTagIndex(null)
                .withListTagIndex(null)
                .withItemDepth(depth)
                .build();
    }

}
