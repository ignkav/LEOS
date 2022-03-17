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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import eu.europa.ec.leos.annotate.helper.ColumnSensingReplacementDataSetLoader;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.AnnotationReferencesHandler;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.AnnotationTestRepository;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.repository.TagRepository;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.UUIDGeneratorService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DbUnitConfiguration(dataSetLoader = ColumnSensingReplacementDataSetLoader.class)
public class AnnotationRepliesTest {

    private static final String UNEXPECTED_EXCEPTION = "Unexpected exception received: ";
    private static final String ACCT_PREFIX = "acct:myusername@";
    private static final String CLEAR_DB_FILE = "clearDb.xml";

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    @Qualifier("annotationTestRepos")
    private AnnotationTestRepository annotRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private TagRepository tagRepos;

    @Autowired
    private AnnotationService annotService;

    @Autowired
    private UUIDGeneratorService uuidGeneratorService;

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * a new top-level annotation is created in EdiT, and a reply for it
     * -> verifies that the reply was created and is associated to the parent annotation
     */
    @Test
    @DatabaseSetup("annotationRepliesTest_InsertAnnotationAndReply_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testInsertAnnotationAndReply() throws URISyntaxException {

        final String authority = Authorities.EdiT;
        final String hypothesisUsername = ACCT_PREFIX + authority;

        final User user = userRepos.findById(1);
        final UserInformation userInfo = new UserInformation(user, authority);

        // let the root annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUsername);
        Annotation annot = null;
        try {
            annot = annotService.createAnnotation(jsAnnot, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }
        Assert.assertNotNull(annot);
        Assert.assertTrue(!annot.getId().isEmpty());

        // create a reply to the root annotation (-> reference contains the root annotation's ID)
        final JsonAnnotation jsAnnotReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annot.getDocument().getUri()),
                Arrays.asList(annot.getId()));
        Annotation annotReply = null;
        try {
            annotReply = annotService.createAnnotation(jsAnnotReply, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }
        Assert.assertTrue(!annotReply.getId().isEmpty());

        // verification: search the reply again based on its ID...
        final Annotation ann = annotService.findAnnotationById(annotReply.getId());
        Assert.assertNotNull(ann);
        Assert.assertEquals(annot.getId(), ann.getReferences());
        Assert.assertEquals(1, AnnotationReferencesHandler.getReferencesList(ann).size());
        Assert.assertEquals(annot.getId(), AnnotationReferencesHandler.getReferencesList(ann).get(0));
        Assert.assertEquals(annot.getId(), ann.getRootAnnotationId()); // computed value set correctly for annotation root
        Assert.assertTrue(AnnotationReferencesHandler.isReply(ann));

        // reply must be assigned to the same metadata as the root annotation
        Assert.assertEquals(annot.getMetadata().getId(), ann.getMetadata().getId());
        
        // ...and there must be two annotations in total and three tags (2 from annotation, 1 from reply)
        Assert.assertEquals(2, annotRepos.count());
        Assert.assertEquals(3, tagRepos.count());
    }

    /**
     * a new top-level annotation is created;
     * a reply is created, coming from a different group (same user)
     * -> verifies that the reply was created and is associated to the correct (different) group
     */
    @Test
    @DatabaseSetup("annotationRepliesTest_InsertAnnotationAndReplyFromDifferentGroup_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testInsertAnnotationAndReplyFromDifferentGroup() throws URISyntaxException, CannotCreateAnnotationException {

        final String authority = Authorities.EdiT;
        final String hypothesisUsername = ACCT_PREFIX + authority;

        final User user = userRepos.findById(1);
        final UserInformation userInfo = new UserInformation(user, authority);
        final Group firstGroup = groupRepos.findOne(2L);
        final Group secondGroup = groupRepos.findOne(3L);

        // let the root annotation be created in the first group
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(hypothesisUsername);
        jsAnnot.setGroup(firstGroup.getName());
        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);
        Assert.assertNotNull(annot);
        Assert.assertTrue(!annot.getId().isEmpty());
        Assert.assertEquals(firstGroup.getId(), annot.getGroup().getId());

        // create a reply to the root annotation, but in the second group
        final JsonAnnotation jsAnnotReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annot.getDocument().getUri()),
                Arrays.asList(annot.getId()));
        jsAnnotReply.setGroup(secondGroup.getName());
        Annotation annotFirstReply = annotService.createAnnotation(jsAnnotReply, userInfo);
        Assert.assertTrue(!annotFirstReply.getId().isEmpty());

        // verification: search the reply again based on its ID...
        annotFirstReply = annotService.findAnnotationById(annotFirstReply.getId());
        Assert.assertNotNull(annotFirstReply);
        Assert.assertEquals(annot.getId(), annotFirstReply.getReferences());
        Assert.assertEquals(annot.getId(), annotFirstReply.getRootAnnotationId()); // computed value set correctly for annotation root
        Assert.assertTrue(AnnotationReferencesHandler.isReply(annotFirstReply));

        // ...and the correct group must be assigned, thus different metadata than the root
        Assert.assertEquals("wrong group ID assigned to reply",
                secondGroup.getId(), annotFirstReply.getGroup().getId());
        Assert.assertNotEquals(annotFirstReply.getMetadataId(), annot.getMetadataId());

        // create another reply (this implicitly tests that a reply reuses existing metadata as
        // the required ones have already been created for the first reply)
        final JsonAnnotation jsAnnotSecondReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annot.getDocument().getUri()),
                Arrays.asList(annot.getId()));
        jsAnnotSecondReply.setGroup(secondGroup.getName());
        Annotation annotSecondReply = annotService.createAnnotation(jsAnnotSecondReply, userInfo);
        Assert.assertTrue(!annotSecondReply.getId().isEmpty());

        // must have same metadata as first reply
        annotSecondReply = annotService.findAnnotationById(annotSecondReply.getId());
        Assert.assertEquals(annotFirstReply.getMetadataId(), annotSecondReply.getMetadataId());
    }

    @Test
    @DatabaseSetup("annotationRepliesTest_CannotReplyToAcceptedAnnotation_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testCannotReplyToAcceptedAnnotation() throws URISyntaxException {

        final Annotation annot = annotRepos.findById("annAcc");
        final ReplyAndUserInformation replyAndUserInfo = prepareReplyToAnnotationInEditContext(annot);

        Assert.assertThrows("Replying to an accepted annotation is not allowed", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(replyAndUserInfo.reply, replyAndUserInfo.userInfo));
    }

    @Test
    @DatabaseSetup("annotationRepliesTest_CannotReplyToDeletedAnnotation_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testCannotReplyToDeletedAnnotation() throws URISyntaxException {

        final Annotation annot = annotRepos.findById("annDel");

        final ReplyAndUserInformation replyAndUserInfo = prepareReplyToAnnotationInEditContext(annot);
        Assert.assertThrows("Replying to a deleted annotation is not allowed", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(replyAndUserInfo.reply, replyAndUserInfo.userInfo));
    }

    @Test
    @DatabaseSetup("annotationRepliesTest_CannotReplyToRejectedAnnotation_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testCannotReplyToRejectedAnnotation() throws URISyntaxException {

        final Annotation annot = annotRepos.findById("annRej");

        final ReplyAndUserInformation replyAndUserInfo = prepareReplyToAnnotationInEditContext(annot);
        Assert.assertThrows("Replying to a rejected annotation is not allowed", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(replyAndUserInfo.reply, replyAndUserInfo.userInfo));
    }

    /**
     * a new top-level annotation is created, and a reply for it
     * -> verifies that the reply was created and is associated to the parent annotation
     */
    @Test
    @DatabaseSetup("annotationRepliesTest_CannotReplyToSentAnnotation_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testCannotReplyToSentAnnotation() throws URISyntaxException {

        final Annotation annot = annotRepos.findById("annSent");

        final ReplyAndUserInformation replyAndUserInfo = prepareReplyToAnnotationInEditContext(annot);
        Assert.assertThrows("Replying to a SENT annotation is not allowed", 
                CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(replyAndUserInfo.reply, replyAndUserInfo.userInfo));
    }

    @Test
    @DatabaseSetup("annotationRepliesTest_CannotReplyToSentAnnotationInEditContext_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testCannotReplyToSentAnnotationInEditContext() throws URISyntaxException {

        final Annotation annot = annotRepos.findById("annSent");

        final ReplyAndUserInformation replyAndUserInfo = prepareReplyToAnnotationInEditContext(annot);

        Assert.assertThrows("Replying to a SENT annotation is not allowed",
                CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(replyAndUserInfo.reply, replyAndUserInfo.userInfo));
    }

    /**
     *  a new top-level annotation should be created - but its metadata has response status
     *  set to "SENT" already -> creation is refused
     */
    @Test(expected = CannotCreateAnnotationException.class)
    @DatabaseSetup("annotationRepliesTest_CannotCreateIscAnnotationAlreadySent_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testCannotCreateIscAnnotationAlreadySent() throws CannotCreateAnnotationException {

        final String annotHypothesisUsername = ACCT_PREFIX + Authorities.ISC;
        final String ISC_REF = "ISC/4/8";

        final User user = userRepos.findById(1L);

        final UserInformation userInfoAnnot = new UserInformation(user, Authorities.ISC);

        // let the root annotation be created
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(annotHypothesisUsername);
        jsAnnot.getDocument().getMetadata().put(Metadata.PROP_RESPONSE_STATUS, "SENT");
        jsAnnot.getDocument().getMetadata().put(Metadata.PROP_ISC_REF, ISC_REF);

        annotService.createAnnotation(jsAnnot, userInfoAnnot);
    }

    /**
     * a new top-level annotation is created and two replies for it
     * -> verifies that the replies were created and are associated to the parent annotation
     */
    @Test
    @DatabaseSetup("annotationRepliesTest_InsertAnnotationWithTwoReplies_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testInsertAnnotationWithTwoReplies() throws URISyntaxException {

        final String hypothesisUsername = ACCT_PREFIX + "europa.eu";

        final User user = userRepos.findById(1L);
        final UserInformation userInfo = new UserInformation(user, Authorities.EdiT);

        // retrieve existing root annotation from DB
        final Annotation annot = annotRepos.findById("ann");

        // create a reply to the root annotation
        final JsonAnnotation jsAnnotFirstReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annot.getDocument().getUri()),
                Arrays.asList(annot.getId()));
        Annotation annotFirstReply = null;
        try {
            annotFirstReply = annotService.createAnnotation(jsAnnotFirstReply, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }
        Assert.assertTrue(!annotFirstReply.getId().isEmpty());

        // create a second reply to the root annotation
        final JsonAnnotation jsAnnotSecondReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annot.getDocument().getUri()),
                Arrays.asList(annot.getId()));
        Annotation annotSecondReply = null;
        try {
            annotSecondReply = annotService.createAnnotation(jsAnnotSecondReply, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }
        Assert.assertTrue(!annotSecondReply.getId().isEmpty());

        // verification: search the replies again based on their IDs...
        final Annotation annFirstReply = annotService.findAnnotationById(annotFirstReply.getId());
        Assert.assertNotNull(annFirstReply);
        Assert.assertEquals(annot.getId(), annFirstReply.getRootAnnotationId());
        Assert.assertTrue(AnnotationReferencesHandler.isReply(annFirstReply));

        final Annotation annSecondReply = annotService.findAnnotationById(annotSecondReply.getId());
        Assert.assertNotNull(annSecondReply);
        Assert.assertEquals(annot.getId(), annSecondReply.getRootAnnotationId());
        Assert.assertTrue(AnnotationReferencesHandler.isReply(annSecondReply));

        // ...and there must be three annotations in total, two of which are replies
        Assert.assertEquals(3, annotRepos.count());
        Assert.assertEquals(2, annotRepos.countByRootAnnotationIdNotNull());
    }

    /**
     * a new top-level annotation is created, a reply for it, and a reply to the reply
     * -> verifies that the replies were created and are associated to the parent annotation
     */
    @Test
    @DatabaseSetup("annotationRepliesTest_InsertAnnotationWithReplyAndReplyReply_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    public void testInsertAnnotationWithReplyAndReplyReply() throws URISyntaxException {

        final String hypothesisUsername = ACCT_PREFIX + "europa.eu";

        final User user = userRepos.findById(1L);
        final UserInformation userInfo = new UserInformation(user, Authorities.ISC);

        // retrieve existing root annotation from DB
        final Annotation annot = annotRepos.findById("ann1Root90123456789012");

        // create a reply to the root annotation
        final JsonAnnotation jsAnnotReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annot.getDocument().getUri()),
                Arrays.asList(annot.getId()));
        Annotation annotReply = null;
        try {
            annotReply = annotService.createAnnotation(jsAnnotReply, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }
        Assert.assertTrue(!annotReply.getId().isEmpty());

        // create a reply to the reply
        final JsonAnnotation jsAnnotReplyReply = TestData.getTestReplyToAnnotation(hypothesisUsername, new URI(annot.getDocument().getUri()),
                Arrays.asList(annot.getId(), annotReply.getId()));
        Annotation annotReplyReply = null;
        try {
            annotReplyReply = annotService.createAnnotation(jsAnnotReplyReply, userInfo);
        } catch (CannotCreateAnnotationException e) {
            Assert.fail(UNEXPECTED_EXCEPTION + e);
        }
        Assert.assertTrue(!annotReplyReply.getId().isEmpty());

        // verification: search the reply-reply again based on its ID...
        final Annotation ann = annotService.findAnnotationById(annotReplyReply.getId());
        Assert.assertNotNull(ann);
        Assert.assertEquals(annot.getId() + "," + annotReply.getId(), ann.getReferences()); // ancestor chain set correctly
        Assert.assertEquals(annot.getId(), ann.getRootAnnotationId()); // computed value set correctly for annotation root
        Assert.assertTrue(AnnotationReferencesHandler.isReply(ann)); // reply-reply also is a reply ;-)

        // verify ordering of parent items
        Assert.assertEquals(2, AnnotationReferencesHandler.getReferencesList(ann).size());
        Assert.assertEquals(annot.getId(), AnnotationReferencesHandler.getReferencesList(ann).get(0)); // root
        Assert.assertEquals(annotReply.getId(), AnnotationReferencesHandler.getReferencesList(ann).get(1)); // immediate parent (first degree reply)

        // ...and there must be three annotations in total, two of which are replies
        Assert.assertEquals(3, annotRepos.count());
        Assert.assertEquals(2, annotRepos.countByRootAnnotationIdNotNull());
    }

    /**
     * a reply for an annotation is created, but the referenced top-level annotation is missing
     * -> verifies that the reply was not created
     */
    @Test
    @DatabaseSetup("annotationRepliesTest_InsertReplyWithoutExistingParent_init.xml")
    @DatabaseTearDown(CLEAR_DB_FILE)
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testInsertReplyWithoutExistingParent() throws Exception {

        final String hypothesisUsername = ACCT_PREFIX + "europa.eu";
        final URI uri = new URI("http://leos/document?id=3");
        final User user = userRepos.findById(1L);

        // create a reply to the root annotation
        // -> reference contains the root annotation's ID, which does not exist
        // as the 'root' property is a foreign key on the annotation ID, this is a foreign key violation
        final JsonAnnotation jsAnnotReply = TestData.getTestReplyToAnnotation(hypothesisUsername, uri,
                Arrays.asList(uuidGeneratorService.generateUrlSafeUUID()));

        Assert.assertThrows("Inserting a reply to a non-existing annotation should fail", CannotCreateAnnotationException.class,
                () -> annotService.createAnnotation(jsAnnotReply, new UserInformation(user, "authority")));

        Assert.assertEquals(0, annotRepos.count());
        Assert.assertEquals(0, tagRepos.count());
    }

    public class ReplyAndUserInformation {
        public JsonAnnotation reply;
        public UserInformation userInfo;

        public ReplyAndUserInformation(final JsonAnnotation reply, final UserInformation userInfo) {
            this.reply = reply;
            this.userInfo = userInfo;
        }
    }

    private ReplyAndUserInformation prepareReplyToAnnotationInEditContext(final Annotation annotation) throws URISyntaxException {

        final String replyHypothesisUsername = "acct:otheruser@" + Authorities.EdiT;
        final User replyUser = userRepos.findById(2L);
        final UserInformation userInfoReply = new UserInformation(replyUser, Authorities.EdiT);

        return new ReplyAndUserInformation(
                TestData.getTestReplyToAnnotation(replyHypothesisUsername, new URI(annotation.getDocument().getUri()),
                        Collections.singletonList(annotation.getId())),
                userInfoReply);
    }

}
