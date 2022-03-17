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
package eu.europa.ec.leos.annotate;

import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.helper.AnnotationReferencesHandler;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationDocument;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.exceptions.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
public class SuggestionJustificationTest {

    private static final String UNEXPECTED_EXCEPTION = "Unexpected exception received: ";

    private Group defaultGroup;

    @Autowired
    private AnnotationService annotService;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private MetadataRepository metadataRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Before
    public void cleanDatabaseBeforeTests() {

        TestDbHelper.cleanupRepositories(this);

        defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);
    }

    @After
    public void cleanDatabaseAfterTests() {

        TestDbHelper.cleanupRepositories(this);
    }

    @Test
    public void testInsertSuggestionAndTwoRepliesInEdiTContext() throws URISyntaxException {

        final UserInformation userInfo = createUserWithAuthority(Authorities.EdiT);
        final String hypothesisUsername = getHypothesisUsername(userInfo);

        final Annotation suggestion = createRootSuggestion(userInfo);

        final JsonAnnotation jsReply1 = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(suggestion.getDocument().getUri()),
                Collections.singletonList(suggestion.getId()));
        Annotation reply1 = null;
        try {
            reply1 = annotService.createAnnotation(jsReply1, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }

        final Annotation reply1Retrieved = annotService.findAnnotationById(reply1.getId());
        Assert.assertNotNull(reply1Retrieved);
        Assert.assertEquals(suggestion.getId(), reply1Retrieved.getReferences());
        Assert.assertEquals(suggestion.getId(), reply1Retrieved.getRootAnnotationId());
        Assert.assertTrue(AnnotationReferencesHandler.isReply(reply1Retrieved));

        final JsonAnnotation jsReply2 = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(suggestion.getDocument().getUri()),
                Collections.singletonList(suggestion.getId()));
        Annotation reply2 = null;
        try {
            reply2 = annotService.createAnnotation(jsReply2, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }

        final Annotation reply2Retrieved = annotService.findAnnotationById(reply2.getId());
        Assert.assertNotNull(reply2Retrieved);
        Assert.assertEquals(suggestion.getId(), reply2Retrieved.getReferences());
        Assert.assertEquals(suggestion.getId(), reply2Retrieved.getRootAnnotationId());
        Assert.assertTrue(AnnotationReferencesHandler.isReply(reply2Retrieved));
    }

    // a justification is created in ISC
    @Test
    public void testInsertSuggestionAndReplyInIscContext() throws URISyntaxException {

        final UserInformation userInfo = createUserWithAuthority(Authorities.ISC);
        final Annotation suggestion = createRootSuggestionIsc(userInfo);
        final Annotation justification = createReplyToAnnotation(suggestion, userInfo);

        // make sure that the justification (ISC "reply" to suggestion) has the same metadata
        // by verifying that there is only a single metadata entry in the database
        Assert.assertEquals(1, metadataRepos.count());
        Assert.assertEquals(suggestion.getMetadataId(), justification.getMetadataId());
    }
    
    @Test
    public void testInsertSuggestionAndSecondReplyNotAllowedInIscContext() throws URISyntaxException {

        final UserInformation userInfo = createUserWithAuthority(Authorities.ISC);
        final Annotation suggestion = createRootSuggestion(userInfo);
        createReplyToAnnotation(suggestion, userInfo);

        final String hypothesisUsername = getHypothesisUsername(userInfo);
        final JsonAnnotation jsReply2 = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(suggestion.getDocument().getUri()),
                Collections.singletonList(suggestion.getId()));
        Assert.assertThrows("A second reply is not allowed for a suggestion in the ISC context", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(jsReply2, userInfo));
    }

    @Test
    public void testInsertSuggestionAndReplyToReplyNotAllowedInIscContext() throws URISyntaxException {

        final UserInformation userInfo = createUserWithAuthority(Authorities.ISC);
        final Annotation suggestion = createRootSuggestion(userInfo);
        final Annotation reply = createReplyToAnnotation(suggestion, userInfo);

        final String hypothesisUsername = getHypothesisUsername(userInfo);
        final ArrayList<String> replyToReplyReferences = new ArrayList<>();
        replyToReplyReferences.add(suggestion.getId());
        replyToReplyReferences.add(reply.getId());
        final JsonAnnotation jsReplyToReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(suggestion.getDocument().getUri()),
                replyToReplyReferences);
        Assert.assertThrows("A reply to a reply is not allowed for a suggestion in the ISC context", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(jsReplyToReply, userInfo));
    }

    @Test
    public void testInsertSuggestionAndReplyToReplyNotAllowedInEdiTContext() throws URISyntaxException {

        final UserInformation userInfo = createUserWithAuthority(Authorities.EdiT);
        final Annotation suggestion = createRootSuggestion(userInfo);
        final Annotation reply = createReplyToAnnotation(suggestion, userInfo);

        final String hypothesisUsername = getHypothesisUsername(userInfo);
        final ArrayList<String> replyToReplyReferences = new ArrayList<>();
        replyToReplyReferences.add(suggestion.getId());
        replyToReplyReferences.add(reply.getId());
        final JsonAnnotation jsReplyToReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(suggestion.getDocument().getUri()),
                replyToReplyReferences);
        Assert.assertThrows("A reply to a reply is not allowed for a suggestion in the EdiT context.", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(jsReplyToReply, userInfo));
    }

    @Test
    public void testDisplayJustificationToDifferentGroupInEdiTContext() throws URISyntaxException, MissingPermissionException {

        final UserInformation authorUserInfo = createUserWithAuthority(Authorities.ISC);
        final String authorHypothesisUsername = getHypothesisUsername(authorUserInfo);

        final Annotation suggestion = createRootSuggestion(authorUserInfo);

        final JsonAnnotation jsJustification = TestData.getTestReplyToAnnotation(authorHypothesisUsername, new URI(suggestion.getDocument().getUri()),
                Collections.singletonList(suggestion.getId()));
        final Annotation justification = createAnnotation(jsJustification, authorUserInfo, null);

        setAnnotationResponseStatusToSent(justification);

        final String viewerAuthority = Authorities.EdiT;
        final User viewer = new User("b", viewerAuthority);
        userRepos.save(viewer);
        userGroupRepos.save(new UserGroup(viewer.getId(), defaultGroup.getId()));

        final Annotation justificationRetrievedAsViewer = annotService.findAnnotationById(justification.getId(), viewer.getLogin(), viewerAuthority);
        Assert.assertNotNull(justificationRetrievedAsViewer);
        Assert.assertEquals(justification.getId(), justificationRetrievedAsViewer.getId());
    }

    @Test
    public void testEditSentReplyToSuggestionByDifferentGroupNotAllowedInEdiTContext() throws URISyntaxException {

        final UserInformationAndGroup userOfGroupA = createUserWithGroupAndAuthority("a", "A", Authorities.EdiT);
        final String hypothesisUsernameA = getHypothesisUsername(userOfGroupA.userInfo);
        final Annotation suggestionByUserOfGroupA = createRootSuggestion(userOfGroupA.userInfo, userOfGroupA.group);
        final JsonAnnotation jsJustificationByUserOfGroupA = TestData.getTestReplyToAnnotation(hypothesisUsernameA,
                new URI(suggestionByUserOfGroupA.getDocument().getUri()), Collections.singletonList(suggestionByUserOfGroupA.getId()));
        final Annotation justificationByUserOfGroupA = createAnnotation(jsJustificationByUserOfGroupA, userOfGroupA.userInfo, userOfGroupA.group);
        setAnnotationResponseStatusToSent(justificationByUserOfGroupA);
        final UserInformationAndGroup userOfGroupB = createUserWithGroupAndAuthority("b", "B", Authorities.EdiT);

        Assert.assertThrows("Editing a reply to a suggestion by a foreign group is not allowed in EdiT context.", CannotUpdateSentAnnotationException.class,
                () -> annotService.updateAnnotation(justificationByUserOfGroupA.getId(), jsJustificationByUserOfGroupA, userOfGroupB.userInfo));
    }

    @Test
    public void testInsertSuggestionAndReplyByDifferentGroupNotAllowed() throws URISyntaxException {

        final UserInformationAndGroup userOfGroupA = createUserWithGroupAndAuthority("a", "A", Authorities.EdiT);
        final Annotation suggestionByUserOfGroupA = createRootSuggestion(userOfGroupA.userInfo, userOfGroupA.group);
        final UserInformationAndGroup userOfGroupB = createUserWithGroupAndAuthority("b", "B", Authorities.EdiT);

        final String hypothesisUsernameB = getHypothesisUsername(userOfGroupB.userInfo);
        final JsonAnnotation jsReplyByUserOfGroupB = TestData.getTestReplyToAnnotation(hypothesisUsernameB,
                new URI(suggestionByUserOfGroupA.getDocument().getUri()), Collections.singletonList(suggestionByUserOfGroupA.getId()));
        jsReplyByUserOfGroupB.setGroup(userOfGroupB.group.getName());
        Assert.assertThrows("Replying to a suggestion from a foreign group is not allowed.", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(jsReplyByUserOfGroupB, userOfGroupB.userInfo));
    }

    @Test
    public void testDeleteSentReplyToSuggestionBySameGroupInEdiTContext() throws URISyntaxException {

        final UserInformationAndGroup user1 = createUserWithGroupAndAuthority("a", "A", Authorities.EdiT);
        final Annotation suggestionByUser1 = createRootSuggestion(user1.userInfo, user1.group);
        final Annotation justificationByUser1 = createReplyToAnnotation(suggestionByUser1, user1.userInfo, user1.group);
        setAnnotationResponseStatusToSent(justificationByUser1);
        final UserInformationAndGroup user2 = createUserWithGroupAndAuthority("b", "A", Authorities.EdiT);

        try {
            annotService.deleteAnnotationById(justificationByUser1.getId(), user2.userInfo);
        } catch (CannotDeleteAnnotationException | CannotDeleteSentAnnotationException e) {
            Assert.fail("Deleting a reply to suggestion by the same group is allowed in EdiT context. " + e);
        }
    }

    @Test
    public void testDeleteSentReplyToSuggestionBySameGroupInIscContext() throws URISyntaxException {

        final UserInformationAndGroup user1 = createUserWithGroupAndAuthority("a", "A", Authorities.ISC);
        final Annotation suggestionByUser1 = createRootSuggestion(user1.userInfo, user1.group);
        final Annotation justificationByUser1 = createReplyToAnnotation(suggestionByUser1, user1.userInfo, user1.group);
        setAnnotationResponseStatusToSent(justificationByUser1);
        final UserInformationAndGroup user2 = createUserWithGroupAndAuthority("b", "A", Authorities.ISC);

        try {
            annotService.deleteAnnotationById(justificationByUser1.getId(), user2.userInfo);
        } catch (CannotDeleteAnnotationException | CannotDeleteSentAnnotationException e) {
            Assert.fail("Deleting a reply to suggestion by the same group is allowed in ISC context. " + e);
        }
    }

    @Test
    public void testDeleteSentReplyToSuggestionByDifferentGroupNotAllowedInEdiTContext() throws URISyntaxException {

        final UserInformationAndGroup userOfGroupA = createUserWithGroupAndAuthority("a", "A", Authorities.EdiT);
        final Annotation suggestionByUserOfGroupA = createRootSuggestion(userOfGroupA.userInfo, userOfGroupA.group);
        final Annotation justificationByUserOfGroupA = createReplyToAnnotation(suggestionByUserOfGroupA, userOfGroupA.userInfo, userOfGroupA.group);
        setAnnotationResponseStatusToSent(justificationByUserOfGroupA);
        final UserInformationAndGroup userOfGroupB = createUserWithGroupAndAuthority("b", "B", Authorities.EdiT);

        Assert.assertThrows("Deleting a reply to a suggestion by a foreign group is not allowed in EdiT context.", CannotDeleteSentAnnotationException.class,
                () -> annotService.deleteAnnotationById(justificationByUserOfGroupA.getId(), userOfGroupB.userInfo));
    }

    @Test
    public void testDeleteSentReplyToSuggestionByDifferentGroupNotAllowedInIscContext() throws URISyntaxException {

        final UserInformationAndGroup userOfGroupA = createUserWithGroupAndAuthority("a", "A", Authorities.ISC);
        final Annotation suggestionByUserOfGroupA = createRootSuggestion(userOfGroupA.userInfo, userOfGroupA.group);
        final Annotation justificationByUserOfGroupA = createReplyToAnnotation(suggestionByUserOfGroupA, userOfGroupA.userInfo, userOfGroupA.group);
        setAnnotationResponseStatusToSent(justificationByUserOfGroupA);
        final UserInformationAndGroup userOfGroupB = createUserWithGroupAndAuthority("b", "B", Authorities.ISC);

        Assert.assertThrows("Deleting a reply to a suggestion by a foreign group is not allowed in ISC context.", CannotDeleteSentAnnotationException.class,
                () -> annotService.deleteAnnotationById(justificationByUserOfGroupA.getId(), userOfGroupB.userInfo));
    }

    @Test
    public void testEditSentReplyToSuggestionBySameGroupInIscContext() throws URISyntaxException {

        final UserInformationAndGroup user1 = createUserWithGroupAndAuthority("a", "A", Authorities.ISC);
        final String hypothesisUsername1 = getHypothesisUsername(user1.userInfo);
        final Annotation suggestionByUser1 = createRootSuggestion(user1.userInfo, user1.group);
        final JsonAnnotation jsJustificationByUser1 = TestData.getTestReplyToAnnotation(hypothesisUsername1, new URI(suggestionByUser1.getDocument().getUri()),
                Collections.singletonList(suggestionByUser1.getId()));
        final Annotation justificationByUser1 = createAnnotation(jsJustificationByUser1, user1.userInfo, user1.group);
        setAnnotationResponseStatusToSent(justificationByUser1);
        final JsonAnnotationDocument doc = new JsonAnnotationDocument();
        doc.setMetadata(new SimpleMetadata());
        jsJustificationByUser1.setDocument(doc);
        final UserInformationAndGroup user2 = createUserWithGroupAndAuthority("b", "A", Authorities.ISC);

        try {
            annotService.updateAnnotation(justificationByUser1.getId(), jsJustificationByUser1, user2.userInfo);
        } catch (CannotUpdateAnnotationException | MissingPermissionException | CannotUpdateSentAnnotationException e) {
            Assert.fail("Editing a reply to a suggestion by the same group is allowed in the ISC context. " + e);
        }
    }

    @Test
    public void testEditSentReplyToSuggestionByDifferentGroupNotAllowedInIscContext() throws URISyntaxException {

        final UserInformationAndGroup userOfGroupA = createUserWithGroupAndAuthority("a", "A", Authorities.ISC);
        final String hypothesisUsernameA = getHypothesisUsername(userOfGroupA.userInfo);
        final Annotation suggestionByUserOfGroupA = createRootSuggestion(userOfGroupA.userInfo, userOfGroupA.group);
        final JsonAnnotation jsJustificationByUserOfGroupA = TestData.getTestReplyToAnnotation(hypothesisUsernameA,
                new URI(suggestionByUserOfGroupA.getDocument().getUri()), Collections.singletonList(suggestionByUserOfGroupA.getId()));
        final Annotation justificationByUserOfGroupA = createAnnotation(jsJustificationByUserOfGroupA, userOfGroupA.userInfo, userOfGroupA.group);
        setAnnotationResponseStatusToSent(justificationByUserOfGroupA);
        final UserInformationAndGroup userOfGroupB = createUserWithGroupAndAuthority("b", "B", Authorities.ISC);

        Assert.assertThrows("Editing a reply to a suggestion by a foreign group is not allowed in ISC context.", CannotUpdateSentAnnotationException.class,
                () -> annotService.updateAnnotation(justificationByUserOfGroupA.getId(), jsJustificationByUserOfGroupA, userOfGroupB.userInfo));
    }

    private class UserInformationAndGroup {

        public UserInformation userInfo;
        public Group group;

        public UserInformationAndGroup(final UserInformation userInfo, final Group group) {
            this.userInfo = userInfo;
            this.group = group;
        }
    }

    private Annotation createAnnotation(final JsonAnnotation jsAnnotation, final UserInformation userInfo, final Group userGroup) {

        if (userGroup != null) {
            jsAnnotation.setGroup(userGroup.getName());
        }
        try {
            return annotService.createAnnotation(jsAnnotation, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
            return null;
        }
    }

    private Annotation createReplyToAnnotation(final Annotation annotation, final UserInformation userInfo) throws URISyntaxException {
        
        return createReplyToAnnotation(annotation, userInfo, null);
    }

    private Annotation createReplyToAnnotation(final Annotation annotation, final UserInformation userInfo, final Group userGroup) throws URISyntaxException {

        final String hypothesisUsername = getHypothesisUsername(userInfo);
        final JsonAnnotation jsReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annotation.getDocument().getUri()),
                Collections.singletonList(annotation.getId()));
        return createAnnotation(jsReply, userInfo, userGroup);
    }

    private Annotation createRootSuggestion(final UserInformation userInfo) {
        
        return createRootSuggestion(userInfo, null);
    }

    private Annotation createRootSuggestionIsc(final UserInformation userInfo) {
        
        return createRootSuggestionIsc(userInfo, null);
    }
    
    private Annotation createRootSuggestion(final UserInformation userInfo, final Group userGroup) {

        final String hypothesisUsername = getHypothesisUsername(userInfo);
        final JsonAnnotation jsSuggestion = TestData.getTestSuggestionObject(hypothesisUsername);
        return createAnnotation(jsSuggestion, userInfo, userGroup);
    }

    private Annotation createRootSuggestionIsc(final UserInformation userInfo, final Group userGroup) {

        final String hypothesisUsername = getHypothesisUsername(userInfo);
        final JsonAnnotation jsSuggestion = TestData.getTestSuggestionObject(hypothesisUsername);
        
        // add some realistic metadata
        final SimpleMetadata meta = jsSuggestion.getDocument().getMetadata();
        meta.put(Metadata.PROP_RESPONSE_VERSION, "1");
        meta.put(Metadata.PROP_ISC_REF, "ISC/2021/0816");
        meta.put(Metadata.PROP_RESPONSE_ID, "DIGIT");
        jsSuggestion.getDocument().setMetadata(meta);
        return createAnnotation(jsSuggestion, userInfo, userGroup);
    }
    
    private UserInformation createUserWithAuthority(final String authority) {
        
        final User user = new User("a", authority);
        userRepos.save(user);
        userGroupRepos.save(new UserGroup(user.getId(), defaultGroup.getId()));
        return new UserInformation(user, authority);
    }

    private UserInformationAndGroup createUserWithGroupAndAuthority(final String login, final String groupName, final String authority) {

        final User user = new User(login);
        userRepos.save(user);
        Group group = groupRepos.findByName(groupName);
        if (group == null) {
            group = groupRepos.save(new Group(groupName, true));
        }
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));
        final UserInformation userInfo = new UserInformation(user, authority);
        return new UserInformationAndGroup(userInfo, group);
    }

    private String getHypothesisUsername(final UserInformation userInfo) {
        
        return "acct:" + userInfo.getLogin() + "@" + userInfo.getAuthority();
    }

    private void setAnnotationResponseStatusToSent(final Annotation annotation) {
        
        final Metadata savedMeta = annotService.findAnnotationById(annotation.getId()).getMetadata();
        savedMeta.setResponseStatus(ResponseStatus.SENT);
        metadataRepos.save(savedMeta);
    }

}
