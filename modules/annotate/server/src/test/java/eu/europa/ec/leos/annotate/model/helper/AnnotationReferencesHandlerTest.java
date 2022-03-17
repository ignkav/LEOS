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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.europa.ec.leos.annotate.helper.SpotBugsAnnotations;
import eu.europa.ec.leos.annotate.model.entity.Annotation;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains tests for functions operating on {@link Annotation} entities in {@link AnnotationReferencesHandler}
 */
public class AnnotationReferencesHandlerTest {

    // annotation does not denote a reply -> check that respective properties return expected values accordingly
    @Test
    public void testNoReply() {

        final Annotation ann = new Annotation();
        ann.setId("id");

        // no reply!
        Assert.assertFalse(AnnotationReferencesHandler.isReply(ann));
        Assert.assertNull(AnnotationReferencesHandler.getReferencesList(ann));

        // change value, but still it is no reply!
        ann.setReferences("");
        Assert.assertFalse(AnnotationReferencesHandler.isReply(ann));
        Assert.assertNull(AnnotationReferencesHandler.getReferencesList(ann));
    }
    
    // set empty values for the references -> annotation should not be considered being a reply
    @SuppressFBWarnings(value = SpotBugsAnnotations.KnownNullValue, justification = SpotBugsAnnotations.KnownNullValueReason)
    @Test
    public void testAnnotationEmptyReferences() {
        
        final Annotation ann = new Annotation();
        ann.setId("id");
        
        List<String> referencesList = null;
        AnnotationReferencesHandler.setReferences(ann, referencesList);
        Assert.assertFalse(AnnotationReferencesHandler.isReply(ann));
        Assert.assertNull(AnnotationReferencesHandler.getReferencesList(ann));
        
        // different value, but still no references
        referencesList = new ArrayList<String>();
        AnnotationReferencesHandler.setReferences(ann, referencesList);
        Assert.assertFalse(AnnotationReferencesHandler.isReply(ann));
        Assert.assertNull(AnnotationReferencesHandler.getReferencesList(ann));
    }
    
    // set meaningful value to references -> annotation becomes a reply
    @Test
    public void testAnnotationBecomesReply() {
        
        final Annotation ann = new Annotation();
        ann.setId("id");
        Assert.assertFalse(AnnotationReferencesHandler.isReply(ann));
        
        // now set references to other items
        final List<String> referencesList = Arrays.asList("ref1", "ref2");
        AnnotationReferencesHandler.setReferences(ann, referencesList);
        
        // now it became a reply
        Assert.assertTrue(AnnotationReferencesHandler.isReply(ann));
        Assert.assertNotNull(AnnotationReferencesHandler.getReferencesList(ann));
        Assert.assertEquals(2, AnnotationReferencesHandler.getReferencesList(ann).size());
        Assert.assertTrue(AnnotationReferencesHandler.getReferencesList(ann).contains("ref1"));
        Assert.assertTrue(AnnotationReferencesHandler.getReferencesList(ann).contains("ref2"));
    }

}
