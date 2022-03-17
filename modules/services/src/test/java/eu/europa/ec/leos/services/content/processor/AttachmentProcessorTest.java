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
package eu.europa.ec.leos.services.content.processor;

import eu.europa.ec.leos.services.support.xml.XPathCatalog;
import eu.europa.ec.leos.services.support.xml.XPathV1Catalog;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessorProposal;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

public class AttachmentProcessorTest extends LeosTest {

    private XmlContentProcessor xmlContentProcessor = new XmlContentProcessorProposal();
    @InjectMocks
    private XPathCatalog xPathCatalog = spy(new XPathV1Catalog());
    @InjectMocks
    private AttachmentProcessor attachmentProcessor = new AttachmentProcessorImpl(xmlContentProcessor);

    @Test
    public void test_addAttachment_NoAttachmentsTag() {
        // Given
        String xml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"/>" +
                "</bill>";
        String href = "annex_href";
        String showAs = "";
        String expectedXml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"/>" +
                "<attachments>" +
                "<attachment xml:id=\"newAttach\"><documentRef href=\"" + href + "\" showAs=\"" + showAs + "\" xml:id=\"newDocRef\"/></attachment>" +
                "</attachments>" +
                "</bill>";

        // When
        byte[] result = attachmentProcessor.addAttachmentInBill(xml.getBytes(UTF_8), href, showAs);

        // Then
        assertEquals(squeezeXml(expectedXml), squeezeXml(new String(result, UTF_8)));
    }

    @Test
    public void test_addAttachment_WithAttachmentsTag() {
        // Given
        String xml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"/>" +
                "<attachments xml:id=\"attachs\">" +
                "<attachment xml:id=\"atta\"><documentRef href=\"someHref\" showAs=\"\" xml:id=\"docref\"/></attachment>" +
                "</attachments>" +
                "</bill>";
        String href = "annex_href";
        String showAs = "";
        String expectedXml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"/>" +
                "<attachments xml:id=\"attachs\">" +
                "<attachment xml:id=\"atta\"><documentRef href=\"someHref\" showAs=\"\" xml:id=\"docref\"/></attachment>" +
                "<attachment xml:id=\"newAttach\"><documentRef href=\"" + href + "\" showAs=\"" + showAs + "\" xml:id=\"newDocRef\"/></attachment>" +
                "</attachments>" +
                "</bill>";

        // When
        byte[] result = attachmentProcessor.addAttachmentInBill(xml.getBytes(UTF_8), href, showAs);

        // Then
        assertEquals(squeezeXml(expectedXml), squeezeXml(new String(result, UTF_8)));
    }

    @Test
    public void test_removeAttachmentWithSingleAttachment() {
        // Given
        String href = "annex_href";
        String showAs = "";

        String xml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"/>" +
                "<attachments xml:id=\"id1\">" +
                "<attachment xml:id=\"id2\"><documentRef href=\"" + href + "\" showAs=\"" + showAs + "\" xml:id=\"id3\"/></attachment>" +
                "</attachments>" +
                "</bill>";

        String expectedXml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"/>" +
                "</bill>";

        // When
        byte[] result = attachmentProcessor.removeAttachmentFromBill(xml.getBytes(UTF_8), href);

        // Then
        assertEquals(squeezeXml(expectedXml), squeezeXml(new String(result, UTF_8)));
    }

    @Test
    public void test_removeAttachmentWithMultipleAttachment() {
        // Given
        String href = "annex_href";
        String showAs = "";
        String href2 = "annex_href2";
        String showAs2 = "";

        String xml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"></meta>" +
                "<attachments xml:id=\"id1\">" +
                "<attachment xml:id=\"id2\"><documentRef href=\"" + href + "\" showAs=\"" + showAs + "\" xml:id=\"id3\"/></attachment>" +
                "<attachment xml:id=\"id2\"><documentRef href=\"" + href2 + "\" showAs=\"" + showAs2 + "\" xml:id=\"id3\"/></attachment>" +
                "</attachments>" +
                "</bill>";

        String expectedXml = "<bill xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\">" +
                "<meta xml:id=\"ElementId\"/>" +
                "<attachments xml:id=\"id1\">" +
                "<attachment xml:id=\"id2\"><documentRef href=\"" + href2 + "\" showAs=\"" + showAs2 + "\" xml:id=\"id3\"/></attachment>" +
                "</attachments>" +
                "</bill>";

        // When
        byte[] result = attachmentProcessor.removeAttachmentFromBill(xml.getBytes(UTF_8), href);

        // Then
        assertEquals(squeezeXml(expectedXml), squeezeXml(new String(result, UTF_8)));
    }
}