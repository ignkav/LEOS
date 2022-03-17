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
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.entity.Tag;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class AnnotationCheckerTest {

    @Test
    public void testHighlightIsRecognized() {

        final Annotation annot = new Annotation();
        annot.setTags(TagBuilder.getTagList(Arrays.asList(Annotation.ANNOTATION_HIGHLIGHT), annot));

        Assert.assertTrue(AnnotationChecker.isHighlight(annot));
    }

    @Test
    public void testCommentIsNotRecognizedAsHighlight() {

        final Annotation annot = new Annotation();
        annot.setTags(TagBuilder.getTagList(Arrays.asList(Annotation.ANNOTATION_COMMENT), annot));

        Assert.assertFalse(AnnotationChecker.isHighlight(annot));
    }

    @Test
    public void testSuggestionIsNotRecognizedAsHighlight() {

        final Annotation annot = new Annotation();
        annot.setTags(TagBuilder.getTagList(Arrays.asList(Annotation.ANNOTATION_SUGGESTION), annot));

        Assert.assertFalse(AnnotationChecker.isHighlight(annot));
    }

    @Test
    public void testAnnotationWithoutTagsIsNotRecognizedAsHighlight() {

        final Annotation annot = new Annotation();
        annot.setTags(null);

        Assert.assertFalse(AnnotationChecker.isHighlight(annot));

        // set empty list of tags -> still no suggestion
        annot.setTags(new ArrayList<Tag>());
        Assert.assertFalse(AnnotationChecker.isHighlight(annot));
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testUndefinedAnnotationIsNotRecognizedAsHighlight() {

        AnnotationChecker.isHighlight(null);
    }

    @Test
    public void testResponseSent() {

        final Annotation ann = new Annotation();

        Assert.assertFalse(AnnotationChecker.isResponseStatusSent(ann));

        final Metadata meta = new Metadata();
        ann.setMetadata(meta);
        Assert.assertFalse(AnnotationChecker.isResponseStatusSent(ann));

        meta.setResponseStatus(ResponseStatus.IN_PREPARATION);
        Assert.assertFalse(AnnotationChecker.isResponseStatusSent(ann));

        meta.setResponseStatus(ResponseStatus.SENT);
        Assert.assertTrue(AnnotationChecker.isResponseStatusSent(ann));
    }

    @Test
    public void testSuggestionIsRecognized() {

        final Annotation annot = new Annotation();
        annot.setTags(TagBuilder.getTagList(Arrays.asList(Annotation.ANNOTATION_SUGGESTION), annot));

        Assert.assertTrue(AnnotationChecker.isSuggestion(annot));
    }

    @Test
    public void testCommentIsNotRecognizedAsSuggestion() {

        final Annotation annot = new Annotation();
        annot.setTags(TagBuilder.getTagList(Arrays.asList(Annotation.ANNOTATION_COMMENT), annot));

        Assert.assertFalse(AnnotationChecker.isSuggestion(annot));
    }

    @Test
    public void testHighlightIsNotRecognizedAsSuggestion() {

        final Annotation annot = new Annotation();
        annot.setTags(TagBuilder.getTagList(Arrays.asList(Annotation.ANNOTATION_HIGHLIGHT), annot));

        Assert.assertFalse(AnnotationChecker.isSuggestion(annot));
    }

    @Test
    public void testAnnotationWithoutTagsIsNotRecognizedAsSuggestion() {

        final Annotation annot = new Annotation();
        annot.setTags(null);

        Assert.assertFalse(AnnotationChecker.isSuggestion(annot));

        // set empty list of tags -> still no suggestion
        annot.setTags(new ArrayList<Tag>());
        Assert.assertFalse(AnnotationChecker.isSuggestion(annot));
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressFBWarnings(value = SpotBugsAnnotations.ExceptionIgnored, justification = SpotBugsAnnotations.ExceptionIgnored)
    public void testUndefinedAnnotationIsNotRecognizedAsSuggestion() {

        AnnotationChecker.isSuggestion(null);
    }

}
