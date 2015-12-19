/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.security;

import org.unitedinternet.cosmo.model.User;

import java.security.Principal;

/**
 * Base class for implementations of {@link CosmoSecurityContext}.
 */
public abstract class BaseSecurityContext implements CosmoSecurityContext {

    private Principal principal;
    private User user;

    public BaseSecurityContext(Principal principal) {
        this.principal = principal;
        processPrincipal();
    }

    /**
     * @return an instance of {@link User} describing the user
     * represented by the security context, or <code>null</code> if
     * the context does not represent a user.
     */
    public User getUser() {
        return user;
    }

    /* ----- our methods ----- */

    protected Principal getPrincipal() {
        return principal;
    }

    protected void setUser(User user) {
        this.user = user;
    }

    /**
     * Called by the constructor to set the context state. Examines
     * the principal to decide if it represents user or
     * anonymous access.
     */
    protected abstract void processPrincipal();

}
