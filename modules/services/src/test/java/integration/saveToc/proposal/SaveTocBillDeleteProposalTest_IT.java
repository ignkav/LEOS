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
import static integration.saveToc.TocVOCreateUtils.getElementById;
import static org.junit.Assert.assertEquals;

public class SaveTocBillDeleteProposalTest_IT extends SaveTocBillProposalTest_IT {

    private static final Logger log = LoggerFactory.getLogger(SaveTocBillDeleteProposalTest_IT.class);

    @Test
    public void test_delete__article() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_add__article__expected_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "bill_with1Article_ec.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        body.removeChildItem(getElementById(toc, "new_art_1"));

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
    public void test_delete__3Articles() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_add__3articles__expected_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "bill_with1Article_ec.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO body = getElementById(toc, "body");
        body.removeChildItem(getElementById(toc, "art_2"));
        body.removeChildItem(getElementById(toc, "art_3"));
        body.removeChildItem(getElementById(toc, "art_4"));

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
    public void test_delete__mixedElements() {
        // Given
        final byte[] xmlInput = TestUtils.getFileContent("", "/contentProcessor/test_createDocumentContentWithNewTocList.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_SAVE_TOC_BILL_EC, "test_delete__mixedElements_expected.xml");
        List<TableOfContentItemVO> toc = buildTableOfContentBill(xmlInput);

        TableOfContentItemVO cits = getElementById(toc, "cits");
        cits.removeChildItem(getElementById(toc, "cit_2"));
        cits.removeChildItem(getElementById(toc, "cit_3"));
        cits.removeChildItem(getElementById(toc, "cit_4"));
        cits.removeChildItem(getElementById(toc, "cit_5"));
        cits.removeChildItem(getElementById(toc, "cit_6"));

        TableOfContentItemVO recs = getElementById(toc, "recs");
        recs.removeChildItem(getElementById(toc, "rec_2"));
        recs.removeChildItem(getElementById(toc, "rec_3"));

        TableOfContentItemVO body = getElementById(toc, "body");
        body.removeChildItem(getElementById(toc, "art_2"));
        body.removeChildItem(getElementById(toc, "art_3"));

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
