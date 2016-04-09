/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.caldav.property;

import static carldav.CarldavConstants.CALENDAR_DATA;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.xml.DomUtils;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CalendarData extends StandardDavProperty implements CaldavConstants, ICalendarConstants {

    public CalendarData(String calendarData) {
        super(CALENDAR_DATA, calendarData);
    }

    public Element toXml(Document document) {
        Element e = super.toXml(document);

        DomUtils.setAttribute(e, ATTR_CALDAV_CONTENT_TYPE, CarldavConstants.c(ATTR_CALDAV_CONTENT_TYPE), ICALENDAR_MEDIA_TYPE);
        DomUtils.setAttribute(e, ATTR_CALDAV_VERSION, CarldavConstants.c(ATTR_CALDAV_VERSION), ICALENDAR_VERSION);

        return e;
    }
}
