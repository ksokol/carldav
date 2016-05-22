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

import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.impl.DavItemResourceBase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FileProvider extends BaseProvider {

    public FileProvider(DavResourceFactory resourceFactory) {
        super(resourceFactory);
    }

    // DavProvider methods

    public void put(HttpServletRequest request,
                    HttpServletResponse response,
                    WebDavResource content)
        throws CosmoDavException, IOException {
        if (! content.getParent().exists()) {
            throw new ConflictException("One or more intermediate collections must be created");
        }
        
        if(!(content instanceof DavItemResourceBase)){
            throw new IllegalArgumentException("Expected type for 'content' is: [" + DavItemResourceBase.class.getName() + "]");
        }
        int status = content.exists() ? 204 : 201;
        content.getParent().addContent(content, createInputContext(request));
        response.setStatus(status);
        response.setHeader("ETag", content.getETag());
    }
}
