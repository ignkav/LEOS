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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchOptions;
import eu.europa.ec.leos.annotate.model.search.Consts;
import eu.europa.ec.leos.annotate.model.web.IncomingSearchOptions;

public class AnnotationSearchOptionsBuilderTest {

    private static final String ASC = "asc";

    // check that creation of AnnotationSearchOptions from IncomingSearchOptions fails when no input is given
    @Test(expected = IllegalArgumentException.class)
    public void testInitFromIncomingSearchOptionsFails() {

        AnnotationSearchOptionsBuilder.fromIncomingSearchOptions(null, true);
    }

    // check successful that creation of AnnotationSearchOptions from IncomingSearchOptions
    @Test
    public void testInitFromIncomingSearchOptions() {

        final IncomingSearchOptions incOpts = new IncomingSearchOptions();
        incOpts.setAny("any");
        incOpts.setGroup("myGroup");
        incOpts.setLimit(48);
        incOpts.setOffset(5);
        incOpts.setOrder(ASC);
        incOpts.setSort("created");
        incOpts.setMetadatasets("[{\"prop1\":\"val1\", \"status\":[\"DELETED\"]}," +
                "{\"set2prop1\":\"val2\", \"status\": [\"ACCEPTED\",\"REJECTED\"]}]");
        incOpts.setTag("tag");
        incOpts.setUri("https://leos/84");
        incOpts.setUser("user");
        incOpts.setMode("private");
        incOpts.setShared(true);

        final AnnotationSearchOptions opts = AnnotationSearchOptionsBuilder.fromIncomingSearchOptions(incOpts, true);
        Assert.assertNotNull(opts);
        Assert.assertEquals(true, opts.isSeparateReplies());
        Assert.assertEquals(incOpts.getGroup(), opts.getGroup());

        Assert.assertEquals(incOpts.getLimit(), opts.getItemLimit());
        Assert.assertEquals(incOpts.getOffset(), opts.getItemOffset());

        Assert.assertEquals("created", opts.getSortColumn());
        Assert.assertEquals("ASC", opts.getOrder().name());

        Assert.assertEquals(2, opts.getMetadataMapsWithStatusesList().size());
        final SimpleMetadata meta0 = opts.getMetadataMapsWithStatusesList().get(0).getMetadata();
        Assert.assertEquals(1, meta0.size());
        Assert.assertEquals("val1", meta0.get("prop1"));

        final List<AnnotationStatus> status0 = opts.getMetadataMapsWithStatusesList().get(0).getStatuses();
        Assert.assertEquals(1, status0.size());
        Assert.assertEquals(AnnotationStatus.DELETED, status0.get(0));

        final SimpleMetadata meta1 = opts.getMetadataMapsWithStatusesList().get(1).getMetadata();
        Assert.assertEquals(1, meta1.size());
        Assert.assertEquals("val2", meta1.get("set2prop1"));

        final List<AnnotationStatus> status1 = opts.getMetadataMapsWithStatusesList().get(1).getStatuses();
        Assert.assertEquals(2, status1.size());
        Assert.assertEquals(AnnotationStatus.ACCEPTED, status1.get(0));
        Assert.assertEquals(AnnotationStatus.REJECTED, status1.get(1));

        Assert.assertEquals(incOpts.getUri(), opts.getUri().toString());

        Assert.assertEquals(Consts.SearchUserType.Contributor, opts.getSearchUser()); // due to "private" as "mode" parameter

        Assert.assertEquals(incOpts.getShared(), opts.getShared());
    }

    // check that a standard user is set when unknown "mode" parameter is set
    @Test
    public void testInitFromIncomingSearchOptions2() {

        final IncomingSearchOptions incOpts = new IncomingSearchOptions();
        incOpts.setUri("https://leos/4");
        incOpts.setMode("something");

        final AnnotationSearchOptions opts = AnnotationSearchOptionsBuilder.fromIncomingSearchOptions(incOpts, true);
        Assert.assertNotNull(opts);
        Assert.assertEquals(Consts.SearchUserType.Unknown, opts.getSearchUser());
        Assert.assertNull(opts.getShared()); // check default
    }

}
