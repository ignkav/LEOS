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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.helper.ColumnSensingReplacementDataSetLoader;
import eu.europa.ec.leos.annotate.helper.TestData;
import eu.europa.ec.leos.annotate.helper.TestDbHelper;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Tag;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.repository.TagRepository;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = "spring.config.name=anot")
@ActiveProfiles("test")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DbUnitConfiguration(dataSetLoader = ColumnSensingReplacementDataSetLoader.class)
public class TagsServiceTest {

    private User user;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private TagsService tagsService;

    @Autowired
    private AnnotationService annotService;

    @Autowired
    private TagRepository tagRepos;

    @Autowired
    private UserRepository userRepos;

    // -------------------------------------
    // Cleanup of database content
    // -------------------------------------
    @Before
    public void cleanDatabaseBeforeTests() {

        // user with ID 1 is created in DB creation files
        user = userRepos.findById(1);
    }

    @After
    public void cleanDatabaseAfterTests() {
        TestDbHelper.cleanupRepositories(this);
    }

    // -------------------------------------
    // Tests
    // -------------------------------------

    /**
     * test removing tags
     */
    @Test
    @DatabaseSetup("tagsservice_init.xml")
    public void testRemoveTags() throws CannotCreateAnnotationException {

        // an annotation with two tags is contained in the database
        // remove tags one by one
        tagsService.removeTags(((List<Tag>) tagRepos.findAll()).subList(0, 1));
        Assert.assertEquals(1, tagRepos.count());

        tagsService.removeTags((List<Tag>) tagRepos.findAll());
        Assert.assertEquals(0, tagRepos.count());

        // removing empty list should not throw error
        tagsService.removeTags(null);
    }

    @Test
    @DatabaseSetup("tagsservice_add_init.xml")
    public void testAddTags() throws CannotCreateAnnotationException {

        final String userlogin = "demo";

        // save an annotation with two tags
        final JsonAnnotation jsAnnot = TestData.getTestAnnotationObject(userlogin);

        final List<String> tagStrings = new ArrayList<String>();
        tagStrings.add("mytag");
        tagStrings.add("mysecondtag");

        jsAnnot.setTags(tagStrings);

        annotService.createAnnotation(jsAnnot, new UserInformation(user, Authorities.ISC)); // use the service in order to save effort...

        Assert.assertEquals(2, tagRepos.count());
    }

}
