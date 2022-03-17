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

import java.time.LocalDateTime;

import eu.europa.ec.leos.annotate.model.entity.Token;

/**
 * Helper functions for {@link Token} entities
 */
public final class TokenHandler {

    private TokenHandler() {
        // Prevent instantiation as all methods are static.
    }

    public static boolean isAccessTokenExpired(final Token token) {
        return token.getAccessTokenExpires().isBefore(LocalDateTime.now());
    }

    public static boolean isRefreshTokenExpired(final Token token) {
        return token.getRefreshTokenExpires().isBefore(LocalDateTime.now());
    }

    public static void setAccessToken(final Token token, final String accessToken, final int lifetimeSeconds) {
        token.setAccessToken(accessToken);
        token.setAccessTokenLifetimeSeconds(lifetimeSeconds);

        token.setAccessTokenExpires(LocalDateTime.now().plusSeconds(lifetimeSeconds));
    }

    public static void setRefreshToken(final Token token, final String refreshToken, final int lifetimeSeconds) {
        token.setRefreshToken(refreshToken);
        token.setRefreshTokenLifetimeSeconds(lifetimeSeconds);

        token.setRefreshTokenExpires(LocalDateTime.now().plusSeconds(lifetimeSeconds));
    }

}
