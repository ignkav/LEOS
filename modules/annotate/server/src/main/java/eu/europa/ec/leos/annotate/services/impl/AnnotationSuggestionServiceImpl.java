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
package eu.europa.ec.leos.annotate.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.AnnotationChecker;
import eu.europa.ec.leos.annotate.services.AnnotationPermissionService;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.AnnotationSuggestionService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotAcceptSentSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotAcceptSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotRejectSentSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotRejectSuggestionException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotUpdateAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;
import eu.europa.ec.leos.annotate.services.exceptions.NoSuggestionException;

@Service
public class AnnotationSuggestionServiceImpl implements AnnotationSuggestionService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationSuggestionServiceImpl.class);
    private static final String ERROR_USERINFO_MISSING = "Required user information missing.";

    // -------------------------------------
    // Required services
    // -------------------------------------

    @Autowired
    private AnnotationPermissionService annotPermService;

    @Autowired
    private AnnotationService annotService;

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public void acceptSuggestionById(final String suggestionId, final UserInformation userInfo)
            throws CannotAcceptSuggestionException, NoSuggestionException, CannotAcceptSentSuggestionException, MissingPermissionException {

        Assert.isTrue(!StringUtils.isEmpty(suggestionId), "Required suggestion/annotation ID missing.");
        Assert.notNull(userInfo, ERROR_USERINFO_MISSING);

        final Annotation ann = annotService.findAnnotationById(suggestionId);
        if (ann == null) {
            throw new CannotAcceptSuggestionException("Suggestion not found");
        }

        if (!AnnotationChecker.isSuggestion(ann)) {
            throw new NoSuggestionException("Given ID '" + suggestionId + "' does not represent a suggestion");
        }

        // in ISC, accepting a suggestion is not allowed - but in LEOS!
        if (AnnotationChecker.isResponseStatusSent(ann) && !Authorities.isLeos(userInfo.getAuthority())) {
            LOG.info("Annotation/suggestion '{}' has response status SENT and thus cannot be accepted in {}", ann.getId(), userInfo.getAuthority());
            throw new CannotAcceptSentSuggestionException("Annotation/suggestion has response status SENT, cannot be accepted in " + userInfo.getAuthority());
        }

        final User user = userInfo.getUser();
        try {
            if (!annotPermService.hasUserPermissionToAcceptSuggestion(ann, user)) {
                final String login = user == null ? "unknown user" : user.getLogin();
                LOG.warn("User '{}' does not have permission to accept suggestion/annotation with id '{}'.", login, suggestionId);
                throw new MissingPermissionException(login);
            }
        } catch (IllegalArgumentException iae) {
            final MissingPermissionException mpe = new MissingPermissionException("No user given for accepting suggestion");
            mpe.initCause(iae);
            throw mpe;
        }

        try {
            annotService.updateAnnotationStatus(ann, AnnotationStatus.ACCEPTED, user.getId());
        } catch (CannotUpdateAnnotationException cuae) {
            // wrap into more specific exception
            throw new CannotAcceptSuggestionException(cuae);
        }
    }

    @Override
    public void rejectSuggestionById(final String suggestionId, final UserInformation userInfo)
            throws CannotRejectSuggestionException, NoSuggestionException,
            MissingPermissionException, CannotRejectSentSuggestionException {

        Assert.isTrue(!StringUtils.isEmpty(suggestionId), "Required suggestion/annotation ID missing.");
        Assert.notNull(userInfo, ERROR_USERINFO_MISSING);

        final Annotation ann = annotService.findAnnotationById(suggestionId);
        if (ann == null) {
            throw new CannotRejectSuggestionException("Suggestion not found");
        }

        if (!AnnotationChecker.isSuggestion(ann)) {
            throw new NoSuggestionException("Given ID '" + suggestionId + "' does not represent a suggestion");
        }

        // in ISC, rejecting a suggestion is not allowed - but in LEOS!
        if (AnnotationChecker.isResponseStatusSent(ann) && !Authorities.isLeos(userInfo.getAuthority())) {
            LOG.info("Annotation/suggestion '{}' has response status SENT and thus cannot be rejected in {}", ann.getId(), userInfo.getAuthority());
            throw new CannotRejectSentSuggestionException("Annotation/suggestion has response status SENT, cannot be rejected in " + userInfo.getAuthority());
        }

        final User user = userInfo.getUser();
        try {
            if (!annotPermService.hasUserPermissionToRejectSuggestion(ann, user)) {
                final String login = user == null ? "unknown user" : user.getLogin();
                LOG.warn("User '{}' does not have permission to reject suggestion/annotation with id '{}'.", login, suggestionId);
                throw new MissingPermissionException(login);
            }
        } catch (IllegalArgumentException iae) {
            final MissingPermissionException mpe = new MissingPermissionException("No user given for rejecting suggestion");
            mpe.initCause(iae);
            throw mpe;
        }

        try {
            annotService.updateAnnotationStatus(ann, AnnotationStatus.REJECTED, user.getId());
        } catch (CannotUpdateAnnotationException cuae) {
            // wrap into more specific exception
            throw new CannotRejectSuggestionException(cuae);
        }
    }

}
