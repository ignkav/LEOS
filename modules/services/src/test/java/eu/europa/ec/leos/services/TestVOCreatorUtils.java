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
package eu.europa.ec.leos.services;

import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;

import java.util.ArrayList;
import java.util.List;

public class TestVOCreatorUtils {

    public static User getJohnTestUser() {
        return getTestUser("John", "SMITH", "smithj", "smithj@test.com");
    }

    public static User getJaneTestUser() {
        return getTestUser("jane", "demo", "jane", "jane@test.com");
    }

    public static User getTestUser(String firstName, String lastName, String userLogin, String userEmail) {
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "EXT.A1", "Ext"));
        List<String> roles = new ArrayList<String>();
        roles.add("ADMIN");
        User user1 = new User(1l, userLogin, lastName + " " + firstName, entities, userEmail, roles);
        return user1;
    }
}
