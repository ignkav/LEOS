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

import eu.europa.ec.leos.annotate.model.UserDetails;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.web.user.JsonUserProfile;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.exceptions.UserAlreadyExistingException;
import eu.europa.ec.leos.annotate.services.exceptions.UserNotFoundException;

public interface UserService {

    /**
     * saving a given user note: ID of the user is set
     *
     * @param user the user to be saved
     * @throws UserAlreadyExistingException if the user is already registered with his login, this exception is thrown
     * @throws DefaultGroupNotFoundException if the name of the default user group is not found, this exception is thrown
     * 
     * @return saved {@link User} object, with properties like Id updated
     */
    User createUser(User user) throws UserAlreadyExistingException, DefaultGroupNotFoundException;

    /**
     * saving a given user
     *
     * @param login the login of the user to be saved
     * @param context the context of the user to be saved
     * @throws UserAlreadyExistingException if the user is already registered with his login, this exception is thrown
     * @throws DefaultGroupNotFoundException if the name of the default user group is not found, this exception is thrown
     * 
     * @return saved {@link User} object, with properties like Id updated
     */
    User createUser(String login, String context) throws UserAlreadyExistingException, DefaultGroupNotFoundException;

    /**
     * check if a user is already registered - register him if not
     *
     * @param login the login of the user to be saved
     * @param context the context of the user to be saved
     * @throws UserAlreadyExistingException if the user is already registered with his login, this exception is thrown
     * @throws DefaultGroupNotFoundException if the name of the default user group is not found, this exception is thrown
     */
    User createUserIfNotExists(String login, String context) throws UserAlreadyExistingException, DefaultGroupNotFoundException;

    /**
     * add a user to a group if it has its associate entity property set
     * 
     *  @param userinfo the {@link UserInformation} collected about the user, contains info from our database and from UD repo
     *  
     *  @return flag indicating success or nothing to do; FALSE indicates a problem
     */
    boolean addUserToEntityGroup(UserInformation userInfo);

    /**
     * search for a user given its login name and its context
     *
     * @param login the user's login name
     * @param context the user's context name
     * @return found user, or {@literal null}
     */
    User findByLoginAndContext(String login, String context);

    /**
     * search for a user given its ID
     * 
     * @param userId the user's ID
     * @return found user, or {@literal null}
     */
    User getUserById(Long userId);

    /**
     * retrieve the full user profile for the hypothes.is client
     *
     * @param userInfo the user for which the profile is to be retrieved
     *
     * @return {@link JsonUserProfile} containing all relevant information (preferences, groups, ...)
     */
    JsonUserProfile getUserProfile(UserInformation userInfo) throws UserNotFoundException;

    /**
     * update the user's setting whether the sidebar tutorial is to be shown
     *
     * @param userLogin the user's login name
     * @param userContext the user's context
     * @param visible the new value
     *
     * @throws UserNotFoundException throws this exception when the given user is unknown
     */
    User updateSidebarTutorialVisible(String userLogin, String userContext, boolean visible) throws UserNotFoundException;

    /**
     * retrieve user details (email address, name, DG, display name, ...) from the external user repository (via REST
     * interface) note: information is cached in order to avoid too many unnecessary calls; cache will be wiped after a
     * few minutes
     *
     * @param login the login of the user for which details are required
     * @param context the context for which details are required
     * @return {@link UserDetails} object containing all user properties
     */
    UserDetails getUserDetailsFromUserRepo(String login, String context);

    boolean isContextEnabled();

    /**
     * determine the {@link User} running the search - and check if he is member of the requested group at all
     * 
     * @param userInfo 
     *        {@link UserInformation} containing user details
     * @param group 
     *        {@link Group} for which the user requested to retrieve information
     * @return {@link User} object being member of given group, or {@literal null}
     */
    User getExecutingUser(final UserInformation userInfo, final Group group);

}
