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

import org.springframework.util.StringUtils;

import eu.europa.ec.leos.annotate.model.UserInformation;

public final class EntityChecker {

    private EntityChecker() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * check if the annotation belongs to the user's entity (i.e. if the user's connectedEntity equals the responseId of ISC)
     * 
     * @param userInfo
     *        given {@link UserInformation} with user details, e.g. the connectedEntity
     * @param responseId
     *        response Id of an ISC response (usually DG name)
     * @return flag indicating if both are filled and coincide
     */
    public static boolean isResponseFromUsersEntity(final UserInformation userInfo, final String responseId) {

        if (StringUtils.isEmpty(responseId)) {
            return false;
        }

        if (userInfo == null || StringUtils.isEmpty(userInfo.getConnectedEntity())) {
            return false;
        }

        return responseId.equals(userInfo.getConnectedEntity());
    }

}
