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

import eu.europa.ec.leos.model.notification.EmailNotification;

import java.util.ArrayList;
import java.util.List;

abstract public class PDFGenerationEmailNotification implements EmailNotification {

    private List<String> recipients = new ArrayList<String>();
    private String recipient;
    private String emailBody;
    private String emailSubject;
    private String title;
    private byte[] attachmentContent;
    private String legFileName;

    public PDFGenerationEmailNotification(String recipient, String title, byte[] attachmentContent, String legFileName) {
        this.recipient = recipient;
        addRecipients(recipient);
        this.title = title;
        this.attachmentContent = attachmentContent;
        this.legFileName = legFileName;
    }

    private void addRecipients(String recipient) {
        if(recipient.indexOf(";") != -1) {
           String[] recipientsList = recipient.split(";");
           for(String r : recipientsList) {
               recipients.add(r);
           }
        } else {
            recipients.add(recipient);
        }
    }

    @Override
    public List<String> getRecipients() {
        return recipients;
    }

    @Override
    public String getNotificationName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getEmailSubject() {
        return emailSubject;
    }

    @Override
    public String getEmailBody() {
        return emailBody;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public byte[] getAttachmentContent() {
        return attachmentContent;
    }

    public void setAttachmentContent(byte[] attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    public String getLegFileName() {
        return legFileName;
    }

    public void setLegFileName(String legFileName) {
        this.legFileName = legFileName;
    }

    abstract public String getEmailSubjectKey();
}
