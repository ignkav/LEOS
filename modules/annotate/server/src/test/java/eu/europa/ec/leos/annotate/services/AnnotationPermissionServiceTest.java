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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.services.impl.AnnotationPermissionServiceImpl;
import eu.europa.ec.leos.annotate.services.impl.UserServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationPermissionServiceTest {

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @InjectMocks
    private AnnotationPermissionServiceImpl annotPermMockService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private GroupService groupService;

    // -------------------------------------
    // Tests
    // -------------------------------------

    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    public void testHasPermissionToUpdate_AnnotationNull() throws Exception {

        final Annotation annot = null;
        final UserInformation userinfo = new UserInformation("itsme", null, Authorities.EdiT);
        annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, userinfo);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    public void testHasPermissionToUpdate_UserinfoNull() throws Exception {

        final Annotation annot = new Annotation();
        final UserInformation userinfo = null;

        annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, userinfo);
    }

    @Test
    public void testHasPermissionToUpdate_OtherMetadata_EditUser() throws Exception {

        final String LOGIN = "theuser";

        final User user = new User(LOGIN);
        user.setId(Long.valueOf(2));
        Mockito.when(userService.createUser(LOGIN, null)).thenReturn(user);
        Mockito.when(userService.findByLoginAndContext(LOGIN, null)).thenReturn(user);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT, it is the user's annotation -> it can be updated
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(annot.getUser().getLogin(), null, Authorities.EdiT)));
    }

    // user wanting to update is not the annotation's creator -> refused
    @Test
    public void testHasPermissionToUpdate_OtherMetadata_OtherEditUser() throws Exception {

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(new User("theuser"));

        String login = "nottheuser";
        User user = new User(login);
        user.setId(1L); // Use a different ID as the annotation's creator ID is 0.
        Mockito.when(userService.findByLoginAndContext(login, null)).thenReturn(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> ok, but user is not the annotation's creator
        Assert.assertFalse(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(login, null, Authorities.EdiT)));
    }

    /**
     * user wanting to update is:
     *  - from same group
     *  - not the annotation's creator
     *  - annotation is from CONTRIBUTION
     *  -> allowed
      */
    @Test
    public void testHasPermissionToUpdate_OtherMetadata_OtherIscUser_ContributionAnnot() throws Exception {

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        //Test also ignoreCase comparison
        MetadataHandler.setOriginMode(meta, "pRiVaTe");
        annot.setMetadata(meta);
        annot.setUser(new User("theuser"));

        String login = "nottheuser";
        User user = new User(login);
        user.setId(1L); // Use a different ID as the annotation's creator ID is 0.
        Mockito.when(userService.findByLoginAndContext(login, null)).thenReturn(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> ok, user is not the annotation's creator but annotation is from CONTRIBUTION
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(login, null, Authorities.ISC)));
    }

    /**
     * user wanting to update is:
     *  - from same group
     *  - not the annotation's creator
     *  - annotation is NOT from CONTRIBUTION
     *  -> refused
     */
    @Test
    public void testHasPermissionToUpdate_OtherMetadata_OtherIscUser_NOTContributionAnnot() throws Exception {

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(new User("theuser"));

        String login = "nottheuser";
        User user = new User(login);
        user.setId(1L); // Use a different ID as the annotation's creator ID is 0.
        Mockito.when(userService.findByLoginAndContext(login, null)).thenReturn(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> ok, user is not the annotation's creator but annotation is from CONTRIBUTION
        Assert.assertFalse(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(login, null, Authorities.ISC)));
    }

    @Test
    public void testHasPermissionToUpdate_OtherMetadata_IscUser() throws Exception {

        final String LOGIN = "dave";
        final User user = new User(LOGIN);
        user.setId(Long.valueOf(8));

        Mockito.when(userService.findByLoginAndContext(LOGIN, null)).thenReturn(user);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(user);

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        // verify: status is not SENT -> it can be updated
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(annot.getUser().getLogin(), null, Authorities.ISC)));
    }

    @Test
    public void testHasPermissionToUpdateNotSuccessful_Sent_EditUser() throws Exception {

        String login = "a";
        User user = new User(login);
        Mockito.when(userService.findByLoginAndContext(login, null)).thenReturn(user);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        annot.setMetadata(meta);
        annot.setUser(user);

        meta.setResponseStatus(ResponseStatus.SENT);

        // verify: status is SENT -> it cannot be updated any more
        Assert.assertFalse(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(annot.getUser().getLogin(), null, Authorities.EdiT)));
    }

    // an ISC user wants to update a SENT annotation; he belongs to the same group as the annotation
    @Test
    public void testHasPermissionToUpdateSuccessful_IscUserOfSameGroup() throws Exception {

        final String LOGIN_ANNOT = "annotUser";
        final String LOGIN_OTHER = "other";

        final User annotUser = new User(LOGIN_ANNOT);
        final User otherUser = new User(LOGIN_OTHER);
        final Group group = new Group("mygroup", true);

        Mockito.when(userService.findByLoginAndContext(LOGIN_ANNOT, null)).thenReturn(annotUser);
        Mockito.when(userService.findByLoginAndContext(LOGIN_OTHER, null)).thenReturn(otherUser);
        Mockito.when(groupService.isUserMemberOfGroup(annotUser, group)).thenReturn(true);
        Mockito.when(groupService.isUserMemberOfGroup(otherUser, group)).thenReturn(true);

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        meta.setGroup(group);
        annot.setMetadata(meta);
        annot.setUser(annotUser);
        
        meta.setResponseStatus(ResponseStatus.SENT);

        // verify: status is SENT, but user belongs to same group -> it can be updated
        Assert.assertTrue(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(otherUser, Authorities.ISC)));
    }

    // an ISC user wants to update a SENT annotation; he does not belong to the same group as the annotation
    @Test
    public void testHasPermissionToUpdateSuccessful_IscUserOfOtherGroup() throws Exception {

        final String LOGIN_ANNOT = "annotUser";
        final String LOGIN_OTHER = "other";

        final User annotUser = new User(LOGIN_ANNOT);
        final User otherUser = new User(LOGIN_OTHER);
        final Group group = new Group("mygroup", true);

        Mockito.when(userService.findByLoginAndContext(LOGIN_ANNOT, null)).thenReturn(annotUser);
        Mockito.when(userService.findByLoginAndContext(LOGIN_OTHER, null)).thenReturn(otherUser);
        Mockito.when(groupService.isUserMemberOfGroup(annotUser, group)).thenReturn(true);
        Mockito.when(groupService.isUserMemberOfGroup(otherUser, group)).thenReturn(false); //

        final Annotation annot = new Annotation();
        final Metadata meta = new Metadata();
        meta.setGroup(group);
        annot.setMetadata(meta);
        annot.setUser(annotUser);

        meta.setResponseStatus(ResponseStatus.SENT);

        // verify: status is SENT, but user does not belong to same group -> it cannot be updated
        Assert.assertFalse(annotPermMockService.hasUserPermissionToUpdateAnnotation(annot, new UserInformation(otherUser, Authorities.ISC)));
    }

}
