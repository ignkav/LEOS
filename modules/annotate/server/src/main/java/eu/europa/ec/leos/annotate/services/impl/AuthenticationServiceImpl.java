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
package eu.europa.ec.leos.annotate.services.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import eu.europa.ec.leos.annotate.model.AuthenticatedUserStore;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.AuthClient;
import eu.europa.ec.leos.annotate.model.entity.Token;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.AuthClientHandler;
import eu.europa.ec.leos.annotate.model.helper.TokenHandler;
import eu.europa.ec.leos.annotate.repository.AuthClientRepository;
import eu.europa.ec.leos.annotate.repository.TokenRepository;
import eu.europa.ec.leos.annotate.services.AuthenticationServiceWithTestFunctions;
import eu.europa.ec.leos.annotate.services.UUIDGeneratorService;
import eu.europa.ec.leos.annotate.services.exceptions.*;
import eu.europa.ec.leos.annotate.services.impl.util.RegisteredClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service containing all user authentication functionality
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationServiceWithTestFunctions {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private static final String BEARER_SUFFIX = "Bearer ";

    private static final Pattern SUBJECT_PATTERN = Pattern.compile("acct:(.+)@(.+)");

    private static final int TOKENS_EXPIRE_IN_MIN = 50000; // large value for simplifying testing

    // internal list of all clients, enables easy access to algorithms, JWTVerifier and configuration data
    private List<RegisteredClient> clients;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private final AuthClientRepository authClientRepos;

    @Autowired
    private final TokenRepository tokenRepository;

    @Autowired
    private UUIDGeneratorService uuidService;

    @Value("${token.access.lifetime}")
    private int lifetimeAccessToken;

    @Value("${token.refresh.lifetime}")
    private int lifetimeRefreshToken;

    @Autowired
    private AuthenticatedUserStore authUserStore;

    // -------------------------------------
    // Constructor
    // -------------------------------------
    @Autowired
    public AuthenticationServiceImpl(final AuthClientRepository authClientRepos, final TokenRepository tokenRepos) {

        this.authClientRepos = authClientRepos;
        this.tokenRepository = tokenRepos;
    }

    @Override
    public String createToken(final String userId, final String clientId) throws NoClientsAvailableException {

        final Date now = Calendar.getInstance().getTime();
        final Calendar expires = Calendar.getInstance();
        expires.add(Calendar.MINUTE, TOKENS_EXPIRE_IN_MIN);
        final Date expiresAt = expires.getTime();

        // read clients from DB, then search the one having the given client ID
        refreshClientList();
        final RegisteredClient registeredClient = this.clients.stream().filter(regClient -> regClient.getClient().getClientId().equals(clientId))
                .findFirst()
                .get();

        // any more claims we need to verify?
        return JWT.create()
                .withIssuer(registeredClient.getClient().getClientId())
                .withSubject(String.format("acct:%s@%s", userId, registeredClient.getClient().getAuthorities()))
                .withIssuedAt(now)
                .withNotBefore(now)
                .withExpiresAt(expiresAt)
                .sign(registeredClient.getAlgorithm());
    }

    @Override
    public UserInformation getUserLoginFromTokenWithContext(final String token, final String context)
            throws NoClientsAvailableException, TokenFromUnknownClientException, TokenInvalidForClientAuthorityException {

        try {
            final AtomicReference<AuthClient> clientThatDecodedRef = new AtomicReference<>();
            final DecodedJWT jwt = tryDecoding(token, clientThatDecodedRef);
            if (jwt == null) {
                throw new TokenFromUnknownClientException(String.format("Received token '%s' could not be decoded with registered clients", token));
            }
            final String subject = jwt.getSubject();
            final Matcher match = SUBJECT_PATTERN.matcher(subject);
            if (!match.matches()) {
                return null;
            }

            final AuthClient client = clientThatDecodedRef.get();
            final List<String> authorities = AuthClientHandler.getAuthoritiesList(client);

            // check authority - client does not have any? -> token accepted
            if (authorities == null) {
                LOG.debug("Client may authenticate all authorities -> pass extracted user");
                return new UserInformation(match.group(1), context, match.group(2));
            }

            if (authorities.contains(match.group(2))) {
                LOG.debug("Client may authenticate authority '{}' -> pass extracted user", match.group(2));
                return new UserInformation(match.group(1), context, match.group(2));
            }
            LOG.info("Client {} may not authenticate authority '{}' -> token ignored", client.getId(), match.group(2));
            throw new TokenInvalidForClientAuthorityException(String.format("Client %s may not authenticate authority '%s'", client.getId(), match.group(2)));

        } catch (Exception e) {
            LOG.error("Received exception during token verification", e);
            throw e;
        }
    }

    /**
     * loop over registered clients and see if any can be used for decoding the received token
     * 
     * @param token token to be decoded
     * @return returns a decoded token, or {@literal null} if no registered client can decode it
     * @throws NoClientsAvailableException 
     */
    private DecodedJWT tryDecoding(final String token, final AtomicReference<AuthClient> successfulClientByRef) throws NoClientsAvailableException {

        refreshClientList();

        for (final RegisteredClient registeredClient : this.clients) {

            try {
                final DecodedJWT decoded = registeredClient.getVerifier().verify(token);
                LOG.info("Verified received token using client {}", registeredClient.getClient().getId());
                successfulClientByRef.set(registeredClient.getClient());
                return decoded;
            } catch (JWTVerificationException verifExc) {
                LOG.info("Received token could not be verified for client {}", registeredClient.getClient().getId());
            }
        }

        // when reaching this point, no client could verify the token!
        return null;
    }

    @Override
    public String getUserLogin(final HttpServletRequest request) throws AccessTokenExpiredException {

        final String authenticationHeader = request.getHeader("Authorization");

        if (StringUtils.isEmpty(authenticationHeader) || !authenticationHeader.startsWith(BEARER_SUFFIX)) {
            authUserStore.clear();
            return null;
        }

        final String accessToken = authenticationHeader.substring(BEARER_SUFFIX.length());

        final UserInformation info = findUserByAccessToken(accessToken);
        authUserStore.setUserInfo(info);

        if (info == null) {
            return null;
        }

        final Token foundToken = info.getCurrentToken();
        if (foundToken != null && TokenHandler.isAccessTokenExpired(foundToken)) {
            throw new AccessTokenExpiredException();
        }

        return info.getLogin();
    }

    @Override
    public Token generateAndSaveTokensForUser(final UserInformation userInfo) throws CannotStoreTokenException {

        // store access token / refresh token in database
        final Token newUserToken = storeTokensForUser(userInfo.getUser(), userInfo.getAuthority(), uuidService.generateUrlSafeUUID(),
                uuidService.generateUrlSafeUUID());

        cleanupExpiredUserTokens(userInfo.getUser());
        return newUserToken;
    }

    @Override
    public UserInformation findUserByRefreshToken(final String refreshToken) {

        if (StringUtils.isEmpty(refreshToken)) {
            LOG.error("Cannot search for user by empty refresh token");
            return null;
        }

        final Token foundToken = tokenRepository.findByRefreshToken(refreshToken);
        final UserInformation result = new UserInformation(foundToken);
        if (foundToken == null) {
            LOG.debug("Refresh token '{}' not found in database", refreshToken);
            return result;
        }

        // note: due to DB constraints, user must be valid if token is valid
        LOG.debug("Found refresh token '{}', belongs to user '{}'", refreshToken, foundToken.getUser().getLogin());

        if (TokenHandler.isRefreshTokenExpired(foundToken)) {
            LOG.info("Found refresh token '{}' is already expired", refreshToken);
        }
        return result;
    }

    @Override
    public UserInformation findUserByAccessToken(final String accessToken) {

        if (StringUtils.isEmpty(accessToken)) {
            LOG.error("Cannot search for user by empty access token");
            return null;
        }

        final Token foundToken = tokenRepository.findByAccessToken(accessToken);
        final UserInformation result = new UserInformation(foundToken);
        if (foundToken == null) {
            LOG.debug("Access token '{}' not found in database", accessToken);
            return result;
        }

        // note: due to DB constraints, user must be valid if token is valid
        LOG.debug("Found access token '{}', belongs to user '{}'", accessToken, foundToken.getUser().getLogin());

        if (TokenHandler.isAccessTokenExpired(foundToken)) {
            LOG.debug("Found access token '{}' is already expired", accessToken);
        }
        return result;
    }

    /**
     * persistence of a set of access and refresh token for a user 
     * note: creates the user if it does not exist yet
     * 
     * @param user the {@link User} for whom to store tokens
     * @param authority the authority to which the token is bound
     * @param accessToken the access token to be stored
     * @param refreshToken the refresh token to be stored
     * 
     * @return {@link Token} with updated properties
     *
     * @throws CannotStoreTokenException exception thrown when storing the token fails or when required data is missing
     */
    private Token storeTokensForUser(final User user, final String authority, final String accessToken, final String refreshToken) throws CannotStoreTokenException {

        if (user == null) {
            throw new CannotStoreTokenException("No user available");
        }
        if (StringUtils.isEmpty(authority)) {
            throw new CannotStoreTokenException("Authority for which the token is issued is missing!");
        }
        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) {
            throw new CannotStoreTokenException("Access and/or refresh token to be stored is/are missing!");
        }

        final Token newToken = new Token();
        TokenHandler.setAccessToken(newToken, accessToken, lifetimeAccessToken);
        TokenHandler.setRefreshToken(newToken, refreshToken, lifetimeRefreshToken);
        newToken.setUser(user);
        newToken.setAuthority(authority);

        try {
            tokenRepository.save(newToken);
        } catch (Exception e) {
            LOG.error("Error storing tokens for user", e);
            throw new CannotStoreTokenException("Error saving tokens", e);
        }

        return newToken;
    }

    @Override
    public boolean cleanupExpiredUserTokens(final User user) {

        if (user == null) {
            LOG.warn("Received invalid user for database token cleanup");
            return false;
        }

        boolean cleanedSomething = false;

        try {
            final List<Token> expiredAccessTokens = tokenRepository.findByUserAndAccessTokenExpiresLessThanEqualAndRefreshTokenExpiresLessThanEqual(user,
                    LocalDateTime.now(), LocalDateTime.now());
            if (!expiredAccessTokens.isEmpty()) {
                LOG.debug("Discovered {} expired access tokens for user '{}'; delete them", expiredAccessTokens.size(), user.getLogin());
                tokenRepository.delete(expiredAccessTokens);
                cleanedSomething = true;
            }
        } catch (Exception e) {
            LOG.error("Unexpected error upon cleaning expired access tokens", e);
        }

        return cleanedSomething;
    }

    /**
     * read the database content and (re)initialise the internal list of clients
     * @throws NoClientsAvailableException 
     */
    private void refreshClientList() throws NoClientsAvailableException {

        clients = new ArrayList<>();

        final List<AuthClient> clientsInDb = (List<AuthClient>) authClientRepos.findAll();
        if (clientsInDb.isEmpty()) {
            throw new NoClientsAvailableException();
        }
        for (final AuthClient cl : clientsInDb) {

            try {
                final Algorithm alg = Algorithm.HMAC256(cl.getSecret());

                // any more things to verify (i.e. claims)?
                final JWTVerifier jwtVerifier = JWT.require(alg)
                        .withIssuer(cl.getClientId())
                        .acceptLeeway(0) // no grace period for timing issues, fail immediately
                        .acceptNotBefore(0)
                        .acceptIssuedAt(0)
                        .acceptExpiresAt(0)
                        .build(); // Reusable verifier instance

                clients.add(new RegisteredClient(cl, alg, jwtVerifier));
            } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                // note: initialisation failure of one client should not block other clients as well - therefore we don't throw the exception!
                LOG.error("Could not initialize JWT verification for client " + cl.getId(), e);
            }
        }
    }

}
