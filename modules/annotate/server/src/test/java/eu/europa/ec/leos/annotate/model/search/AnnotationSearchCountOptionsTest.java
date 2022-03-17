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

import org.assertj.core.api.StringAssert;
import org.junit.Assert;
import org.junit.Test;

/**
 * tests for the {@link AnnotationSearchCountOptions} POJO
 */
public class AnnotationSearchCountOptionsTest {

    @Test
    public void testValidUri() {

        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://val.id", "group", true, "");
        Assert.assertNotNull(asco);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUri() {

        new AnnotationSearchCountOptions("http://in^val.id", "group", true, "");
    }

    @Test
    public void testNothingToDecodeWhenMetadataEmpty() {

        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://some.url", "thegroup", true, "");
        Assert.assertEquals("", asco.getMetadatasets());

        // act
        asco.decodeEscapedBrackets();
        Assert.assertEquals("", asco.getMetadatasets());

    }

    @Test
    public void testDecodeMetadataEncoding() {

        final String metadata = "%7B\"prop\":\"val\\end\"%7D";
        final String metadataDecoded = "{\"prop\":\"valend\"}";
        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://some.url", "thegroup", true, metadata);
        Assert.assertEquals(metadata, asco.getMetadatasets());

        // act
        asco.decodeEscapedBrackets();
        Assert.assertEquals(metadataDecoded, asco.getMetadatasets());

    }

    @Test
    public void testValidGroup() {

        final String groupName = "group";
        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://val2.id", groupName, true, "");

        Assert.assertNotNull(asco);
        Assert.assertEquals(groupName, asco.getGroup());
    }

    @Test
    public void testSharedAsPublic() {
        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://val3.id", "group2", true, "");

        Assert.assertNotNull(asco);
        Assert.assertTrue(asco.getShared());
    }

    @Test
    public void testSharedAsPrivate() {
        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://val4.id", "group4", false, "");

        Assert.assertNotNull(asco);
        Assert.assertFalse(asco.getShared());
    }

    @Test
    public void testSharedUndefined() {
        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://my.link", "anygroup", null, "");

        Assert.assertNotNull(asco);
        Assert.assertNull(asco.getShared());
    }

    @Test
    public void testNoUserByDefault() {
        final AnnotationSearchCountOptions asco = new AnnotationSearchCountOptions("http://val5.id", "group5", false, "");

        Assert.assertNotNull(asco);
        final StringAssert strAss = new StringAssert(asco.getUser());
        strAss.isNullOrEmpty();
    }
}
