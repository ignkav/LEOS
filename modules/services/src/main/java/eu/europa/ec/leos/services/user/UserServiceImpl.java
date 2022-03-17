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
package eu.europa.ec.leos.services.user;

import eu.europa.ec.leos.integration.UsersProvider;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.AuthenticatedUser;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private UsersProvider usersClient;

    @Autowired
    public UserServiceImpl(UsersProvider usersClient) {
        this.usersClient = usersClient;
    }

    @Override
    @Cacheable(value="users", cacheManager = "cacheManager")
    public User getUser(String login) {
        User result = usersClient.getUserByLogin(login);
        return result;
    }

    @Override
    public List<User> searchUsersByKey(String key) {
        List<User> result = usersClient.searchUsers(key);
        return result;
    }

    @Override
    public List<User> searchUsersInContextByKey(String key, String searchContext) {
        List<User> result = usersClient.searchUsersInContext(key, searchContext);
        return result;
    }

    @Override
    public void switchUser(String login) {
        Validate.notNull(login, "login is required!");
        LOG.debug("User switch request received for user -" + login);
        PreAuthenticatedAuthenticationToken preAuthRequest =
                new PreAuthenticatedAuthenticationToken(new AuthenticatedUser(usersClient.getUserByLogin(login)), "");
        preAuthRequest.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(preAuthRequest);
        LOG.debug("User switch request is successful. New user is -" + login);
    }

    @Override
    public void switchUserWithAuthorities(String login, Collection<? extends GrantedAuthority> authorities) {
        Validate.notNull(login, "login is required!");
        LOG.debug("User switch request received for user -" + login);
        PreAuthenticatedAuthenticationToken preAuthRequest =
                new PreAuthenticatedAuthenticationToken(new AuthenticatedUser(usersClient.getUserByLogin(login)), "", authorities);
        SecurityContextHolder.getContext().setAuthentication(preAuthRequest);
        LOG.debug("User switch request is successful. New user is -" + login);
    }
}
