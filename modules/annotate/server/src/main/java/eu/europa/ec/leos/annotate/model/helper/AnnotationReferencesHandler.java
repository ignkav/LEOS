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

import java.util.Arrays;
import java.util.List;

import eu.europa.ec.leos.annotate.model.entity.Annotation;

/**
 * Helper functions for {@link Annotation} entities
 */
public final class AnnotationReferencesHandler {

    private AnnotationReferencesHandler() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * returns a list of referenced annotation IDs
     */
    public static List<String> getReferencesList(final Annotation annotation) {

        final String references = annotation.getReferences();
        if (references == null || references.isEmpty()) {
            return null;
        }
        // split by comma
        return Arrays.asList(references.split(","));
    }

    /**
     * states whether the object denotes a reply to an annotation
     */
    public static boolean isReply(final Annotation annotation) {
        return annotation.getReferences() != null && !annotation.getReferences().isEmpty();
    }

    /**
     * sets a list of referenced annotation IDs
     */
    public static void setReferences(final Annotation annotation, final List<String> references) {

        if (references == null || references.isEmpty()) {
            annotation.setReferences(null);
        } else {
            annotation.setReferences(String.join(",", references));
        }
    }

}
