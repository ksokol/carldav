/*
 * Copyright 2005-2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.acegisecurity.userdetails;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.List;

public class CosmoUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CosmoUserDetailsService(final UserDao userDao) {
        Assert.notNull(userDao, "userDao is null");
        this.userDao = userDao;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        User user = userDao.getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("user " + username + " not found");
        }

        final boolean accountNonLocked = !user.isLocked();
        final List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(StringUtils.defaultIfBlank(user.getRole(), "ROLE_USER"));

        return new CosmoUserDetails(user.getEmail(), user.getPassword(), true, true, true, accountNonLocked, authorities, user);
    }
}
