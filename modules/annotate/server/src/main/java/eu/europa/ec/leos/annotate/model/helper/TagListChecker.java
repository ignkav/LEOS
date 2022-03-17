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

import java.util.List;

import org.springframework.util.CollectionUtils;

import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Tag;

public final class TagListChecker {

    private TagListChecker() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * check if a tag identifying an annotation as a highlight is present in a given list of tags
     *  
     * @param tags
     *        list of tags to be examined
     * @return flag indicating whether any of the tags can be identified as a highlight tag
     */
    public static boolean hasHighlightTag(final List<Tag> tags) {

        if (CollectionUtils.isEmpty(tags)) {
            return false;
        }

        return tags.stream().anyMatch(tag -> tag.getName().equals(Annotation.ANNOTATION_HIGHLIGHT));
    }

    /**
     * check if a tag identifying an annotation as a suggestion is present in a given list of tags
     *  
     * @param tags
     *        list of tags to be examined
     * @return flag indicating whether any of the tags can be identified as a suggestion tag
     */
    public static boolean hasSuggestionTag(final List<Tag> tags) {

        if (CollectionUtils.isEmpty(tags)) {
            return false;
        }

        return tags.stream().anyMatch(tag -> tag.getName().equals(Annotation.ANNOTATION_SUGGESTION));
    }

    public static boolean hasTag(final List<Tag> tags, final String needle) {

        if (CollectionUtils.isEmpty(tags)) {
            return false;
        }

        return tags.stream().anyMatch(tag -> tag.getName().equals(needle));
    }

}
