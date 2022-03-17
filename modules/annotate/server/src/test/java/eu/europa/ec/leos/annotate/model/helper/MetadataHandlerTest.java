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

import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;
import eu.europa.ec.leos.annotate.model.entity.Metadata;

import org.assertj.core.api.StringAssert;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * simple tests for methods of the {@link MetadataHandler} class
 */
@SuppressWarnings("PMD.TooManyMethods")
public class MetadataHandlerTest {

    @Test
    public void testConvertToSimpleMetadataWithStatuses() {

        final Metadata meta = new Metadata();

        final SimpleMetadataWithStatuses response = MetadataHandler.convertToSimpleMetadataWithStatuses(meta);
        Assert.assertEquals(new SimpleMetadata(), response.getMetadata());
        Assert.assertEquals(1, response.getStatuses().size());
        Assert.assertEquals(AnnotationStatus.NORMAL, response.getStatuses().get(0));
    }

    @Test
    public void testConvertToSimpleMetadataWithStatusesList() {

        final List<Metadata> metadata = new ArrayList<Metadata>();
        final Metadata meta1 = new Metadata();
        metadata.add(meta1);
        final Metadata meta2 = new Metadata();
        meta2.setKeyValuePairs("a:b");
        metadata.add(meta2);

        final List<SimpleMetadataWithStatuses> response = MetadataHandler.convertToSimpleMetadataWithStatusesList(metadata);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals(new SimpleMetadata(), response.get(0).getMetadata());
        Assert.assertEquals(AnnotationStatus.NORMAL, response.get(0).getStatuses().get(0));
        Assert.assertEquals(new SimpleMetadata("a", "b"), response.get(1).getMetadata());
        Assert.assertEquals(AnnotationStatus.NORMAL, response.get(1).getStatuses().get(0));
    }

    @Test
    public void testGetIscReferenceEmpty() {

        final Metadata meta = new Metadata();
        Assert.assertEquals("", MetadataHandler.getIscReference(meta));
    }

    @Test
    public void testGetIscReferenceFound() {

        final String IscReference = "ISC/2015/048";
        final Metadata meta = new Metadata();
        meta.setKeyValuePairs("ISCReference:" + IscReference);

        Assert.assertEquals(IscReference, MetadataHandler.getIscReference(meta));
    }

    @Test
    public void testGetResponseId_Empty() {

        final Metadata meta = new Metadata();

        final StringAssert strAss = new StringAssert(MetadataHandler.getResponseId(meta));
        strAss.isNull();
    }

    @Test
    public void testGetResponseId() {

        final Metadata meta = new Metadata();
        meta.setKeyValuePairs("responseId:something");

        final StringAssert strAss = new StringAssert(MetadataHandler.getResponseId(meta));
        strAss.isEqualTo("something");
    }

    @Test
    public void testGetResponseVersion_Empty() {

        final Metadata meta = new Metadata();

        final long respVers = MetadataHandler.getResponseVersion(meta);
        Assert.assertEquals(-1L, respVers);
    }

    @Test
    public void testGetResponseVersion() {

        final Metadata meta = new Metadata();
        meta.setKeyValuePairs("responseVersion:4");

        final long respVers = MetadataHandler.getResponseVersion(meta);
        Assert.assertEquals(4L, respVers);
    }

    @Test
    public void testGetOriginMode_Empty() {

        final Metadata meta = new Metadata();

        final StringAssert strAss = new StringAssert(MetadataHandler.getOriginMode(meta));
        strAss.isNull();
    }

    @Test
    public void testGetOriginMode() {

        final Metadata meta = new Metadata();
        meta.setKeyValuePairs("originMode:private");

        final StringAssert strAss = new StringAssert(MetadataHandler.getOriginMode(meta));
        strAss.isEqualTo("private");
    }

    @Test
    public void testIsResponseInPreparation() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        Assert.assertTrue(MetadataHandler.isResponseStatusInPreparation(meta));
    }

    @Test
    public void testIsNotResponseInPreparation1() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.SENT);

        Assert.assertFalse(MetadataHandler.isResponseStatusInPreparation(meta));
    }

    @Test
    public void testIsNotResponseInPreparation2() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.UNKNOWN);

        Assert.assertFalse(MetadataHandler.isResponseStatusInPreparation(meta));
    }

    @Test
    public void testIsResponseSent() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.SENT);

        Assert.assertTrue(MetadataHandler.isResponseStatusSent(meta));
    }

    @Test
    public void testIsNotResponseSent1() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);

        Assert.assertFalse(MetadataHandler.isResponseStatusSent(meta));
    }

    @Test
    public void testIsNotResponseSent2() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.UNKNOWN);

        Assert.assertFalse(MetadataHandler.isResponseStatusSent(meta));
    }

    @Test
    public void testRemoveResponseVersion() {

        final Metadata meta = new Metadata();
        meta.setKeyValuePairs("responseVersion:4\nsomething:a");

        // act
        MetadataHandler.removeResponseVersion(meta);

        Assert.assertEquals("something:a\n", meta.getKeyValuePairs());
    }

    @Test
    public void testRemoveResponseVersion_noResponseVersionSet() {

        final Metadata meta = new Metadata();

        // act
        MetadataHandler.removeResponseVersion(meta);

        Assert.assertEquals("", meta.getKeyValuePairs());
    }

    @Test
    public void testSetResponseVersion() {

        final Metadata meta = new Metadata();

        // act
        MetadataHandler.setResponseVersion(meta, 4);

        Assert.assertEquals(4, MetadataHandler.getResponseVersion(meta));
        Assert.assertEquals("4", MetadataHandler.getAllMetadataAsSimpleMetadata(meta).get(Metadata.PROP_RESPONSE_VERSION));
    }

    @Test
    public void testSetOriginMode() {

        final Metadata meta = new Metadata();

        // act
        MetadataHandler.setOriginMode(meta, "pRiVaTe");

        Assert.assertEquals("pRiVaTe", MetadataHandler.getOriginMode(meta));
        Assert.assertEquals("pRiVaTe", MetadataHandler.getAllMetadataAsSimpleMetadata(meta).get(Metadata.PROP_ORIGIN_MODE));
    }

    @Test
    public void testUpdateIfDifferentResponseStatus() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);
        
        final Long userId = (long)5;
        final LocalDateTime timestamp = LocalDateTime.now(java.time.ZoneOffset.UTC);

        // act
        final boolean success = MetadataHandler.updateMetadataIfDifferentResponseStatus(meta, 
                ResponseStatus.SENT, userId, timestamp);
        
        // verify
        Assert.assertTrue(success);
        Assert.assertEquals(ResponseStatus.SENT, meta.getResponseStatus());
        Assert.assertNotNull(meta.getResponseStatusUpdated());
        Assert.assertEquals(timestamp, meta.getResponseStatusUpdated());
        Assert.assertEquals(userId,  meta.getResponseStatusUpdatedBy());
    }
    
    @Test
    public void testDontUpdateIfResponseStatusSame() {

        final Metadata meta = new Metadata();
        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);
        
        final Long userId = 5L;

        // act
        final boolean success = MetadataHandler.updateMetadataIfDifferentResponseStatus(meta, 
                ResponseStatus.IN_PREPARATION, userId, LocalDateTime.now(java.time.ZoneOffset.UTC));
        
        // verify
        Assert.assertFalse(success);
        
        // rest is unchanged
        Assert.assertEquals(ResponseStatus.IN_PREPARATION, meta.getResponseStatus());
        Assert.assertNull(meta.getResponseStatusUpdated());
        Assert.assertNull(meta.getResponseStatusUpdatedBy());
    }
    
    @Test
    public void testIncreaseResponseVersion() {
        
        final Metadata meta = new Metadata();
        meta.setKeyValuePairs("responseVersion:5");
        
        // act
        final boolean success = MetadataHandler.increaseResponseVersion(meta);
        
        // verify
        Assert.assertTrue(success);
        Assert.assertEquals(6, MetadataHandler.getResponseVersion(meta));
    }
    
    @Test
    public void testCannotIncreaseResponseVersion() {
        
        final Metadata meta = new Metadata();
        // no response version is set
        
        // act
        final boolean success = MetadataHandler.increaseResponseVersion(meta);
        
        // verify
        Assert.assertFalse(success);
        Assert.assertEquals(-1L, MetadataHandler.getResponseVersion(meta));
    }
}
