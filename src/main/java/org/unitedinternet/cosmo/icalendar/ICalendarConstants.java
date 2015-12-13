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
package org.unitedinternet.cosmo.icalendar;


/**
 * Provides constants for values specified by iCalendar that are not
 * otherwise defined by iCal4J.
 */
public interface ICalendarConstants {

    /**
     * The highest version number of the iCalendar specification that
     * is implemented by Cosmo.
     */
    String ICALENDAR_VERSION = "2.0";

    /**
     * The MIME media type identifying a content item containing
     * data formatted with iCalendar.
     */
    String ICALENDAR_MEDIA_TYPE = "text/calendar";

    /**
     * The icalender parameter used on a DTSTART to indicate that the event
     * which contains the DTSTART is an "anytime" event. This is a OSAF custom 
     * parameter.
     */
    String PARAM_X_OSAF_ANYTIME = "X-OSAF-ANYTIME";

    /**
     * iCalendar value for "TRUE"
     */
    String VALUE_TRUE = "TRUE";

    /**
     * VAVAILABILITY component (not yet fully supported in ical4j)
     */
    String COMPONENT_VAVAILABLITY = "VAVAILABILITY";
}
