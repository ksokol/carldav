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
package org.unitedinternet.cosmo.dav.caldav;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.unitedinternet.cosmo.util.CosmoQName;

/**
 * Provides constants for media types, XML namespaces, names and values, DAV properties and resource types defined by the CalDAV spec.
 */
public interface CaldavConstants {

    String VALUE_YES = "yes";
    String VALUE_NO = "no";
    /** The iCalendar media type */
    String CT_ICALENDAR = "text/calendar";
    /** The media type for calendar collections */
    String CONTENT_TYPE_CALENDAR_COLLECTION = "application/x-calendar-collection";

    /** The CalDAV XML namespace */
    String PRE_CALDAV = "C";
    String NS_CALDAV = "urn:ietf:params:xml:ns:caldav";
    Namespace NAMESPACE_CALDAV = Namespace.getNamespace(PRE_CALDAV, NS_CALDAV);

    /** The Calendar Server XML namespace */
    String PRE_CS = "CS";
    String NS_CS = "http://calendarserver.org/ns/";
    Namespace NAMESPACE_CS = Namespace.getNamespace(PRE_CS, NS_CS);

    /** The CalDAV XML element name <CALDAV:mkcalendar> */
    String ELEMENT_CALDAV_MKCALENDAR = "mkcalendar";
    String QN_MKCALENDAR = DomUtil.getQualifiedName(ELEMENT_CALDAV_MKCALENDAR, NAMESPACE_CALDAV);
    /** The CalDAV XML element name <CALDAV:calendar> */
    String ELEMENT_CALDAV_CALENDAR = "calendar";
    /** The CalDAV XML element name <CALDAV:comp> */
    String ELEMENT_CALDAV_COMP = "comp";
    /** The CalDAV XML element name <CALDAV:supported-collation> */
    String ELEMENT_CALDAV_SUPPORTEDCOLLATION = "supported-collation";
    /** The CalDAV XML element name <CALDAV:calendar-data> */
    String ELEMENT_CALDAV_CALENDAR_DATA = "calendar-data";
    /** The CalDAV XML element name <CALDAV:timezone> */
    String ELEMENT_CALDAV_TIMEZONE = "timezone";
    String QN_CALDAV_TIMEZONE = DomUtil.getQualifiedName(ELEMENT_CALDAV_TIMEZONE, NAMESPACE_CALDAV);
    /** The CalDAV XML element name <CALDAV:allcomp> */
    String ELEMENT_CALDAV_ALLCOMP = "allcomp";
    /** The CalDAV XML element name <CALDAV:allprop> */
    String ELEMENT_CALDAV_ALLPROP = "allprop";
    /** The CalDAV XML element name <CALDAV:prop> */
    String ELEMENT_CALDAV_PROP = "prop";
    /** The CalDAV XML element name <CALDAV:expand> */
    String ELEMENT_CALDAV_EXPAND = "expand";
    /** The CalDAV XML element name <CALDAV:limit-recurrence-set> */
    String ELEMENT_CALDAV_LIMIT_RECURRENCE_SET = "limit-recurrence-set";
    /** The CalDAV XML element name <CALDAV:filter> */
    String ELEMENT_CALDAV_FILTER = "filter";
    /** The CalDAV XML element name <CALDAV:comp-filter> */
    String ELEMENT_CALDAV_COMP_FILTER = "comp-filter";
    /** The CalDAV XML element name <CALDAV:prop-filter> */
    String ELEMENT_CALDAV_PROP_FILTER = "prop-filter";
    /** The CalDAV XML element name <CALDAV:param-filter> */
    String ELEMENT_CALDAV_PARAM_FILTER = "param-filter";
    /** The CalDAV XML element name <CALDAV:time-range> */
    String ELEMENT_CALDAV_TIME_RANGE = "time-range";
    String QN_CALDAV_TIME_RANGE = DomUtil.getQualifiedName(ELEMENT_CALDAV_TIME_RANGE, NAMESPACE_CALDAV);
    /** The CalDAV XML element name <CALDAV:is-not-defined> */
    String ELEMENT_CALDAV_IS_NOT_DEFINED = "is-not-defined";
    /** The (old) CalDAV XML element name <CALDAV:is-defined> */
    String ELEMENT_CALDAV_IS_DEFINED = "is-defined";
    /** The CalDAV XML element name <CALDAV:text-match> */
    String ELEMENT_CALDAV_TEXT_MATCH = "text-match";
    /** The CalDAV XML element name <CALDAV:calendar-multiget> */
    String ELEMENT_CALDAV_CALENDAR_MULTIGET = "calendar-multiget";
    /** The CalDAV XML element name <CALDAV:calendar-query> */
    String ELEMENT_CALDAV_CALENDAR_QUERY = "calendar-query";
    /** The CalDAV XML element name <CALDAV:free-busy-query> */
    String ELEMENT_CALDAV_CALENDAR_FREEBUSY = "free-busy-query";
    /** The CalDAV XML attribute name CALDAV:name */
    String ATTR_CALDAV_NAME = "name";
    /** The CalDAV XML attribute name CALDAV:content-type */
    String ATTR_CALDAV_CONTENT_TYPE = "content-type";
    /** The CalDAV XML attribute name CALDAV:version */
    String ATTR_CALDAV_VERSION = "version";
    /** The CalDAV XML attribute name CALDAV:novalue */
    String ATTR_CALDAV_NOVALUE = "novalue";
    /** The CalDAV XML attribute name CALDAV:collation */
    String ATTR_CALDAV_COLLATION = "collation";
    /** The CalDAV XML attribute name CALDAV:negate-condition */
    String ATTR_CALDAV_NEGATE_CONDITION = "negate-condition";
    /** The CalDAV XML attribute name CALDAV:start */
    String ATTR_CALDAV_START = "start";
    /** The CalDAV XML attribute name CALDAV:end */
    String ATTR_CALDAV_END = "end";

    /** The CalDAV property name CALDAV:calendar-data */
    String PROPERTY_CALDAV_CALENDAR_DATA = "calendar-data";
    /** The CalDAV property name CALDAV:calendar-description */
    String PROPERTY_CALDAV_CALENDAR_DESCRIPTION = "calendar-description";
    /** The CalDAV property name CALDAV:calendar-timezone */
    String PROPERTY_CALDAV_CALENDAR_TIMEZONE = "calendar-timezone";
    /** The CalDAV property name CALDAV:supported-calendar-component-set */
    String PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET = "supported-calendar-component-set";
    /** The CalDAV property name CALDAV:supported-collation-set */
    String PROPERTY_CALDAV_SUPPORTED_COLLATION_SET = "supported-collation-set";
    /** The CalDAV property name CALDAV:supported-calendar-data */
    String PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA = "supported-calendar-data";
    /** The CalDAV property name CALDAV:calendar-home-set */
    String PROPERTY_CALDAV_CALENDAR_HOME_SET = "calendar-home-set";
    /** The CalDAV XML element name <CALDAV:calendar-user-address-set> */
    String PROPERTY_CALDAV_CALENDAR_USER_ADDRESS_SET = "calendar-user-address-set";
    /** The CalDAV XML element name <CALDAV:schedule-inbox-URL> */
    String PROPERTY_CALDAV_SCHEDULE_INBOX_URL = "schedule-inbox-URL";
    /** The CalDAV XML element name <CALDAV:schedule-outbox-URL> */
    String PROPERTY_CALDAV_SCHEDULE_OUTBOX_URL = "schedule-outbox-URL";
    /** The CalDAV property name CALDAV:max-resource-size */
    String PROPERTY_CALDAV_MAX_RESOURCE_SIZE = "max-resource-size";

    /** The Calendar Server property name CS:getctag */
    String PROPERTY_CS_GET_CTAG = "getctag";

    /** The CalDAV property CALDAV:calendar-data */
    DavPropertyName CALENDARDATA = DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_DATA, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-description */
    DavPropertyName CALENDARDESCRIPTION = DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_DESCRIPTION, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-timezone */
    DavPropertyName CALENDARTIMEZONE = DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_TIMEZONE, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-calendar-component-set */
    DavPropertyName SUPPORTEDCALENDARCOMPONENTSET = DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET,
            NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-collation-component-set */
    DavPropertyName SUPPORTEDCOLLATIONSET = DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_COLLATION_SET, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-calendar-data */
    DavPropertyName SUPPORTEDCALENDARDATA = DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-home-set */
    DavPropertyName CALENDARHOMESET = DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_HOME_SET, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-user-address-set */
    DavPropertyName CALENDARUSERADDRESSSET = DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_USER_ADDRESS_SET, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:schedule-inbox-URL */
    DavPropertyName SCHEDULEINBOXURL = DavPropertyName.create(PROPERTY_CALDAV_SCHEDULE_INBOX_URL, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:schedule-outbox-URL */
    DavPropertyName SCHEDULEOUTBOXURL = DavPropertyName.create(PROPERTY_CALDAV_SCHEDULE_OUTBOX_URL, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:max-resource-size */
    DavPropertyName MAXRESOURCESIZE = DavPropertyName.create(PROPERTY_CALDAV_MAX_RESOURCE_SIZE, NAMESPACE_CALDAV);

    CosmoQName RESOURCE_TYPE_CALENDAR = new CosmoQName(NS_CALDAV, ELEMENT_CALDAV_CALENDAR, PRE_CALDAV);

    /** The Calendar Server property CS:getctag */
    DavPropertyName GET_CTAG = DavPropertyName.create(PROPERTY_CS_GET_CTAG, NAMESPACE_CS);
}
