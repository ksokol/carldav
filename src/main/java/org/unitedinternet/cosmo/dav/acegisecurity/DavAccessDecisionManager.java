/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.acegisecurity;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.acl.AclEvaluator;
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.UserService;
import org.unitedinternet.cosmo.util.UriTemplate;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Makes access control decisions for users and user
 * resources.  Allow service layer to handle authorization
 * for all other resources.
 * </p>
 */
public class DavAccessDecisionManager implements AccessDecisionManager, ExtendedDavConstants {

    private final UserService userService;
    private final PrincipalEvaluator userPrincipalEvaluator;
    private final PrincipalEvaluator userPrincipalCollectionEvaluator;

    public DavAccessDecisionManager(final UserService userService, final PrincipalEvaluator userPrincipalEvaluator, final PrincipalEvaluator userPrincipalCollectionEvaluator) {
        Assert.notNull(userService, "userService is null");
        Assert.notNull(userPrincipalEvaluator, "userPrincipalEvaluator is null");
        Assert.notNull(userPrincipalCollectionEvaluator, "userPrincipalCollectionEvaluator is null");
        this.userPrincipalEvaluator = userPrincipalEvaluator;
        this.userPrincipalCollectionEvaluator = userPrincipalCollectionEvaluator;
        this.userService = userService;
    }

    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {

        AclEvaluator evaluator;
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            CosmoUserDetails details = (CosmoUserDetails) authentication.getPrincipal();
            evaluator = new UserAclEvaluator(details.getUser());
        } else if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            User user = userService.getUser((String)authentication.getPrincipal());
            evaluator = new UserAclEvaluator(user);
        } else {
            throw new InsufficientAuthenticationException("Unrecognized authentication token");
        }

        HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();

        String path = request.getPathInfo();
        if (path == null) {
            path = "/";
        }
        // remove trailing slash that denotes a collection
        if (!path.equals("/") && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        try {
            match(path, request.getMethod(), evaluator);
        } catch (AclEvaluationException e) {
            throw new DavAccessDeniedException(request.getRequestURI(), e.getPrivilege());
        }
    }

    /**
     * Always returns true, as this manager does not support any
     * config attributes.
     */
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /**
     * Returns true if the secure object is a
     * {@link FilterInvocation}.
     */
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    protected void match(String path, String method, AclEvaluator evaluator) {
        if (TEMPLATE_USERS.match(false, path) != null) {
            userPrincipalCollectionEvaluator.evaluate(method, (UserAclEvaluator) evaluator);
            return;
        }

        final UriTemplate.Match match = TEMPLATE_USER.match(false, path);
        if (match != null) {
            String username = match.get("username");
            User user = userService.getUser(username);
            if (user == null) {
                return;
            }

            userPrincipalEvaluator.evaluate(method, (UserAclEvaluator) evaluator);
        }
    }
}
