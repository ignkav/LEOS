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

public class TagBuilderTest {

    /**
     * test the conversion of a list of strings to Tags objects
     */
    @Test
    public void testGetTagList() {

        final Annotation annot = new Annotation();

        // generate a list of a random number of strings representing tags
        final java.util.Random rand = new java.util.Random();
        final int numberOfTags = rand.nextInt(100) + 1;

        final List<String> tagStrings = new ArrayList<String>();
        for (int i = 0; i < numberOfTags; i++) {
            tagStrings.add("tag" + i);
        }

        // let the list be generated to tag objects associated to a given annotation
        final List<Tag> tagList = TagBuilder.getTagList(tagStrings, annot);

        // verify assignment to annotation and that the tag is part of the initial list
        Assert.assertNotNull(tagList);
        Assert.assertEquals(numberOfTags, tagList.size());

        for (final Tag t : tagList) {
            Assert.assertEquals(annot, t.getAnnotation());
            Assert.assertTrue(tagStrings.contains(t.getName()));
        }

        // retrieve all tag names generated
        final List<String> generatedTagNames = new ArrayList<String>();
        tagList.stream().forEach(tag -> generatedTagNames.add(tag.getName()));

        // check if this list is identical to the original list
        Assert.assertTrue(generatedTagNames.removeAll(tagStrings));
        Assert.assertEquals(0, generatedTagNames.size());
    }

    /**
     * test that empty list of tag Strings does not produce any tags
     */
    @Test
    public void testGetTagList_EmptyStringList() {

        final Annotation annot = new Annotation();

        // empty list of Strings should not produce any result
        final List<String> tagStrings = new ArrayList<String>();

        List<Tag> tagList = TagBuilder.getTagList(tagStrings, annot);
        Assert.assertNotNull(tagList);
        Assert.assertEquals(0, tagList.size());

        // null list should not produce any result either
        tagList = TagBuilder.getTagList(null, annot);
        Assert.assertNotNull(tagList);
        Assert.assertEquals(0, tagList.size());
    }

    /**
     * test that missing annotation does not produce any tags
     */
    @Test
    public void testGetTagList_MissingAnnotationDoesNotProduceTags() {

        // empty list of Strings should not produce any result
        final List<String> tagStrings = new ArrayList<String>();
        tagStrings.add("mytag");

        final List<Tag> tagList = TagBuilder.getTagList(tagStrings, null);
        Assert.assertNull(tagList);
    }
}
