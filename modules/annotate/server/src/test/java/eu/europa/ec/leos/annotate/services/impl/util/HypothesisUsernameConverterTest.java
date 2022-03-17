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
import org.junit.Assert;
import org.junit.Test;

public class HypothesisUsernameConverterTest {

    /**
     * tests extraction of user account from hypothesis-wrapped user account
     */
    @Test
    public void testHypothesisAccountToUsername() {

        final String hypoUsername = "acct:user@domain.eu";
        final String expectedUsername = "user@domain.eu";

        Assert.assertEquals(expectedUsername, HypothesisUsernameConverter.getUsernameFromHypothesisUserAccount(hypoUsername));
    }

    /**
     * tests extraction of user account from non-hypothesis-wrapped user account
     */
    @Test
    public void testHypothesisAccountToUsername_InvalidHypothesisAccount() {

        // test with string not matching default hypothes.is format
        final String hypoUsername = "theacct:user@domain.eu";
        Assert.assertNull(HypothesisUsernameConverter.getUsernameFromHypothesisUserAccount(hypoUsername));

        // test with empty string
        Assert.assertNull(HypothesisUsernameConverter.getUsernameFromHypothesisUserAccount(""));
    }

    /**
     * test wrapping of user name to hypothesis user account
     */
    @Test
    public void testUsernameToHypoUserAccount() {

        final String userId = "theuserId12";
        final String expectedHypoUsername = "acct:" + userId;

        Assert.assertEquals(expectedHypoUsername, HypothesisUsernameConverter.getHypothesisUserAccountFromUsername(userId));
    }

    /**
     * test wrapping of user name to hypothesis user account - with empty user
     */
    @Test
    public void testEmptyUsernameToHypoUserAccount() {

        // test with empty input
        Assert.assertEquals("", HypothesisUsernameConverter.getHypothesisUserAccountFromUsername(""));
    }

    /**
     * test that determining hypothesis user account returns empty string when no user is supplied 
     */
    @Test
    public void testGetHypothesisUserAccountFromUser_UserNull() {

        Assert.assertEquals("", HypothesisUsernameConverter.getHypothesisUserAccountFromUser(null, null));
    }

    /**
     * test that determining hypothesis user account returns empty string when user is incomplete 
     */
    @Test
    public void testGetHypothesisUserAccountFromUser_UserDetailsIncomplete() {

        final String login = "thelogin";
        final User user = new User(login);

        Assert.assertEquals("", HypothesisUsernameConverter.getHypothesisUserAccountFromUser(user, ""));
    }

}
