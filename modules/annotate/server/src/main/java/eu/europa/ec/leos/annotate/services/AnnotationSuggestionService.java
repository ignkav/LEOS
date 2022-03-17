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

import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.services.exceptions.CannotAcceptSentSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotAcceptSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotDeleteAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotRejectSentSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotRejectSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;
import eu.europa.ec.leos.annotate.services.exceptions.NoSuggestionException;

public interface AnnotationSuggestionService {

    /**
     * accept a suggestion, taking permissions and authority into account
     * 
     * @param suggestionId
     *        the ID of the suggestion (annotation) to be accepted
     * @param userInfo
     *        information about the user requesting to accept the suggestion
     *        
     * @return returns the found annotation object, or {@literal null}
     * 
     * @throws CannotAcceptSuggestionException
     *         this exception is thrown when the referenced suggestion does not exist
     * @throws NoSuggestionException
     *         this exception is thrown when the referenced annotation is not a suggestion, but a different kind of annotation
     * @throws CannotAcceptSentSuggestionException
     *         this exception is thrown when the annotation has response status SENT and cannot be accepted
     * @throws MissingPermissionException 
     *         this exception is thrown when the user does not have the permission to reject the suggestion
     */
    void acceptSuggestionById(String suggestionId, UserInformation userInfo)
            throws CannotAcceptSuggestionException, NoSuggestionException, MissingPermissionException, CannotDeleteAnnotationException,
            CannotAcceptSentSuggestionException;

    /**
     * reject a suggestion, taking permissions and authority into account
     * 
     * @param suggestionId
     *        the ID of the suggestion (annotation) to be rejected
     * @param userInfo
     *        information about the user requesting to reject the suggestion
     *        
     * @return returns the found annotation object, or {@literal null}
     * 
     * @throws CannotRejectSuggestionException
     *         this exception is thrown when the referenced suggestion does not exist
     * @throws NoSuggestionException
     *         this exception is thrown when the referenced annotation is not a suggestion, but a different kind of annotation
     * @throws CannotRejectSentSuggestionException
     *         this exception is thrown when the annotation has response status SENT and cannot be accepted
     * @throws MissingPermissionException 
     *         this exception is thrown when the user does not have the permission to reject the suggestion
     */
    void rejectSuggestionById(String suggestionId, UserInformation userInfo)
            throws CannotRejectSuggestionException, NoSuggestionException, MissingPermissionException, CannotDeleteAnnotationException,
            CannotRejectSentSuggestionException;

}
