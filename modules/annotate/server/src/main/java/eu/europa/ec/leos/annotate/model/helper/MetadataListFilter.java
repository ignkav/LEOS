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

import eu.europa.ec.leos.annotate.model.entity.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.stream.Collectors;

public final class MetadataListFilter {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataListFilter.class);

    private MetadataListFilter() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * filters a given list of {@link Metadata} items for those featuring a given ISC reference
     * 
     * @param metaCandidates
     *        {@link Metadata} list to be filtered
     * @return filtered list of items, at least an empty list
     */
    @Nonnull
    public static List<Metadata> filterByIscReference(final @Nonnull List<Metadata> metaCandidates, final String iscReference) {

        if (StringUtils.isEmpty(iscReference)) {
            return metaCandidates;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Filter list of metadata items for ISC reference '" + iscReference + "'");
        }
        return metaCandidates.stream()
                .filter(meta -> iscReference.equals(MetadataHandler.getIscReference(meta)))
                .collect(Collectors.toList());
    }

    /**
     * filters a given list of {@link Metadata} items for those featuring a given response version
     * 
     * @param metaCandidates
     *        {@link Metadata} list to be filtered
     * @return filtered list of items, at least an empty list
     */
    @Nonnull
    public static List<Metadata> filterByResponseVersion(final @Nonnull List<Metadata> metaCandidates, final String respVers) {

        if (StringUtils.isEmpty(respVers)) {
            return metaCandidates;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Filter list of metadata items for response version'" + respVers + "'");
        }
        return metaCandidates.stream()
                .filter(meta -> respVers.equals(Long.toString(MetadataHandler.getResponseVersion(meta))))
                .collect(Collectors.toList());
    }

}
