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

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.unitedinternet.cosmo.util.CosmoQName;
import org.unitedinternet.cosmo.util.UriTemplate;

/**
 * Provides constants defined by Cosmo proprietary *DAV extensions.
 */
public interface ExtendedDavConstants extends DavConstants {

    String PRE_COSMO = "cosmo";
    String NS_COSMO = "http://osafoundation.org/cosmo/DAV";
    String XML_LANG = "lang";
    String XML_PRIVILEGE = "privilege";
    Namespace NAMESPACE_XML =
            Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");

    /** The Cosmo XML namespace  */
    Namespace NAMESPACE_COSMO =
        Namespace.getNamespace(PRE_COSMO, NS_COSMO);

    /** The Cosmo property name <code>exclude-free-busy-rollup</code> */
    String PROPERTY_EXCLUDE_FREE_BUSY_ROLLUP =
        "exclude-free-busy-rollup";

    /** The Cosmo property <code>cosmo:exclude-free-busy-rollup</code> */
    DavPropertyName EXCLUDEFREEBUSYROLLUP =
        DavPropertyName.create(PROPERTY_EXCLUDE_FREE_BUSY_ROLLUP,
                               NAMESPACE_COSMO);

    /** The Cosmo property name <code>uuid</code> */
    String PROPERTY_UUID = "uuid";

    /** The Cosmo property <code>cosmo:uuid</code> */
    DavPropertyName UUID =
        DavPropertyName.create(PROPERTY_UUID, NAMESPACE_COSMO);

    DavPropertyName SUPPORTEDREPORTSET =
        DavPropertyName.create("supported-report-set", NAMESPACE);
    DavPropertyName CURRENTUSERPRINCIPAL =
            DavPropertyName.create("current-user-principal", NAMESPACE);


    String QN_PROPFIND =
        DomUtil.getQualifiedName(XML_PROPFIND, NAMESPACE);
    String QN_PROPERTYUPDATE =
        DomUtil.getQualifiedName(XML_PROPERTYUPDATE, NAMESPACE);
    String QN_SET =
        DomUtil.getQualifiedName(XML_SET, NAMESPACE);
    String QN_REMOVE =
        DomUtil.getQualifiedName(XML_REMOVE, NAMESPACE);
    String QN_PROP =
        DomUtil.getQualifiedName(XML_PROP, NAMESPACE);
    String QN_HREF =
        DomUtil.getQualifiedName(XML_HREF, NAMESPACE);

    CosmoQName RESOURCE_TYPE_COLLECTION =
        new CosmoQName(NAMESPACE.getURI(), XML_COLLECTION, NAMESPACE.getPrefix());

    UriTemplate TEMPLATE_COLLECTION =
        new UriTemplate("/collection/{uid}/*");
    UriTemplate TEMPLATE_ITEM =
        new UriTemplate("/item/{uid}/*");
    UriTemplate TEMPLATE_USERS =
        new UriTemplate("/users");
    UriTemplate TEMPLATE_USER =
        new UriTemplate("/users/{username}");
    UriTemplate TEMPLATE_HOME =
        new UriTemplate("/{username}/*");
}
