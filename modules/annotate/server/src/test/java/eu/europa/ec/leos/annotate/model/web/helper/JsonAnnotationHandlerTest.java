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
package eu.europa.ec.leos.annotate.model.web.helper;

import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationDocument;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * simple tests for helper functions of {@link JsonAnnotation} class
 */
public class JsonAnnotationHandlerTest {

    @Test
    public void testGetRootAnnotationId() {

        final String ROOT_ID = "rootId";
        final String REPLY_ID = "firstReplyId";

        final JsonAnnotation jsAnnot = new JsonAnnotation();
        jsAnnot.setReferences(null);

        Assert.assertNull(JsonAnnotationHandler.getRootAnnotationId(jsAnnot));

        jsAnnot.setReferences(new ArrayList<String>());
        Assert.assertNull(JsonAnnotationHandler.getRootAnnotationId(jsAnnot));

        jsAnnot.setReferences(Arrays.asList(ROOT_ID));
        Assert.assertEquals(ROOT_ID, JsonAnnotationHandler.getRootAnnotationId(jsAnnot));

        jsAnnot.setReferences(Arrays.asList(ROOT_ID, REPLY_ID));
        Assert.assertEquals(ROOT_ID, JsonAnnotationHandler.getRootAnnotationId(jsAnnot));
    }

    @Test
    public void testHasMetadata() {
        
        final JsonAnnotation jsAnnot = new JsonAnnotation();
        Assert.assertFalse(JsonAnnotationHandler.hasMetadata(jsAnnot));
        
        final JsonAnnotationDocument jsDoc = new JsonAnnotationDocument();
        jsAnnot.setDocument(jsDoc);
        Assert.assertFalse(JsonAnnotationHandler.hasMetadata(jsAnnot));
        
        // finally initialize the metadata, now it should say {@literal true}
        jsDoc.setMetadata(new SimpleMetadata());
        Assert.assertTrue(JsonAnnotationHandler.hasMetadata(jsAnnot));
    }

    // test standard case for a public annotation
    @Test
    public void testIsNoPrivateAnnotation() throws Exception {

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject("acct:user@domain");
        Assert.assertFalse(JsonAnnotationHandler.isPrivateAnnotation(jsAnnot));
    }

    // test standard case for a private annotation
    @Test
    public void testIsPrivateAnnotation() throws Exception {

        final String user = "acct:user@domain";

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(user);

        final List<String> userPermission = new ArrayList<String>();
        userPermission.add(user);
        jsAnnot.getPermissions().setRead(userPermission);

        Assert.assertTrue(JsonAnnotationHandler.isPrivateAnnotation(jsAnnot));
    }

    // test that the method for checking a private annotation returns {@literal false} on all other code paths
    @Test
    public void testIsPrivateAnnotationOnIncompleteAnnotation() throws Exception {

        Assert.assertFalse(JsonAnnotationHandler.isPrivateAnnotation(null));

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject("acct:user@domain");

        jsAnnot.getPermissions().setRead(null);
        Assert.assertFalse(JsonAnnotationHandler.isPrivateAnnotation(jsAnnot));

        jsAnnot.getPermissions().setRead(new ArrayList<String>());
        Assert.assertFalse(JsonAnnotationHandler.isPrivateAnnotation(jsAnnot));

        final List<String> tooManyReadPermissions = new ArrayList<String>();
        tooManyReadPermissions.add("firstuser");
        tooManyReadPermissions.add("anotherUser");
        jsAnnot.getPermissions().setRead(tooManyReadPermissions);

        Assert.assertFalse(JsonAnnotationHandler.isPrivateAnnotation(jsAnnot));
    }

    @Test
    public void testIsReply() throws Exception {

        final JsonAnnotation jsAnnot = TestData.getTestReplyToAnnotation("username", new URI("uri"), Arrays.asList("parentId"));
        Assert.assertTrue(JsonAnnotationHandler.isReply(jsAnnot));
    }

    @Test
    public void testIsNoReply() throws Exception {

        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject("username");
        Assert.assertFalse(JsonAnnotationHandler.isReply(jsAnnot));
    }

}
