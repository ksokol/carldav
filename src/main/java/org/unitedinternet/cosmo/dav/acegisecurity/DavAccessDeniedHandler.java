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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.unitedinternet.cosmo.dav.acl.NeedsPrivilegesException;
import org.unitedinternet.cosmo.dav.impl.StandardDavResponse;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * Handles <code>AccessDeniedException</code> by sending a 403 response enclosing an XML body describing the error.
 * </p>
 */
public class DavAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        final StandardDavResponse davResponse = new StandardDavResponse(response);
        NeedsPrivilegesException exceptionToSend;

        if (accessDeniedException instanceof DavAccessDeniedException) {
            DavAccessDeniedException exception = (DavAccessDeniedException) accessDeniedException;
            exceptionToSend = new NeedsPrivilegesException(exception.getHref(), exception.getPrivilege());
        } else {
            exceptionToSend = new NeedsPrivilegesException(accessDeniedException.getMessage());
        }

        davResponse.sendDavError(exceptionToSend);
    }
}
