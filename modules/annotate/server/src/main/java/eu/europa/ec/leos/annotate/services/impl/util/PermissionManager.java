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

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.leos.annotate.Generated;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.helper.AnnotationChecker;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationPermissions;

public final class PermissionManager {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionManager.class);

    private PermissionManager() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * individual permissions that might be assigned
     */
    public final static class PossiblePermissions {

        private static final String NO_PERMISSION = "";
        private final String USER_PERMISSION;
        private final String ANNOT_GROUP_PERMISSION;
        private final String EVERYBODY_PERMISSION;

        public PossiblePermissions(final String userOnly, final String group, final String defaultGroupName) {

            USER_PERMISSION = userOnly;
            ANNOT_GROUP_PERMISSION = group;
            EVERYBODY_PERMISSION = "group:" + defaultGroupName;
        }

        @Generated
        public List<String> getNoPermission() {
            return Collections.singletonList(NO_PERMISSION);
        }

        @Generated
        public List<String> getUserPermission() {
            return Collections.singletonList(USER_PERMISSION);
        }

        @Generated
        public List<String> getGroupPermission() {
            return Collections.singletonList(ANNOT_GROUP_PERMISSION);
        }

        @Generated
        public List<String> getEverybodyPermission() {
            return Collections.singletonList(EVERYBODY_PERMISSION);
        }
    }

    public static List<String> getContributorDeleteEditPermissions(final Annotation annot, final PossiblePermissions possiblePerms,
            final UserInformation userInfo) {

        if (annot.getStatus() == AnnotationStatus.DELETED) {
            return possiblePerms.getNoPermission();
        } else if (AnnotationAuthorChecker.isContributorOfAnnotation(annot, userInfo.getUser())) {
            // contributor may delete and update his own annotation
            return possiblePerms.getUserPermission();
        } else {
            // contributor may not delete and not update other people's annotation
            return possiblePerms.getNoPermission();
        }
    }

    public static List<String> getIscDeleteEditPermissions(final Annotation annot, final PossiblePermissions possiblePerms, final UserInformation userInfo) {

        if (AnnotationChecker.isResponseStatusSent(annot)) {

            final String responseId = MetadataHandler.getResponseId(annot.getMetadata()); // note: Metadata cannot be null
            final boolean userIsFromSameEntity = EntityChecker.isResponseFromUsersEntity(userInfo, responseId);

            // ISC: if the user is from the same group as the SENT annotation, he may delete and edit it; unless it was already deleted
            if (annot.getStatus() == AnnotationStatus.DELETED) {
                return possiblePerms.getNoPermission();
            } else if (userIsFromSameEntity) {
                return possiblePerms.getUserPermission();
            } else {
                // otherwise, it is read-only
                return possiblePerms.getNoPermission();
            }
        } else {
            return possiblePerms.getUserPermission();
        }
    }

    public static List<String> getLeosDeletePermissions(final Annotation annot, final PossiblePermissions possiblePerms) {

        if (AnnotationChecker.isResponseStatusSent(annot)) {
            // all users may delete: in order to simulate "everybody", we assign the default group
            // in which all users are members
            return possiblePerms.getEverybodyPermission();
        } else {
            return possiblePerms.getUserPermission();
        }
    }

    public static List<String> getLeosEditPermissions(final Annotation annot, final PossiblePermissions possiblePerms) {

        if (AnnotationChecker.isResponseStatusSent(annot)) {

            // in EdiT, SENT annotations may not be edited
            return possiblePerms.getNoPermission();
        } else {
            return possiblePerms.getUserPermission();
        }
    }

    @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
    public static void setAdminPermissions(final Annotation annot, final PossiblePermissions possiblePerms, final UserInformation userInfo,
            final JsonAnnotationPermissions permissions) {

        switch (userInfo.getSearchUser()) {

            case Contributor:

                if (AnnotationAuthorChecker.isContributorOfAnnotation(annot, userInfo.getUser())) {
                    permissions.setAdmin(possiblePerms.getUserPermission());
                } else {
                    permissions.setAdmin(possiblePerms.getNoPermission());
                }
                break;

            default:
                // standard EdiT or ISC user

                if (AnnotationChecker.isResponseStatusSent(annot)) {
                    permissions.setAdmin(possiblePerms.getNoPermission());
                } else {
                    permissions.setAdmin(possiblePerms.getUserPermission());
                }
                break;
        }
    }

    public static void setDeletePermissions(final Annotation annot, final PossiblePermissions possiblePerms, final UserInformation userInfo,
            final JsonAnnotationPermissions permissions) {

        switch (userInfo.getSearchUser()) {

            case Contributor:
                permissions.setDelete(getContributorDeleteEditPermissions(annot, possiblePerms, userInfo));
                break;

            case EdiT:
                permissions.setDelete(getLeosDeletePermissions(annot, possiblePerms));
                break;

            case ISC:
                permissions.setDelete(getIscDeleteEditPermissions(annot, possiblePerms, userInfo));
                break;

            default:
                // this case should not occur
                LOG.error("Asked for delete permissions for unknown user type.");
                permissions.setDelete(null);
                break;
        }
    }

    public static void setEditPermissions(final Annotation annot, final PossiblePermissions possiblePerms, final UserInformation userInfo,
            final JsonAnnotationPermissions permissions) {

        switch (userInfo.getSearchUser()) {

            case Contributor:
                permissions.setUpdate(getContributorDeleteEditPermissions(annot, possiblePerms, userInfo));
                break;

            case EdiT:
                permissions.setUpdate(getLeosEditPermissions(annot, possiblePerms));
                break;

            case ISC:
                permissions.setUpdate(getIscDeleteEditPermissions(annot, possiblePerms, userInfo));
                break;

            default:
                // this case should not occur
                LOG.error("Asked for edit permissions for unknown user type.");
                permissions.setUpdate(null);
                break;
        }
    }

    public static void setReadPermissions(final Annotation annot, final PossiblePermissions possiblePerms,
            final JsonAnnotationPermissions permissions) {

        permissions.setRead(annot.isShared() ? possiblePerms.getGroupPermission() : possiblePerms.getUserPermission());
    }

}
