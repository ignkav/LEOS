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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Tag;

public final class TagBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(TagBuilder.class);

    private TagBuilder() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * conversion of string list of tags to {@link Tag} objects associated to an annotation
     * 
     * @param tags
     *        list of tag names to be converted
     * @param annotation
     *        the {@link Annotation} to which the tags are to be assigned after conversion
     * @return converted list of tags
     */
    public static List<Tag> getTagList(final List<String> tags, final Annotation annotation) {

        if (annotation == null) {
            LOG.error("Cannot save tags as belonging annotation is missing!");
            return null;
        }

        if (tags == null || tags.isEmpty()) {
            LOG.debug("No tags found for saving");
            return new ArrayList<>();
        }

        return tags.stream()
                .map(tag -> new Tag(tag, annotation))
                .collect(Collectors.toList());
    }

}
