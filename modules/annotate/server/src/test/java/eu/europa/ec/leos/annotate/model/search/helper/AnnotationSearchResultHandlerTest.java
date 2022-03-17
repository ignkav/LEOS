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

import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AnnotationSearchResultHandlerTest {

    @Test
    public void testExtractIds() {
        
        final AnnotationSearchResult result = new AnnotationSearchResult();
        final List<Annotation> annotations = new ArrayList<>();
        final Annotation annot1 = new Annotation();
        annot1.setId("a");
        annotations.add(annot1);
        final Annotation annot2 = new Annotation();
        annot2.setId("b");
        annotations.add(annot2);
        result.setItems(annotations);
        
        Assert.assertArrayEquals(new String[]{"a", "b"}, AnnotationSearchResultHandler.extractIds(result).toArray());
    }

    @Test
    public void testExtractDistinctMetadata() {

        final AnnotationSearchResult result = new AnnotationSearchResult();
        final List<Annotation> annotations = new ArrayList<>();
        final Annotation annot1 = new Annotation();
        final Metadata meta1 = new Metadata();
        meta1.setId(1L);
        annot1.setMetadata(meta1);
        annotations.add(annot1);
        
        final Annotation annot2 = new Annotation();
        final Metadata meta2 = new Metadata();
        meta2.setId(2L);
        annot2.setMetadata(meta2);
        annotations.add(annot2);
        result.setItems(annotations);

        Assert.assertEquals(2, AnnotationSearchResultHandler.extractDistinctMetadata(result).size());
        Assert.assertEquals(1L, AnnotationSearchResultHandler.extractDistinctMetadata(result).get(0).getId());
        Assert.assertEquals(2L, AnnotationSearchResultHandler.extractDistinctMetadata(result).get(1).getId());
    }

    @Test
    public void testExtractDistinctMetadata_SameMetadataTwice() {

        final AnnotationSearchResult result = new AnnotationSearchResult();
        final Metadata meta = new Metadata();
        final List<Annotation> annotations = new ArrayList<>();
        final Annotation annot1 = new Annotation();
        annot1.setMetadata(meta);
        annotations.add(annot1);
        final Annotation annot2 = new Annotation();
        annot2.setMetadata(meta);
        annotations.add(annot2);
        result.setItems(annotations);

        Assert.assertEquals(1, AnnotationSearchResultHandler.extractDistinctMetadata(result).size());
        Assert.assertEquals(0L, AnnotationSearchResultHandler.extractDistinctMetadata(result).get(0).getId());
    }

    @Test
    public void testRemoveContainedPublicAnnotations() {

        final AnnotationSearchResult result = new AnnotationSearchResult();
        final List<Annotation> annotations = new ArrayList<>();
        final Annotation annot1 = new Annotation();
        annot1.setId("a");
        annot1.setShared(false);
        annotations.add(annot1);
        final Annotation annot2 = new Annotation();
        annot2.setId("b");
        annot2.setShared(true);
        annotations.add(annot2);
        result.setItems(annotations);

        AnnotationSearchResultHandler.removeContainedPublicAnnotations(result);
        Assert.assertEquals(1, result.getItems().size());
        Assert.assertEquals("a", result.getItems().get(0).getId());
        Assert.assertEquals(1, result.getTotalItems());
    }

    @Test
    public void testRemoveContainedPublicAnnotations_NoneRemoved() {

        final AnnotationSearchResult result = new AnnotationSearchResult();
        final List<Annotation> annotations = new ArrayList<>();
        final Annotation annot1 = new Annotation();
        annot1.setId("a");
        annot1.setShared(false);
        annotations.add(annot1);
        final Annotation annot2 = new Annotation();
        annot2.setId("b");
        annot2.setShared(false);
        annotations.add(annot2);
        result.setItems(annotations);

        AnnotationSearchResultHandler.removeContainedPublicAnnotations(result);
        Assert.assertEquals(2, result.getItems().size());
        Assert.assertEquals("a", result.getItems().get(0).getId());
        Assert.assertEquals("b", result.getItems().get(1).getId());
        Assert.assertEquals(2, result.getTotalItems());
    }

    @Test
    public void testRemoveContainedPublicAnnotations_AllRemoved() {

        final AnnotationSearchResult result = new AnnotationSearchResult();
        final List<Annotation> annotations = new ArrayList<>();
        final Annotation annot1 = new Annotation();
        annot1.setId("a");
        annot1.setShared(true);
        annotations.add(annot1);
        final Annotation annot2 = new Annotation();
        annot2.setId("b");
        annot2.setShared(true);
        annotations.add(annot2);
        result.setItems(annotations);

        AnnotationSearchResultHandler.removeContainedPublicAnnotations(result);
        Assert.assertEquals(0, result.getItems().size());
        Assert.assertEquals(0, result.getTotalItems());
    }

}
