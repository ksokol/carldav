/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.impl;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

public class DavHomeCollection extends DavCollectionBase {

    public DavHomeCollection(HibCollectionItem collection,
                             DavResourceLocator locator,
                             DavResourceFactory factory)
            throws CosmoDavException {
        super(collection, locator, factory);
    }

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, PROPFIND";
    }

}
