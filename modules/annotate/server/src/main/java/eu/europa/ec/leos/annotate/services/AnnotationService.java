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
package eu.europa.ec.leos.annotate.services;

import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.services.exceptions.*;

import javax.annotation.Nonnull;

import java.time.LocalDateTime;
import java.util.List;

public interface AnnotationService {

    /**
     * create an annotation in the database based on an incoming, JSON-deserialized annotation
     * 
     * @param webAnnot
     *        the incoming annotation
     * @param userInfo
     *        information about the user wanting to create an annotation
     * @return created annotation
     * 
     * @throws CannotCreateAnnotationException 
     *         the exception is thrown when the annotation cannot be created due to unfulfilled constraints
     *         (e.g. missing document or user information)
     */
    Annotation createAnnotation(JsonAnnotation annot, UserInformation userInfo)
            throws CannotCreateAnnotationException;

    /**
     * update an existing annotation in the database based on an incoming, JSON-deserialized annotation
     * note: if a SENT annotation is updated in ISC (if permitted), then a new annotation is created 
     *       and returned!
     * 
     * @param annotationId 
     *        id of the annotation to be updated
     * @param webAnnot 
     *        the incoming annotation
     * @param userInfo
     *        information about the user wanting to update an annotation
     * @return updated annotation (which is a new one for SENT ISC annotations)
     *        
     * @throws CannotUpdateAnnotationException 
     *         the exception is thrown when the annotation cannot be updated (e.g. when it is not existing)
     * @throws CannotUpdateSentAnnotationException
     *         exception thrown when an annotation with response status SENT is tried to be updated
     * @throws MissingPermissionException 
     *         the exception is thrown when the user lacks permissions for updating the annotation
     */
    Annotation updateAnnotation(String annotationId, JsonAnnotation jsonAnnotation, UserInformation userInfo)
            throws CannotUpdateAnnotationException, MissingPermissionException, CannotUpdateSentAnnotationException;

    /**
     * simple function that just looks up an annotation based on its ID WITHOUT permission checks
     * to be used only from tests or from services layer
     *  
     * @param annotId
     *        the ID of the wanted annotation
     *        
     * @return returns the found annotation object, or {@literal null}; note: only non-deleted annotations are returned
     */
    Annotation findAnnotationById(String annotId);

    /**
     * look up an annotation based on its ID, taking permissions into account
     * 
     * @param annotId
     *        the ID of the wanted annotation
     * @param userlogin
     *        the login of the user requesting to get the annotation
     * @param userContext
     *        the context of the user requesting to get the annotation
     *        
     * @return returns the found annotation object, or null
     * @throws MissingPermissionException 
     *         this exception is thrown when the user does not have the permission to view this annotation
     */
    Annotation findAnnotationById(String annotId, String userlogin, String userContext)
            throws MissingPermissionException;

    /**
     * find sentDeleted annotations having certain statuses and metadata IDs
     * 
     * @param metadataIds
     *        list of {@link Metadata} IDs to match
     * @param statuses
     *        list of {@link AnnotationStatus} to match
     * @return found list of {@Annotation}s, or empty list
     */
    @Nonnull
    List<Annotation> findSentDeletedByMetadataIdAndStatus(final List<Long> metadataIds, final List<AnnotationStatus> statuses);

    /**
     * find annotations having certain status and metadata IDs
     * 
     * @param metadataIds
     *        list of {@link Metadata} IDs to match
     * @param status
     *        the {@link AnnotationStatus} to match
     * @return found list of {@Annotation}s, or empty list
     */
    @Nonnull
    List<Annotation> findByMetadataAndStatus(final List<Long> metadataIds, final AnnotationStatus status);

    /**
     * find annotations having certain metadata IDs
     * 
     * @param metadataIds
     *        list of {@link Metadata} IDs to match
     * @return found list of {@Annotation}s, or empty list
     */
    @Nonnull
    List<Annotation> findByMetadata(final List<Long> metadataIds);
    
    /**
     * save a given list of {@link Annotation}s
     * this method just encapsules the {@link AnnotationRepository}
     * 
     * @param annot
     *        annotation to be saved
     * @return saved annotation
     */
    List<Annotation> saveAll(final List<Annotation> annot);
    
    /**
     * delete an annotation in the database based on an annotation ID
     * 
     * @param annotationId
     *        the ID of the annotation to be deleted
     * @param userInfo
     *        information about the user requesting to delete an annotation
     * @throws CannotDeleteAnnotationException 
     *         the exception is thrown when the annotation cannot be deleted, e.g. when it is not existing or due to unexpected database error)
     * @throws CannotDeleteSentAnnotationException
     *         the exception is thrown when trying to delete a SENT annotation
     */
    void deleteAnnotationById(String annotationId, UserInformation userInfo)
            throws CannotDeleteAnnotationException, CannotDeleteSentAnnotationException;

    /**
     * delete a set of annotations in the database based on their IDs
     * 
     * @param annotationIds
     *        the list of IDs of the annotation to be deleted
     * @param userInfo
     *        information about the user requesting to delete annotations
     * @return returns a list of annotations that were successfully deleted
     */
    List<String> deleteAnnotationsById(List<String> annotationIds, UserInformation userInfo);

    /**
     * soft deletion of an annotation
     * (recursive if annotation is a root annotation)
     *  
     * @param annot 
     *        the annotation to be deleted
     * @param userId 
     *        ID of the user requesting rejection
     * 
     * @throws CannotDeleteAnnotationException
     *         the exception is thrown when the annotation cannot be deleted, e.g. when it is not existing or due to unexpected database error)
     */
    void softDeleteAnnotation(final Annotation annot, final long userId)
            throws CannotDeleteAnnotationException;

    /**
     * change a given set of annotations to become public and save them
     * 
     * @param annots
     *        list of {@link Annotation}s to be made public
     */
    void makeShared(final List<Annotation> annots);

    /**
     * method for updating the status of an annotation (recursive, if needed, e.g. for root annotations)
     * 
     * @param annot 
     *        annotation (root) to be updated
     * @param newStatus 
     *        the new status to be applied
     * @param userId 
     *        internal DB id of the user requesting status change
     * 
     * @throws CannotUpdateAnnotationException
     *         exception thrown when saving the updated annotation fails
     */
    void updateAnnotationStatus(final Annotation annot, final AnnotationStatus newStatus, final long userId) throws CannotUpdateAnnotationException;

    /**
     * method for batch updating the "updated" timestamp of a given list of annotations
     * 
     * @param annots
     *        list of {@link Annotation}s to be updated
     * @param timestamp
     *        the new timestamp to be applied
     */
    void saveWithUpdatedTimestamp(final List<Annotation> annots, final LocalDateTime timestamp);
}
