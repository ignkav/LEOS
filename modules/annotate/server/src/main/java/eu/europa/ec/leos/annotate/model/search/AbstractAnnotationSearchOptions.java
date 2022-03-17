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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public abstract class AbstractAnnotationSearchOptions {

    /**
     * group name being originator for the annotations
     */
    private String group;

    /**
     * public (shared = true), private (shared = false) or both (shared = null) annotations (optional)
     */
    private Boolean shared;

    /**
     * URI of the document for which annotations are wanted
     */
    private URI uri;

    /**
     * user login being originator for the annotations (optional)
     */
    private String user;

    public AbstractAnnotationSearchOptions() {
        // default constructor
    }

    public AbstractAnnotationSearchOptions(final String group, final String uri) {
        super();
        this.group = group;
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot search as given URI is invalid", e);
        }
    }

    @Generated
    public String getGroup() {
        return group;
    }

    @Generated
    public void setGroup(String group) {
        this.group = group;
    }

    @Generated
    public Boolean getShared() {
        return shared;
    }

    @Generated
    public void setShared(final Boolean shared) {
        this.shared = shared;
    }

    @Generated
    public URI getUri() {
        return uri;
    }

    @Generated
    public void setUri(final URI uri) {
        this.uri = uri;
    }

    public void setUri(final String uri) {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Given search URI is invalid", e);
        }
    }

    @Generated
    public String getUser() {
        return user;
    }

    @Generated
    public void setUser(final String user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, shared, uri, user);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final AbstractAnnotationSearchOptions other = (AbstractAnnotationSearchOptions) obj;
        return Objects.equals(this.uri, other.uri) &&
                Objects.equals(this.group, other.group) &&
                Objects.equals(this.shared, other.shared) &&
                Objects.equals(this.user, other.user);
    }

}
