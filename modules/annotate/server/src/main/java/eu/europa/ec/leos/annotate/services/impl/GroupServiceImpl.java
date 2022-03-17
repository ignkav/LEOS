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
import eu.europa.ec.leos.annotate.model.GroupComparator;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.entity.UserGroup;
import eu.europa.ec.leos.annotate.model.web.helper.JsonConverter;
import eu.europa.ec.leos.annotate.model.web.user.JsonGroupWithDetails;
import eu.europa.ec.leos.annotate.repository.GroupRepository;
import eu.europa.ec.leos.annotate.repository.UserGroupRepository;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.exceptions.DefaultGroupNotFoundException;
import eu.europa.ec.leos.annotate.services.exceptions.GroupAlreadyExistingException;
import eu.europa.ec.leos.annotate.services.impl.util.InternalGroupName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for managing user groups 
 */
@Service
public class GroupServiceImpl implements GroupService {

    private static final Logger LOG = LoggerFactory.getLogger(GroupServiceImpl.class);

    /**
     * default group name injected from configuration
     */
    @Value("${defaultgroup.name}")
    private String defaultGroupName;

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private GroupRepository groupRepos;

    @Autowired
    private UserGroupRepository userGroupRepos;

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public String getDefaultGroupName() {
        return defaultGroupName;
    }

    @Override
    public Group createGroup(final String name, final boolean isPublic) throws GroupAlreadyExistingException {

        Assert.isTrue(!StringUtils.isEmpty(name), "Cannot create group without a given name!");

        LOG.info("Save group with name '{}' in the database", name);

        final Group newGroup = new Group(InternalGroupName.getInternalGroupName(name), name, isPublic);
        try {
            groupRepos.save(newGroup);
            LOG.debug("Group '{}' created with id {}", name, newGroup.getId());
        } catch (DataIntegrityViolationException dive) {
            LOG.error("The group '{}' already exists", name);
            throw new GroupAlreadyExistingException(dive);
        } catch (Exception ex) {
            LOG.error("Exception while creating group", ex);
            throw new GroupAlreadyExistingException(ex);
        }
        return newGroup;
    }

    @Override
    public Group findGroupByName(final String groupName) {

        Group foundGroup = groupRepos.findByName(groupName);
        LOG.debug("Found group based on group name: {}", foundGroup != null);
        if(foundGroup == null) {
            // try again with URL-conform internal name
            foundGroup = groupRepos.findByName(InternalGroupName.getInternalGroupName(groupName));
            LOG.debug("Found group based on internal group name: {}", foundGroup != null);
        }
        return foundGroup;
    }

    @Override
    public Group findDefaultGroup() {
        return findGroupByName(defaultGroupName);
    }

    @Override
    public void assignUserToDefaultGroup(final User user) throws DefaultGroupNotFoundException {

        final Group defaultGroup = findDefaultGroup();
        if (defaultGroup == null) {
            LOG.error("Cannot assign user to the default group; seems not to be configured in database");
            throw new DefaultGroupNotFoundException();
        }

        assignUserToGroup(user, defaultGroup);
    }

    @Override
    public boolean assignUserToGroup(final User user, final Group group) {

        Assert.notNull(user, "User must be defined to assign user to group");
        Assert.notNull(group, "Group must be defined to assign user to group");

        // check if already assigned
        if (isUserMemberOfGroup(user, group)) {
            LOG.info("User '{}' is already member of group '{}' - nothing to do", user.getLogin(), group.getName());
            return true;
        }

        final long userId = user.getId();
        final long groupId = group.getId();
        final UserGroup foundUserGroup = new UserGroup(userId, groupId);
        userGroupRepos.save(foundUserGroup);
        LOG.info("Saved user '{}' (id {}) as member of group '{}' (id {})", user.getLogin(), userId, group.getName(), groupId);

        return true;
    }

    @Override
    @Transactional
    public boolean removeUserFromGroup(final User user, final Group group) {
        
        Assert.notNull(user, "User must be defined to remove user to group");
        Assert.notNull(group, "Group must be defined to remove user to group");

        // check if already assigned
        if (!isUserMemberOfGroup(user, group)) {
            LOG.info("User '{}' is no member of group '{}' - nothing to do", user.getLogin(), group.getName());
            return false;
        }

        final long userId = user.getId();
        final long groupId = group.getId();
        userGroupRepos.deleteByUserIdAndGroupId(userId, groupId);
        LOG.info("Removed user '{}' (id {}) as member of group '{}' (id {})", user.getLogin(), userId, group.getName(), groupId);

        return true;
    }

    @Override
    public void throwIfNotExistsDefaultGroup() throws DefaultGroupNotFoundException {

        if (findDefaultGroup() == null) {
            LOG.error("Default group seems not to be configured in database; throw DefaultGroupNotFoundException");
            throw new DefaultGroupNotFoundException();
        }
    }

    @Override
    public boolean isUserMemberOfGroup(final User user, final Group group) {

        Assert.notNull(user, "Cannot check if user is group member when no user is given");
        Assert.notNull(group, "Cannot check if user is group member when no group is given");

        final UserGroup membership = userGroupRepos.findByUserIdAndGroupId(user.getId(), group.getId());
        LOG.debug("User '{}' (id {}) is member of group '{}' (id {}): {}", user.getLogin(), user.getId(), group.getName(), group.getId(), membership != null);
        return membership != null;
    }

    @Override
    public List<Group> getGroupsOfUser(final User user) {

        Assert.notNull(user, "Cannot search for groups of undefined User (null)");

        final List<UserGroup> foundUserGroups = userGroupRepos.findByUserId(user.getId());
        LOG.debug("Found {} groups in which user '{}' is member", foundUserGroups == null ? 0 : foundUserGroups.size(), user.getLogin());

        if (foundUserGroups == null) {
            return null;
        }

        // extract groupIds of found assignments and get corresponding groups
        return groupRepos.findByIdIn(foundUserGroups.stream().map(usergroup -> usergroup.getGroupId()).collect(Collectors.toList()));
    }

    @Override
    public List<Long> getGroupIdsOfUser(final User user) {

        if (user == null) {
            LOG.warn("Cannot retrieve group IDs from undefined user");
            return null;
        }

        final List<UserGroup> userGroups = userGroupRepos.findByUserId(user.getId());
        if (userGroups == null) return null;

        // extract the groupIds
        return userGroups.stream().map(UserGroup::getGroupId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserIdsOfGroup(final Group group) {

        if (group == null) {
            LOG.warn("Cannot retrieve user IDs from undefined group");
            return null;
        }

        final List<UserGroup> userGroups = userGroupRepos.findByGroupId(group.getId());
        if (userGroups == null) return null;

        // extract the userId
        return userGroups.stream().map(UserGroup::getUserId).collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserIdsOfGroup(final String groupName) {

        final Group group = findGroupByName(groupName);
        return getUserIdsOfGroup(group);
    }

    @Override
    public List<JsonGroupWithDetails> getUserGroupsAsJson(final UserInformation userinfo) {

        List<Group> allGroups;

        if (userinfo == null || userinfo.getUser() == null || StringUtils.isEmpty(userinfo.getAuthority())) {
            LOG.info("Groups retrieval request received without user - return default group only");
            allGroups = new ArrayList<>();
            allGroups.add(findDefaultGroup());
        } else {

            final User user = userinfo.getUser();
            allGroups = getGroupsOfUser(user);
            if (allGroups == null) {
                LOG.warn("Did not receive a valid result from querying groups of user");
                return null;
            }
            LOG.debug("Found {} groups for user '{}'", allGroups.size(), user.getLogin());
            
            if(Authorities.isIsc(userinfo.getAuthority())) {
                // for ISC, we should not return the default group -> filter out
                LOG.debug("Remove default group for ISC user {}", user.getLogin());
                allGroups.remove(findDefaultGroup());
            }
        }

        // sort the groups as desired
        allGroups.sort(new GroupComparator(defaultGroupName));

        return JsonConverter.convertToJsonGroupWithDetailsList(allGroups);
    }

    @Override
    public Group getConnectedEntityGroup(final UserInformation userInfo) {

        if(userInfo == null) {
            return null;
        }
        
        if(!StringUtils.isEmpty(userInfo.getConnectedEntity())) {
            return findGroupByName(userInfo.getConnectedEntity());
        }
        
        return null;
    }

}
