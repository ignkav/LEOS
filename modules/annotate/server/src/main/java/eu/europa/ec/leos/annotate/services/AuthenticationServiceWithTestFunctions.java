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
package eu.europa.ec.leos.annotate.services;

import eu.europa.ec.leos.annotate.services.exceptions.NoClientsAvailableException;

/**
 * interface extending the AuthenticationService with additional test functions
 */
public interface AuthenticationServiceWithTestFunctions extends AuthenticationService {

    /**
     * creation of a JWT token
     * NOTE: currently used for tests only
     * 
     * @param userId the user login to be encoded in the JWT token
     * @param clientId the ID of the client to use (client ID, not DB table ID!)
     * @return the JWT token as string
     * 
     * @throws NoClientsAvailableException this exception is thrown when no clients are available
     */
    String createToken(String userId, String clientId) throws NoClientsAvailableException;
}
