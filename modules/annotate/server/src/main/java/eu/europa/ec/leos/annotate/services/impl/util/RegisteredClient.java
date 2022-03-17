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
package eu.europa.ec.leos.annotate.services.impl.util;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import eu.europa.ec.leos.annotate.model.entity.AuthClient;

/**
 * internal class to ease access from a given configured client to its algorithm and token verifier
 */
public final class RegisteredClient {

    private final AuthClient client;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    // -------------------------------------
    // Constructor
    // -------------------------------------
    public RegisteredClient(final AuthClient client, final Algorithm algorithm, final JWTVerifier verifier) {
        this.client = client;
        this.algorithm = algorithm;
        this.verifier = verifier;
    }

    // -------------------------------------
    // Getter
    // -------------------------------------
    public AuthClient getClient() {
        return client;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public JWTVerifier getVerifier() {
        return verifier;
    }

}