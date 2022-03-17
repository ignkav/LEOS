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
package eu.europa.ec.leos.annotate.model.search;

import eu.europa.ec.leos.annotate.Generated;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 *  class representing the options available for searching for the number of annotations of a URI/group/metadata
 */
public class AnnotationSearchCountOptions extends AbstractAnnotationSearchOptions {

    // -------------------------------------
    // Available properties
    // -------------------------------------

    /**
     * list of metadata sets requested (JSON format); can also contain the statuses that should be matched
     */
    private String metadatasets;

    /**
     * entity for which a user is connected (optional)
     */
    private String connectedEntity;

    // -------------------------------------
    // Constructors
    // -------------------------------------

    public AnnotationSearchCountOptions() {
        // default constructor
        super();
    }

    // constructor with mandatory search parameters
    public AnnotationSearchCountOptions(final String uri, final String group, final Boolean shared, final String serializedMetadata) {

        super(group, uri);

        this.metadatasets = serializedMetadata;
        setShared(shared);
    }

    // -------------------------------------
    // Useful functions
    // -------------------------------------
    public void decodeEscapedBrackets() {

        // at least during test scenarios, we experienced problems when sending JSON metadata
        // with only one entry - therefore, we had encoded the curly brackets URL-conform,
        // and have to decode this again here
        if (!StringUtils.isEmpty(this.metadatasets)) {
            this.metadatasets = this.metadatasets.replace("%7B", "{")
                    .replace("%7D", "}")
                    .replace("\\", "");
        }
    }

    // -------------------------------------
    // Getters & setters
    // -------------------------------------

    @Generated
    public String getMetadatasets() {
        return metadatasets;
    }

    @Generated
    public void setMetadatasets(final String metadataMap) {
        this.metadatasets = metadataMap;
    }

    @Generated
    public String getConnectedEntity() {
        return connectedEntity;
    }

    @Generated
    public void setConnectedEntity(final String connectedEntity) {
        this.connectedEntity = connectedEntity;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(metadatasets, connectedEntity);
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

        final AnnotationSearchCountOptions other = (AnnotationSearchCountOptions) obj;
        return super.equals(obj) &&
                Objects.equals(this.metadatasets, other.metadatasets) &&
                Objects.equals(this.connectedEntity, other.connectedEntity);
    }

}
