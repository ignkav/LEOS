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

import org.junit.Assert;
import org.junit.Test;

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.model.UserInformation;

public class EntityCheckerTest {

    // test that user is not considered belonging to same entity if no information is available at all
    @Test
    public void testIsResponseFromUsersEntity_RespIdNull() throws Exception {

        Assert.assertFalse(EntityChecker.isResponseFromUsersEntity(null, null));
    }

    // test that user is not considered belonging to same entity if no information is available at all
    @Test
    public void testIsResponseFromUsersEntity_RespIdEmpty() throws Exception {

        Assert.assertFalse(EntityChecker.isResponseFromUsersEntity(null, null));
    }

    // test that user is not considered belonging to same entity if no information is available at all
    @Test
    public void testIsResponseFromUsersEntity_UserinfoNull() throws Exception {

        Assert.assertFalse(EntityChecker.isResponseFromUsersEntity(null, "SG"));
    }

    // test that user is not considered belonging to same entity if no information is available about user's connected entity
    @Test
    public void testIsResponseFromUsersEntity_UserinfoConnectedEntityEmpty() throws Exception {

        final UserInformation userInfo = new UserInformation("me", null, Authorities.ISC);
        Assert.assertFalse(EntityChecker.isResponseFromUsersEntity(userInfo, "SG"));
    }

    // test that user is not considered belonging to same entity if user's connected entity is different
    @Test
    public void testIsResponseFromUsersEntity_NotEquals() throws Exception {

        final UserInformation userInfo = new UserInformation("me", null, Authorities.ISC);
        userInfo.setConnectedEntity("DIGIT");
        Assert.assertFalse(EntityChecker.isResponseFromUsersEntity(userInfo, "AGRI"));
    }

    // test that user is considered belonging to same entity if user's connected entity is identical
    @Test
    public void testIsResponseFromUsersEntity_Identical() throws Exception {

        final UserInformation userInfo = new UserInformation("me", null, Authorities.ISC);
        userInfo.setConnectedEntity("SJ");
        Assert.assertTrue(EntityChecker.isResponseFromUsersEntity(userInfo, "SJ"));
    }

}
