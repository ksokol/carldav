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
package org.unitedinternet.cosmo.dav.provider;

import org.apache.jackrabbit.webdav.WebdavResponse;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.WebDavResource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface DavProvider {

    void get(HttpServletRequest request, WebdavResponse response, WebDavResource resource) throws CosmoDavException, IOException;

    void head(HttpServletRequest request, WebdavResponse response, WebDavResource resource) throws CosmoDavException, IOException;

    void propfind(HttpServletRequest request, WebdavResponse response, WebDavResource resource) throws CosmoDavException, IOException;

    void put(HttpServletRequest request, HttpServletResponse response, DavContent content) throws CosmoDavException, IOException;

    void delete(HttpServletRequest request, WebdavResponse response, WebDavResource resource) throws CosmoDavException, IOException;

    void report(HttpServletRequest request, WebdavResponse response, WebDavResource resource) throws CosmoDavException, IOException;
}
