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
package eu.europa.ec.leos.annotate.model.helper;

import java.util.Arrays;
import java.util.List;

import eu.europa.ec.leos.annotate.model.entity.AuthClient;

/**
 * Helper functions for {@link AuthClient} entities
 */
public final class AuthClientHandler {

    private AuthClientHandler() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * returns a list of the individual authorities for which the client may authenticate
     */
    public static List<String> getAuthoritiesList(final AuthClient client) {

        final String authorities = client.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return null;
        }
        return Arrays.asList(authorities.split(";"));
    }

}
