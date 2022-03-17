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
package eu.europa.ec.leos.annotate.model.web.helper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationPermissions;

/**
 * Helper functions for {@link JsonAnnotation} objects
 */
public final class JsonAnnotationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JsonAnnotationHandler.class);

    private JsonAnnotationHandler() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * retrieves the root of a communication thread (for replies)
     */
    public static String getRootAnnotationId(final JsonAnnotation annotation) {

        if (!isReply(annotation)) {
            return null;
        }

        return annotation.getReferences().get(0);
    }

    public static String getSerializedTargets(final JsonAnnotation annotation) {

        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(annotation.getTarget());
        } catch (JsonProcessingException e) {
            LOG.error("Annotation target selectors could not be serialized to JSON", e);
        }
        return "";
    }

    /**
     * returns whether the object features metadata
     */
    public static boolean hasMetadata(final JsonAnnotation annotation) {
        return annotation.getDocument() != null && annotation.getDocument().getMetadata() != null;
    }

    /**
     * checks if an annotation is visible for "only me" this is done based on the read permissions set: 
     * If it is readable for the user only, it is private.
     * 
     * @param annotation
     *        {@link JsonAnnotation} object to be checked
     * @return flag indicating whether annotation is meant to be visible for "only me"
     */
    public static boolean isPrivateAnnotation(final JsonAnnotation annotation) {

        if (annotation == null) {
            LOG.error("Annotation to be checked for privacy is invalid!");
            return false;
        }

        final JsonAnnotationPermissions perms = annotation.getPermissions();
        if (perms == null) {
            LOG.error("Annotation to be checked for privacy does not contain any permission information!");
            return false;
        }

        final List<String> readPerms = perms.getRead();
        if (readPerms == null || readPerms.isEmpty()) {
            LOG.error("Annotation to be checked for privacy does not contain any read permissions!");
            return false;
        }

        // the annotation is private if and only if the user itself has read permissions
        return readPerms.size() == 1 &&
                readPerms.get(0).equals(annotation.getUser());
    }

    /**
     * states whether the object denotes a reply to an annotation
     */
    public static boolean isReply(final JsonAnnotation annotation) {
        return annotation.getReferences() != null && !annotation.getReferences().isEmpty();
    }

}
