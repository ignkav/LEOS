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

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.entity.*;
import eu.europa.ec.leos.annotate.repository.*;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
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

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public class AnnotationServiceTest {

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private AnnotationService annotService;

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
    public void testMakeShared_noItems() throws CannotCreateAnnotationException {

        createAnnotation();

        // try making them public
        annotService.makeShared(null);

        // check that nothing was changed in the repository
        final List<Annotation> allAnnots = (List<Annotation>) annotRepos.findAll();
        Assert.assertFalse(allAnnots.get(0).isShared());
    }

    @Test
    public void testMakeShared_OneItem() throws CannotCreateAnnotationException {

        final Annotation annot = createAnnotation();
        
        // try making them public
        annotService.makeShared(Arrays.asList(annot));

        // check that the single item was changed in the repository
        final List<Annotation> allAnnots = (List<Annotation>) annotRepos.findAll();
        Assert.assertEquals(1, allAnnots.size());
        Assert.assertTrue(allAnnots.get(0).isShared());
    }
    
    private Annotation createAnnotation() {
        
        final Document doc = new Document(URI.create("http://some5.url"), "some5 title");
        documentRepos.save(doc);

        final Group group = new Group("secretgroup5", true);
        groupRepos.save(group);

        final User user = new User("its_me4");
        userRepos.save(user);
        userGroupRepos.save(new UserGroup(user.getId(), group.getId()));

        final Metadata meta = new Metadata(doc, group, Authorities.ISC);
        metadataRepos.save(meta);

        final Annotation annot = new Annotation();
        annot.setId("annot_xy");
        annot.setCreated(LocalDateTime.now(java.time.ZoneOffset.UTC));
        annot.setUpdated(LocalDateTime.now(java.time.ZoneOffset.UTC));
        annot.setText("text");
        annot.setUser(user);
        annot.setMetadata(meta);
        annot.setTargetSelectors("ab");
        annot.setShared(false);
        return annotRepos.save(annot);
    }
    
    @Test
    public void testSaveUpdatedTimestamp_noItem() {
        
        final LocalDateTime modifiedTime = LocalDateTime.now().minusDays(1);
        final Annotation annot = createAnnotation();
        annot.setUpdated(modifiedTime);
        annotRepos.save(annot);
        
        // act
        annotService.saveWithUpdatedTimestamp(null, LocalDateTime.now());
        
        // verify that nothing was updated
        final Annotation readAnnot = annotService.findAnnotationById(annot.getId());
        Assert.assertEquals(modifiedTime, readAnnot.getUpdated());
    }
    
    @Test
    public void testSaveUpdatedTimestamp_oneItem() {
        
        final LocalDateTime modifiedTime = LocalDateTime.now().minusDays(1);
        final Annotation annot = createAnnotation();
        annot.setUpdated(modifiedTime);
        annotRepos.save(annot);
        
        // act
        annotService.saveWithUpdatedTimestamp(Arrays.asList(annot), LocalDateTime.now());
        
        // verify that the 'updated' field was changed
        final Annotation readAnnot = annotService.findAnnotationById(annot.getId());
        Assert.assertNotEquals(modifiedTime, readAnnot.getUpdated());
    }
}
