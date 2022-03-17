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
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CONCLUSIONS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.FORMULA;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITAL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class XmlTOCHelperBillMandateTest extends XmlTableOfContentHelperTest {

    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-CN.xml";
    }

    @Ignore // to be implemented the Mandate logic with more children
    @Test
    public void test_buildTableOfContent() {
        byte[] fileContent = TestUtils.getFileContent(FILE_PREFIX + "/bill_basic.xml");

        List<TableOfContentItemVO> TOC = tableOfContentHelper.buildTableOfContent(BILL, fileContent, TocMode.NOT_SIMPLIFIED);
        assertThat(TOC, is(notNullValue()));
        assertThat(TOC.size(), is(4));

        List<TableOfContentItemVO> expectedTOC = buildTOCProgrammatically();

        compareTOCs(expectedTOC, TOC, false);
    }

    // Mandate logic with more children
    private List<TableOfContentItemVO> buildTOCProgrammatically() {
        TableOfContentItemVO preface = buildSingleTOCVo("preface", PREFACE, null, null, null, null, "");

        TableOfContentItemVO preamble = TocItemVOBuilder.getBuilder()
                .withId("preamble")
                .withTocItem(
                        TocItemUtils.getTocItemByName(tocItems, "preamble")
                )
                .withContent("")
                .withChild(
                        buildSingleTOCVo("preamble__formula_1", FORMULA, null, null, null, null,
                                "THE EUROPEAN PARLIAMENT AND THE COUNCIL OF THE EUROPEAN UNION,")
                )
                .withChild(TocItemVOBuilder.getBuilder()
                        .withId("cits")
                        .withTocItem(
                                TocItemUtils.getTocItemByName(tocItems, "citations")
                        )
                        .withChild(
                                buildSingleTOCVo("cit_1", CITATION, null, null, null, null, "Citation 1 content with newLine")
                        )
                        .withChild(
                                buildSingleTOCVo("cit_2", CITATION, null, null, null, null, "Citation 2 content with AuthorialNote Inside Authorial note ,")
                        )
                        .withContent("")
                        .withVtdIndex(null)
                        .withNumTagIndex(null)
                        .withHeadingTagIndex(null)
                        .withIntroTagIndex(null)
                        .withListTagIndex(null)
                        .withItemDepth(0)
                        .build()
                )
                .withChild(TocItemVOBuilder.getBuilder()
                        .withId("recs")
                        .withTocItem(
                                TocItemUtils.getTocItemByName(tocItems, "recitals")
                        )
                        .withChild(
                                buildSingleTOCVo("rec_1", RECITAL, null, "(1)", "rec_1_num", null, "Recital 1 content")
                        )
                        .withChild(
                                buildSingleTOCVo("rec_2", RECITAL, null, "(2)", "rec_2_num", null, "Recital 2 content with special character Larosière")
                        )
                        .withContent("")
                        .withVtdIndex(null)
                        .withNumTagIndex(null)
                        .withHeadingTagIndex(null)
                        .withIntroTagIndex(null)
                        .withListTagIndex(null)
                        .withItemDepth(0)
                        .build()
                )
                .withChild(
                        buildSingleTOCVo("preamble__formula_2", FORMULA, null, null, null, null, "HAVE ADOPTED THIS REGULATION:")
                )
                .withVtdIndex(null)
                .withNumTagIndex(null)
                .withHeadingTagIndex(null)
                .withIntroTagIndex(null)
                .withListTagIndex(null)
                .withParentItem(null)
                .withItemDepth(0)
                .build();

        TableOfContentItemVO body = TocItemVOBuilder.getBuilder()
                .withId("body")
                .withTocItem(
                        TocItemUtils.getTocItemByName(tocItems, "body")
                )
                .withContent("")
                .withChild(TocItemVOBuilder.getBuilder()
                        .withId("akn_part_htJBP6")
                        .withTocItem(
                                TocItemUtils.getTocItemByName(tocItems, "part")
                        )
                        .withHeading("100 [...] Articles(bold Article) text in header")
                        .withContent("Article 1 Article 1 Heading 1. Article 1 paragraph 1 content 2. Article 1 paragraph 2 content")
                        .withChild(
                                buildSingleTOCVo("art_1", ARTICLE, "Article 1 Heading", "1", "art_1_num", CN, "1. Article 1 paragraph 1 content")
                        )
                        .withChild(
                                buildSingleTOCVo("art_2", ARTICLE, "Article 2 Heading", "2", "art_2_num", EC,
                                        "1. Sub paragraph -- of Paragraph 1 Article 2 (a) point (a) (b) point (b) (c) point (c) content. This is alinea (i) point (i) content (ii) point (i) content (1) point (1) content. This is alinea (2) point (2) content - point - (first indent) content. this is alinea point - (first indent) content. this is alinea (3) point (3) content (iii) point (iii) content")
                        )
                        .withChild(
                                buildSingleTOCVo("art_3", ARTICLE, "Article 3 Heading", "3", "art_3_num", null, "Article 3 first paragraph (unnumbered) content")
                        )
                        .withNumber("I")
                        .withVtdIndex(null)
                        .withNumTagIndex(null)
                        .withHeadingTagIndex(null)
                        .withIntroTagIndex(null)
                        .withListTagIndex(null)
                        .withItemDepth(0)
                        .withElementNumberId("akn_BE55oX")
                        .build()
                )
                .withVtdIndex(null)
                .withNumTagIndex(null)
                .withHeadingTagIndex(null)
                .withIntroTagIndex(null)
                .withListTagIndex(null)
                .withParentItem(null)
                .withItemDepth(0)
                .build();

        TableOfContentItemVO conclusions = buildSingleTOCVo("conclusions", CONCLUSIONS, null, null, null, null,
                "Done at Brussels, For the European Parliament The President [...] For the Council The President [...]");

        return Arrays.asList(preface, preamble, body, conclusions);
    }

    private TableOfContentItemVO buildSingleTOCVo(String id, String aknTag, String heading, String number, String numberId, String origin, String content) {
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
                .withVtdIndex(null)
                .withNumTagIndex(null)
                .withHeadingTagIndex(null)
                .withIntroTagIndex(null)
                .withListTagIndex(null)
                .withItemDepth(0)
                .build();
    }
}
