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
package org.unitedinternet.cosmo.dav.acl;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

/**
 * <p>
 * A set of WebDAV access control privileges.
 * </p>
 
 */
public class DavPrivilegeSet extends HashSet<DavPrivilege>
    implements ExtendedDavConstants, CaldavConstants, XmlSerializable {

    private static final long serialVersionUID = 1977693588813371074L;

    public DavPrivilegeSet() {
        super();
    }

    // XmlSerializable methods

    public Element toXml(Document document) {
        Element root =
            DomUtil.createElement(document, "privilege", NAMESPACE);
        for (DavPrivilege p : this) {
            if (p.isAbstract()) {
                continue;
            }
            root.appendChild(p.toXml(document));
        }
        return root;
    }

    // our methods

    public boolean containsAny(DavPrivilege... privileges) {
        for (DavPrivilege p : privileges) {
            if (contains(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsRecursive(DavPrivilege test) {
        for (DavPrivilege p : this) {
            if (p.equals(test) || p.containsRecursive(test)) {
                return true;
            }
        }
        return false;
    }


    public String toString() {
        return StringUtils.join(this, ", ");
    }

    public static final DavPrivilegeSet createFromXml(Element root) {
        if (! DomUtil.matches(root, "privilege", NAMESPACE)) {
            throw new IllegalArgumentException("Expected DAV:privilege element");
        }
        DavPrivilegeSet privileges = new DavPrivilegeSet();

        if (DomUtil.hasChildElement(root, "read", NAMESPACE)) {
            privileges.add(DavPrivilege.READ);
        }
        if (DomUtil.hasChildElement(root, "write", NAMESPACE)) {
            privileges.add(DavPrivilege.WRITE);
        }
        if (DomUtil.hasChildElement(root, "read-free-busy", NAMESPACE_CALDAV)) {
            privileges.add(DavPrivilege.READ_FREE_BUSY);
        }

        return privileges;
    }
}
