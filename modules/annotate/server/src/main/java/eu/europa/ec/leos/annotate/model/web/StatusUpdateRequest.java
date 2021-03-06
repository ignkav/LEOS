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
package eu.europa.ec.leos.annotate.model.web;

import eu.europa.ec.leos.annotate.Generated;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;

import java.util.Objects;

/**
 * Class hosting the parameters given for an annotation status update request
 */
public class StatusUpdateRequest {

    // -----------------------------------------------------------
    // Fields
    // -----------------------------------------------------------
    private String group;
    private String uri;
    private ResponseStatus responseStatus = ResponseStatus.UNKNOWN;
    private SimpleMetadata metadataToMatch;
    private boolean migrateVersion;

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    public StatusUpdateRequest() {
        // default constructor
    }

    public StatusUpdateRequest(final String group, final String uri, final ResponseStatus resp) {
        this.group = group;
        this.uri = uri;
        this.responseStatus = resp;
    }
    
    public StatusUpdateRequest(final String group, final String uri, final ResponseStatus resp,
            final boolean migrateVersion) {
        this.group = group;
        this.uri = uri;
        this.responseStatus = resp;
        this.migrateVersion = migrateVersion;
    }

    // -----------------------------------------------------------
    // Getters & setters
    // -----------------------------------------------------------
    
    @Generated
    public String getGroup() {
        return group;
    }

    @Generated
    public void setGroup(final String group) {
        this.group = group;
    }

    @Generated
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Generated
    public void setResponseStatus(final ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    @Generated
    public SimpleMetadata getMetadataToMatch() {
        return metadataToMatch;
    }

    @Generated
    public void setMetadataToMatch(final SimpleMetadata metadataToMatch) {
        this.metadataToMatch = metadataToMatch;
    }

    @Generated
    public String getUri() {
        return uri;
    }

    @Generated
    public void setUri(final String uri) {
        this.uri = uri;
    }

    @Generated
    public boolean isMigrateVersion() {
        return migrateVersion;
    }

    @Generated
    public void setMigrateVersion(final boolean migrateVersion) {
        this.migrateVersion = migrateVersion;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(group, uri, responseStatus, metadataToMatch, migrateVersion);
    }

    @Generated
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final StatusUpdateRequest other = (StatusUpdateRequest) obj;
        return Objects.equals(this.group, other.group) &&
                Objects.equals(this.uri, other.uri) &&
                Objects.equals(this.responseStatus, other.responseStatus) &&
                Objects.equals(this.metadataToMatch, other.metadataToMatch) &&
                Objects.equals(this.migrateVersion, other.migrateVersion);
    }
}