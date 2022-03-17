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
package eu.europa.ec.leos.annotate.model.search.helper;

import java.util.Arrays;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Sort.Direction;

import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchOptions;

public class AnnotationSearchOptionsHandlerTest {

    private static final String URL = "http://the.url";
    private static final String WORLD_GROUP = "__world__";
    private static final String CREATED = "created";
    private static final String ASC = "asc";

    // check that valid sort columns are accepted as being valid
    @Test
    public void testValidSortColumnCreated() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL, // URI
                WORLD_GROUP, // group
                true, // separateReplies
                200, // limit
                0, // offset
                ASC, // order
                CREATED); // sortColumn
        Assert.assertNotNull(options);
        Assert.assertNotNull(AnnotationSearchOptionsHandler.getSort(options));
    }

    // check that valid sort columns are accepted as being valid
    @Test
    public void testValidSortColumnUpdated() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL, // URI
                WORLD_GROUP, // group
                true, // separateReplies
                200, // limit
                0, // offset
                "desc", // order
                "updated"); // sortColumn
        Assert.assertNotNull(options);
        Assert.assertNotNull(AnnotationSearchOptionsHandler.getSort(options));
        Assert.assertEquals(Direction.DESC, options.getOrder());
    }

    // check that valid sort columns are accepted as being valid
    @Test
    public void testValidSortColumnShared() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL, // URI
                WORLD_GROUP, // group
                true, // separateReplies
                200, // limit
                0, // offset
                ASC, // order
                "shared"); // sortColumn
        Assert.assertNotNull(options);
        Assert.assertNotNull(AnnotationSearchOptionsHandler.getSort(options));
        Assert.assertEquals("shared", options.getSortColumn());
        Assert.assertEquals(Direction.ASC, options.getOrder());
    }

    // check that invalid sort columns are ignored
    @Test
    public void testInvalidSortColumnText() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL, // URI
                WORLD_GROUP, // group
                true, // separateReplies
                200, // limit
                0, // offset
                ASC, // order
                "text"); // sortColumn: invalid
        Assert.assertNotNull(options);
        Assert.assertNull("invalid sorting column was not ignored", AnnotationSearchOptionsHandler.getSort(options));
    }

    // check that invalid sort columns are ignored
    @Test
    public void testInvalidSortColumnReferences() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL, // URI
                WORLD_GROUP, // group
                true, // separateReplies
                200, // limit
                0, // offset
                ASC, // order
                "references"); // sortColumn: invalid
        Assert.assertNotNull(options);
        Assert.assertNull("invalid sorting column was not ignored", AnnotationSearchOptionsHandler.getSort(options));

        options.setSortColumn("");
        Assert.assertEquals("", options.getSortColumn());
        Assert.assertNull(AnnotationSearchOptionsHandler.getSort(options));
    }

    // check that setting status null still keeps the NORMAL status
    @Test
    public void testSetStatuses() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL, // URI, is valid
                WORLD_GROUP, // group
                true, // separateReplies
                200, // limit
                0, // offset
                ASC, // order
                "text4"); // sortColumn

        // create two metadata sets
        final SimpleMetadataWithStatuses smws1 = new SimpleMetadataWithStatuses();
        smws1.getMetadata().put("key1", "value1");
        smws1.setStatuses(Arrays.asList(AnnotationStatus.DELETED));

        final SimpleMetadataWithStatuses smws2 = new SimpleMetadataWithStatuses();
        smws2.getMetadata().put("key2", "value2");
        smws2.getMetadata().put("key3", "value3");
        smws2.setStatuses(Arrays.asList(AnnotationStatus.ACCEPTED, AnnotationStatus.NORMAL));

        options.setMetadataMapsWithStatusesList(Arrays.asList(smws1, smws2));

        // act
        AnnotationSearchOptionsHandler.setStatuses(options, Arrays.asList(AnnotationStatus.NORMAL, AnnotationStatus.REJECTED));

        // verify that the new statuses have been applied to all metadata sets
        Assert.assertEquals(2, options.getMetadataMapsWithStatusesList().size());
        options.getMetadataMapsWithStatusesList().forEach(
                smws -> MatcherAssert.assertThat(smws.getStatuses(),
                        org.hamcrest.Matchers.containsInAnyOrder(AnnotationStatus.NORMAL, AnnotationStatus.REJECTED)));
    }

}
