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

/**
 * Provides constants for media types, XML namespaces, names and values, DAV properties and resource types defined by the CalDAV spec.
 */
public interface CaldavConstants {

    String VALUE_YES = "yes";
    String VALUE_NO = "no";
    /** The iCalendar media type */
    String CT_ICALENDAR = "text/calendar";
    /** The CalDAV XML namespace */
    String PRE_CALDAV = "C";
    String PRE_CARD = "CARD";

    String NS_CALDAV = "urn:ietf:params:xml:ns:caldav";
    String NS_CARDDAV = "urn:ietf:params:xml:ns:carddav";

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

    String CONTACTS = "contacts";
    String CALENDAR = "calendar";
    String HOME_COLLECTION = "homeCollection";
}
