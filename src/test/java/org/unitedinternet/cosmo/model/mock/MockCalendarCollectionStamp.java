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
package org.unitedinternet.cosmo.model.mock;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.unitedinternet.cosmo.hibernate.validator.Timezone;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.hibernate.HibQName;


/**
 * Represents a Calendar Collection.
 */
public class MockCalendarCollectionStamp extends MockStamp implements
        java.io.Serializable, ICalendarConstants, CalendarCollectionStamp {
    
    // CalendarCollection specific attributes
    public static final QName ATTR_CALENDAR_TIMEZONE = new MockQName(
            CalendarCollectionStamp.class, "timezone");

    /** default constructor */
    public MockCalendarCollectionStamp() {
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getType()
     */
    /**
     * Gets type.
     * @return The type.
     */
    public String getType() {
        return "calendar";
    }
    
    /**
     * Constructor.
     * @param collection The collection item.
     */
    public MockCalendarCollectionStamp(CollectionItem collection) {
        this();
        setItem(collection);
    }

    /**
     * Copy.
     * {@inheritDoc}
     * @return stamp
     */
    public Stamp copy() {
        CalendarCollectionStamp stamp = new MockCalendarCollectionStamp();
        return stamp;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezoneCalendar()
     */
    /**
     * Gets timezone.
     * @return calendar.
     */
    @Timezone
    public Calendar getTimezoneCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return MockICalendarAttribute.getValue(getItem(), ATTR_CALENDAR_TIMEZONE);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezone()
     */
    /**
     * Gets timezone.
     * @return timezone.
     */
    public TimeZone getTimezone() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null) {
            return null;
        }
        VTimeZone vtz = (VTimeZone) timezone.getComponents().getComponent(Component.VTIMEZONE);
        return new TimeZone(vtz);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarCollectionStamp#setTimezoneCalendar(net.fortuna.ical4j.model.Calendar)
     */
    /**
     * Sets timezone calendar.
     * @param timezone The timezone.
     */
    public void setTimezoneCalendar(Calendar timezone) {
        // timezone stored as ICalendarAttribute on Item
        MockICalendarAttribute.setValue(getItem(), ATTR_CALENDAR_TIMEZONE, timezone);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
