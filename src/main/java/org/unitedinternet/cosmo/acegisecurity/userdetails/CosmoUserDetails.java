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

import carldav.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Wraps a Cosmo <code>User</code> to provide Acegi Security with access to user account information.
 * <p>
 * If the associated user is an administrator, contains an authority named <code>ROLE_ROOT</code>.
 * <p>
 * If the associated user is not an administrator, contains an authority named <code>ROLE_USER</code>.
 *
 * @see UserDetails
 * @see GrantedAuthority
 */
@Deprecated
public class CosmoUserDetails extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 3034617040424768103L;

    private final User user;

    @Deprecated
    public CosmoUserDetails(final String username, final String password, final boolean enabled, final boolean accountNonExpired,
            final boolean credentialsNonExpired, final boolean accountNonLocked, final Collection<? extends GrantedAuthority> authorities, final User user) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        Assert.notNull(user, "user is null");
        this.user = user;
    }

    @Deprecated
    public User getUser() {
        return user;
    }
}
