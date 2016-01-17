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
package org.unitedinternet.cosmo.model.hibernate;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.unitedinternet.cosmo.hibernate.validator.DisplayName;
import org.unitedinternet.cosmo.hibernate.validator.Timezone;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.Item;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("calendar")
public class HibCalendarCollectionStamp extends HibStamp implements ICalendarConstants {
    
    private static final long serialVersionUID = 1L;

    public static final HibQName ATTR_CALENDAR_TIMEZONE = new HibQName(HibCalendarCollectionStamp.class, "timezone");
    public static final HibQName ATTR_CALENDAR_DESCRIPTION = new HibQName(HibCalendarCollectionStamp.class, "description");
    public static final HibQName ATTR_CALENDAR_LANGUAGE = new HibQName(HibCalendarCollectionStamp.class, "language");

    public HibCalendarCollectionStamp() {
    }

    public String getType() {
        return "calendar";
    }
    
    public HibCalendarCollectionStamp(HibCollectionItem collection) {
        this();
        setItem(collection);
    }

    public String getDescription() {
        return HibStringAttribute.getValue(getItem(), ATTR_CALENDAR_DESCRIPTION);
    }

    public void setDescription(String description) {
        HibStringAttribute.setValue(getItem(), ATTR_CALENDAR_DESCRIPTION, description);
    }

    public String getLanguage() {
        return HibStringAttribute.getValue(getItem(), ATTR_CALENDAR_LANGUAGE);
    }

    public void setLanguage(String language) {
        HibStringAttribute.setValue(getItem(), ATTR_CALENDAR_LANGUAGE, language);
    }

    @Timezone
    public Calendar getTimezoneCalendar() {
        return HibICalendarAttribute.getValue(getItem(), ATTR_CALENDAR_TIMEZONE);
    }

    public TimeZone getTimezone() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null) {
            return null;
        }
        VTimeZone vtz = (VTimeZone) timezone.getComponents().getComponent(Component.VTIMEZONE);
        return new TimeZone(vtz);
    }

    public void setTimezoneCalendar(Calendar timezone) {
        HibICalendarAttribute.setValue(getItem(), ATTR_CALENDAR_TIMEZONE, timezone);
    }

    /**
     * Return CalendarCollectionStamp from Item
     * @param item
     * @return CalendarCollectionStamp from Item
     */
    public static HibCalendarCollectionStamp getStamp(Item item) {
        return (HibCalendarCollectionStamp) item.getStamp(HibCalendarCollectionStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }

    @DisplayName
    public String getDisplayName() {
        return getItem().getDisplayName();
    }

    public void setDisplayName(String displayName) {
        getItem().setDisplayName(displayName);
    }
}
