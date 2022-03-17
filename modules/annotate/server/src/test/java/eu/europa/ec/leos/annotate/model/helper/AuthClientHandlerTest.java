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

import eu.europa.ec.leos.annotate.model.entity.AuthClient;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class contains tests for functions operating on {@link AuthClient} entities in the {@link AuthClientHandler}
 */
public class AuthClientHandlerTest {

    @Test
    public void testGetAuthoritiesList() {

        final AuthClient client = new AuthClient();

        client.setAuthorities("a");
        Assert.assertEquals(Arrays.asList("a"), AuthClientHandler.getAuthoritiesList(client));

        client.setAuthorities("a;b");
        Assert.assertEquals(Arrays.asList("a", "b"), AuthClientHandler.getAuthoritiesList(client));

        client.setAuthorities("a;b;c");
        Assert.assertEquals(Arrays.asList("a", "b", "c"), AuthClientHandler.getAuthoritiesList(client));
    }

    @Test
    public void testGetAuthoritiesList_Empty() {

        final AuthClient client = new AuthClient();

        Assert.assertNull(AuthClientHandler.getAuthoritiesList(client));

        client.setAuthorities("");
        Assert.assertNull(AuthClientHandler.getAuthoritiesList(client));
    }

}
