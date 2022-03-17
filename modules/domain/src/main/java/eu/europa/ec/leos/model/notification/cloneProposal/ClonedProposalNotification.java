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
package eu.europa.ec.leos.model.notification.cloneProposal;

import eu.europa.ec.leos.model.notification.EmailNotification;

import java.util.ArrayList;
import java.util.List;

public class ClonedProposalNotification implements EmailNotification {

    private List<String> recipients = new ArrayList<String>();
    private String recipient;
    private String emailBody;
    private String emailSubject;
    private String title;
    private String legFileName;
    private String proposalUrl;
    private String iscRef;

    public ClonedProposalNotification(String recipient, String title, String legFileName, String proposalUrl, String iscRef) {
        this.recipient = recipient;
        addRecipients(recipient);
        this.title = title;
        this.legFileName = legFileName;
        this.proposalUrl = proposalUrl;
        this.iscRef = iscRef;
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

    @Override
    public byte[] getAttachmentContent() { return new byte[0]; }

    @Override
    public String getAttachmentName() { return null; }

    @Override
    public String getMimeType() { return null; }

    @Override
    public boolean withAttachment() { return false; }

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
    
    public String getLegFileName() {
        return legFileName;
    }

    public void setLegFileName(String legFileName) {
        this.legFileName = legFileName;
    }

    public String getEmailSubjectKey() { return "notification.clone.proposal.subject"; };

    public String getProposalUrl() { return proposalUrl; }

    public void setProposalUrl(String proposalUrl) { this.proposalUrl = proposalUrl; }

    public String getIscRef() { return iscRef; }

    public void setIscRef(String iscRef) { this.iscRef = iscRef; }
}
