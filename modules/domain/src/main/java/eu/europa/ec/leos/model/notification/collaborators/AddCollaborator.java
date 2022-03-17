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
package eu.europa.ec.leos.model.notification.collaborators;

import eu.europa.ec.leos.model.user.User;

public class AddCollaborator extends CollaboratorEmailNotification {

    public AddCollaborator(User recipient, String selectedEntity, String leosAuthority, String documentId, String link) {
        super(recipient, selectedEntity, leosAuthority, documentId, link);
    }

    @Override
    public String getEmailSubjectKey() {
        return "notification.collaborator.added.subject";
    }

    @Override
    public boolean withAttachment() {
        return false;
    }

    @Override
    public byte[] getAttachmentContent() {
        return null;
    }

    @Override
    public String getAttachmentName() {
        return "";
    }

    @Override
    public String getMimeType() {
        return "";
    }
}