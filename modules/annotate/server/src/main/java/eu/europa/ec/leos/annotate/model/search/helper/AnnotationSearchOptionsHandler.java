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

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchOptions;

/**
 * Helper functions for {@link AnnotationSearchOptions} objects
 */
public final class AnnotationSearchOptionsHandler {

    private AnnotationSearchOptionsHandler() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * @return the {@link Sort} object to be used for sorting database content
     * note: {@literal null} is returned when specified sorting criterion was invalid
     */
    public static Sort getSort(final AnnotationSearchOptions options) {

        if (StringUtils.isEmpty(options.getSortColumn())) {
            return null;
        }
        return new Sort(options.getOrder(), options.getSortColumn());
    }

    /**
     * sets the statuses of all metadata sets defined
     */
    public static void setStatuses(final AnnotationSearchOptions options, final List<AnnotationStatus> statuses) {
        if (options.getMetadataMapsWithStatusesList() == null) {
            options.setMetadataMapsWithStatusesList(new ArrayList<SimpleMetadataWithStatuses>());
        }
        if (CollectionUtils.isEmpty(options.getMetadataMapsWithStatusesList())) {
            options.getMetadataMapsWithStatusesList().add(new SimpleMetadataWithStatuses(null, null));
        }
        options.getMetadataMapsWithStatusesList().forEach(mmwsl -> mmwsl.setStatuses(statuses));
    }

}
