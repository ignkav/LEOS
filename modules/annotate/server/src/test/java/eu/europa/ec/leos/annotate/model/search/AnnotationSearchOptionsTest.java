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
package eu.europa.ec.leos.annotate.model.search;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests on the AnnotationSearchOptions class' default values and behaviour
 */
public class AnnotationSearchOptionsTest {

    private static final String URL = "http://the.url";
    private static final String WORLD_GROUP = "__world__";
    private static final String CREATED = "created";
    private static final String ASC = "asc";

    // if negative limit is given, options should be prepared to be able to retrieve all items
    @Test
    public void testNegativeLimitSetToMaximum_Offset0() {

        // giving a negative limit should set the limit to the maximum Integer value
        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,         // URI
                WORLD_GROUP, // group
                true,        // separateReplies
                -1,          // limit
                0,           // offset
                ASC,         // order
                CREATED);    // sortColumn
        Assert.assertEquals("given negative limit parameter was not increased to maximum possible value",
                Integer.MAX_VALUE, options.getItemLimit()); // was increased to maximum value
    }

    // if negative limit is given, options should be prepared to be able to retrieve all items
    @Test
    public void testNegativeLimitSetToMaximum_OffsetPositive() {

        // negative limit, set an arbitrary offset -> offset should be set to 0, since we want all items without paging
        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,         // URI
                WORLD_GROUP, // group
                true,        // separateReplies
                -1,          // limit
                50,          // offset
                ASC,         // order
                CREATED);    // sortColumn
        Assert.assertEquals("given negative limit parameter was not increased to maximum possible value",
                Integer.MAX_VALUE, options.getItemLimit()); // was increased to maximum value
        Assert.assertEquals("offset was not set to default (due to negative limit value)",
                Consts.DEFAULT_SEARCH_OFFSET, options.getItemOffset()); // was increased to maximum value
    }

    // if negative limit is given, options should be prepared to be able to retrieve all items
    @Test
    public void testNegativeLimitSetToMaximum_OffsetNegative() {

        // negative limit, set a negative offset -> offset should be set to default
        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,         // URI
                WORLD_GROUP, // group
                true,        // separateReplies
                -1,          // limit
                -5,          // offset
                ASC,         // order
                CREATED);    // sortColumn
        Assert.assertEquals("given negative limit parameter was not increased to maximum possible value",
                Integer.MAX_VALUE, options.getItemLimit()); // was increased to maximum value
        Assert.assertEquals("offset was not set to default (due to negative limit value)",
                Consts.DEFAULT_SEARCH_OFFSET, options.getItemOffset()); // was increased to maximum value
    }

    // any given positive maximum limit is kept now
    @Test
    public void testMaximumLimitAccepted() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,         // URI
                WORLD_GROUP, // group
                true,        // separateReplies
                5000,        // limit
                0,           // offset
                ASC,         // order
                CREATED);    // sortColumn
        Assert.assertEquals("given positive limit value was not kept", 5000, options.getItemLimit()); // was kept
        Assert.assertEquals(0, options.getItemOffset()); // was kept
    }

    // negative offset is set to zero
    @Test
    public void testNegativeOffsetCorrected() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,         // URI
                WORLD_GROUP, // group
                true,        // separateReplies
                200,         // limit
                -1,          // offset
                ASC,         // order
                CREATED);    // sortColumn
        Assert.assertEquals("given invalid negative offset value was not set to 0",
                0, options.getItemOffset()); // was increased to non-negative value
    }

    // default values are used if given limit is 0
    @Test
    public void testDefaultValuesWhenLimitIsZero_NegativeOffset() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,         // URI
                WORLD_GROUP, // group
                true,        // separateReplies
                0,           // limit
                -1,          // offset: will be set to default as well since value is invalid
                ASC,         // order
                CREATED);    // sortColumn
        Assert.assertEquals("given invalid zero limit was not set to default",
                Consts.DEFAULT_SEARCH_LIMIT, options.getItemLimit()); // was set to default
        Assert.assertEquals("offset value was not set to default",
                Consts.DEFAULT_SEARCH_OFFSET, options.getItemOffset()); // was set to default
    }

    // default values are used if given limit is 0
    @Test
    public void testDefaultValuesWhenLimitIsZero_PositiveOffset() {

        // offset has a valid value and is thus not set back to default offset value
        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,         // URI
                WORLD_GROUP, // group
                true,        // separateReplies
                0,           // limit
                10,          // offset: will be kept (not set to default) as value is valid
                ASC,         // order
                CREATED);    // sortColumn
        Assert.assertEquals("given invalid zero limit was not set to default",
                Consts.DEFAULT_SEARCH_LIMIT, options.getItemLimit()); // was set to default
        Assert.assertEquals("offset value was modified, should not",
                10, options.getItemOffset()); // was set to default
    }

    // check that invalid URIs are not accepted in constructor
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUriInConstructor() {

        new AnnotationSearchOptions(
                "invalid^url", // URI, is invalid
                WORLD_GROUP,   // group
                true,          // separateReplies
                200,           // limit
                0,             // offset
                ASC,           // order
                "text2");       // sortColumn
    }

    // check that invalid URIs are not accepted during updating URI
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUriInSetter() {

        final AnnotationSearchOptions options = new AnnotationSearchOptions(
                URL,              // URI, is valid
                WORLD_GROUP,      // group
                true,             // separateReplies
                200,              // limit
                0,                // offset
                ASC,              // order
                "text2");          // sortColumn
        options.setUri("other^url"); // previously valid URI is overwritten -> exception!
    }
}
