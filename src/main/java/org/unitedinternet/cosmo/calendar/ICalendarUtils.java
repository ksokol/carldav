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

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Trigger;
import org.unitedinternet.cosmo.CosmoParseException;
import org.unitedinternet.cosmo.calendar.util.Dates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains utility methods for creating/updating net.fortuna.ical4j
 * objects.
 */
public class ICalendarUtils {

    /**
     * Get the duration for an event.  If the DURATION property
     * exist, use that.  Else, calculate duration from DTSTART and
     * DTEND.
     * @param event The event.
     * @return duration for event
     */
    public static Dur getDuration(Component event) {
        Duration duration = (Duration)
            event.getProperties().getProperty(Property.DURATION);
        if (duration != null) {
            return duration.getDuration();
        }
        DtStart dtstart = getStartDate(event);
        if (dtstart == null) {
            return null;
        }
        DtEnd dtend = (DtEnd) event.getProperties().getProperty(Property.DTEND);
        if (dtend == null) {
            return null;
        }
        return new Duration(dtstart.getDate(), dtend.getDate()).getDuration();
    }

    public static DtStart getStartDate(Component component) {
        if(component==null) {
            return null;
        }

        final Property startDate = component.getProperty(Property.DTSTART);
        if(startDate != null) {
            return ((DtStart) startDate);

        }
        return null;
    }

    /**
     * Construct a new DateTime instance for floating times (no timezone).
     * If the specified date is not floating, then the instance is returned. 
     * 
     * This allows a floating time to be converted to an instant in time
     * depending on the specified timezone.
     * 
     * @param date floating date
     * @param tz timezone
     * @return new DateTime instance representing floating time pinned to
     *         the specified timezone
     */
    public static DateTime pinFloatingTime(Date date, TimeZone tz) {
        
        try {   
            if(date instanceof DateTime) {
                DateTime dt = (DateTime) date;
                if(dt.isUtc() || dt.getTimeZone()!=null) {
                    return dt;
                }
                else {
                    return new DateTime(date.toString(), tz);
                }
            }
            else {
                return new DateTime(date.toString() + "T000000", tz);
            }
        } catch (ParseException e) {
            throw new CosmoParseException("error parsing date", e);
        }
    }
    
    /**
     * Return a Date instance that represents the day that a point in
     * time translates into local time given a timezone.
     * @param utcDateTime point in time
     * @param tz timezone The timezone.
     * @return The date.
     */
    public static Date normalizeUTCDateTimeToDate(DateTime utcDateTime, TimeZone tz) {
        if(!utcDateTime.isUtc()) {
            throw new IllegalArgumentException("datetime must be utc");
        }
        
        // if no timezone, use default
        if (tz == null) {
            return new Date(utcDateTime);
        }
        
        DateTime copy = (DateTime) Dates.getInstance(utcDateTime, utcDateTime);
        copy.setTimeZone(tz);
        
        try {
            return new Date(copy.toString().substring(0, 8));
        } catch (ParseException e) {
            throw new CosmoParseException("error creating Date instance", e);
        }
    }
    
    /**
     * Return a DateTime instance that is normalized according to the
     * offset of the specified timezone as compared to the default
     * system timezone.
     * 
     * @param utcDateTime point in time
     * @param tz timezone The timezone.
     * @return The date.
     */
    public static Date normalizeUTCDateTimeToDefaultOffset(DateTime utcDateTime, TimeZone tz) {
        if(!utcDateTime.isUtc()) {
            throw new IllegalArgumentException("datetime must be utc");
        }
        
        // if no timezone nothing to do
        if (tz == null) {
            return utcDateTime;
        }
        
        // create copy, and set timezone
        DateTime copy = (DateTime) Dates.getInstance(utcDateTime, utcDateTime);
        copy.setTimeZone(tz);
        
        
        // Create floating instance of local time, which will give
        // us the correct offset
        try {
            return new DateTime(copy.toString());
        } catch (ParseException e) {
            throw new CosmoParseException("error creating Date instance", e);
        }
    }
    
    /**
     * Convert a Date instance to a utc DateTime instance for a
     * specified timezone.
     * @param date date to convert
     * @param tz timezone
     * @return UTC DateTime instance
     */
    public static DateTime convertToUTC(Date date, TimeZone tz) {
        
        // handle DateTime
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            
            // if utc already, then nothing to do
            if(dt.isUtc()) {
                return dt;
            }
            
            // if DateTime has timezone, then create copy, set to utc
            if(dt.getTimeZone()!=null) {
                dt = (DateTime) Dates.getInstance(date, date);
                dt.setUtc(true);
                return dt;
            }
            
            // otherwise DateTime is floating
            
            // If timezone specified, use it to pin the floating DateTime
            // to an instant
            if(tz!=null) {
                dt = pinFloatingTime(date, tz);
                dt.setUtc(true);
                return dt;
            }
            
            // Otherwise use default timezone for utc instant
            dt = (DateTime) Dates.getInstance(date, date);
            dt.setUtc(true);
            return dt;
        }
        
        
        // handle Date instances
        
        // If timezone specified, use it to pin the floating Date
        // to an instant
        if(tz!=null) {
            DateTime dt = pinFloatingTime(date, tz);
            dt.setUtc(true);
            return dt;
        }
        
        // Otherwise use default timezone for utc instant
        DateTime dt = new DateTime(date);
        dt.setUtc(true);
        return dt;
    }
    
    /**
     * Compare Date instances using a timezone to pin floating Date and 
     * DateTimes.
     * @param date1 The date.
     * @param date2 The date.
     * @param tz timezone to use when interpreting floating Date and DateTime
     * @return The result.
     */
    public static int compareDates(Date date1, Date date2, TimeZone tz) {
       
        if(tz!=null) {
            if(isFloating(date1)) {
                date1 = pinFloatingTime(date1, tz);
            }
            if(isFloating(date2)) {
                date2 = pinFloatingTime(date2, tz);
            }
        }
        
        return date1.compareTo(date2);
    }
    
    /**
     * Determine if a Date is floating.  A floating Date is a Date
     * instance or a DateTime that is not utc and does not have a timezone.
     * @param date The date.
     * @return true if the date is floating, otherwise false
     */
    public static boolean isFloating(Date date) {
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            return !dt.isUtc() && dt.getTimeZone()==null;
        } else {
            return true;
        }
    }
    
    /**
     * 
     * @param test The date.
     * @param date The date.
     * @param tz The timezone.
     * @return The result.
     */
    public static boolean beforeDate(Date test, Date date, TimeZone tz) {
        return compareDates(test, date, tz) < 0;
    }
    
    /**
     * 
     * @param test The date.
     * @param date The date.
     * @param tz The timezone.
     * @return The result.
     */
    public static boolean afterDate(Date test, Date date, TimeZone tz) {
        return compareDates(test, date, tz) > 0;
    }
    
    /**
     * 
     * @param test The date.
     * @param date The date.
     * @param tz The timezone.
     * @return The result.
     */
    public static boolean equalsDate(Date test, Date date, TimeZone tz) {
        return compareDates(test, date, tz)==0;
    }
    
    /**
     * Return list of subcomponents for a component.  Ica4j doesn't have
     * a generic way to do this.
     * @param component The component.
     * @return list of subcomponents
     */
    public static ComponentList getSubComponents(Component component) {
        if(component instanceof VEvent) {
            return ((VEvent) component).getAlarms();
        }
        else if(component instanceof VTimeZone) {
            return ((VTimeZone) component).getObservances();
        }
        else if(component instanceof VToDo) {
            return ((VToDo) component).getAlarms();
        }
        
        return new ComponentList();
    }
    
    /**
     * Return the list of dates that an alarm will trigger.
     * @param alarm alarm component
     * @param parent parent compoennt (VEvent,VToDo)
     * @return dates that alarm is configured to trigger
     */
    public static List<Date> getTriggerDates(VAlarm alarm, Component parent) {
        ArrayList<Date> dates = new ArrayList<Date>();
        Trigger trigger = alarm.getTrigger();
        if(trigger==null) {
            return dates;
        }
        
        Date initialTriggerDate = getTriggerDate(trigger, parent);
        if(initialTriggerDate==null) {
            return dates;
        }
        
        dates.add(initialTriggerDate);
        
        Duration dur = alarm.getDuration();
        if(dur==null) {
            return dates;
        }
        Repeat repeat = alarm.getRepeat(); 
        if(repeat==null) {
            return dates;
        }
        
        Date nextTriggerDate = initialTriggerDate;
        for(int i=0;i<repeat.getCount();i++) {
            nextTriggerDate = Dates.getInstance(dur.getDuration().getTime(nextTriggerDate), nextTriggerDate);
            dates.add(nextTriggerDate);
        }
        
        return dates;
    }
    
    /**
     * Return the date that a trigger refers to, which can be an absolute
     * date or a date relative to the start or end time of a parent 
     * component (VEVENT/VTODO).
     * @param trigger The trigger.
     * @param parent The component.
     * @return date of trigger.
     */
    public static Date getTriggerDate(Trigger trigger, Component parent) {
        
        if(trigger==null) {
            return null;
        }
        
        // if its absolute then we are done
        if(trigger.getDateTime()!=null) {
            return trigger.getDateTime();
        }
        
        // otherwise we need a start date if VEVENT
        DtStart start = (DtStart) parent.getProperty(Property.DTSTART);
        if(start==null && parent instanceof VEvent) {
            return null;
        }
        
        // is trigger relative to start or end
        Related related = (Related) trigger.getParameter(Parameter.RELATED);
        if(related==null || related.equals(Related.START)) {    
            // must have start date
            if(start==null) {
                return null;
            }
            
            // relative to start
            return Dates.getInstance(trigger.getDuration().getTime(start.getDate()), start.getDate());
        } else {
            // relative to end
            Date endDate = null;
            
            // need an end date or duration or due 
            DtEnd end = (DtEnd) parent.getProperty(Property.DTEND);
            if(end!=null) {
                endDate = end.getDate();
            }
           
            if(endDate==null) {
                Duration dur = (Duration) parent.getProperty(Property.DURATION);
                if(dur!=null && start!=null) {
                    endDate= Dates.getInstance(dur.getDuration().getTime(start.getDate()), start.getDate());
                }
            }
            
            if(endDate==null) {
                Due due = (Due) parent.getProperty(Property.DUE);
                if(due!=null) {
                    endDate = due.getDate();
                }
            }
            
            // require end date
            if(endDate==null) {
                return null;
            }
            
            return Dates.getInstance(trigger.getDuration().getTime(endDate), endDate);
        }
    }
    
    /**
     * Find and return the first DISPLAY VALARM in a comoponent
     * @param component VEVENT or VTODO
     * @return first DISPLAY VALARM, null if there is none
     */
    public static VAlarm getDisplayAlarm(Component component) {
        ComponentList alarms = null;
        
        if(component instanceof VEvent) {
            alarms = ((VEvent) component).getAlarms();
        }
        else if(component instanceof VToDo) {
            alarms = ((VToDo) component).getAlarms();
        }
        
        if(alarms==null || alarms.size()==0) {
            return null;
        }
        
        for(Iterator<VAlarm> it = alarms.iterator();it.hasNext();) {
            VAlarm alarm = it.next();
            if(Action.DISPLAY.equals(alarm.getAction())) {
                return alarm;
            }
        }
        
        return null;   
    }
}
