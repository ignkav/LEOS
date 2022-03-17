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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Tag;

public class TagListCheckerTest {

    @Test
    public void testIsHighlight() {

        final Annotation annot = new Annotation();
        final List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag(Annotation.ANNOTATION_HIGHLIGHT, annot));

        Assert.assertTrue(TagListChecker.hasHighlightTag(tagList));
    }

    @Test
    public void testIsNoHighlight() {

        final Annotation annot = new Annotation();
        final List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("whatever", annot));

        Assert.assertFalse(TagListChecker.hasHighlightTag(tagList));
    }

    @Test
    public void testIsHighlight_NoTags() {

        Assert.assertFalse(TagListChecker.hasHighlightTag(null));
    }

    @Test
    public void testIsHighlight_EmptyTagList() {

        Assert.assertFalse(TagListChecker.hasHighlightTag(new ArrayList<Tag>()));
    }

    @Test
    public void testIsSuggestion() {

        final Annotation annot = new Annotation();
        final List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag(Annotation.ANNOTATION_SUGGESTION, annot));

        Assert.assertTrue(TagListChecker.hasSuggestionTag(tagList));
    }

    @Test
    public void testIsNoSuggestion() {

        final Annotation annot = new Annotation();
        final List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("something", annot));

        Assert.assertFalse(TagListChecker.hasSuggestionTag(tagList));
    }

    @Test
    public void testIsSuggestion_NoTags() {

        Assert.assertFalse(TagListChecker.hasSuggestionTag(null));
    }

    @Test
    public void testIsSuggestion_EmptyTagList() {

        Assert.assertFalse(TagListChecker.hasSuggestionTag(new ArrayList<Tag>()));
    }

    @Test
    public void testHasTag() {

        final Annotation annot = new Annotation();
        final List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("a", annot));

        Assert.assertTrue(TagListChecker.hasTag(tagList, "a"));
    }

    @Test
    public void testHasTag_NotMatching() {

        final Annotation annot = new Annotation();
        final List<Tag> tagList = new ArrayList<Tag>();
        tagList.add(new Tag("a", annot));

        Assert.assertFalse(TagListChecker.hasTag(tagList, "b"));
    }

    @Test
    public void testHasTag_NoTags() {

        Assert.assertFalse(TagListChecker.hasTag(null, "a"));
    }

    @Test
    public void testHasTag_EmptyTagList() {

        Assert.assertFalse(TagListChecker.hasTag(new ArrayList<Tag>(), "a"));
    }

}
