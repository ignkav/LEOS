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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;
import eu.europa.ec.leos.annotate.model.entity.Metadata;

/**
 * Helper functions for {@link Metadata} entities
 */
public final class MetadataHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataHandler.class);

    private MetadataHandler() {
        // Prevent instantiation as all methods are static.
    }

    public static SimpleMetadataWithStatuses convertToSimpleMetadataWithStatuses(final Metadata metadata) {

        return new SimpleMetadataWithStatuses(MetadataHandler.getKeyValuePropertyAsSimpleMetadata(metadata),
                Collections.singletonList(AnnotationStatus.NORMAL));
    }

    public static List<SimpleMetadataWithStatuses> convertToSimpleMetadataWithStatusesList(final List<Metadata> metadataList) {

        return metadataList.stream()
                .map(MetadataHandler::convertToSimpleMetadataWithStatuses)
                .collect(Collectors.toList());
    }

    /**
     * returns a {@link SimpleMetadata} (={@link Map}) containing all metadata items, 
     * including the system ID and response status! 
     */
    public static SimpleMetadata getAllMetadataAsSimpleMetadata(final Metadata metadata) {

        // retrieve key-value pairs
        final SimpleMetadata result = getKeyValuePropertyAsSimpleMetadata(metadata);

        // add response status, if available
        if (metadata.getResponseStatus() != null) {
            result.put(Metadata.PROP_RESPONSE_STATUS, metadata.getResponseStatus().toString());
        }

        // add the version
        if (!StringUtils.isEmpty(metadata.getVersion())) {
            result.put(Metadata.PROP_VERSION, metadata.getVersion());
        }

        // add system id
        result.put(Metadata.PROP_SYSTEM_ID, metadata.getSystemId());

        return result;
    }

    /**
     * returns a {@link SimpleMetadata} (={@link Map}) containing all items stored in the key-values column
     * note: does not return the system ID or response status! 
     */
    @Nonnull
    public static SimpleMetadata getKeyValuePropertyAsSimpleMetadata(final Metadata metadata) {

        final SimpleMetadata result = new SimpleMetadata();

        if (!StringUtils.isEmpty(metadata.getKeyValuePairs())) {
            final List<String> parts = Arrays.asList(metadata.getKeyValuePairs().split("\n"));
            for (final String part : parts) {
                // each part has the format: key:"value"
                if (part.contains(":")) {
                    final String key = part.substring(0, part.indexOf(':'));
                    final String value = part.substring(part.indexOf(':') + 1);
                    result.put(key, value.replace("\r", ""));
                }
            }
        }

        return result;
    }

    /**
     * extract the ISCReference
     */
    public static String getIscReference(final Metadata metadata) {

        final SimpleMetadata props = getKeyValuePropertyAsSimpleMetadata(metadata);
        if (props.isEmpty()) {
            return "";
        }

        return props.get(Metadata.PROP_ISC_REF);
    }

    /**
     * extracts the responseId from the metadata, if available
     */
    public static String getResponseId(final Metadata metadata) {

        final SimpleMetadata props = getKeyValuePropertyAsSimpleMetadata(metadata);
        return props.get(Metadata.PROP_RESPONSE_ID);
    }

    /**
     * extracts the responseVersion from the metadata, if available
     */
    public static long getResponseVersion(final Metadata metadata) {

        final SimpleMetadata props = getKeyValuePropertyAsSimpleMetadata(metadata);
        final String respVers = props.get(Metadata.PROP_RESPONSE_VERSION);
        return StringUtils.isEmpty(respVers) ? -1L : Long.parseLong(respVers);
    }

    /**
     * extracts the originMode from the metadata, if available
     */
    public static String getOriginMode(final Metadata metadata) {
        final SimpleMetadata props = getKeyValuePropertyAsSimpleMetadata(metadata);
        return props.get(Metadata.PROP_ORIGIN_MODE);
    }

    /**
     * check if the metadata was "IN_PREPERATION" already
     */
    public static boolean isResponseStatusInPreparation(final Metadata metadata) {

        return metadata.getResponseStatus() == ResponseStatus.IN_PREPARATION;
    }

    /**
     * check if the metadata was "SENT" already
     */
    public static boolean isResponseStatusSent(final Metadata metadata) {

        return metadata.getResponseStatus() == ResponseStatus.SENT;
    }

    /**
     * remove the responseVersion from the metadata, if available
     */
    public static void removeResponseVersion(final Metadata metadata) {

        final SimpleMetadata props = getKeyValuePropertyAsSimpleMetadata(metadata);
        props.remove(Metadata.PROP_RESPONSE_VERSION);
        setKeyValuePropertyFromSimpleMetadata(metadata, props);
    }

    public static void setKeyValuePropertyFromSimpleMetadata(final Metadata metadata, final SimpleMetadata hashMap) {

        if (hashMap == null) {
            return;
        }

        String foundValue = hashMap.getOrDefault(Metadata.PROP_SYSTEM_ID, null);
        if (!StringUtils.isEmpty(foundValue)) {
            metadata.setSystemId(foundValue);
        }

        foundValue = hashMap.getOrDefault(Metadata.PROP_VERSION, null);
        if (!StringUtils.isEmpty(foundValue)) {
            metadata.setVersion(foundValue);
        }

        foundValue = hashMap.getOrDefault(Metadata.PROP_RESPONSE_STATUS, null);
        if (!StringUtils.isEmpty(foundValue)) {
            try {
                metadata.setResponseStatus(ResponseStatus.valueOf(foundValue));
            } catch (IllegalArgumentException e) {
                LOG.error("Found invalid value for response status in hashMap", e);
            }
        }

        final StringBuilder dbKeyValueList = new StringBuilder();
        hashMap.forEach((key, value) -> {
            if (!Metadata.PROPS_OWN_COLS.contains(key)) {
                dbKeyValueList.append(key).append(':').append(value).append('\n');
            }
        });
        metadata.setKeyValuePairs(dbKeyValueList.toString());
    }

    /**
     * set the responseVersion to the given value
     */
    public static void setResponseVersion(final Metadata metadata, final long respVersion) {

        final SimpleMetadata props = getKeyValuePropertyAsSimpleMetadata(metadata);
        props.put(Metadata.PROP_RESPONSE_VERSION, Long.toString(respVersion));
        setKeyValuePropertyFromSimpleMetadata(metadata, props);
    }

    /**
     * set the originMode to the given value
     */
    public static void setOriginMode(final Metadata metadata, final String originMode) {

        final SimpleMetadata props = getKeyValuePropertyAsSimpleMetadata(metadata);
        props.put(Metadata.PROP_ORIGIN_MODE, originMode);
        setKeyValuePropertyFromSimpleMetadata(metadata, props);
    }

    /**
     * Updates the response status if the newer one is different.
     * 
     * @param metadata
     *        {@link Metadata} item to update
     * @param targetRespStatus
     *        the {@link ResponseStatus} to be applied
     * @param userId
     *        ID of the user performing the update
     * @param timeToSet
     *        the timestamp to apply
     *        
     * @return whether the item has been updated
     */
    public static boolean updateMetadataIfDifferentResponseStatus(
            final Metadata metadata,
            final ResponseStatus targetRespStatus,
            final Long userId,
            final LocalDateTime timeToSet) {

        if (metadata.getResponseStatus() == targetRespStatus) {
            return false;
        }

        metadata.setResponseStatus(targetRespStatus);
        metadata.setResponseStatusUpdated(timeToSet);
        metadata.setResponseStatusUpdatedBy(userId);

        return true;
    }

    /**
     * Increases the response version value by one. If no response version is set yet,
     * nothing is done and method returns {@code false}.
     * 
     * @param metadata
     *        the {@link Metadata} item to update
     * @return whether the response version was present and could be incremented
     */
    public static boolean increaseResponseVersion(final Metadata metadata) {

        final long currentResponseVersion = getResponseVersion(metadata);
        if (currentResponseVersion <= 0) return false;

        final long newResponseVersion = currentResponseVersion + 1;
        setResponseVersion(metadata, newResponseVersion);

        return true;
    }
}
