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

import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Token;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.services.exceptions.*;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    /**
     * extract the user login from a given token and a given context
     * 
     * @param token the token, as String
     * @param context the context of the user
     * @return extracted user login
     * 
     * @throws NoClientsAvailableException this exception is thrown when no clients are available
     * @throws TokenFromUnknownClientException this exception is thrown when none of the registered clients can decode the token
     * @throws TokenInvalidForClientAuthorityException this exception is thrown when the decoding client may not authenticate the authority issuing the token
     */
    UserInformation getUserLoginFromTokenWithContext(String token, String context)
            throws NoClientsAvailableException, TokenFromUnknownClientException, TokenInvalidForClientAuthorityException;

    /**
     * Extracts the access token from the given request and returns the user name
     * 
     * It checks against the database to ensure that the user is authenticated
     * 
     * @param request the incoming HttpServletRequest containing the 'Authorization' header
     * 
     * @return the proper user name or {@literal null} if the user is not authenticated / an invalid or no header is presented
     * 
     * @throws AccessTokenExpiredException this exception is thrown when the provided access token is known, but has expired
     */
    String getUserLogin(HttpServletRequest request) throws AccessTokenExpiredException;

    /**
     * have access and refresh tokens generated for a user and stored directly
     * 
     * @param userInfo the {@link UserInformation} containing user for which tokens are to be generated
     * @return updated {@link User} object with new tokens set
     * 
     * @throws CannotStoreTokenException exception thrown when storing the token fails
     */
    Token generateAndSaveTokensForUser(UserInformation userInfo) throws CannotStoreTokenException;

    /**
     * find the user owning a certain refresh token
     *
     * @param refreshToken the refresh token previously given to a user
     * 
     * @return found {@Link UserInformation}, or {@literal null}
     */
    UserInformation findUserByRefreshToken(String refreshToken);

    /**
     * find the user owning a certain access token
     *
     * @param accessToken the access token previously given to a user
     * 
     * @return found {@link UserInformation}, or {@literal null}
     */
    UserInformation findUserByAccessToken(String accessToken);

    /**
     * cleanup procedure for removing tokens that have already expired - they only consume space and no longer serve any purpose
     * 
     * @param user the {@link User} whose tokens are to be cleaned up
     * 
     * @return {@literal true} if something was cleaned; {@literal false} if there was nothing to clean or an error occured 
     */
    boolean cleanupExpiredUserTokens(User user);
}
