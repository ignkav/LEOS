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

import eu.europa.ec.leos.annotate.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Locale;

public final class HypothesisUsernameConverter {

    private static final Logger LOG = LoggerFactory.getLogger(HypothesisUsernameConverter.class);

    private final static String HYPOTHESIS_ACCOUNT_PREFIX = "acct:";

    private HypothesisUsernameConverter() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * extract the user id from the content received in JSON (e.g.: acct:xy@domain.eu)
     *
     * @param hypoClientUser the account data as received by hypothesis client
     * @return extracted user id
     */
    public static String getUsernameFromHypothesisUserAccount(final String hypoClientUser) {

        if (StringUtils.isEmpty(hypoClientUser)) {
            return null;
        }

        // the hypothesis client provide the user name as follows:
        // "user": "acct:sela83@hypothes.is"
        // i.e. "acct:" + <user id> + "@" + <authority>
        if (!hypoClientUser.toLowerCase(Locale.ENGLISH).startsWith(HYPOTHESIS_ACCOUNT_PREFIX) || hypoClientUser.length() <= 5) {

            LOG.error("Given user name (" + hypoClientUser + ") is not in expected format");
            // expected format not present
            return null;
        }

        return hypoClientUser.substring(HYPOTHESIS_ACCOUNT_PREFIX.length());
    }

    /**
     * wrap a user name in the format expected by hypothesis client
     *
     * @param username the user name to be converted
     * @return converted user name
     */
    public static String getHypothesisUserAccountFromUsername(final String username) {

        if (StringUtils.isEmpty(username)) {
            return "";
        }

        return HYPOTHESIS_ACCOUNT_PREFIX + username;
    }

    /**
     * retrieve the user login of a user and convert it for hypothes.is format
     *
     * @param user the user object for which to retrieve hypothes.is user id format
     * @param authority the authority of the user
     * @return converted user login
     */
    public static String getHypothesisUserAccountFromUser(final User user, final String authority) {

        if (user == null) {
            LOG.error("Cannot determine annotate/hypothesis user account: no user given!");
            return "";
        }
        if (StringUtils.isEmpty(authority)) {
            LOG.error("Cannot determine annotate/hypothesis user account: no authority given!");
            return "";
        }

        final String username = user.getLogin() + "@" + authority;
        return getHypothesisUserAccountFromUsername(username);
    }

}
