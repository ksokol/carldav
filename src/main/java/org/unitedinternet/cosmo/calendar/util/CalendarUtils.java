/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.calendar.util;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility methods for working with icalendar data.
 */
public class CalendarUtils implements ICalendarConstants {
    
    private static String[] SUPPORTED_COMPONENT_TYPES = { Component.VEVENT,
        Component.VTODO, Component.VJOURNAL};
    
    private static String[] SUPPORTED_COLLATIONS = {
        "i;ascii-casemap", "i;octet"
    };

    /**
     * Parse icalendar data from InputStream
     * @param is icalendar data inputstream
     * @return Calendar object
     * @throws ParserException - if something is wrong this exception is thrown.
     * @throws IOException - if something is wrong this exception is thrown.
     */
    public static Calendar parseCalendar(InputStream is) throws ParserException, IOException {
        CalendarBuilder builder = new CalendarBuilder();
        clearTZRegistry(builder);
        return builder.build(is);
    }

    public static boolean isSupportedComponent(String type) {
        for (String s : SUPPORTED_COMPONENT_TYPES) {
            if (s.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isSupportedCollation(String collation) {
        for (String s : SUPPORTED_COLLATIONS) {
            if (s.equalsIgnoreCase(collation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasMultipleComponentTypes(Calendar calendar) {
        String found = null;
        for (Object component: calendar.getComponents()) {
            if (component instanceof VTimeZone) {
                continue;
            }
            if (found == null) {
                found = ((CalendarComponent)component).getName();
                continue;
            }
            if (! found.equals(((CalendarComponent)component).getName())) {
                return true;
            }
        }
        return false;
    }

    private static void clearTZRegistry(CalendarBuilder cb) {
        // clear timezone registry if present
        TimeZoneRegistry tzr = cb.getRegistry();
        if(tzr!=null) {
            tzr.clear();
        }
    }
}
