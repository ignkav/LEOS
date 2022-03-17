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
package eu.europa.ec.leos.annotate.model.helper;

import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MetadataListFilterTest {

    // test various filters for different ISC references on a given list of metadata items
    @Test
    public void testFilterIscReference() {

        final List<Metadata> metaList = new ArrayList<Metadata>();

        final Metadata meta1 = new Metadata();
        final SimpleMetadata simpleMeta1 = new SimpleMetadata(Metadata.PROP_ISC_REF, "ISC/1");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta1, simpleMeta1);
        metaList.add(meta1);

        final Metadata meta1_2 = new Metadata(meta1);
        metaList.add(meta1_2);

        final Metadata meta2 = new Metadata();
        final SimpleMetadata simpleMeta2 = new SimpleMetadata(Metadata.PROP_ISC_REF, "ISC/2");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta2, simpleMeta2);
        metaList.add(meta2);

        final Metadata meta3 = new Metadata();
        metaList.add(meta3);

        List<Metadata> filtered = MetadataListFilter.filterByIscReference(metaList, "ISC/1");
        Assert.assertEquals(2, filtered.size());

        filtered = MetadataListFilter.filterByIscReference(metaList, "ISC/2");
        Assert.assertEquals(1, filtered.size());

        filtered = MetadataListFilter.filterByIscReference(metaList, "ISC/3");
        Assert.assertEquals(0, filtered.size());
    }

    // test that filtering for an empty ISC reference returns the input
    @Test
    public void testFilterIscReference_emptyFilter() {

        final List<Metadata> metaList = new ArrayList<Metadata>();

        final Metadata meta1 = new Metadata();
        final SimpleMetadata simpleMeta1 = new SimpleMetadata(Metadata.PROP_ISC_REF, "ISC/2020/2");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta1, simpleMeta1);
        metaList.add(meta1);

        final List<Metadata> filtered = MetadataListFilter.filterByIscReference(metaList, "");
        Assert.assertEquals(1, filtered.size());
        Assert.assertEquals(meta1, filtered.get(0));
    }

    // filter an empty list of Metadata
    @Test
    public void testFilterIscReference_emptyList() {

        final List<Metadata> metaList = new ArrayList<Metadata>();

        final List<Metadata> filtered = MetadataListFilter.filterByIscReference(metaList, "ISC/4");
        Assert.assertEquals(0, filtered.size());
    }

    // test various filters for different response versions on a given list of metadata items
    @Test
    public void testFilterResponseVersion() {

        final List<Metadata> metaList = new ArrayList<Metadata>();

        final Metadata meta1 = new Metadata();
        final SimpleMetadata simpleMeta1 = new SimpleMetadata(Metadata.PROP_RESPONSE_VERSION, "1");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta1, simpleMeta1);
        metaList.add(meta1);

        final Metadata meta1_2 = new Metadata(meta1);
        metaList.add(meta1_2);

        final Metadata meta2 = new Metadata();
        final SimpleMetadata simpleMeta2 = new SimpleMetadata(Metadata.PROP_RESPONSE_VERSION, "2");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta2, simpleMeta2);
        metaList.add(meta2);

        final Metadata meta3 = new Metadata();
        final SimpleMetadata simpleMeta3 = new SimpleMetadata(Metadata.PROP_RESPONSE_VERSION, "3");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta3, simpleMeta3);
        metaList.add(meta3);

        List<Metadata> filtered = MetadataListFilter.filterByResponseVersion(metaList, "1");
        Assert.assertEquals(2, filtered.size());

        filtered = MetadataListFilter.filterByResponseVersion(metaList, "2");
        Assert.assertEquals(1, filtered.size());

        filtered = MetadataListFilter.filterByResponseVersion(metaList, "3");
        Assert.assertEquals(1, filtered.size());

        filtered = MetadataListFilter.filterByResponseVersion(metaList, "4");
        Assert.assertEquals(0, filtered.size());
    }

    // test that filtering for an empty response version returns the input
    @Test
    public void testFilterResponseVersion_emptyFilter() {

        final List<Metadata> metaList = new ArrayList<Metadata>();

        final Metadata meta1 = new Metadata();
        final SimpleMetadata simpleMeta1 = new SimpleMetadata(Metadata.PROP_RESPONSE_VERSION, "5");
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(meta1, simpleMeta1);
        metaList.add(meta1);

        final List<Metadata> filtered = MetadataListFilter.filterByResponseVersion(metaList, "");
        Assert.assertEquals(1, filtered.size());
        Assert.assertEquals(meta1, filtered.get(0));
    }

    // filter an empty list of Metadata
    @Test
    public void testFilterResponseVersion_emptyList() {

        final List<Metadata> metaList = new ArrayList<Metadata>();

        final List<Metadata> filtered = MetadataListFilter.filterByResponseVersion(metaList, "4");
        Assert.assertEquals(0, filtered.size());
    }

}
