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

import java.util.List;
import java.util.Locale;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchOptions;
import eu.europa.ec.leos.annotate.model.search.Consts;
import eu.europa.ec.leos.annotate.model.web.IncomingSearchOptions;
import eu.europa.ec.leos.annotate.model.web.helper.JsonStringDeserializer;

/**
 * Helper functions to build {@link AnnotationSearchOptions} objects
 */
public final class AnnotationSearchOptionsBuilder {

    private AnnotationSearchOptionsBuilder() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * creation of new search options based on {@link IncomingSearchOptions} instance
     * 
     * @param inOpts instance of {@link IncomingSearchOptions}
     * @param separate_replies flag indicating whether separate replies are wanted (separate options since it isn't properly mapped by Spring)
     * @return initialised {@link AnnotationSearchOptions} instance
     * @throws IllegalArgumentException if input search options are {@literal null} 
     */
    public static AnnotationSearchOptions fromIncomingSearchOptions(final IncomingSearchOptions incOpts, final boolean separate_replies) {

        Assert.notNull(incOpts, "Valid IncomingSearchOptions required");

        final AnnotationSearchOptions options = new AnnotationSearchOptions(incOpts.getUri(),
                incOpts.getGroup(), separate_replies,
                incOpts.getLimit(), incOpts.getOffset(),
                incOpts.getOrder(), incOpts.getSort());

        if (incOpts.getUrl() != null && !incOpts.getUrl().isEmpty()) {
            options.setUri(incOpts.getUrl());
        }
        // optional parameters
        options.setUser(incOpts.getUser());

        String metadataProcessed = incOpts.getMetadatasets();
        if (!StringUtils.isEmpty(metadataProcessed)) {
            // at least during test scenarios, we experienced problems when sending JSON metadata
            // with only one entry - therefore, we had encoded the curly brackets URL-conform,
            // and have to decode this again here
            metadataProcessed = metadataProcessed.replace("%7B", "{").replace("%7D", "}").replace("\\", "");
        }

        final List<SimpleMetadataWithStatuses> converted = JsonStringDeserializer.convertJsonToSimpleMetadataWithStatusesList(metadataProcessed);
        options.setMetadataMapsWithStatusesList(converted);

        if (!StringUtils.isEmpty(incOpts.getMode()) && incOpts.getMode().toLowerCase(Locale.ENGLISH).equals("private")) {
            options.setSearchUser(Consts.SearchUserType.Contributor);
        }

        options.setShared(incOpts.getShared());

        return options;
    }

}
