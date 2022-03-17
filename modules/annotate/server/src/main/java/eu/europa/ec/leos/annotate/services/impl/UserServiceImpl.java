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
import eu.europa.ec.leos.annotate.model.UserDetails;
import eu.europa.ec.leos.annotate.model.UserEntity;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.web.user.JsonGroup;
import eu.europa.ec.leos.annotate.model.web.user.JsonUserProfile;
import eu.europa.ec.leos.annotate.model.web.user.JsonUserShowSideBarPreference;
import eu.europa.ec.leos.annotate.repository.UserRepository;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.UserService;
import eu.europa.ec.leos.annotate.services.UserServiceWithTestFunctions;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.exceptions.UserAlreadyExistingException;
import eu.europa.ec.leos.annotate.services.exceptions.UserNotFoundException;
import eu.europa.ec.leos.annotate.services.impl.util.EntityChecker;
import eu.europa.ec.leos.annotate.services.impl.util.InternalGroupName;
import eu.europa.ec.leos.annotate.services.impl.util.UserDetailsCache;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for managing annotating users Note: communicates with external UD-repo for retrieval of user
 * detail information
 */
@Service
public class UserServiceImpl implements UserService, UserServiceWithTestFunctions {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${user.repository.timeout}")
    private int testingTimeout;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    // URL to the external user repository
    @Value("${user.repository.url}")
    private String repositoryUrl;

    // URL to the external user repository's entities API
    @Value("${user.repository.url.entities}")
    private String repositoryUrlEntities;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    // REST template used for calling UD-repo
    @Autowired
    private RestTemplate restOperations;

    // cache for the user details
    @Autowired
    private UserDetailsCache userDetailsCache;

    // -------------------------------------
    // Constructors and other functions used for testing; these test functions are not part of the UserService interface
    // -------------------------------------
    // note: use a custom constructor in order to ease testability by benefiting from dependency injection
    @Autowired
    public UserServiceImpl(final RestTemplate restOps) {
        if (this.restOperations == null && restOps != null) {
            
            final ClientHttpRequestFactory clientFactory = getClientHttpRequestFactory();
            if(clientFactory != null) {
                LOG.info("Http Request factory overwritten using specific timeout");
                restOps.setRequestFactory(clientFactory);
            }
            this.restOperations = restOps;
        }
        if (this.userDetailsCache == null) {
            this.userDetailsCache = new UserDetailsCache();
        }
    }

    // create a custom client HTTP request factory with a shorter timeout for testing
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        
        if(testingTimeout <= 0) {
            LOG.info("No specific timeout configured for UD repo access; use default");
            return null;
        } else {
            LOG.info("Configured timeout for UD repo connection is: {}", testingTimeout);
        }
        
        final RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(testingTimeout)
                .setConnectionRequestTimeout(testingTimeout)
                .setSocketTimeout(testingTimeout)
                .build();
        final CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }

    // custom constructor in order to ease testability by benefiting from mock object injection via Mockito
    public UserServiceImpl(final UserRepository userRepos, final GroupService groupService) {
        this.userRepository = userRepos;
        this.groupService = groupService;
    }

    // possibility to inject a custom RestTemplate - USED FOR TESTING, NOT CONTAINED IN PUBLIC SERVICE INTERFACE
    @Override
    public void setRestTemplate(final RestTemplate restOps) {
        this.restOperations = restOps;
    }

    // possibility to cache a custom user entry - USED FOR TESTING, NOT CONTAINED IN PUBLIC SERVICE INTERFACE
    @Override
    public void cacheUserDetails(final String key, final UserDetails details) {
        this.userDetailsCache.cache(key, null, details);
    }

    // possibility to inject a custom group service - USED FOR TESTING, NOT CONTAINED IN PUBLIC SERVICE INTERFACE
    @Override
    public void setGroupService(final GroupService grpServ) {
        this.groupService = grpServ;
    }

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public User createUser(final User user) throws UserAlreadyExistingException, DefaultGroupNotFoundException {

        // if the default user group is not available, we stop here
        groupService.throwIfNotExistsDefaultGroup();

        LOG.info("Save user '{} - {}' in the database", user.getLogin(), user.getContext());

        User modifiedUser;
        try {
            modifiedUser = userRepository.save(user); // updates the ID
            LOG.debug("User '{} - {}' created with id {}", modifiedUser.getLogin(), modifiedUser.getContext(), modifiedUser.getId());
        } catch (DataIntegrityViolationException dive) {
            LOG.error("The user '{} - {}' already exists", user.getLogin(), user.getContext());
            throw new UserAlreadyExistingException(dive);
        } catch (Exception ex) {
            LOG.error("Exception while creating user", ex);
            throw new UserAlreadyExistingException(ex);
        }

        // assign to default group
        groupService.assignUserToDefaultGroup(modifiedUser);

        return modifiedUser;
    }

    @Override
    public User createUser(final String login, final String context) throws UserAlreadyExistingException, DefaultGroupNotFoundException {

        final User user = new User(login, context);
        return createUser(user);
    }

    @Override
    public User createUserIfNotExists(final String login, final String context) throws UserAlreadyExistingException, DefaultGroupNotFoundException {

        Assert.isTrue(!StringUtils.isEmpty(login), "Cannot register user without valid login");

        final User registeredUser = findByLoginAndContext(login, context);
        if (registeredUser == null) {
            return createUser(login, context);
        } else {
            LOG.debug("User '{} - {}' already exists, no need for registering.", login, context);
            return registeredUser;
        }
    }

    @Override
    public boolean addUserToEntityGroup(final UserInformation userInfo) {

        Assert.notNull(userInfo, "Received invalid user object when trying to assign user to entity-based group");

        final User user = userInfo.getUser();
        Assert.notNull(user, "Received invalid user object when trying to assign user to entity-based group");

        final UserDetails userDetails = userInfo.getUserDetails();
        Assert.notNull(userDetails, "Received invalid user details object when trying to assign user to entity-based group");

        if (CollectionUtils.isEmpty(userDetails.getEntities())) {
            LOG.debug("User '{}' does not have any entity assigned; won't assign to any further group though", userDetails.getLogin());
            return false;
        }

        boolean result = true;
        final List<UserEntity> entitiesToAssign = Collections.synchronizedList(userDetails.getEntities());

        synchronized (entitiesToAssign) {
            if (!CollectionUtils.isEmpty(userDetails.getAllEntities())) {
                entitiesToAssign.addAll(userDetails.getAllEntities());
            }

            // if we reach this point, the user is associated to at least one entity
            // -> for each entity: retrieve or create the corresponding group, and assign
            for (final UserEntity entity : entitiesToAssign) {
                Group entityGroup = groupService.findGroupByName(entity.getName());
                if (entityGroup == null) {

                    // group not yet defined, so this must be the first user associated to the entity logging in -> create non-public group
                    try {
                        entityGroup = groupService.createGroup(entity.getName(), false);
                    } catch (Exception e) {
                        LOG.error("Received error creating group:", e);
                    }
                    if (entityGroup == null) {
                        LOG.warn("It seems the new group with name '{}' could not be created! Cannot assign user '{}' to it.", entity.getName(),
                                userDetails.getLogin());
                        return false;
                    }
                }

                // now the group is available - either was already, or has newly been created -> assign user
                result = groupService.assignUserToGroup(user, entityGroup) && result;
            }
        }

        // remove all group memberships not covered by the given entities any more
        // to identify these, get all groups and filter out the ones given from UD-repo and the default group
        final List<Group> groupsOfUser = groupService.getGroupsOfUser(user);

        // note: as the group name might have been modified in order to be URL-compliant, we need to map these group names in the same way
        final List<String> entityNames = userDetails.getEntities().stream()
                .map(ent -> InternalGroupName.getInternalGroupName(ent.getName()))
                .collect(Collectors.toList());

        final String defGroupName = groupService.getDefaultGroupName();
        final List<Group> superfluousGroups = groupsOfUser.stream().filter(
                grp -> !grp.getName().equals(defGroupName) && !entityNames.contains(grp.getName()))
                .collect(Collectors.toList());

        superfluousGroups.forEach(group -> groupService.removeUserFromGroup(user, group));

        return result;
    }

    @Override
    public User findByLoginAndContext(final String login, final String context) {

        final User foundUser = userRepository.findByLoginAndContext(login, context);
        LOG.debug("Found user based on login and context: {}", foundUser != null);
        return foundUser;
    }

    @Override
    public User getUserById(final Long userId) {

        final User foundUser = userRepository.findById(userId);
        LOG.debug("Found user based on id: {}", foundUser != null);
        return foundUser;
    }

    @Override
    public JsonUserProfile getUserProfile(final UserInformation userInfo) throws UserNotFoundException {

        Assert.notNull(userInfo, "userInfo undefined");

        final User user = userInfo.getUser();
        if (user == null) {
            LOG.error("User '" + userInfo.getLogin() + " - " + userInfo.getContext() + "' is unknown; cannot return its profile.");
            throw new UserNotFoundException(userInfo.getLogin(), userInfo.getContext());
        }

        final JsonUserProfile profile = new JsonUserProfile();
        profile.setAuthority(userInfo.getAuthority());

        profile.setUserid(userInfo.getAsHypothesisAccount());

        final UserDetails userInfoFromRepo = getUserDetailsFromUserRepo(user.getLogin(), user.getContext());
        if (userInfoFromRepo != null) {
            profile.setDisplayName(userInfoFromRepo.getDisplayName());
            final String entityName = userInfoFromRepo
                    .getEntities()
                    .stream()
                    // validate if user belongs to connectedEntity (organization name) and if so, use it as selected group
                    .filter(userEntity -> EntityChecker.isResponseFromUsersEntity(userInfo, userEntity.getName()))
                    .findFirst()
                    // Else, report the first one
                    .orElse(userInfoFromRepo.getEntities().get(0))
                    .getName();
            profile.setEntityName(entityName);
        }

        final List<Group> groups = groupService.getGroupsOfUser(user);
        if (groups != null) {

            if (Authorities.isIsc(userInfo.getAuthority())) {
                // for ISC, we should not return the default group -> filter out
                LOG.debug("Remove default group for ISC user {} - {}", user.getLogin(), user.getContext());
                groups.remove(groupService.findDefaultGroup());
            }

            for (final Group g : groups) {
                profile.addGroup(new JsonGroup(g.getDisplayName(), g.getName(), g.isPublicGroup()));
            }
        }

        // preferences
        final JsonUserShowSideBarPreference showSidebar = new JsonUserShowSideBarPreference();
        showSidebar.setShow_sidebar_tutorial(!user.isSidebarTutorialDismissed());
        profile.setPreferences(showSidebar);

        return profile;
    }

    @Override
    public User updateSidebarTutorialVisible(final String userLogin, final String userContext, final boolean visible) throws UserNotFoundException {

        Assert.isTrue(!StringUtils.isEmpty(userLogin), "Required user login missing");

        final User foundUser = userRepository.findByLoginAndContext(userLogin, userContext);
        if (foundUser == null) {
            LOG.error("User '" + userLogin + " - " + userContext + "' is unknown; cannot update its preferences.");
            throw new UserNotFoundException(userLogin, userContext);
        }

        foundUser.setSidebarTutorialDismissed(!visible);
        userRepository.save(foundUser);

        return foundUser;
    }

    @Override
    public UserDetails getUserDetailsFromUserRepo(final String login, final String context) {

        // check cache first - especially useful when returning search result with multiple annotations from save user!
        final UserDetails cachedDetails = userDetailsCache.getCachedUserDetails(login, context);
        if (cachedDetails != null) {
            LOG.debug("User details for user '{} - {}' still cached, use cached info", login, context);
            return cachedDetails;
        }

        final Map<String, String> params = new ConcurrentHashMap<>();
        params.put("userId", login);
        if (isContextEnabled()) {
            params.put("context", context);
        }

        // contact the ud-repo and cache the result
        try {
            LOG.debug("Searching for user '{}' in user repository for context '{}'", login, context);
            final UserDetails foundUser = restOperations.getForObject(repositoryUrl, UserDetails.class, params);

            if (foundUser != null) {
                try {
                    // additionally retrieve the list of total entities
                    // note: using a wrapper class didn't work, JSON deserialisation problem;
                    // so we use an array for simplicity
                    final UserEntity[] allEnts = restOperations.getForObject(repositoryUrlEntities, UserEntity[].class, params);
                    if (allEnts != null) {
                        foundUser.setAllEntities(Arrays.asList(allEnts));
                    }
                } catch (RestClientException rce) {
                    LOG.warn("Exception while getting user entities: {}", rce.getMessage());
                }
            }

            userDetailsCache.cache(login, context, foundUser);
            return foundUser;
        } catch (RestClientException e) {
            LOG.warn("Exception while getting user by login and context: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isContextEnabled() {
        return repositoryUrl != null && repositoryUrl.contains("{context}") &&
                repositoryUrlEntities != null && repositoryUrlEntities.contains("{context}");
    }

    @Override
    public User getExecutingUser(final UserInformation userInfo, final Group group) {

        User executingUser = userInfo.getUser();
        if (executingUser == null) {
            executingUser = findByLoginAndContext(userInfo.getLogin(), userInfo.getContext());
        }

        if (!groupService.isUserMemberOfGroup(executingUser, group)) {
            LOG.info("User {} - {} is not member of group {}, so he may not see any content", executingUser.getLogin(), executingUser.getContext(), group.getName());
            return null;
        }

        return executingUser;
    }

}
