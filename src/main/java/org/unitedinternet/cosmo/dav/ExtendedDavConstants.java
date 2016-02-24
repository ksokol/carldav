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
package org.unitedinternet.cosmo.dav;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.NAMESPACE_CARDDAV;

import carldav.jackrabbit.webdav.CustomDavConstants;
import carldav.jackrabbit.webdav.CustomDavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.util.CosmoQName;
import org.unitedinternet.cosmo.util.UriTemplate;

/**
 * Provides constants defined by Cosmo proprietary *DAV extensions.
 */
public interface ExtendedDavConstants extends CustomDavConstants {

    String PRE_COSMO = "cosmo";
    String NS_COSMO = "http://osafoundation.org/cosmo/DAV";
    String XML_LANG = "lang";
    String ADDRESSBOOK = "addressbook";

    CustomDavPropertyName SUPPORTEDREPORTSET =
            CustomDavPropertyName.create("supported-report-set", NAMESPACE);

    String QN_PROPFIND =
        DomUtil.getQualifiedName(XML_PROPFIND, NAMESPACE);
    String QN_HREF =
        DomUtil.getQualifiedName(XML_HREF, NAMESPACE);

    CosmoQName RESOURCE_TYPE_COLLECTION =
        new CosmoQName(NAMESPACE.getURI(), XML_COLLECTION, NAMESPACE.getPrefix());

    CosmoQName RESOURCE_TYPE_ADDRESSBOOK =
            new CosmoQName(NAMESPACE_CARDDAV.getURI(), ADDRESSBOOK, NAMESPACE_CARDDAV.getPrefix());

    UriTemplate TEMPLATE_HOME = new UriTemplate("/{username}/*");
    UriTemplate CARD_HOME = new UriTemplate("/{username}/contacts");
    UriTemplate TEMPLATE_USER = new UriTemplate("/users/{username}/*");
    UriTemplate TEMPLATE_PRINCIPALS = new UriTemplate("/");
}
