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
package eu.europa.ec.leos.annotate.model.entity;

import eu.europa.ec.leos.annotate.Generated;
import eu.europa.ec.leos.annotate.model.ResponseStatus;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Class representing a set of metadata logically assigned to a group and a document 
 */
@Entity
@Table(name = "METADATA", indexes = {
        @Index(columnList = "SYSTEM_ID", name = "METADATA_IX_SYSTEM_ID"),
        @Index(columnList = "RESPONSE_STATUS", name = "METADATA_IX_RESPONSE_STATUS"),
        @Index(columnList = "VERSION", name = "METADATA_IX_VERSION")})
public class Metadata {

    // -----------------------------------------------------------
    // Constants for known metadata properties
    // -----------------------------------------------------------
    public static final String PROP_SYSTEM_ID = "systemId";
    public static final String PROP_RESPONSE_STATUS = "responseStatus";
    public static final String PROP_RESPONSE_ID = "responseId";
    public static final String PROP_RESPONSE_VERSION = "responseVersion";
    public static final String PROP_VERSION = "version";
    public static final String PROP_ISC_REF = "ISCReference";
    public static final String PROP_ORIGIN_MODE = "originMode";
    public static final List<String> PROPS_OWN_COLS = Arrays.asList(PROP_SYSTEM_ID, PROP_RESPONSE_STATUS, PROP_VERSION);

    public static final String VERSION_SEARCH_UP_TO = "<=";

    // -------------------------------------
    // column definitions
    // -------------------------------------

    @Id
    @Column(name = "ID", nullable = false)
    @GenericGenerator(name = "metadataSequenceGenerator", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
            @Parameter(name = "sequence_name", value = "METADATA_SEQ"),
            @Parameter(name = "increment_size", value = "1")
    })
    @GeneratedValue(generator = "metadataSequenceGenerator")
    @SuppressWarnings("PMD.ShortVariable")
    private long id;

    /**
     * document ID column, filled by hibernate
     */
    @Column(name = "DOCUMENT_ID", insertable = false, updatable = false, nullable = false)
    private long documentId;

    /**
     * associate user, mapped by hibernate using DOCUMENTS.DOCUMENT_ID column
     */
    @OneToOne
    @JoinColumn(name = "DOCUMENT_ID")
    private Document document;

    /**
     * group ID column, filled by hibernate
     */
    @Column(name = "GROUP_ID", insertable = false, updatable = false, nullable = false)
    private long groupId;

    /**
     * associate user, mapped by hibernate using GROUPS.GROUP_ID column
     */
    @OneToOne
    @JoinColumn(name = "GROUP_ID")
    private Group group;

    /**
     * systemId calling
     */
    @Column(name = "SYSTEM_ID", nullable = false)
    private String systemId;

    /**
     * version
     */
    @Column(name = "VERSION", nullable = true)
    private String version;
    
    /**
     * response status enum
     */
    @Column(name = "RESPONSE_STATUS")
    @Enumerated(EnumType.ORDINAL)
    private ResponseStatus responseStatus;

    /**
     * line-separated list of key-value pairs (metadata name, metadata value)
     */
    @Column(name = "KEYVALUES")
    private String keyValuePairs;

    /**
     * track the datetime of modification of the response status
     * note: NOT auto-filled by DB trigger as this would require different implementations for Oracle and H2
     */
    @Column(name = "RESPONSE_STATUS_UPDATED", nullable = true)
    private LocalDateTime responseStatusUpdated;

    /**
     * track who modified the responsestatus
     * (user id, but we don't create a foreign key as it might slow down things unintentionally)
     */
    @Column(name = "RESPONSE_STATUS_UPDATED_BY", nullable = true)
    private Long responseStatusUpdatedBy;

    // -----------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------

    public Metadata() {
        // parameterless constructor required by JPA
    }

    public Metadata(final Document document, final Group group, final String systemId) {
        this.document = document;
        this.group = group;
        this.systemId = systemId;
    }

    public Metadata(final Metadata other) {
        
        // copy constructor
        this.document = other.document;
        this.group = other.group;
        this.systemId = other.systemId;
        this.version = other.version;
        this.keyValuePairs = other.keyValuePairs;
        this.responseStatus = other.responseStatus;
        this.responseStatusUpdated = other.responseStatusUpdated;
        this.responseStatusUpdatedBy = other.responseStatusUpdatedBy;
    }

    // -----------------------------------------------------------
    // Getters & setters
    // -----------------------------------------------------------

    @Generated
    public long getId() {
        return id;
    }

    @Generated
    public void setId(final long newId) {
        this.id = newId;
    }

    @Generated
    public long getDocumentId() {
        return documentId;
    }

    @Generated
    public void setDocumentId(final long documentId) {
        this.documentId = documentId;
    }

    @Generated
    public Document getDocument() {
        return document;
    }

    @Generated
    public void setDocument(final Document document) {
        this.document = document;
    }

    @Generated
    public long getGroupId() {
        return groupId;
    }

    @Generated
    public void setGroupId(final long groupId) {
        this.groupId = groupId;
    }

    @Generated
    public Group getGroup() {
        return group;
    }

    @Generated
    public void setGroup(final Group group) {
        this.group = group;
    }

    @Generated
    public String getSystemId() {
        return systemId;
    }

    @Generated
    public void setSystemId(final String systemId) {
        this.systemId = systemId;
    }

    @Generated
    public String getVersion() {
        return version;
    }
    
    @Generated
    public void setVersion(final String version) {
        this.version = version;
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
    public LocalDateTime getResponseStatusUpdated() {
        return responseStatusUpdated;
    }

    @Generated
    public void setResponseStatusUpdated(final LocalDateTime upd) {
        this.responseStatusUpdated = upd;
    }

    @Generated
    public Long getResponseStatusUpdatedBy() {
        return responseStatusUpdatedBy;
    }

    @Generated
    public void setResponseStatusUpdatedBy(final Long userId) {
        this.responseStatusUpdatedBy = userId;
    }

    @Generated
    public String getKeyValuePairs() {
        return keyValuePairs;
    }

    @Generated
    public void setKeyValuePairs(final String keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(id, documentId, document, groupId, group, systemId,
                responseStatus, responseStatusUpdated, responseStatusUpdatedBy, keyValuePairs);
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
        final Metadata other = (Metadata) obj;
        return Objects.equals(this.id, other.id) &&
                Objects.equals(this.documentId, other.documentId) &&
                Objects.equals(this.document, other.document) &&
                Objects.equals(this.groupId, other.groupId) &&
                Objects.equals(this.group, other.group) &&
                Objects.equals(this.systemId, other.systemId) &&
                Objects.equals(this.version, other.version) &&
                Objects.equals(this.responseStatus, other.responseStatus) &&
                Objects.equals(this.responseStatusUpdated, other.responseStatusUpdated) &&
                Objects.equals(this.responseStatusUpdatedBy, other.responseStatusUpdatedBy) &&
                Objects.equals(this.keyValuePairs, other.keyValuePairs);
    }
}
