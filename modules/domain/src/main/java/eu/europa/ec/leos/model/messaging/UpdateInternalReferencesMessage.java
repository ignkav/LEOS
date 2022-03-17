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
package eu.europa.ec.leos.model.messaging;

public class UpdateInternalReferencesMessage {

    private String documentId;
    private String documentRef;
    private String presenterId;

    public UpdateInternalReferencesMessage() {//json deserialized need the empty constructor
    }

    public UpdateInternalReferencesMessage(String documentId, String documentRef, String presenterId) {
        this.documentId = documentId;
        this.documentRef = documentRef;
        this.presenterId = presenterId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentRef() {
        return documentRef;
    }

    public void setDocumentRef(String documentRef) {
        this.documentRef = documentRef;
    }

    public String getPresenterId() {
        return presenterId;
    }

    public void setPresenterId(String presenterId) {
        this.presenterId = presenterId;
    }

    @Override
    public String toString() {
        return "UpdateInternalReferencesMessage{" +
                "documentId='" + documentId + '\'' +
                ", documentRef='" + documentRef + '\'' +
                ", presenterId='" + presenterId + '\'' +
                '}';
    }

}

