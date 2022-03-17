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
package eu.europa.ec.leos.annotate.services.impl.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;

public final class MetadataChecker {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataChecker.class);

    private MetadataChecker() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * comparison of key-value properties with DB object values
     * note: comparison of systemId, version and responseStatus properties should have been done before (separate function)
     *  
     * @param metadataRequired set of required key/value pairs ({@link SimpleMetadata})
     * @param candidateMeta {@link Metadata} instance to be inspected whether it contains all requested key/value pairs
     * 
     * @return {@literal false} in case of discrepancy, {@literal true} otherwise
     */
    public static boolean checkKeyValueProperties(final SimpleMetadata metadataRequired, final Metadata candidateMeta) {

        final SimpleMetadata foundMeta = MetadataHandler.getKeyValuePropertyAsSimpleMetadata(candidateMeta);
        for (final Map.Entry<String, String> requiredEntry : metadataRequired.entrySet()) {

            // note: response status and systemId were already matched before and removed
            final String requiredEntryKey = requiredEntry.getKey();
            final String foundValue = foundMeta.get(requiredEntryKey);
            if (foundValue == null) {
                LOG.debug("Required metadata field '{}' not stored in DB.", requiredEntry.getKey());
                return false;
            }
            if (!foundValue.equals(requiredEntry.getValue())) {
                LOG.debug("Value of field '{}' different in DB.", requiredEntryKey);
                return false;
            }
        }

        // when reaching this point, all required metadata is available and matches
        return true;
    }

    /**
     * response status comparison
     * 
     * @param metadataRequired set of required key/value pairs ({@link SimpleMetadata})
     * @param candidateMeta {@link Metadata} instance to be inspected whether it contains the same response status
     * 
     * @return {@literal false} in case of discrepancy, {@literal true} otherwise
     */
    public static boolean checkResponseStatus(final SimpleMetadata metadataRequired, final Metadata candidateMeta) {

        if (metadataRequired.containsKey(Metadata.PROP_RESPONSE_STATUS)) {
            final ResponseStatus expectedResponseStatus = candidateMeta.getResponseStatus();
            if (expectedResponseStatus == null) {
                LOG.debug("ResponseStatus does not match: candidate item has no response status set");
                return false;
            }
            if (!metadataRequired.get(Metadata.PROP_RESPONSE_STATUS).equals(candidateMeta.getResponseStatus().toString())) {
                LOG.debug("ResponseStatus does not match");
                return false;
            }
            metadataRequired.remove(Metadata.PROP_RESPONSE_STATUS);
        }

        return true;
    }

    /**
     * system ID comparison
     *  
     * @param metadataRequired set of required key/value pairs ({@link SimpleMetadata})
     * @param candidateMeta {@link Metadata} instance to be inspected whether it contains the same system ID
     * 
     * @return {@literal false} in case of discrepancy, {@literal true} otherwise
     */
    public static boolean checkSystemId(final SimpleMetadata metadataRequired, final Metadata candidateMeta) {

        if (metadataRequired.containsKey(Metadata.PROP_SYSTEM_ID)) {
            if (StringUtils.isEmpty(candidateMeta.getSystemId())) {
                LOG.debug("SystemId does not match: candidate item has no systemId set");
                return false;
            }
            if (!metadataRequired.get(Metadata.PROP_SYSTEM_ID).equals(candidateMeta.getSystemId())) {
                LOG.debug("SystemId does not match");
                return false;
            }
            metadataRequired.remove(Metadata.PROP_SYSTEM_ID);
        }

        return true;
    }

    /**
     * moved the version comparison to a separate function
     * note: in call hierarchy, this is only used for a single Metadata item; 
     *        for filtering lists of Metadata items, the DB-based function is used
     *  
     * @param metadataRequired set of required key/value pairs ({@link SimpleMetadata})
     * @param candidateMeta {@link Metadata} instance to be inspected whether it contains the same version
     * 
     * @return {@literal false} in case of discrepancy, {@literal true} otherwise
     */
    public static boolean checkVersion(final SimpleMetadata metadataRequired, final Metadata candidateMeta) {

        String version = metadataRequired.get(Metadata.PROP_VERSION);
        if (!StringUtils.isEmpty(version)) {
            if (StringUtils.isEmpty(candidateMeta.getVersion())) {
                LOG.debug("version does not match: candidate item has no version set");
                return false;
            }

            final VersionSearchType versSearch = VersionSearchType.getVersionSearchType(version);
            switch (versSearch) {
                case UP_TO:
                    version = version.substring(Metadata.VERSION_SEARCH_UP_TO.length());
                    // "1.0".compareTo("0.1") = 1, i.e. first is larger
                    // "1.0".compareTo("1.0") = 0
                    // "1.0".compareTo("1.1") = -1, i.e. first is smaller
                    if (version.compareTo(candidateMeta.getVersion()) < 0) { // first is smaller -> no candidate
                        LOG.debug("version does not match (for 'up to')");
                        return false;
                    }
                    metadataRequired.remove(Metadata.PROP_VERSION);
                    break;

                case EQUALITY:
                default:
                    if (!version.equals(candidateMeta.getVersion())) {
                        LOG.debug("version does not match (for equality)");
                        return false;
                    }
                    metadataRequired.remove(Metadata.PROP_VERSION);
                    break;
            }

        }

        return true;
    }
}
