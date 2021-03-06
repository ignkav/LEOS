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
package eu.europa.ec.leos.model.notification.pdfGeneration;

public class PDFGenerationNotification extends PDFGenerationEmailNotification {

    public PDFGenerationNotification(String recipient, String title, byte[] attachmentContent, String legFileName) {
        super(recipient, title, attachmentContent, legFileName);
    }

    @Override
    public String getEmailSubjectKey() {
        return "notification.pdf.generation.subject";
    }

    @Override
    public String getAttachmentName() {
        return "proposal.zip";
    }
    
    @Override
    public String getMimeType() {
        return "application/zip";
    }
    
    @Override
    public boolean withAttachment() {
        return true;
    }
}
