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
package eu.europa.ec.leos.annotate.model.web.websocket;

import eu.europa.ec.leos.annotate.Generated;

import java.util.Objects;

public class SubscriptionRequest {

    private Filter filter;

    // -----------------------------------------------------------
    // Getters & setters
    // -----------------------------------------------------------

    @Generated
    public Filter getFilter() {
        return filter;
    }

    @Generated
    public void setFilter(final Filter filter) {
        this.filter = filter;
    }

    // -----------------------------------------------------------
    // equals and hashCode
    // -----------------------------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(filter);
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
        final SubscriptionRequest other = (SubscriptionRequest) obj;
        return Objects.equals(this.filter, other.filter);
    }

}
