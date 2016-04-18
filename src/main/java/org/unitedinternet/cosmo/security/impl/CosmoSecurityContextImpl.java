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
package org.unitedinternet.cosmo.security.impl;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.security.BaseSecurityContext;

public class CosmoSecurityContextImpl extends BaseSecurityContext {

    public CosmoSecurityContextImpl(Authentication authentication) {
        super(authentication);
    }

    protected void processPrincipal() {  
        //anonymous principals do not have CosmoUserDetails and by
        //definition are not running as other principals
        if (getPrincipal() instanceof UsernamePasswordAuthenticationToken) {
            User details = (User) ((Authentication) getPrincipal()).getPrincipal();
            setUser(details);
        } else {
            throw new CosmoException("Unknown principal type " + getPrincipal().getClass().getName(),
                    new CosmoException());
        }
    }
}
