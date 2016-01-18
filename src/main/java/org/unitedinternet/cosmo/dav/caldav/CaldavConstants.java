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
    String PRE_CARD = "CARD";

    String NS_CALDAV = "urn:ietf:params:xml:ns:caldav";
    String NS_CARDDAV = "urn:ietf:params:xml:ns:carddav";

    Namespace NAMESPACE_CALDAV = Namespace.getNamespace(PRE_CALDAV, NS_CALDAV);
    Namespace NAMESPACE_CARDDAV = Namespace.getNamespace(PRE_CARD, NS_CARDDAV);

    /** The Calendar Server XML namespace */
    String PRE_CS = "CS";
    String NS_CS = "http://calendarserver.org/ns/";
    Namespace NAMESPACE_CS = Namespace.getNamespace(PRE_CS, NS_CS);

    /** The CalDAV XML element name <CALDAV:calendar> */
    String ELEMENT_CALDAV_CALENDAR = "calendar";
    /** The CalDAV XML element name <CALDAV:comp> */
    String ELEMENT_CALDAV_COMP = "comp";
    /** The CalDAV XML element name <CALDAV:supported-collation> */
    String ELEMENT_CALDAV_SUPPORTEDCOLLATION = "supported-collation";
    /** The CalDAV XML element name <CALDAV:calendar-data> */
    String ELEMENT_CALDAV_CALENDAR_DATA = "calendar-data";
    /** The CalDAV XML element name <CARDDAV:address-data-type> */
    String ELEMENT_CARDDAV_ADDRESS_DATA_TYPE = "address-data-type";
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
    /** The CalDAV XML element name <CARDDAV:filter> */
    String ELEMENT_CARDDAV_FILTER = "filter";
    /** The CalDAV XML element name <CALDAV:prop-filter> */
    String ELEMENT_CALDAV_PROP_FILTER = "prop-filter";
    /** The CalDAV XML element name <CALDAV:param-filter> */
    String ELEMENT_CALDAV_PARAM_FILTER = "param-filter";
    /** The CalDAV XML element name <CALDAV:time-range> */
    String ELEMENT_CALDAV_TIME_RANGE = "time-range";
    /** The CalDAV XML element name <CALDAV:is-not-defined> */
    String ELEMENT_CALDAV_IS_NOT_DEFINED = "is-not-defined";
    /** The (old) CalDAV XML element name <CALDAV:is-defined> */
    String ELEMENT_CALDAV_IS_DEFINED = "is-defined";
    /** The CalDAV XML element name <CALDAV:text-match> */
    String ELEMENT_CALDAV_TEXT_MATCH = "text-match";
    /** The CalDAV XML element name <CALDAV:calendar-multiget> */
    String ELEMENT_CALDAV_CALENDAR_MULTIGET = "calendar-multiget";
    /** The CalDAV XML element name <CARDDAV:addressbook-multiget> */
    String ELEMENT_CARDDAV_ADDRESSBOOK_MULTIGET = "addressbook-multiget";
    /** The CalDAV XML element name <CALDAV:calendar-query> */
    String ELEMENT_CALDAV_CALENDAR_QUERY = "calendar-query";
    /** The CalDAV XML element name <CARDDAV:addressbook-query> */
    String ELEMENT_CARDDAV_ADDRESSBOOK_QUERY = "addressbook-query";

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
    /** The CalDAV property name CALDAV:supported-calendar-component-set */
    String PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET = "supported-calendar-component-set";
    /** The CalDAV property name CALDAV:supported-collation-set */
    String PROPERTY_CALDAV_SUPPORTED_COLLATION_SET = "supported-collation-set";
    /** The CalDAV property name CALDAV:supported-calendar-data */
    String PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA = "supported-calendar-data";
    /** The CalDAV property name CARDDAV:supported-address-data */
    String PROPERTY_CARDDAV_SUPPORTED_ADDRESS_DATA = "supported-address-data";

    String PROPERTY_CARDDAV_ADDRESS_DATA = "address-data";
    /** The CardDAV property name CARDDAV:addressbook-home-set */
    String PROPERTY_CALDAV_ADDRESSBOOK_HOME_SET = "addressbook-home-set";
    /** The Calendar Server property name CS:getctag */
    String PROPERTY_CS_GET_CTAG = "getctag";

    /** The CalDAV property CALDAV:calendar-data */
    DavPropertyName CALENDARDATA = DavPropertyName.create(PROPERTY_CALDAV_CALENDAR_DATA, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:calendar-data */
    DavPropertyName ADDRESSDATA = DavPropertyName.create(PROPERTY_CARDDAV_ADDRESS_DATA, NAMESPACE_CARDDAV);

    /** The CalDAV property CALDAV:supported-calendar-component-set */
    DavPropertyName SUPPORTEDCALENDARCOMPONENTSET = DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET,
            NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-collation-component-set */
    DavPropertyName SUPPORTEDCOLLATIONSET = DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_COLLATION_SET, NAMESPACE_CALDAV);
    /** The CalDAV property CALDAV:supported-calendar-data */
    DavPropertyName SUPPORTEDCALENDARDATA = DavPropertyName.create(PROPERTY_CALDAV_SUPPORTED_CALENDAR_DATA, NAMESPACE_CALDAV);
    /** The CalDAV property CARDDAV:supported-address-data */
    DavPropertyName SUPPORTEDADDRESSDATA = DavPropertyName.create(PROPERTY_CARDDAV_SUPPORTED_ADDRESS_DATA, NAMESPACE_CARDDAV);
    /** The CardDAV property CARDDAV:addressbook-home-set */
    DavPropertyName ADDRESSBOOKHOMESET = DavPropertyName.create(PROPERTY_CALDAV_ADDRESSBOOK_HOME_SET, NAMESPACE_CARDDAV);

    CosmoQName RESOURCE_TYPE_CALENDAR = new CosmoQName(NS_CALDAV, ELEMENT_CALDAV_CALENDAR, PRE_CALDAV);

    /** The Calendar Server property CS:getctag */
    DavPropertyName GET_CTAG = DavPropertyName.create(PROPERTY_CS_GET_CTAG, NAMESPACE_CS);
}
