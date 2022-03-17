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
import eu.europa.ec.leos.annotate.model.PublishContributionsResult;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.model.web.PublishContributionsRequest;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.StatusUpdateService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateMetadataException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotPublishContributionsException;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public class PublishContributionsTest {

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private AnnotationService annotService;

    @Autowired
    private StatusUpdateService statusService;

    @Autowired
    @Qualifier("annotationTestRepos")
    private AnnotationTestRepository annotRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private DocumentRepository documentRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Autowired
    private MetadataRepository metadataRepos;

    @Autowired
    private TokenRepository tokenRepos;

    // -------------------------------------
    // Cleanup of database content before running new test
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() {

        TestDbHelper.cleanupRepositories(this);
        TestDbHelper.insertDefaultGroup(groupRepos);
    }

    @After
    public void cleanDatabaseAfterTests() {

        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------
    @Test
    public void testPublish1()
            throws CannotCreateAnnotationException, CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final String LOGIN = "mylogin";
        final String ISCRef = "ISC/2019/4";
        final String GROUPNAME = "SG";
        final String RESPONSEVERSION = "1";
        final User user = new User(LOGIN);
        userRepos.save(user);

        final JsonAnnotation jsAnnot = TestData.getTestPrivateAnnotationObject("acct:user@" + Authorities.ISC);
        jsAnnot.setGroup(GROUPNAME);

        final Group group = new Group(GROUPNAME, true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));

        // create first annotation that should be published
        final SimpleMetadata meta = jsAnnot.getDocument().getMetadata();
        meta.put("ISCReference", ISCRef);
        meta.put("responseVersion", RESPONSEVERSION);
        meta.put("responseId", "SG");
        meta.put(Metadata.PROP_RESPONSE_STATUS, ResponseStatus.IN_PREPARATION.toString());
        jsAnnot.getDocument().setMetadata(meta);

        final Token token = new Token(user, Authorities.ISC, "a", LocalDateTime.now().plusMinutes(5), "r", LocalDateTime.now().plusMinutes(5));
        tokenRepos.save(token);
        final UserInformation userInfo = new UserInformation(token);

        final Annotation annot = annotService.createAnnotation(jsAnnot, userInfo);

        // create second annotation that should be published
        final JsonAnnotation jsAnnot2 = TestData.getTestPrivateAnnotationObject("acct:user@" + Authorities.ISC);
        jsAnnot2.setGroup(GROUPNAME);
        jsAnnot2.getDocument().setMetadata(meta);
        final Annotation annot2 = annotService.createAnnotation(jsAnnot2, userInfo);

        // create another annotation having the same metadata, but not being private
        final JsonAnnotation jsAnnot3 = TestData.getTestAnnotationObject("acct:user@" + Authorities.ISC);
        jsAnnot3.setGroup(GROUPNAME);
        jsAnnot3.getDocument().setMetadata(meta);
        final Annotation annot3 = annotService.createAnnotation(jsAnnot3, userInfo);

        final PublishContributionsRequest publishRequest = new PublishContributionsRequest(
                annot.getDocument().getUri().toString(),
                annot.getGroup().getName(),
                user.getLogin(),
                ISCRef,
                RESPONSEVERSION);
        final PublishContributionsResult publishResult = statusService.publishContributions(publishRequest, userInfo);

        Assert.assertNotNull(publishResult);
        Assert.assertEquals(2, publishResult.getUpdatedAnnotIds().size());
        assertIdContained(publishResult.getUpdatedAnnotIds(), annot.getId());
        assertIdContained(publishResult.getUpdatedAnnotIds(), annot2.getId());

        // read the annotations from the database again
        final Annotation annotDb1 = annotRepos.findById(annot.getId());
        final Annotation annotDb2 = annotRepos.findById(annot2.getId());
        final Annotation annotDb3 = annotRepos.findById(annot3.getId());

        // there should be a new metadata entry in the db...
        Assert.assertEquals(2, metadataRepos.count());

        // ... which is assigned to the two annotations being published and should have the new originMode
        final Metadata meta1 = metadataRepos.findOne(annotDb1.getMetadataId());
        Assert.assertEquals("private", MetadataHandler.getKeyValuePropertyAsSimpleMetadata(meta1).get("originMode"));

        Assert.assertEquals(meta1.getId(), annotDb2.getMetadataId());

        // and there is another entry (the original one), which does not have the originMode entry
        final Metadata meta3 = metadataRepos.findOne(annotDb3.getMetadataId());
        Assert.assertNull(MetadataHandler.getKeyValuePropertyAsSimpleMetadata(meta3).get("originMode"));
    }

    // verify that not giving a publishing request throws an exception
    @Test(expected = CannotPublishContributionsException.class)
    public void testPublish_noPublishRequest() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        statusService.publishContributions(null, null);
    }

    // verify that not giving a valid user throws an exception
    @Test(expected = IllegalArgumentException.class)
    public void testPublish_noUserinfo() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final PublishContributionsRequest request = new PublishContributionsRequest();
        statusService.publishContributions(request, null);
    }

    // verify that giving an EdiT user throws an exception
    @Test(expected = MissingPermissionException.class)
    public void testPublish_EditUser() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final PublishContributionsRequest request = new PublishContributionsRequest();
        final UserInformation userInfo = new UserInformation("me", null, Authorities.EdiT);

        statusService.publishContributions(request, userInfo);
    }

    // verify that an exception is thrown when no matching metadata is found
    @Test(expected = CannotPublishContributionsException.class)
    public void testPublish_noMatchingMetadata() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final PublishContributionsRequest request = new PublishContributionsRequest();
        final UserInformation userInfo = new UserInformation("me2", null, Authorities.ISC);

        statusService.publishContributions(request, userInfo);
    }

    // verify that an exception is thrown when no matching metadata having the desired ISC reference is found
    @Test(expected = CannotPublishContributionsException.class)
    public void testPublish_noMatchingIscReference() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final Document doc = new Document(URI.create("http://some.url"), "some title");
        documentRepos.save(doc);

        final Group group = new Group("secretgroup", true);
        groupRepos.save(group);

        final Metadata meta = new Metadata(doc, group, Authorities.ISC);
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta, new SimpleMetadata("ISCRef", "4"));
        metadataRepos.save(meta);

        // define the publishing request - but the saved metadata does not have the same ISC reference
        final UserInformation userInfo = new UserInformation("me2", null, Authorities.ISC);
        final PublishContributionsRequest request = new PublishContributionsRequest(
                doc.getUri(), group.getName(), userInfo.getLogin(), "ISC/1", "");

        statusService.publishContributions(request, userInfo);
    }

    // verify that an exception is thrown when no matching metadata having the desired response version is found
    @Test(expected = CannotPublishContributionsException.class)
    public void testPublish_noMatchingResponseVersion() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final Document doc = new Document(URI.create("http://somewhere.over.the"), "rainbow");
        documentRepos.save(doc);

        final Group group = new Group("groupygroup", true);
        groupRepos.save(group);

        final Metadata meta = new Metadata(doc, group, Authorities.ISC);
        final SimpleMetadata simpMeta = new SimpleMetadata(Metadata.PROP_RESPONSE_VERSION, "4");
        simpMeta.put(Metadata.PROP_ISC_REF, "ISC/1/2");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta, simpMeta);
        metadataRepos.save(meta);

        // define the publishing request - but the saved metadata does not have the same ISC reference
        final UserInformation userInfo = new UserInformation("me3", null, Authorities.ISC);
        final PublishContributionsRequest request = new PublishContributionsRequest(
                doc.getUri(), group.getName(), userInfo.getLogin(), "ISC/1/2", "3");

        statusService.publishContributions(request, userInfo);
    }

    // verify that an exception is thrown when matching metadata is found, but no annotations assigned to it
    @Test(expected = CannotPublishContributionsException.class)
    public void testPublish_noAnnotations() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final String IscRef = "ISC8";

        final Document doc = new Document(URI.create("http://some2.url"), "some2 title");
        documentRepos.save(doc);

        final Group group = new Group("secretgroup2", true);
        groupRepos.save(group);

        final User user = new User("its_me");
        userRepos.save(user);
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));

        final Metadata meta = new Metadata(doc, group, Authorities.ISC);
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta, new SimpleMetadata(Metadata.PROP_ISC_REF, IscRef));
        metadataRepos.save(meta);

        // define the publishing request - but the saved metadata does not have the same ISC reference
        final UserInformation userInfo = new UserInformation(user, Authorities.ISC);
        final PublishContributionsRequest request = new PublishContributionsRequest(
                doc.getUri(), group.getName(), userInfo.getLogin(), IscRef, "1");

        statusService.publishContributions(request, userInfo);
    }

    // verify that no exception is thrown when only public annotations are found, but result is empty
    @Test
    public void testPublish_noPrivateAnnotations() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final String IscRef = "ISC8";
        final String RespVers = "4";

        final Document doc = new Document(URI.create("http://some3.url"), "some3 title");
        documentRepos.save(doc);

        final Group group = new Group("secretgroup3", true);
        groupRepos.save(group);

        final User user = new User("its_me2");
        userRepos.save(user);
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));

        final Token token = new Token(user, Authorities.ISC, "ac", LocalDateTime.now(), "re", LocalDateTime.now());
        tokenRepos.save(token);

        final Metadata meta = new Metadata(doc, group, Authorities.ISC);
        final SimpleMetadata simpMeta = new SimpleMetadata(Metadata.PROP_ISC_REF, IscRef);
        simpMeta.put(Metadata.PROP_RESPONSE_VERSION, RespVers);
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta, simpMeta);
        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);
        metadataRepos.save(meta);

        final Annotation annot = new Annotation();
        annot.setId("an1");
        annot.setCreated(LocalDateTime.now(java.time.ZoneOffset.UTC));
        annot.setUpdated(LocalDateTime.now(java.time.ZoneOffset.UTC));
        annot.setText("text");
        annot.setUser(user);
        annot.setMetadata(meta);
        annot.setTargetSelectors("a");
        annot.setShared(true);
        annotRepos.save(annot);

        // define the publishing request - but the saved metadata does not have the same ISC reference
        final UserInformation userInfo = new UserInformation(token);
        final PublishContributionsRequest request = new PublishContributionsRequest(
                doc.getUri(), group.getName(), userInfo.getLogin(), IscRef, RespVers);

        final PublishContributionsResult result = statusService.publishContributions(request, userInfo);
        Assert.assertNotNull(result);
        Assert.assertTrue(CollectionUtils.isEmpty(result.getUpdatedAnnotIds()));
    }

    /**
     * verify the result when a private annotation is found and published;
     * verify that no additional metadata is created when all annotations are assigned
     *   to a single metadata (which is updated in this case)
     */
    @Test
    public void testPublish_allAnnotationsToSameMetadata()
            throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final String IscRef = "ISC4";

        final Document doc = new Document(URI.create("http://some4.url"), "some4 title");
        documentRepos.save(doc);

        final Group group = new Group("secretgroup4", true);
        groupRepos.save(group);

        final User user = new User("its_me3");
        userRepos.save(user);
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));

        final Token token = new Token(user, Authorities.ISC, "ac", LocalDateTime.now(), "re", LocalDateTime.now());
        tokenRepos.save(token);

        final Metadata meta = saveMetadata(doc, group, IscRef, "");
        saveAnnotation(user, meta, "annot");

        // define the publishing request - but the saved metadata does not have the same ISC reference
        final UserInformation userInfo = new UserInformation(token);
        final PublishContributionsRequest request = new PublishContributionsRequest(
                doc.getUri(), group.getName(), userInfo.getLogin(), IscRef, "");

        final PublishContributionsResult result = statusService.publishContributions(request, userInfo);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getUpdatedAnnotIds().size());

        // no additional entry was created; and this entry has the "originMode" set
        Assert.assertEquals(1, metadataRepos.count());
        Assert.assertEquals("private", MetadataHandler.getAllMetadataAsSimpleMetadata(((List<Metadata>) metadataRepos.findAll()).get(0)).get("originMode"));
    }

    // different response versions and ISCRef combinations available,
    // verify that only the requested one is published
    @Test
    public void testOnlyCorrectAnnotsArePublished() throws CannotPublishContributionsException, MissingPermissionException, CannotCreateMetadataException {

        final String IscRef1 = "ISC/2020/1";
        final String IscRef2 = "ISC/2020/2";
        final String RespVers1 = "1";
        final String RespVers2 = "2";

        final Document doc = new Document(URI.create("http://nice.doc"), "nice title");
        documentRepos.save(doc);

        final Group group = new Group("greatgroup", true);
        groupRepos.save(group);

        final User user = new User("greatuser");
        userRepos.save(user);
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));

        final Token token = new Token(user, Authorities.ISC, "ac$", LocalDateTime.now(), "re$", LocalDateTime.now());
        tokenRepos.save(token);

        // create four different metadata sets with all combinations of ISC reference and response version
        final Metadata metaRef1Vers1 = saveMetadata(doc, group, IscRef1, RespVers1);
        final Metadata metaRef1Vers2 = saveMetadata(doc, group, IscRef1, RespVers2);
        final Metadata metaRef2Vers1 = saveMetadata(doc, group, IscRef2, RespVers1);
        final Metadata metaRef2Vers2 = saveMetadata(doc, group, IscRef2, RespVers2);

        // create one annotation per metadata
        final Annotation annotRef1Vers1 = saveAnnotation(user, metaRef1Vers1, "an1");
        final Annotation annotRef1Vers2 = saveAnnotation(user, metaRef1Vers2, "an2");
        final Annotation annotRef2Vers1 = saveAnnotation(user, metaRef2Vers1, "an3");
        final Annotation annotRef2Vers2 = saveAnnotation(user, metaRef2Vers2, "an4");

        // define the publishing request for one of the four created items
        final UserInformation userInfo = new UserInformation(token);
        final PublishContributionsRequest request = new PublishContributionsRequest(
                doc.getUri(), group.getName(), userInfo.getLogin(), IscRef2, RespVers1);

        final PublishContributionsResult result = statusService.publishContributions(request, userInfo);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getUpdatedAnnotIds().size());
        assertIdContained(result.getUpdatedAnnotIds(), annotRef2Vers1.getId());

        // check the annotation status in the database - only the published one should be shared
        Assert.assertFalse(annotRepos.findOne(annotRef1Vers1.getId()).isShared());
        Assert.assertFalse(annotRepos.findOne(annotRef1Vers2.getId()).isShared());
        Assert.assertTrue(annotRepos.findOne(annotRef2Vers1.getId()).isShared());
        Assert.assertFalse(annotRepos.findOne(annotRef2Vers2.getId()).isShared());
    }

    private Annotation saveAnnotation(final User user, final Metadata meta, final String annotId) {

        final Annotation annot = new Annotation();
        annot.setId(annotId);
        annot.setCreated(LocalDateTime.now(java.time.ZoneOffset.UTC));
        annot.setUpdated(LocalDateTime.now(java.time.ZoneOffset.UTC));
        annot.setText("text");
        annot.setUser(user);
        annot.setMetadata(meta);
        annot.setTargetSelectors("a");
        annot.setShared(false); // -> will be published
        return annotRepos.save(annot);
    }

    private Metadata saveMetadata(final Document doc, final Group group, final String iscRef, final String respVers) {

        final Metadata meta = new Metadata(doc, group, Authorities.ISC);
        final SimpleMetadata simp = new SimpleMetadata();
        simp.put(Metadata.PROP_ISC_REF, iscRef);
        simp.put(Metadata.PROP_RESPONSE_VERSION, respVers);
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta, simp);
        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);
        return metadataRepos.save(meta);
    }

    private void assertIdContained(final List<String> annots, final String idToCheck) {

        Assert.assertTrue(annots.contains(idToCheck));
    }
}
