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

import java.util.List;
import java.util.stream.Collectors;

public final class AnnotationSearchResultHandler {

    private AnnotationSearchResultHandler() {
        // Prevent instantiation as all methods are static.
    }

    public static List<String> extractIds(final AnnotationSearchResult result) {
        return result.getItems().stream()
                .map(Annotation::getId)
                .collect(Collectors.toList());
    }

    public static List<Metadata> extractDistinctMetadata(final AnnotationSearchResult result) {
        return result.getItems().stream()
                .map(Annotation::getMetadata)
                .distinct()
                .collect(Collectors.toList());
    }

    public static void removeContainedPublicAnnotations(final AnnotationSearchResult result) {
        result.setItems(result.getItems().stream()
                .filter(ann -> !ann.isShared())
                .collect(Collectors.toList()));
        result.setTotalItems(result.getItems().size());
    }

}
