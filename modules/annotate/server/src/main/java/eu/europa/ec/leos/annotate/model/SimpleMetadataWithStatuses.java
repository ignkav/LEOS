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
package eu.europa.ec.leos.annotate.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.europa.ec.leos.annotate.Generated;
import eu.europa.ec.leos.annotate.model.web.helper.SimpleMetadataWithStatusesSerializer;

import java.util.List;
import java.util.Objects;

/**
 * POJO class hosting metadata and statuses to be used for searching
 * internally makes sure that at least default values are always available
 */
@JsonSerialize(using = SimpleMetadataWithStatusesSerializer.class)
public class SimpleMetadataWithStatuses {

    // -------------------------------------
    // private properties
    // -------------------------------------

    private SimpleMetadata metadata;
    private List<AnnotationStatus> statuses;

    // -------------------------------------
    // Constructors
    // -------------------------------------

    public SimpleMetadataWithStatuses() {
        // default constructor
        initMembers();
    }

    public SimpleMetadataWithStatuses(final SimpleMetadata meta, final List<AnnotationStatus> statuses) {
        this.metadata = meta;
        this.statuses = statuses;
        initMembers();
    }

    private void initMembers() {
        if (this.metadata == null) {
            this.metadata = new SimpleMetadata();
        }
        if (this.statuses == null) {
            this.statuses = AnnotationStatus.getDefaultStatus();
        }
    }

    public boolean isEmptyDefaultEntry() {
        if(!this.metadata.equals(new SimpleMetadata())) {
            return false;
        }
        
        return this.statuses.equals(AnnotationStatus.getDefaultStatus());
    }
    
    // -------------------------------------
    // Getters & setters
    // -------------------------------------

    @Generated
    public SimpleMetadata getMetadata() {
        return metadata;
    }

    @Generated
    public void setMetadata(final SimpleMetadata metadata) {
        this.metadata = metadata;
    }

    @Generated
    public List<AnnotationStatus> getStatuses() {
        return statuses;
    }

    @Generated
    public void setStatuses(final List<AnnotationStatus> statuses) {
        this.statuses = statuses;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(metadata, statuses);
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
        final SimpleMetadataWithStatuses other = (SimpleMetadataWithStatuses) obj;
        return Objects.equals(this.metadata, other.metadata) &&
                Objects.equals(this.statuses, other.statuses);
    }
}
