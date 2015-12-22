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
package org.unitedinternet.cosmo.calendar;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import org.unitedinternet.cosmo.CosmoConstants;

/**
 * Contains utility methods for creating/updating net.fortuna.ical4j
 * objects.
 */
public class ICalendarUtils {

    /**
     * Create a base Calendar containing a single component.
     *
     * @param comp    Component to add to the base Calendar
     * @param icalUid uid of component, if null no UID
     *                property will be added to the component
     * @return base Calendar
     */
    public static Calendar createBaseCalendar(CalendarComponent comp, String icalUid) {
        Uid uid = new Uid(icalUid);
        comp.getProperties().add(uid);
        return createBaseCalendar(comp);
    }

    /**
     * Create a base Calendar containing a single component.
     *
     * @param comp Component to add to the base Calendar
     * @return base Calendar
     */
    public static Calendar createBaseCalendar(CalendarComponent comp) {
        Calendar cal = createBaseCalendar();
        cal.getComponents().add(comp);
        return cal;
    }

    /**
     * Create a base Calendar containing no components.
     *
     * @return base Calendar
     */
    public static Calendar createBaseCalendar() {
        Calendar cal = new Calendar();
        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);

        return cal;
    }

    /**
     * Update the SUMMARY property on a component.
     *
     * @param text SUMMARY value to update.  If null, the SUMMARY property
     *             will be removed
     * @param comp component to update
     */
    public static void setSummary(String text, Component comp) {
        Summary summary = (Summary)
                comp.getProperties().getProperty(Property.SUMMARY);
        if (text == null) {
            if (summary != null) {
                comp.getProperties().remove(summary);
            }
            return;
        }
        if (summary == null) {
            summary = new Summary();
            comp.getProperties().add(summary);
        }
        summary.setValue(text);
    }

    /**
     * Update the X property on a component.
     *
     * @param property the property to update
     * @param value    the value to set
     * @param comp     component to update
     */
    public static void setXProperty(String property, String value, Component comp) {
        Property prop = comp.getProperties().getProperty(property);
        if (prop != null) {
            comp.getProperties().remove(prop);
        }

        if (value != null) {
            prop = new XProperty(property, value);
            comp.getProperties().add(prop);
        }
    }

    /**
     * Update the DESCRIPTION property on a component.
     *
     * @param text DESCRIPTION value to update.  If null, the DESCRIPTION property
     *             will be removed
     * @param comp component to update
     */
    public static void setDescription(String text, Component comp) {
        Description description = (Description)
                comp.getProperties().getProperty(Property.DESCRIPTION);

        if (text == null) {
            if (description != null) {
                comp.getProperties().remove(description);
            }
            return;
        }
        if (description == null) {
            description = new Description();
            comp.getProperties().add(description);
        }
        description.setValue(text);
    }

    /**
     * Update the LOCATION property on a component.
     *
     * @param text LOCATION value to update.  If null, the LOCATION property
     *             will be removed
     * @param comp component to update
     */
    public static void setLocation(String text, Component comp) {
        Location location = (Location)
                comp.getProperties().getProperty(Property.LOCATION);

        if (text == null) {
            if (location != null) {
                comp.getProperties().remove(location);
            }
            return;
        }
        if (location == null) {
            location = new Location();
            comp.getProperties().add(location);
        }
        location.setValue(text);
    }

    /**
     * Update the COMPLETED property on a VTODO component.
     *
     * @param date  completion date. If null, the COMPLETED property will be
     *              removed
     * @param vtodo vtodo component to update
     */
    public static void setCompleted(DateTime date, VToDo vtodo) {
        Completed completed = vtodo.getDateCompleted();
        if (completed != null) {
            vtodo.getProperties().remove(completed);
        }

        if (date != null) {
            completed = new Completed(date);
            vtodo.getProperties().add(completed);
        }

    }

    /**
     * Update the STATUS property on a VTODO component.
     *
     * @param status status to set. If null, the STATUS property will be removed
     * @param vtodo  vtodo component to update
     */
    public static void setStatus(Status status, VToDo vtodo) {
        Status currStatus = vtodo.getStatus();
        if (currStatus != null) {
            vtodo.getProperties().remove(currStatus);
        }

        if (status != null) {
            vtodo.getProperties().add(status);
        }
    }

    /**
     * Update the DTSTAMP property on a component.
     *
     * @param date DTSTAMP value to update.  If null, the DTSTAMP property
     *             will be removed
     * @param comp component to update
     */
    public static void setDtStamp(java.util.Date date, Component comp) {
        DtStamp dtStamp = (DtStamp)
                comp.getProperties().getProperty(Property.DTSTAMP);

        if (date == null) {
            if (dtStamp != null) {
                comp.getProperties().remove(dtStamp);
            }
            return;
        }
        if (dtStamp == null) {
            dtStamp = new DtStamp();
            comp.getProperties().add(dtStamp);
        }

        dtStamp.getDate().setTime(date.getTime());
    }

    /**
     * Update the UID property on a component.
     *
     * @param text UID value to update.  If null, the UID property
     *             will be removed
     * @param comp component to update
     */
    public static void setUid(String text, Component comp) {
        Uid uid = (Uid)
                comp.getProperties().getProperty(Property.UID);

        if (text == null) {
            if (uid != null) {
                comp.getProperties().remove(uid);
            }
            return;
        }
        if (uid == null) {
            uid = new Uid();
            comp.getProperties().add(uid);
        }
        uid.setValue(text);
    }

    /**
     * Get the duration for an event.  If the DURATION property
     * exist, use that.  Else, calculate duration from DTSTART and
     * DTEND.
     *
     * @param event The event.
     * @return duration for event
     */
    public static Dur getDuration(VEvent event) {
        Duration duration = (Duration)
                event.getProperties().getProperty(Property.DURATION);
        if (duration != null) {
            return duration.getDuration();
        }
        DtStart dtstart = event.getStartDate();
        if (dtstart == null) {
            return null;
        }
        DtEnd dtend = (DtEnd) event.getProperties().getProperty(Property.DTEND);
        if (dtend == null) {
            return null;
        }
        return new Duration(dtstart.getDate(), dtend.getDate()).getDuration();
    }

    /**
     * Set the duration for an event.  If DTEND is present, remove it.
     *
     * @param event The event.
     * @param dur   The duration.
     */
    public static void setDuration(VEvent event, Dur dur) {
        Duration duration = (Duration)
                event.getProperties().getProperty(Property.DURATION);


        // remove DURATION if dur is null
        if (dur == null) {
            if (duration != null) {
                event.getProperties().remove(duration);
            }
            return;
        }

        // update dur on existing DURATION
        if (duration != null) {
            duration.setDuration(dur);
        } else {
            // remove the dtend if there was one
            DtEnd dtend = event.getEndDate();
            if (dtend != null) {
                event.getProperties().remove(dtend);
            }
            duration = new Duration(dur);
            event.getProperties().add(duration);
        }
    }
}
