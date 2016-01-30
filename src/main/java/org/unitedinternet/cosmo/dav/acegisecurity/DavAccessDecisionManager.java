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

import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.util.UriComponentsBuilder;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * Makes access control decisions for users and user
 * resources.  Allow service layer to handle authorization
 * for all other resources.
 * </p>
 */
//TODO refactor me
public class DavAccessDecisionManager implements AccessDecisionManager, ExtendedDavConstants {

    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        String userId;

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            userId = ((CosmoUserDetails) authentication.getPrincipal()).getUsername();
        } else {
            throw new InsufficientAuthenticationException("Unrecognized authentication token");
        }

        final HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
        final String result = match(userId, request.getRequestURI());
        final CosmoUserDetails principal = (CosmoUserDetails) authentication.getPrincipal();

        if(result == null || ("user".equalsIgnoreCase(result)) && principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return;
        }
        throw new InsufficientAuthenticationException(principal.getUsername() + " not allowed to call " + result);
    }

    protected String match(String userId, String path) {
        UriComponentsBuilder uriComponentsBuilder;

        try {
            uriComponentsBuilder = UriComponentsBuilder.fromPath(URLDecoder.decode(path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            //TODO
            throw new InsufficientAuthenticationException(e.getMessage(), e);
        }

        final List<String> pathSegments = uriComponentsBuilder.build().getPathSegments();

        if(pathSegments.size() == 0) {
            return null;
        }

        if(!"dav".equals(pathSegments.get(0)) && !"principals".equals(pathSegments.get(0))) {
            return pathSegments.get(0);
        }

        if(pathSegments.size() < 2) {
            throw new InsufficientAuthenticationException("access denied for " + path);
        }

        final String userIdFromUrl = pathSegments.get(1);

        if(!StringUtils.equalsIgnoreCase(userId, userIdFromUrl)) {
            if(pathSegments.size() < 3) {
                throw new InsufficientAuthenticationException("access denied for " + path);
            }

            if(StringUtils.equalsIgnoreCase(userId, pathSegments.get(3))) {
                return null;
            }

            throw new InsufficientAuthenticationException("access denied for " + path);
        }

        return null;
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
}
