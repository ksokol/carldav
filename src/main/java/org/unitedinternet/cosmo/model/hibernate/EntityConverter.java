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

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Trigger;
import org.apache.commons.lang.StringUtils;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.model.TriageStatusUtil;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EntityConverter {

    public Set<HibICalendarItem> convertEventCalendar(HibICalendarItem note, String calendarString) {
        try {
            final Calendar calendar = new CalendarBuilder().build(new StringReader(calendarString));
            final VEvent event = (VEvent) calendar.getComponent("VEVENT");

            setCalendarAttributes(note, event);
            calculateEventStampIndexes(calendar, event, note);

            return Collections.singleton(note);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    public HibICalendarItem convertJournalCalendar(HibJournalItem  note, String calendarString) {
        try {
            final Calendar calendar = new CalendarBuilder().build(new StringReader(calendarString));
            VJournal vj = (VJournal) getMasterComponent(calendar.getComponents(Component.VJOURNAL));
            setCalendarAttributes(note, vj);
            return note;
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * Convert calendar containing single VTODO into NoteItem
     * 
     * @param note
     *            note to update
     * @param calendar
     *            calendar containing VTODO
     * @return NoteItem representation of VTODO
     */
    public HibICalendarItem convertTaskCalendar(HibICalendarItem  note, Calendar calendar) {
        
        note.setCalendar(calendar.toString());
        VToDo todo = (VToDo) getMasterComponent(calendar.getComponents(Component.VTODO));
        
        setCalendarAttributes(note, todo);
        
        return note;
    }

    public Calendar convertContent(HibICalendarItem item) {
        if(item.getCalendar() != null) {
            try {
                new CalendarBuilder().build(new StringReader(item.getCalendar()));
            } catch (Exception exception) {
                //TODO
                exception.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Sets calendar attributes.
     * @param note The note item.
     * @param task The task vToDo.
     */
    private void setCalendarAttributes(HibICalendarItem note, VToDo task) {

        if(task.getStartDate() != null) {
            final DtStart startDate = task.getStartDate();
            final DateTime dateTime = new DateTime(startDate.getDate());

            note.setStartDate(dateTime);
            note.setEndDate(dateTime);
        }

        // UID
        if(task.getUid()!=null) {
            note.setIcalUid(task.getUid().getValue());
        }
        
        // for now displayName is limited to 1024 chars
        if (task.getSummary() != null) {
            note.setDisplayName(StringUtils.substring(task.getSummary()
                    .getValue(), 0, 1024));
        }

        // look for DTSTAMP
        if (task.getDateStamp() != null) {
            note.setClientModifiedDate(task.getDateStamp().getDate());
        }

        // look for absolute VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(task);
        if (va != null && va.getTrigger()!=null) {
            Trigger trigger = va.getTrigger();
            Date reminderTime = trigger.getDateTime();
           if (reminderTime != null) {
                note.setRemindertime(reminderTime);
           }
        }
        
        // look for COMPLETED or STATUS:COMPLETED
        Completed completed = task.getDateCompleted();
        Status status = task.getStatus();
        TriageStatus ts = note.getTriageStatus();
        
        // Initialize TriageStatus if necessary
        if(completed!=null || Status.VTODO_COMPLETED.equals(status)) {
            if (ts == null) {
                ts = TriageStatusUtil.initialize(new TriageStatus());
                note.setTriageStatus(ts);
            }
            
            // TriageStatus.code will be DONE
            note.getTriageStatus().setCode(TriageStatusUtil.CODE_DONE);
            
            // TriageStatus.rank will be the COMPLETED date if present
            // or currentTime
            if(completed!=null) {
                note.getTriageStatus().setRank(
                        TriageStatusUtil.getRank(completed.getDate().getTime()));
            }
            else {
                note.getTriageStatus().setRank(
                        TriageStatusUtil.getRank(System.currentTimeMillis()));
            }
        }
    }

    private void setCalendarAttributes(HibICalendarItem note, Component component) {
        final Property uid = component.getProperty(Property.UID);
        if(uid != null) {
            note.setIcalUid(uid.getValue());
            note.setUid(uid.getValue());
        }

        final Property summary = component.getProperty(Property.SUMMARY);
        if(summary != null) {
            note.setDisplayName(summary.getValue());
        }

        final Property dtStamp = component.getProperty(Property.DTSTAMP);
        if(dtStamp != null) {
            note.setClientModifiedDate((((DtStamp) dtStamp).getDate()));
        }

        final Property startDate = component.getProperty(Property.DTSTART);
        if(startDate != null) {
            final Date dtStart = ((DtStart) startDate).getDate();
            final DateTime dateTime = new DateTime(dtStart);

            note.setStartDate(dateTime);
            note.setEndDate(dateTime);

        }
    }

    /**
     * gets master component.
     * @param components The component list.
     * @return The component.
     */
    private Component getMasterComponent(ComponentList components) {
        Iterator<Component> it = components.iterator();
        while(it.hasNext()) {
            Component c = it.next();
            if(c.getProperty(Property.RECURRENCE_ID)==null) {
                return c;
            }
        }
        
        throw new IllegalArgumentException("no master found");
    }


    private Date getStartDate(VEvent event) {
        if(event==null) {
            return null;
        }

        DtStart dtStart = event.getStartDate();
        if (dtStart == null) {
            return null;
        }
        return dtStart.getDate();
    }

    private Date getEndDate(VEvent event) {
        if(event==null) {
            return null;
        }
        DtEnd dtEnd = event.getEndDate(false);
        // if no DTEND, then calculate endDate from DURATION
        if (dtEnd == null) {
            Date startDate = getStartDate(event);
            Dur duration = ICalendarUtils.getDuration(event);

            // if no DURATION, then there is no end time
            if(duration==null) {
                return null;
            }

            Date endDate = null;
            if(startDate instanceof DateTime) {
                endDate = new DateTime(startDate);
            }
            else {
                endDate = new Date(startDate);
            }

            endDate.setTime(duration.getTime(startDate).getTime());
            return endDate;
        }

        return dtEnd.getDate();
    }

    private boolean isRecurring(VEvent event) {
        if(getRecurrenceRules(event).size()>0) {
            return true;
        }

        DateList rdates = getRecurrenceDates(event);

        return rdates!=null && rdates.size()>0;
    }

    private List<Recur> getRecurrenceRules(VEvent event) {
        ArrayList<Recur> l = new ArrayList<Recur>();
        if(event!=null) {
            for (Object rrule : event.getProperties().getProperties(Property.RRULE)) {
                l.add(((RRule)rrule).getRecur());
            }
        }
        return l;
    }

    private DateList getRecurrenceDates(VEvent event) {
        DateList l = null;

        if(event==null) {
            return null;
        }

        for (Object property : event.getProperties().getProperties(Property.RDATE)) {
            RDate rdate = (RDate) property;
            if(l==null) {
                if(Value.DATE.equals(rdate.getParameter(Parameter.VALUE))) {
                    l = new DateList(Value.DATE);
                }
                else {
                    l = new DateList(Value.DATE_TIME, rdate.getDates().getTimeZone());
                }
            }
            l.addAll(rdate.getDates());
        }

        return l;
    }

    public void calculateEventStampIndexes(Calendar calendar, VEvent event, HibICalendarItem note) {
        Date startDate = getStartDate(event);
        Date endDate = getEndDate(event);

        boolean isRecurring = false;

        if (isRecurring(event)) {
            isRecurring = true;
            RecurrenceExpander expander = new RecurrenceExpander();
            Date[] range = expander.calculateRecurrenceRange(calendar);
            startDate = range[0];
            endDate = range[1];
        } else {
            // If there is no end date, then its a point-in-time event
            if (endDate == null) {
                endDate = startDate;
            }
        }

        boolean isFloating = false;

        // must have start date
        if(startDate==null) {
            return;
        }

        // A floating date is a DateTime with no timezone, or
        // a Date
        if(startDate instanceof DateTime) {
            DateTime dtStart = (DateTime) startDate;
            if(dtStart.getTimeZone()==null && !dtStart.isUtc()) {
                isFloating = true;
            }
        } else {
            // Date instances are really floating because you can't pin
            // the a date like 20070101 to an instant without first
            // knowing the timezone
            isFloating = true;
        }

        final DateTime startDateTime = new DateTime(startDate);
        note.setStartDate(startDateTime);

        // A null endDate equates to infinity, which is represented by
        // a String that will always come after any date when compared.
        if(endDate!=null) {
            final DateTime endDateTime = new DateTime(endDate);
            note.setEndDate(endDateTime);
        }

        note.setFloating(isFloating);
        note.setRecurring(isRecurring);
    }
}
