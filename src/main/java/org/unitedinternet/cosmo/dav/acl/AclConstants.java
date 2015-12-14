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
package org.unitedinternet.cosmo.dav.acl;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.unitedinternet.cosmo.util.CosmoQName;

/**
 * Provides constants for media types, XML namespaces, names and
 * values, DAV properties and resource types defined by the WebDAV ACL
 * spec.
 */
public interface AclConstants extends DavConstants {

    /** The ACL XML element name <DAV:principal> */
    String ELEMENT_ACL_PRINCIPAL = "principal";

    String PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET =
        "current-user-privilege-set";

    DavPropertyName CURRENTUSERPRIVILEGESET =
        DavPropertyName.create(PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET,
                               NAMESPACE);

    CosmoQName RESOURCE_TYPE_PRINCIPAL =
        new CosmoQName(NAMESPACE.getURI(), ELEMENT_ACL_PRINCIPAL,
                  NAMESPACE.getPrefix());
}
