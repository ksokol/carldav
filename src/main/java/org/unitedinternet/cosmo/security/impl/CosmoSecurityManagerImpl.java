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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.Permission;
import org.unitedinternet.cosmo.security.PermissionDeniedException;
import org.unitedinternet.cosmo.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * The default implementation of the {@link CosmoSecurityManager}
 * interface that provides a {@link CosmoSecurityContext} from
 * security information contained in JAAS or Acegi Security.
 */
public class CosmoSecurityManagerImpl implements CosmoSecurityManager {
    private static final Log LOG =
        LogFactory.getLog(CosmoSecurityManagerImpl.class);

    private AuthenticationManager authenticationManager;
    
    private UserService userService;
    
    
    // store additional tickets for authenticated principal
    private ThreadLocal<Set<Ticket>> tickets = new ThreadLocal<Set<Ticket>>();

    /* ----- CosmoSecurityManager methods ----- */

    /**
     * Provide a <code>CosmoSecurityContext</code> representing a
     * Cosmo user previously authenticated by the Cosmo security
     * system.
     */
    public CosmoSecurityContext getSecurityContext()
        throws CosmoSecurityException {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authen = context.getAuthentication();
        if (authen == null) {
            throw new CosmoSecurityException("no Authentication found in " +
                                             "SecurityContext");
        }
        
        if (authen instanceof PreAuthenticatedAuthenticationToken) {
            User user = userService.getUser((String) authen.getPrincipal());
            return new CosmoSecurityContextImpl(authen, tickets.get(), user);
        }


        return createSecurityContext(authen);
    }

    /**
     * Initiate the current security context with the current user.
     * This method is used when the server needs to run code as a
     * specific user.
     */
    public CosmoSecurityContext initiateSecurityContext(User user)
            throws CosmoSecurityException {

        UserDetails details = new CosmoUserDetails(user);

        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                details, "", details.getAuthorities());

        credentials.setDetails(details);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(credentials);
        return createSecurityContext(credentials);
    }

    /* ----- our methods ----- */

    /**
     */
    protected CosmoSecurityContext createSecurityContext(Authentication authen) {
        return new CosmoSecurityContextImpl(authen, tickets.get());
    }

    /**
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * 
     * @param userService UserService
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    public void registerTickets(Set<Ticket> tickets) {
        Set<Ticket> currentTickets = this.tickets.get();
        if(currentTickets==null) {
            this.tickets.set(new HashSet<Ticket>());
        }
        this.tickets.get().addAll(tickets);
    }
    
    public void unregisterTickets() {
        this.tickets.remove();
    }
}
