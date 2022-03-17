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

import eu.europa.ec.leos.domain.common.ErrorCode;
import eu.europa.ec.leos.domain.common.Result;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ReferenceLabelServiceNumberedTest extends ReferenceLabelServiceTest {
    
    @Test
    public void generateLabelString_singleRefWithArticle() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a1"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Article <ref xml:id=\"\" href=\"bill/a1\">1</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Section_multiRefWithSection() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",p3s1"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Part XIV, Section <ref xml:id=\"\" href=\"bill/p3s1\">X</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Section_multiRefWith2Section() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",p3s2", ",p3s1"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Part XIV, Section <ref xml:id=\"\" href=\"bill/p3s1\">X</ref> and <ref xml:id=\"\" href=\"bill/p3s2\">XI</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Higher_multiRefWith2Articles() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a3", ",a2"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Articles "
                + "<ref xml:id=\"\" href=\"bill/a2\">20</ref>"
                + " and "
                + "<ref xml:id=\"\" href=\"bill/a3\">21</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Higher_multiRefWith2siblingArticles() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a2", ",a1"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Articles "
                + "<ref xml:id=\"\" href=\"bill/a1\">1</ref>"
                + " and "
                + "<ref xml:id=\"\" href=\"bill/a2\">20</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Recitals_multiRefWith3Recitals() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",rec_4", ",rec_1", ",rec_2"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Recitals "
                + "<ref xml:id=\"\" href=\"bill/rec_1\">(1)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/rec_2\">(2)</ref>"
                + " and "
                + "<ref xml:id=\"\" href=\"bill/rec_4\">(4)</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Recitals_multiRefWith2siblingRecitals() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",rec_1", ",rec_3"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Recitals "
                + "<ref xml:id=\"\" href=\"bill/rec_1\">(1)</ref>"
                + " and "
                + "<ref xml:id=\"\" href=\"bill/rec_3\">(3)</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Recitals_multiRefWith1Recitals() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",rec_1"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Recital "
                + "<ref xml:id=\"\" href=\"bill/rec_1\">(1)</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Higher_multiRefWith3Articles() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a3", ",a1", ",a2"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Articles <ref xml:id=\"\" href=\"bill/a1\">1</ref>, "
                + "<ref xml:id=\"\" href=\"bill/a2\">20</ref>"
                + " and "
                + "<ref xml:id=\"\" href=\"bill/a3\">21</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Higher_singleRefWithParts() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",part11"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Part <ref xml:id=\"\" href=\"bill/part11\">XI</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Article_withSingleParagraphReference() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a1p3"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Article 1<ref xml:id=\"\" href=\"bill/a1p3\">(3)</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Article_withTwoParagraphReferenceAtLevel1() throws Exception {
        //part XII > Article 1 > Paragraph 3
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a1p3", ",a1p2"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Article 1<ref xml:id=\"\" href=\"bill/a1p2\">(2)</ref>"
                + " and "
                + "<ref xml:id=\"\" href=\"bill/a1p3\">(3)</ref>";
        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Article_withOnePointReferenceAtLevel2() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",art_1_jMGiAd"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Article 45(3), point <ref xml:id=\"\" href=\"bill/art_1_jMGiAd\">(a)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Article_withMultiplePointReferenceAtLevel2() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",art_1_OrhWbv", ",art_1_Uxo4c1", ",art_1_CY6Nsa"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Article 45(3), point (a)<ref xml:id=\"\" href=\"bill/art_1_OrhWbv\">(i)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/art_1_Uxo4c1\">(ii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/art_1_CY6Nsa\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Article_withMultiplePointReferenceAtLevel2_same_article() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",art_1_OrhWbv", ",art_1_Uxo4c1", ",art_1_CY6Nsa"), "bill", "art_1_oxdTif", document.getContent().get().getSource().getBytes());
        String expectedResults = "paragraph (3), point (a)<ref xml:id=\"\" href=\"bill/art_1_OrhWbv\">(i)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/art_1_Uxo4c1\">(ii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/art_1_CY6Nsa\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Article_withMultiplePointReferenceAtLevel2_same_paragraph() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",art_1_OrhWbv", ",art_1_Uxo4c1", ",art_1_CY6Nsa"), "bill", "art_1_TKV1Yb", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (a)<ref xml:id=\"\" href=\"bill/art_1_OrhWbv\">(i)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/art_1_Uxo4c1\">(ii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/art_1_CY6Nsa\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_Article_withMultiplePointReferenceAtLevel2_same_point() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",art_1_OrhWbv", ",art_1_Uxo4c1", ",art_1_CY6Nsa"), "bill", "art_1_HnxDcU", document.getContent().get().getSource().getBytes());
        String expectedResults = "point <ref xml:id=\"\" href=\"bill/art_1_OrhWbv\">(i)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/art_1_Uxo4c1\">(ii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/art_1_CY6Nsa\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }


    @Test
    public void generateLabelString_Recitals_withMultipleRecitals_same_recitals() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",rec_1", ",rec_2", ",rec_3"), "bill", "rec_2", document.getContent().get().getSource().getBytes());
        String expectedResults = "Recitals <ref xml:id=\"\" href=\"bill/rec_1\">(1)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/rec_2\">(2)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/rec_3\">(3)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    @Ignore
    //This case across levels is not handled
    public void generateLabelString_Article_withMultiplePointReferenceAtLevel2andLevel3() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",art_1_OrhWbv", ",art_1_Uxo4c1", ",art_1_Orvvv", ",art_1_CY6Nsa"), "bill", "", document.getContent().get().getSource().getBytes());
        String expectedResults = "Article 45(3)<ref xml:id=\"\" href=\"bill/art_1_OrhWbv\">(i)</ref>"
                + ", (a)<ref xml:id=\"\" href=\"bill/art_1_Uxo4c1\">(ii)</ref>"
                + ", (a)<ref xml:id=\"\" href=\"bill/art_1_CY6Nsa\">(iii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/art_1_Uxo4c1\">(b)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_broken_refs_same_type() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a1p2", ",a2aal1"), "bill", "", document.getContent().get().getSource().getBytes());
        assertEquals(true, result.isError());
        assertEquals(ErrorCode.DOCUMENT_REFERENCE_NOT_VALID, result.getErrorCode().get());
        assertEquals("", result.get());
    }

    @Test
    public void generateLabelString_broken_refs() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a1p2", ",a2aal"), "bill", "", document.getContent().get().getSource().getBytes());
        assertEquals(true, result.isError());
        assertEquals(ErrorCode.DOCUMENT_REFERENCE_NOT_VALID, result.getErrorCode().get());
        assertEquals("", result.get());
    }

    /** Article 47 used for the numbered paragraph:
     * paragraph 1  (a6_PcpmBy)
     *      subparagraph (a6_9CfL6Y)
     *          (a) point  (a6_uYgixH)
     * 		    (b) point  (a6_fVRJ9F)
     *          (c) point  (a6_yPOvBG)
     * 		    (d) point  (a6_g6qoqW)
     *              sub-point  (a6_MM9gYx)  (target)
     *              (1) point  (a6_qJGPtu)
     * 			    (2) point  (a6_JYhj1s)
     *              (3) point  (a6_HVr06Y)
     *                  sub-point   (a6_6CT2lS)
     *                  (i) point   (a6_sdGkGc)
     * 				    (ii) point  (a6_3eqk4L)
     *                  (iii) point (a6_dhDm9U)
     * 				    (iv) point  (a6_d42YEi)
     *                      sub-point (a6_grl0Ed)
     * 					    - indent  (a6_vFs1j9)  (source)
     * 					    - indent  (a6_ed2l3M)
     * 					    - indent  (a6_H9BTwE)
     * 					(v) point   (a6_GxXx51)
     * 			            sub-point (a6_I5bTCT)
     * 			            - indent  (a6_GVVgiJ)
     * 			            - indent  (a6_9WhjXw)
     * paragraph 2  (a6_hJc9Yk)
     * paragraph 3  (a6_v4ETw1)
     * paragraph 4  (a6_h0qOvb)
     * paragraph 5  (a6_Dd29LR)
     */
    @Test
    public void generateLabelString_sameArticle_sourceParagraph2_targetParagraph2() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_hJc9Yk"), "bill", "a6_hJc9Yk", document.getContent().get().getSource().getBytes());
        String expectedResults ="this paragraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourceParagraph2_targetParagraph234() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_h0qOvb", ",a6_v4ETw1", ",a6_hJc9Yk"), "bill", "a6_hJc9Yk", document.getContent().get().getSource().getBytes());
        String expectedResults ="paragraph <ref xml:id=\"\" href=\"bill/a6_hJc9Yk\">(2)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_v4ETw1\">(3)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_h0qOvb\">(4)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourceParagraph2_targetParagraph345() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_Dd29LR", ",a6_h0qOvb", ",a6_v4ETw1"), "bill", "a6_hJc9Yk", document.getContent().get().getSource().getBytes());
        String expectedResults = "paragraph <ref xml:id=\"\" href=\"bill/a6_v4ETw1\">(3)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_h0qOvb\">(4)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_Dd29LR\">(5)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourcePar1PointA_targetPar1PointA() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_uYgixH"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults ="this point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourcePar1PointDSubPoint_targetPar1DSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_MM9gYx"), "bill", "a6_MM9gYx", document.getContent().get().getSource().getBytes());
        String expectedResults ="this subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourcePar1PointD1_targetPar1PointD1() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_qJGPtu"), "bill", "a6_qJGPtu", document.getContent().get().getSource().getBytes());
        String expectedResults ="this point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourcePar1PointD3SubPoint_targetPar1PointD3SubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_6CT2lS"), "bill", "a6_6CT2lS", document.getContent().get().getSource().getBytes());
        String expectedResults ="this subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourcePar1PointD3I_targetPar1PointD3I() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_sdGkGc"), "bill", "a6_sdGkGc", document.getContent().get().getSource().getBytes());
        String expectedResults ="this point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourcePar1PointD3IVSubPoint_targetPar1PointD3IVSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_grl0Ed"), "bill", "a6_grl0Ed", document.getContent().get().getSource().getBytes());
        String expectedResults ="this subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourcePar1PointD3IVIndent_targetPar1PointD3IVIndent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_vFs1j9"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults ="this indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD_target3siblingsABC() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_yPOvBG", ",a6_fVRJ9F", ",a6_uYgixH"), "bill", "a6_g6qoqW", document.getContent().get().getSource().getBytes());
        String expectedResults = "point <ref xml:id=\"\" href=\"bill/a6_uYgixH\">(a)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_fVRJ9F\">(b)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_yPOvBG\">(c)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD_chose2Points() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_JYhj1s", ",a6_qJGPtu"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)<ref xml:id=\"\" href=\"bill/a6_qJGPtu\">(1)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_JYhj1s\">(2)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD3_chose3Points() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_dhDm9U", ",a6_3eqk4L", ",a6_sdGkGc"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)(3)<ref xml:id=\"\" href=\"bill/a6_sdGkGc\">(i)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_3eqk4L\">(ii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_dhDm9U\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_H9BTwE", ",a6_ed2l3M", ",a6_vFs1j9"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)(3)(iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_vFs1j9\">first</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_ed2l3M\">second</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD2_targetPointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_H9BTwE", ",a6_ed2l3M", ",a6_vFs1j9"), "bill", "a6_JYhj1s", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (3)(iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_vFs1j9\">first</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_ed2l3M\">second</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3III_targetPointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_H9BTwE", ",a6_ed2l3M", ",a6_vFs1j9"), "bill", "a6_dhDm9U", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_vFs1j9\">first</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_ed2l3M\">second</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3VIndent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_9WhjXw", ",a6_GVVgiJ"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (v)"
                + ", <ref xml:id=\"\" href=\"bill/a6_GVVgiJ\">first</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_9WhjXw\">second</ref>"
                + " indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_target2Sibilings() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_H9BTwE", ",a6_ed2l3M"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "<ref xml:id=\"\" href=\"bill/a6_ed2l3M\">second</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointABC() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_yPOvBG", ",a6_fVRJ9F", ",a6_uYgixH"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point"
                + " <ref xml:id=\"\" href=\"bill/a6_uYgixH\">(a)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_fVRJ9F\">(b)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_yPOvBG\">(c)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD12() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_JYhj1s", ",a6_qJGPtu"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point"
                + " <ref xml:id=\"\" href=\"bill/a6_qJGPtu\">(1)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_JYhj1s\">(2)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test

    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3_I_II_III() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_dhDm9U", ",a6_3eqk4L", ",a6_sdGkGc"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point <ref xml:id=\"\" href=\"bill/a6_sdGkGc\">(i)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_3eqk4L\">(ii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_dhDm9U\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    // Alinea tests
    // source indent, test all upper Alineas
    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3IVSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_grl0Ed"),"bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "<ref xml:id=\"\" href=\"bill/a6_grl0Ed\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3IV() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_d42YEi"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "this point";

        assertEquals(expectedResults, result.get());
    }

    @Ignore //same behaviour is actually happening. Waiting to discuss with business how to treat sub-points(alinea)
    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3SubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_6CT2lS"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point <ref xml:id=\"\" href=\"bill/a6_HVr06Y\">(3)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_6CT2lS\">first</ref> sub-point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD3() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_HVr06Y"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point <ref xml:id=\"\" href=\"bill/a6_HVr06Y\">(3)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Ignore //same behaviour is actually happening. Waiting to discuss with business how to treat sub-points(alinea)
    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointDSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_MM9gYx"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point <ref xml:id=\"\" href=\"bill/a6_g6qoqW\">(d)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_MM9gYx\">first</ref> sub-point";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointD3IVIndent_targetPointD() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_g6qoqW"), "bill", "a6_vFs1j9", document.getContent().get().getSource().getBytes());
        String expectedResults = "point <ref xml:id=\"\" href=\"bill/a6_g6qoqW\">(d)</ref>";

        assertEquals(expectedResults, result.get());
    }
    // end source indent, test all upper Alineas

    // source point (a), test all lower Alineas
    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD3IVSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_grl0Ed"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)(3)(iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_grl0Ed\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD3IV() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_d42YEi"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)(3)"
                + "<ref xml:id=\"\" href=\"bill/a6_d42YEi\">(iv)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD3SubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_6CT2lS"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)(3)"
                + ", <ref xml:id=\"\" href=\"bill/a6_6CT2lS\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD3() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_HVr06Y"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)"
                + "<ref xml:id=\"\" href=\"bill/a6_HVr06Y\">(3)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointDSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_MM9gYx"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point (d)"
                + ", <ref xml:id=\"\" href=\"bill/a6_MM9gYx\">first</ref> subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetPointD() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_g6qoqW"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults = "point"
                + " <ref xml:id=\"\" href=\"bill/a6_g6qoqW\">(d)</ref>";

        assertEquals(expectedResults, result.get());
    }
    //end aliena

    @Test
    public void generateLabelString_sameArticle_sameParagraph_sourcePointA_targetSubParagraph1() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_9CfL6Y"), "bill", "a6_uYgixH", document.getContent().get().getSource().getBytes());
        String expectedResults ="<ref xml:id=\"\" href=\"bill/a6_9CfL6Y\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourceParagraph2_targetParagraph1SubParagraph1() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_9CfL6Y"), "bill", "a6_hJc9Yk", document.getContent().get().getSource().getBytes());
        String expectedResults ="paragraph (1)"
                + ", <ref xml:id=\"\" href=\"bill/a6_9CfL6Y\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_sameArticle_sourceParagraph2_targetParagraph1PointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_vFs1j9", ",a6_ed2l3M", ",a6_H9BTwE"), "bill", "a6_hJc9Yk", document.getContent().get().getSource().getBytes());
        String expectedResults ="paragraph (1)"
                + ", point (d)(3)(iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_vFs1j9\">first</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_ed2l3M\">second</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph234() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_h0qOvb", ",a6_v4ETw1", ",a6_hJc9Yk"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47<ref xml:id=\"\" href=\"bill/a6_hJc9Yk\">(2)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_v4ETw1\">(3)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_h0qOvb\">(4)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph1PointABC() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_uYgixH", ",a6_fVRJ9F", ",a6_yPOvBG"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1)"
                + ", point <ref xml:id=\"\" href=\"bill/a6_uYgixH\">(a)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_fVRJ9F\">(b)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_yPOvBG\">(c)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph1SubParagraph() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_9CfL6Y"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1)"
                + ", <ref xml:id=\"\" href=\"bill/a6_9CfL6Y\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Ignore //adapt the test for the new solution
    @Test
    public void generateLabelString_differentArticle_targetParagraph1PointDSubPoint() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_MM9gYx"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1), point (d)"
                + ", <ref xml:id=\"\" href=\"bill/a6_MM9gYx\">first</ref>"
                + " subparagraph";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph1D_1_2() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_qJGPtu", ",a6_JYhj1s"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1)"
                + ", point (d)<ref xml:id=\"\" href=\"bill/a6_qJGPtu\">(1)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_JYhj1s\">(2)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph1D3_I_II_III() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_dhDm9U", ",a6_3eqk4L", ",a6_sdGkGc"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1)"
                + ", point (d)(3)"
                + "<ref xml:id=\"\" href=\"bill/a6_sdGkGc\">(i)</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_3eqk4L\">(ii)</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_dhDm9U\">(iii)</ref>";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph1PointD3IV_chose3Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_vFs1j9", ",a6_ed2l3M", ",a6_H9BTwE"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1)"
                + ", point (d)(3)(iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_vFs1j9\">first</ref>"
                + ", <ref xml:id=\"\" href=\"bill/a6_ed2l3M\">second</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";

        assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph1_chose2Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_ed2l3M", ",a6_H9BTwE"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1)"
                + ", point (d)(3)(iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_ed2l3M\">second</ref>"
                + " and <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";
        Assert.assertEquals(expectedResults, result.get());
    }

    @Test
    public void generateLabelString_differentArticle_targetParagraph1_chose1Indent() throws Exception {
        Result<String> result = referenceLabelGenerator.generateLabelStringRef(Arrays.asList(",a6_H9BTwE"), "bill", "art_1_A42pW6", document.getContent().get().getSource().getBytes());
        String expectedResults ="Article 47(1)"
                + ", point (d)(3)(iv)"
                + ", <ref xml:id=\"\" href=\"bill/a6_H9BTwE\">third</ref>"
                + " indent";
        Assert.assertEquals(expectedResults, result.get());
    }

}