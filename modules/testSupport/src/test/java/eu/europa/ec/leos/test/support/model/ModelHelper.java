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
package eu.europa.ec.leos.test.support.model;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import io.atlassian.fugue.Option;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModelHelper {

    public static User buildUser(Long id, String login, String name){
        return new User(id, login, name, null, null, null);
    }
    public static User buildUser(Long id, String login, String name, List<Entity> entities){
        return new User(id, login, name, entities, null, null);
    }

    public static Bill createBillForBytes(byte[] xmlBytes) {
        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        String docId = "1";
        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        when(source.getBytes()).thenReturn(xmlBytes);
        when(content.getSource()).thenReturn(source);

        Bill bill = new Bill(docId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                "", "", "", "", VersionType.MINOR, true,
                "title", collaborators, Arrays.asList(""),
                Option.some(content), Option.some(billMetadata));
        return bill;
    }
}
