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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchCountOptions;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationDocumentLink;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationPermissions;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.AnnotationSearchService;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotDeleteAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotDeleteSentAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public class AnnotationCountTest {

    /**
     * This class contains tests for counting the number of annotations for ISC users/annotations
     */

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private AnnotationService annotService;

    @Autowired
    private AnnotationSearchService annotSearchService;

    @Autowired
    private DocumentRepository documentRepos;

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserRepository userRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    @Autowired
    private MetadataRepository metadataRepos;

    // -------------------------------------
    // Help variables
    // -------------------------------------
    private final static String LOGIN1 = "demo";
    private final static String LOGIN2 = "demo2";
    private final static String ISCREF = "ISCReference";
    private final static String ISCREFVAL = "ISC/2019/007";
    private final static String RESPVERS = "responseVersion";
    private final static String RESPVERS1 = "1";
    private final static String RESPVERS2 = "2";
    private final static String RESPVERS3 = "3";
    private final static String SEPARATOR = ":\"";
    private final static String ACCT = "acct:";

    private URI dummyUri;
    private User theUser, secondUser;

    // -------------------------------------
    // Cleanup of database content before running new test
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() throws URISyntaxException {

        TestDbHelper.cleanupRepositories(this);
        final Group defaultGroup = TestDbHelper.insertDefaultGroup(groupRepos);

        theUser = userRepos.save(new User(LOGIN1));
        secondUser = userRepos.save(new User(LOGIN2));
        userGroupRepos.save(new UserGroup(theUser.getId(), defaultGroup.getId()));
        userGroupRepos.save(new UserGroup(secondUser.getId(), defaultGroup.getId()));
        dummyUri = new URI("http://some.url");
    }

    @After
    public void cleanDatabaseAfterTests() {
        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * retrieving number of annotation is issued by a LEOS user
     * -> will be refused with exception
     */
    @Test(expected = MissingPermissionException.class)
    public void testCountAnnots_NotForLeosUser() throws Exception {

        final UserInformation userInfo = new UserInformation(LOGIN1, null, Authorities.EdiT);
        annotSearchService.getAnnotationsCount(new AnnotationSearchCountOptions(), userInfo);
    }

    /**
     * retrieving number of annotation, but for an unknown document
     * -> error result
     */
    @Test
    public void testCountAnnots_UnknownDocument() throws Exception {

        final UserInformation userInfo = new UserInformation(LOGIN1, null, Authorities.ISC);

        final AnnotationSearchCountOptions options = new AnnotationSearchCountOptions();
        options.setUri(new URI("http://some.thing"));

        Assert.assertEquals(-1, annotSearchService.getAnnotationsCount(options, userInfo));
    }

    /**
     * retrieving number of annotation, but for an unknown group
     * -> error result
     */
    @Test
    public void testCountAnnots_UnknownGroup() throws Exception {

        final UserInformation userInfo = new UserInformation(LOGIN1, null, Authorities.ISC);

        documentRepos.save(new Document(dummyUri, "a"));

        final AnnotationSearchCountOptions options = new AnnotationSearchCountOptions();
        options.setUri(dummyUri);
        options.setGroup("unknownGroup");

        Assert.assertEquals(-1, annotSearchService.getAnnotationsCount(options, userInfo));
    }

    /**
     * retrieving number of annotation, but for an unknown user
     * -> error result
     */
    @Test
    public void testCountAnnots_UnknownUser() throws Exception {

        final UserInformation userInfo = new UserInformation(LOGIN1, null, Authorities.ISC);
        userInfo.setUser(null); // make user "unknown" -> will be searched for

        documentRepos.save(new Document(dummyUri, "a"));

        final Group group = new Group("mygroup", true);
        groupRepos.save(group);

        final AnnotationSearchCountOptions options = new AnnotationSearchCountOptions();
        options.setUri(dummyUri);
        options.setGroup(group.getName());

        Assert.assertEquals(-1, annotSearchService.getAnnotationsCount(options, userInfo));
    }

    /**
     * retrieving number of annotations, but for LEOS annotations (via metadata)
     * -> will be refused with exception
     */
    @Test(expected = MissingPermissionException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    public void testCountAnnots_LeosAnnotations() throws Exception {

        final UserInformation userInfo = new UserInformation(LOGIN1, null, Authorities.ISC);

        documentRepos.save(new Document(dummyUri, "a"));

        final Group group = new Group("group_a", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId())); // assign user

        final String metadataMapJson = "[{\"systemId\":\"LEOS\"}]";

        final AnnotationSearchCountOptions options = new AnnotationSearchCountOptions();
        options.setUri(dummyUri);
        options.setGroup(group.getName());
        options.setMetadatasets(metadataMapJson);

        // asking for LEOS annotations will provoke exception
        annotSearchService.getAnnotationsCount(options, userInfo);
    }

    /**
     * retrieving number of annotation, but there exists no matching metadata
     * -> result should be 0
     */
    @Test
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    public void testCountAnnots_NoMatchingMetadata() throws Exception {

        final UserInformation userInfo = new UserInformation(LOGIN1, null, Authorities.ISC);

        documentRepos.save(new Document(dummyUri, "a"));

        final Group group = new Group("group_b", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId())); // assign user

        final AnnotationSearchCountOptions options = new AnnotationSearchCountOptions();
        options.setUri(dummyUri);
        options.setGroup(group.getName());
        options.setMetadatasets("");

        // should not find any items
        Assert.assertEquals(0, annotSearchService.getAnnotationsCount(options, userInfo));
    }

    /**
     * retrieving number of annotations: ask for none
     * -> all annotations should be counted
     */
    @Test
    public void testCountAnnots_noMetadataRequested() throws Exception {

        // ask without any metadata - should find all four public annotations as there are no restrictions
        Assert.assertEquals(4, runCountAnnotsWithMetadataAndUsers("[]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for empty list
     * -> all annotations should be counted
     */
    @Test
    public void testCountAnnots_noMetadataRequestedViaEmptyList() throws Exception {

        // ask without any metadata - should find all four public annotations as there are no restrictions
        Assert.assertEquals(4, runCountAnnotsWithMetadataAndUsers("[{}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for several empty lists
     * -> all annotations should be counted
     */
    @Test
    public void testCountAnnots_noMetadataRequestedViaEmptyLists() throws Exception {

        // ask without any metadata - should find all four public annotations as there are no restrictions
        Assert.assertEquals(4, runCountAnnotsWithMetadataAndUsers("[{},{}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for ISC reference
     * -> all annotations should be counted
     */
    @Test
    public void testCountAnnots_IscReference() throws Exception {

        // ask for ISC Reference - should find all four public annotations
        Assert.assertEquals(4, runCountAnnotsWithMetadataAndUsers(
                "[{" + ISCREF + SEPARATOR + ISCREFVAL + "\"}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for ISC reference and explicitly NORMAL status
     * -> all annotations should be counted
     */
    @Test
    public void testCountAnnots_IscReference_Normal() throws Exception {

        // ask for ISC Reference - should find all four public annotations
        Assert.assertEquals(4, runCountAnnotsWithMetadataAndUsers(
                "[{\"" + ISCREF + "\":\"" + ISCREFVAL + "\", \"status\":[\"NORMAL\"]}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for ISC reference and explicitly DELETED status
     * -> one annotation should be counted (the deleted page note)
     */
    @Test
    public void testCountAnnots_IscReference_Deleted() throws Exception {

        // ask for ISC Reference - should find one annotation
        Assert.assertEquals(1, runCountAnnotsWithMetadataAndUsers(
                "[{" + ISCREF + SEPARATOR + ISCREFVAL + "\", \"status\":[\"DELETED\"]}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for ISC reference and ACCEPTED or REJECTED status
     * -> no matches
     */
    @Test
    public void testCountAnnots_IscReference_AcceptedRejected() throws Exception {

        // ask for ISC Reference - should not find any annotation
        Assert.assertEquals(0, runCountAnnotsWithMetadataAndUsers(
                "[{\"" + ISCREF + "\":\"" + ISCREFVAL + "\", \"status\":[\"ACCEPTED\",\"REJECTED\"]}]",
                true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response version 1
     * -> one annotation should be counted
     */
    @Test
    public void testCountAnnots_responseVersion1() throws Exception {

        // ask for response version 1
        // -> should find one annotation of first user, one annotation of second user
        Assert.assertEquals(2, runCountAnnotsWithMetadataAndUsers(
                "[{" + RESPVERS + ":" + RESPVERS1 + "}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response version 1 and DELETED status
     * -> one annotation should be counted
     */
    @Test
    public void testCountAnnots_responseVersion1_DELETED() throws Exception {

        // ask for response version 1 - should only find one annotation
        Assert.assertEquals(1, runCountAnnotsWithMetadataAndUsers(
                "[{" + RESPVERS + ":" + RESPVERS1 + ", status:[\"DELETED\"]}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response version 1 and ALL statuses
     * -> two annotations should be counted
     */
    @Test
    public void testCountAnnots_responseVersion1_AllStatuses() throws Exception {

        // ask for response version 1 - should find three annotations (one alive, one deleted)
        // (two annotations of first user, one annotation of second user)
        Assert.assertEquals(3, runCountAnnotsWithMetadataAndUsers(
                "[{" + RESPVERS + ":" + RESPVERS1 + ", status:[\"ALL\"]}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response version 2
     * -> one annotation should be counted
     */
    @Test
    public void testCountAnnots_responseVersion2() throws Exception {

        // ask for response version 2 - should only find one annotation
        Assert.assertEquals(1, runCountAnnotsWithMetadataAndUsers(
                "[{" + RESPVERS + ":" + RESPVERS2 + "}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for unknown response version
     * -> no annotation should be counted
     */
    @Test
    public void testCountAnnots_responseVersionUnknown() throws Exception {

        // ask for an unknown response version - should not find any matches
        Assert.assertEquals(0, runCountAnnotsWithMetadataAndUsers(
                "[{" + RESPVERS + ":999}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 or 2
     * -> two annotations should be counted
     */
    @Test
    public void testCountAnnots_responseVersions1Or2() throws Exception {

        // ask for response versions 1 or 2 - should find three annotations
        // (two annotations of first user, one annotation of second user)
        Assert.assertEquals(3, runCountAnnotsWithMetadataAndUsers(
                "[{" + RESPVERS + ":" + RESPVERS1 + "},{" + RESPVERS + ":" + RESPVERS2 + "}]",
                true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 or 2 and ALL statuses (as first user)
     * -> three annotations should be counted (two annotations, one deleted page note)
     */
    @Test
    public void testCountAnnots_responseVersions1Or2_AllStatuses_firstUser() throws Exception {

        // ask for response versions 1 or 2 in all statuses - should find four items
        // for first user: two alive, one deleted
        // for second user: one alive
        Assert.assertEquals(4, testCountAnnotation_responseVersions1Or2_AllStatuses(LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 or 2 and ALL statuses (as second user)
     * -> three annotations should be counted (two annotations, one deleted page note)
     */
    @Test
    public void testCountAnnots_responseVersions1Or2_AllStatuses_secondUser() throws Exception {

        // ask for response versions 1 or 2 in all statuses - should find four items
        // for first user: two alive, one deleted
        // for second user: one alive
        Assert.assertEquals(4, testCountAnnotation_responseVersions1Or2_AllStatuses(LOGIN2, null));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 or 2 and ALL statuses
     * (as first user, and search annotations of first user only)
     * -> three annotations should be counted (two annotations, one deleted page note)
     */
    @Test
    public void testCountAnnots_responseVersions1Or2_AllStatuses_firstUser_onlyAnnotsOfFirstUser() throws Exception {

        // ask for response versions 1 or 2 in all statuses - should find three items
        // for first user: two alive, one deleted
        Assert.assertEquals(3, testCountAnnotation_responseVersions1Or2_AllStatuses(LOGIN1, LOGIN1));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 or 2 and ALL statuses
     * (as first user, and search annotations of second user only)
     * -> three annotations should be counted (two annotations, one deleted page note)
     */
    @Test
    public void testCountAnnots_responseVersions1Or2_AllStatuses_firstUser_onlyAnnotsOfSecondtUser() throws Exception {

        // ask for response versions 1 or 2 in all statuses - should find one item
        // for second user: one alive
        Assert.assertEquals(1, testCountAnnotation_responseVersions1Or2_AllStatuses(LOGIN1, LOGIN2));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 or 2 and ALL statuses
     * (as second user, and search annotations of first user only)
     * -> three annotations should be counted (two annotations, one deleted page note)
     */
    @Test
    public void testCountAnnots_responseVersions1Or2_AllStatuses_secondUser_onlyAnnotsOfFirstUser() throws Exception {

        // ask for response versions 1 or 2 in all statuses - should find three items
        // for first user: two alive, one deleted
        Assert.assertEquals(3, testCountAnnotation_responseVersions1Or2_AllStatuses(LOGIN2, LOGIN1));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 or 2 and ALL statuses
     * (as second user, and search annotations of second user only)
     * -> three annotations should be counted (two annotations, one deleted page note)
     */
    @Test
    public void testCountAnnots_responseVersions1Or2_AllStatuses_secondUser_onlyAnnotsOfSecondtUser() throws Exception {

        // ask for response versions 1 or 2 in all statuses - should find one item
        // for second user: one alive
        Assert.assertEquals(1, testCountAnnotation_responseVersions1Or2_AllStatuses(LOGIN2, LOGIN2));
    }

    private long testCountAnnotation_responseVersions1Or2_AllStatuses(
            final String searchingUser, final String userToSearch) throws Exception {

        final String metaSets = "[{" + RESPVERS + SEPARATOR + RESPVERS1 + "\", status:[\"ALL\"]},{" + RESPVERS + SEPARATOR + RESPVERS2 +
                "\", status:[\"NORMAL\",\"DELETED\",\"ACCEPTED\",\"REJECTED\"]}]";

        return runCountAnnotsWithMetadataAndUsers(metaSets, true, searchingUser, userToSearch);
    }

    /**
     * retrieving number of annotations: ask for response versions 2 or 999
     * -> one annotation should be counted
     */
    @Test
    public void testCountAnnots_responseVersions2Or999() throws Exception {

        // ask for response versions 2 and 999 - should only find one annotation (having response version 2)
        Assert.assertEquals(1, runCountAnnotsWithMetadataAndUsers(
                "[{" + RESPVERS + ":" + RESPVERS2 + "},{" + RESPVERS + ":999}]", true, LOGIN1, null));
    }

    /**
     * retrieving number of annotations: ask for response versions 1 to 1000
     * -> two annotations should be counted (having response versions 1 or 2)
     */
    @Test
    public void testCountAnnots_response1000Versions() throws Exception {

        final int MAX_SETS = 1000;

        // ask for response versions 1 to 1000
        // -> should only find two annotations (having response version 1 or 2)
        // (two of first user, one of second user)
        final StringBuilder sbMeta = new StringBuilder();
        sbMeta.append('[');
        for (int i = 1; i <= MAX_SETS; i++) {
            sbMeta.append('{').append(RESPVERS).append(':').append(i).append('}');
            if (i < MAX_SETS) {
                sbMeta.append(',');
            }
        }
        sbMeta.append(']');
        Assert.assertEquals(3, runCountAnnotsWithMetadataAndUsers(sbMeta.toString(), true, LOGIN1, null));
    }

    /**
     * retrieving number of private annotations of first user
     * -> none (since it is SENT, the private annotations are not counted) 
     */
    @Test
    public void testCountAnnots_privateAnnots_firstUser() throws Exception {
        Assert.assertEquals(0, runCountAnnotsWithMetadataAndUsers("[{},{}]", false, LOGIN1, null));
    }

    /**
     * retrieving number of private annotations of second user
     * -> none
     */
    @Test
    public void testCountAnnots_privateAnnots_secondUser() throws Exception {
        Assert.assertEquals(0, runCountAnnotsWithMetadataAndUsers("[{},{},{}]", false, LOGIN2, null));
    }

    /**
     * retrieving number of private annotations of first user, but executing search as SECOND USER
     * -> may not receive any number as he may not know about private annotations of other users
     */
    @Test
    public void testCountAnnots_privateAnnots_firstUser_searchAsSecondUser() throws Exception {
        Assert.assertEquals(0, runCountAnnotsWithMetadataAndUsers("[{},{},{}]", false, LOGIN1, LOGIN2));
    }

    /**
     * retrieving number of annotations when shared is NULL. Should be handled as shared = true
     * -> four public annotation should be counted (three of first user, one of second user)
     *    (the private annotation of first user is not visible since it is SENT already, and then
     *     private items are discarded)
     */
    @Test
    public void testCountAnnots_sharedNULL() throws Exception {
        Assert.assertEquals(4, runCountAnnotsWithMetadataAndUsers("[{},{},{}]", null, LOGIN1, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRepliesCountForAnnotation_NoAnnotation() {
        final UserInformation userInfo = new UserInformation("a", null, "b");
        annotSearchService.getRepliesCountForAnnotation(null, userInfo, "c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRepliesCountForAnnotation_NoUserInformation() throws CannotCreateAnnotationException {
        final String hypoAccountUser = ACCT + theUser.getLogin() + "@" + Authorities.ISC;

        final Group group = new Group("group_c", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId()));

        final SimpleMetadata metaIscRef = new SimpleMetadata(ISCREF, ISCREFVAL);

        final UserInformation userInfo = new UserInformation(theUser, Authorities.ISC);
        userInfo.setCurrentToken(new Token(userRepos.findByLoginAndContext(theUser.getLogin(), null), userInfo.getAuthority(), "a1",
                LocalDateTime.now().plusMinutes(1), "r1", LocalDateTime.now().plusMinutes(1)));

        final Annotation annot = createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null);

        annotSearchService.getRepliesCountForAnnotation(annot, null, "b");
    }

    @Test
    public void testGetRepliesCountForAnnotation_NoGroup() throws CannotCreateAnnotationException {
        final String hypoAccountUser = ACCT + theUser.getLogin() + "@" + Authorities.ISC;

        final Group group = new Group("group_d", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId()));

        final SimpleMetadata metaIscRef = new SimpleMetadata(ISCREF, ISCREFVAL);

        final UserInformation userInfo = new UserInformation(theUser, Authorities.ISC);
        userInfo.setCurrentToken(new Token(userRepos.findByLoginAndContext(theUser.getLogin(), null), userInfo.getAuthority(), "a1",
                LocalDateTime.now().plusMinutes(1), "r1", LocalDateTime.now().plusMinutes(1)));

        final Annotation annot = createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null);

        Assert.assertThrows(IllegalArgumentException.class, () -> annotSearchService.getRepliesCountForAnnotation(annot, userInfo, null));
        Assert.assertThrows(IllegalArgumentException.class, () -> annotSearchService.getRepliesCountForAnnotation(annot, userInfo, ""));
    }

    @Test
    public void testGetRepliesCountForAnnotation_NoReply() throws CannotCreateAnnotationException {
        final String hypoAccountUser = ACCT + theUser.getLogin() + "@" + Authorities.ISC;

        final Group group = new Group("theirgroup", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId()));

        final SimpleMetadata metaIscRef = new SimpleMetadata(ISCREF, ISCREFVAL);

        final UserInformation userInfo = new UserInformation(theUser, Authorities.ISC);
        userInfo.setCurrentToken(new Token(userRepos.findByLoginAndContext(theUser.getLogin(), null), userInfo.getAuthority(), "a1",
                LocalDateTime.now().plusMinutes(1), "r1", LocalDateTime.now().plusMinutes(1)));

        final Annotation annot = createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null);

        Assert.assertEquals(0, annotSearchService.getRepliesCountForAnnotation(annot, userInfo, group.getName()));
    }

    @Test
    public void testGetRepliesCountForAnnotation_OneReply() throws CannotCreateAnnotationException {
        final String hypoAccountUser = ACCT + theUser.getLogin() + "@" + Authorities.ISC;

        final Group group = new Group("theirgroup", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId()));

        final SimpleMetadata metaIscRef = new SimpleMetadata(ISCREF, ISCREFVAL);

        final UserInformation userInfo = new UserInformation(theUser, Authorities.ISC);
        userInfo.setCurrentToken(new Token(userRepos.findByLoginAndContext(theUser.getLogin(), null), userInfo.getAuthority(), "a1",
                LocalDateTime.now().plusMinutes(1), "r1", LocalDateTime.now().plusMinutes(1)));

        final Annotation annot = createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null);
        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null, Arrays.asList(annot.getId()));

        Assert.assertEquals(1, annotSearchService.getRepliesCountForAnnotation(annot, userInfo, group.getName()));
    }

    @Test
    public void testGetRepliesCountForAnnotation_TwoReplies() throws CannotCreateAnnotationException {
        final String hypoAccountUser = ACCT + theUser.getLogin() + "@" + Authorities.ISC;

        final Group group = new Group("thegroup", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId()));

        final SimpleMetadata metaIscRef = new SimpleMetadata(ISCREF, ISCREFVAL);

        final UserInformation userInfo = new UserInformation(theUser, Authorities.ISC);
        userInfo.setCurrentToken(new Token(userRepos.findByLoginAndContext(theUser.getLogin(), null), userInfo.getAuthority(), "a1",
                LocalDateTime.now().plusMinutes(1), "r1", LocalDateTime.now().plusMinutes(1)));

        final Annotation annot = createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null);
        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null, Arrays.asList(annot.getId()));
        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null, Arrays.asList(annot.getId()));

        Assert.assertEquals(2, annotSearchService.getRepliesCountForAnnotation(annot, userInfo, group.getName()));
    }

    @Test
    public void testGetRepliesCountForAnnotation_ReplyToReply() throws CannotCreateAnnotationException {
        final String hypoAccountUser = ACCT + theUser.getLogin() + "@" + Authorities.ISC;

        final Group group = new Group("thegroup", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId()));

        final SimpleMetadata metaIscRef = new SimpleMetadata(ISCREF, ISCREFVAL);

        final UserInformation userInfo = new UserInformation(theUser, Authorities.ISC);
        userInfo.setCurrentToken(new Token(userRepos.findByLoginAndContext(theUser.getLogin(), null), userInfo.getAuthority(), "a1",
                LocalDateTime.now().plusMinutes(1), "r1", LocalDateTime.now().plusMinutes(1)));

        final Annotation annot = createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null);
        final Annotation reply = createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null,
                Arrays.asList(annot.getId()));
        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser, group.getName(), metaIscRef, userInfo, null,
                Arrays.asList(annot.getId(), reply.getId()));

        Assert.assertEquals(2, annotSearchService.getRepliesCountForAnnotation(annot, userInfo, group.getName()));
    }

    /**
     * prepare database content and execute test
     * 
     * annotations created by user 1:
     * - comment, SENT, ISCRef, response version 1
     * - comment, SENT, ISCRef, response version 2
     * - private comment, SENT, ISCRef, response version 3
     * - comment, ISCRef, SENT
     * - page note, SENT, ISCRef, response version 1, DELETED
     * - highlight, ISCRef, SENT
     * 
     * annotations created by user 2:
     * - comment, SENT, ISCRef, response version 1 
     */
    @SuppressFBWarnings(value = SpotBugsAnnotations.FieldNotInitialized, justification = SpotBugsAnnotations.FieldNotInitializedReason)
    private long runCountAnnotsWithMetadataAndUsers(final String metadataWithStatus,
            final Boolean shared, final String userLoginSearching, final String userSearchParameter)
            throws CannotCreateAnnotationException, MissingPermissionException, CannotDeleteAnnotationException, CannotDeleteSentAnnotationException {

        final String hypoAccountUser1 = ACCT + theUser.getLogin() + "@" + Authorities.ISC;
        final String hypoAccountUser2 = ACCT + secondUser.getLogin() + "@" + Authorities.ISC;

        // create the group and assign users to the group
        final Group group = new Group("thegroup", true);
        groupRepos.save(group);
        userGroupRepos.save(new UserGroup(theUser.getId(), group.getId()));
        userGroupRepos.save(new UserGroup(secondUser.getId(), group.getId()));

        // create UserInformation instances for the users creating annotations
        final UserInformation userInfo1 = new UserInformation(theUser, Authorities.ISC);
        userInfo1.setCurrentToken(new Token(userRepos.findByLoginAndContext(theUser.getLogin(), null), userInfo1.getAuthority(), "a1", LocalDateTime.now().plusMinutes(1),
                "r1", LocalDateTime.now().plusMinutes(1)));
        final UserInformation userInfo2 = new UserInformation(secondUser, Authorities.ISC);
        userInfo2.setCurrentToken(new Token(userRepos.findByLoginAndContext(secondUser.getLogin(), null), userInfo2.getAuthority(), "a2", LocalDateTime.now().plusMinutes(1),
                "r2", LocalDateTime.now().plusMinutes(1)));

        // prepare metadata: twice with two entries, once with a single entry
        final SimpleMetadata metaIscRef = new SimpleMetadata(ISCREF, ISCREFVAL);

        final SimpleMetadata metaIscRefVers1 = new SimpleMetadata(metaIscRef);
        metaIscRefVers1.put(Metadata.PROP_RESPONSE_VERSION, RESPVERS1);

        final SimpleMetadata metaIscRefVers2 = new SimpleMetadata(metaIscRef);
        metaIscRefVers2.put(Metadata.PROP_RESPONSE_VERSION, RESPVERS2);

        final SimpleMetadata metaIscRefVers3 = new SimpleMetadata(metaIscRef);
        metaIscRefVers3.put(Metadata.PROP_RESPONSE_VERSION, RESPVERS3);

        // create the above-mentioned annotations
        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser1, group.getName(),
                metaIscRefVers1, userInfo1, null);

        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser1, group.getName(),
                metaIscRefVers2, userInfo1, null);

        final JsonAnnotationPermissions permissions = new JsonAnnotationPermissions();
        permissions.setRead(Arrays.asList(hypoAccountUser1));
        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser1, group.getName(),
                metaIscRefVers3, userInfo1, permissions);

        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser1, group.getName(), 
                metaIscRef, userInfo1, null);
        

        createAnnotation(Annotation.ANNOTATION_HIGHLIGHT, hypoAccountUser1, group.getName(),
                metaIscRef, userInfo1, null);

        // create a page note and delete it directly
        final Annotation pageNoteDelRefVers1 = createAnnotation(Annotation.ANNOTATION_PAGENOTE,
                hypoAccountUser1, group.getName(), metaIscRefVers1, userInfo1, null);
        annotService.deleteAnnotationById(pageNoteDelRefVers1.getId(), userInfo1);

        // create annotation by second user
        createAnnotation(Annotation.ANNOTATION_COMMENT, hypoAccountUser2, group.getName(), 
                metaIscRefVers1, userInfo2, null);

        // set all saved metadata to have responseStatus SENT
        final List<Metadata> metas = (List<Metadata>) metadataRepos.findAll();
        for (final Metadata meta : metas) {
            meta.setResponseStatus(ResponseStatus.SENT);
            metadataRepos.save(meta);
        }

        // prepare options and launch the counting
        final AnnotationSearchCountOptions options = new AnnotationSearchCountOptions();
        options.setUri(dummyUri);
        options.setGroup(group.getName());
        options.setMetadatasets(metadataWithStatus);
        options.setShared(shared);
        options.setUser(userSearchParameter);

        final UserInformation userInfo = new UserInformation(userLoginSearching, null, Authorities.ISC);
        userInfo.setCurrentToken(new Token(userRepos.findByLoginAndContext(userLoginSearching, null), userInfo.getAuthority(), "a", LocalDateTime.now().plusMinutes(1),
                "r", LocalDateTime.now().plusMinutes(1)));

        return annotSearchService.getAnnotationsCount(options, userInfo);
    }
    
    private Annotation createAnnotation(final String tag, final String hypoAccount, final String groupName, final SimpleMetadata meta,
            final UserInformation creatingUser, final JsonAnnotationPermissions permissions)
            throws CannotCreateAnnotationException {
        return createAnnotation(tag, hypoAccount, groupName, meta, creatingUser, permissions, null);
    }

    private Annotation createAnnotation(final String tag, final String hypoAccount, final String groupName, final SimpleMetadata meta,
            final UserInformation creatingUser, final JsonAnnotationPermissions permissions, final List<String> references)
            throws CannotCreateAnnotationException {
        
        final JsonAnnotation annot = TestData.getTestAnnotationObject(hypoAccount);
        annot.getDocument().setLink(Arrays.asList(new JsonAnnotationDocumentLink(dummyUri)));
        annot.getDocument().setMetadata(meta);
        annot.setUri(dummyUri);
        annot.setGroup(groupName);
        if(Annotation.ANNOTATION_PAGENOTE.equals(tag)) {
            annot.setTags(null);
        } else {
            annot.setTags(Arrays.asList(tag));
        }
        if(permissions != null) {
            annot.setPermissions(permissions);
        }
        annot.setReferences(references);
        return annotService.createAnnotation(annot, creatingUser);
    }
}
