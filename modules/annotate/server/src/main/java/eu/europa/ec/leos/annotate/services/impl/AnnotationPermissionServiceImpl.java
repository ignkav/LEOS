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

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.AnnotationChecker;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationPermissions;
import eu.europa.ec.leos.annotate.services.AnnotationPermissionServiceWithTestFunctions;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.UserService;
import eu.europa.ec.leos.annotate.services.impl.util.AnnotationAuthorChecker;
import eu.europa.ec.leos.annotate.services.impl.util.PermissionManager;
import eu.europa.ec.leos.annotate.services.impl.util.PermissionManager.PossiblePermissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class AnnotationPermissionServiceImpl implements AnnotationPermissionServiceWithTestFunctions {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    /**
     * default group name injected from properties
     */
    @Value("${defaultgroup.name}")
    private String defaultGroupName;

    // -------------------------------------
    // Methods from test interface part
    // -------------------------------------

    @Override
    public void setDefaultGroupName(final String groupName) {

        this.defaultGroupName = groupName;
    }

    // -------------------------------------
    // Methods from default interface part
    // -------------------------------------

    @Override
    public boolean hasUserPermissionToSeeAnnotation(final Annotation annot, final User user) {

        Assert.notNull(annot, "Checking if user has permission to view annotation fails since annotation unavailable.");
        Assert.notNull(user, "Checking if user has permission to view annotation fails since user unavailable.");

        // if the requesting user created the annotation, then he may see it
        if (AnnotationAuthorChecker.isAnnotationOfUser(annot, user)) {
            return true;
        }

        // otherwise, if annotation must be
        // a) shared
        if (!annot.isShared()) {
            return false;
        }

        // b) user must be member of the group in which the annotation was published
        return groupService.isUserMemberOfGroup(user, annot.getGroup());
    }

    @Override
    public boolean hasUserPermissionToUpdateAnnotation(final Annotation annot, final UserInformation userinfo) {

        Assert.notNull(annot, "Annotation to be checked for being updateable is invalid!");
        Assert.notNull(userinfo, "User to be checked for being able to update annotation is invalid!");

        final boolean isAnnotationSent = AnnotationChecker.isResponseStatusSent(annot);

        final User user = userService.findByLoginAndContext(userinfo.getLogin(), userinfo.getContext());
        final boolean isAuthor = AnnotationAuthorChecker.isAnnotationOfUser(annot, user);

        if (Authorities.isLeos(userinfo.getAuthority())) {

            // EdiT users may not update SENT annotations, and otherwise only update their own annotations
            return !isAnnotationSent && isAuthor;

        } else {

            // when in ISC, users of same entity (group) may update SENT annotations...
            if (isAnnotationSent) {
                return groupService.isUserMemberOfGroup(userinfo.getUser(), annot.getGroup());
            } else {
                // but other annotations may be updated if they are of the user OR if they come from CONTRIBUTORS
                String originMode = MetadataHandler.getOriginMode(annot.getMetadata());
                return isAuthor || (originMode != null && originMode.equalsIgnoreCase("PRIVATE"));
            }
        }
    }

    @Override
    public boolean hasUserPermissionToAcceptSuggestion(final Annotation sugg, final User user) {

        // note: currently, any user of the group may accept a suggestion
        Assert.notNull(user, "No user given for checking permission to accept suggestion");

        // ANOT-135: we do not impose further restrictions here:
        // any author or contributor shall be allowed to accept suggestions, yet these roles are
        // not managed in UD-repo, therefore EdiT will take care of it and restrict the annotate client
        // accordingly
        return true;
    }

    @Override
    public boolean hasUserPermissionToRejectSuggestion(final Annotation sugg, final User user) {

        // note: currently, any user of the group may reject a suggestion
        Assert.notNull(user, "No user given for checking permission to reject suggestion");

        // ANOT-135: we do not impose further restrictions here:
        // any author or contributor shall be allowed to reject suggestions, yet these roles are
        // not managed in UD-repo, therefore EdiT will take care of it and restrict the annotate client
        // accordingly
        return true;
    }

    @Override
    public boolean userMayPublishContributions(final UserInformation userInfo) {

        Assert.notNull(userInfo, "No valid user data given for checking publishing permissions");

        return Authorities.isIsc(userInfo.getAuthority());
    }

    @Override
    public boolean userMayUpdateMetadata(final UserInformation userInfo) {

        Assert.notNull(userInfo, "No valid user data given for checking permissions");

        return Authorities.isIsc(userInfo.getAuthority());
    }

    @Override
    public JsonAnnotationPermissions getJsonAnnotationPermissions(final Annotation annot, final String groupName, final String userAccountForHypo,
            final UserInformation userInfo) {

        Assert.notNull(userInfo, "User information not available");

        final PossiblePermissions possiblePerms = new PossiblePermissions(userAccountForHypo, "group:" + groupName, defaultGroupName);

        final JsonAnnotationPermissions permissions = new JsonAnnotationPermissions();

        PermissionManager.setAdminPermissions(annot, possiblePerms, userInfo, permissions);
        PermissionManager.setDeletePermissions(annot, possiblePerms, userInfo, permissions);
        PermissionManager.setEditPermissions(annot, possiblePerms, userInfo, permissions);
        PermissionManager.setReadPermissions(annot, possiblePerms, permissions);

        return permissions;
    }

}
