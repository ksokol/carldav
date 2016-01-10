/*
 * XCaldavConstants.java Nov 6, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dav.caldav;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Provides constants for media types, XML namespaces, names and
 * values, DAV properties and resource types not defined by the CalDAV
 * spec.
 */
public class XCaldavConstants {
    /** The CalDAV XML namespace */
    public static final String PRE_XCALDAV = "C";
    public static final String NS_XCALDAV =
        "urn:ietf:params:xml:ns:xcaldavoneandone";
    public static final Namespace NAMESPACE_XCALDAV =
        Namespace.getNamespace(PRE_XCALDAV, NS_XCALDAV);
    
    
    /** The CalDAV property name XC:calendar-color */
    public static final String PROPERTY_XCALDAV_CALENDAR_COLOR =
        "calendar-color";

    /** The CalDAV property XC:calendar-color */
    public static final DavPropertyName CALENDAR_COLOR=
        DavPropertyName.create(PROPERTY_XCALDAV_CALENDAR_COLOR,
                               NAMESPACE_XCALDAV);
}
