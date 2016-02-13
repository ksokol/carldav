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

import carldav.service.generator.IdGenerator;
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
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Trigger;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;
import org.unitedinternet.cosmo.model.TriageStatusUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A component that converts iCalendar objects to entities and vice versa.
 * Often this is not a straight one-to-one mapping, because recurring
 * iCalendar events are modeled as multiple events in a single
 * {@link Calendar}, whereas recurring items are modeled as a master
 * {@link HibNoteItem} with zero or more {@link HibNoteItem} modifications
 */
public class EntityConverter {

    private static final String TIME_INFINITY = "Z-TIME-INFINITY";

    private final IdGenerator idGenerator;

    public EntityConverter(IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "idGenerator is null");
        this.idGenerator = idGenerator;
    }

    /**
     * Expands an event calendar and returns a set of notes representing the
     * master and exception items.
     * <p>
     * The provided note corresponds to the recurrence master or, for
     * non-recurring items, the single event in the calendar. The result set
     * includes both the master note as well as a modification note for
     * exception event in the calendar.
     * </p>
     * <p>
     * If the master note does not already have a UUID or an event stamp, one
     * is assigned to it. A UUID is assigned because any modification items
     * that are created require the master's UUID in order to construct
     * their own.
     * </p>
     * <p>
     * If the given note is already persistent, and the calendar does not
     * contain an exception event matching an existing modification, that
     * modification is set inactive. It is still returned in the result set.
     * </p>
     * @param note The note item.
     * @param calendar The calendar.
     * @return set note item.
     */
    public Set<HibNoteItem> convertEventCalendar(HibNoteItem note, Calendar calendar) {
        final VEvent event = (VEvent) calendar.getComponent("VEVENT");

        if(event.getUid()!=null) {
            note.setIcalUid(event.getUid().getValue());
        }

        if (event.getSummary() != null) {
            note.setDisplayName(event.getSummary().getValue());
        }

        final HibBaseEventStamp stamp = note.getStamp();
        final HibEventTimeRangeIndex hibEventTimeRangeIndex = calculateEventStampIndexes(calendar, event, stamp);

        stamp.setTimeRangeIndex(hibEventTimeRangeIndex);

        return Collections.singleton(note);
    }

    /**
     * Update existing NoteItem with calendar containing single VJOURNAL
     * @param note note to update
     * @param calendar calendar containing VJOURNAL
     * @return NoteItem representation of VJOURNAL
     */
    public HibJournalItem convertJournalCalendar(HibJournalItem  note, Calendar calendar) {
        
        VJournal vj = (VJournal) getMasterComponent(calendar.getComponents(Component.VJOURNAL));
        setCalendarAttributes(note, vj);
        return note;
    }

    /**
     * Convert calendar containing single VTODO into NoteItem
     *
     * @param calendar
     *            calendar containing VTODO
     * @return NoteItem representation of VTODO
     */
    public HibNoteItem convertTaskCalendar(Calendar calendar) {
        HibNoteItem note = new HibNoteItem();
        note.setUid(idGenerator.nextStringIdentifier());
        setBaseContentAttributes(note);
        return convertTaskCalendar(note, calendar);
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
    public HibNoteItem convertTaskCalendar(HibNoteItem  note, Calendar calendar) {
        
        note.setTaskCalendar(calendar);
        VToDo todo = (VToDo) getMasterComponent(calendar.getComponents(Component.VTODO));
        
        setCalendarAttributes(note, todo);
        
        return note;
    }

    /**
     * Returns a calendar representing the item.
     * <p>
     * If the item is a {@link HibNoteItem}, delegates to
     * {@link #convertNote(HibNoteItem)}. If the item is a {@link HibICalendarItem},
     * delegates to {@link HibICalendarItem#getCalendar()}. Otherwise,
     * returns null.
     * @param item The content item.
     * @return The calendar.
     * </p>
     */
    public Calendar convertContent(HibItem item) {
        if(item instanceof HibNoteItem) {
            return convertNote((HibNoteItem) item);
        }
        return null;
    }

    public Calendar convertNote(HibNoteItem note) {
        if (note.isEvent()) {
            return note.getStampCalendar();
        }
        return note.getCalendar();
    }

    /**
     * Sets base content attributes.
     * @param item The content item.
     */
    private void setBaseContentAttributes(HibItem item) {

        TriageStatus ts = new TriageStatus();
        TriageStatusUtil.initialize(ts);

        item.setClientCreationDate(new Date());
        item.setClientModifiedDate(item.getClientCreationDate());

        if(item instanceof HibNoteItem) {
            ((HibNoteItem)item).setTriageStatus(ts);
        }
    }

    /**
     * Sets calendar attributes.
     * @param note The note item.
     * @param task The task vToDo.
     */
    private void setCalendarAttributes(HibNoteItem note, VToDo task) {
        
        // UID
        if(task.getUid()!=null) {
            note.setIcalUid(task.getUid().getValue());
        }
        
        // for now displayName is limited to 1024 chars
        if (task.getSummary() != null) {
            note.setDisplayName(StringUtils.substring(task.getSummary()
                    .getValue(), 0, 1024));
        }

        if (task.getDescription() != null) {
            note.setBody(task.getDescription().getValue());
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
    
    /**
     * Sets calendar attributes.
     * @param note The note item.
     * @param journal The VJournal.
     */
    private void setCalendarAttributes(HibJournalItem note, VJournal journal) {
        note.setIcalUid(journal.getUid().getValue());
        note.setUid(journal.getUid().getValue());

        if (journal.getSummary() != null) {
            note.setDisplayName(journal.getSummary().getValue());
        }

        if (journal.getDateStamp() != null) {
            note.setClientModifiedDate(journal.getDateStamp().getDate());
        }

        final DtStart startDate = journal.getStartDate();
        final DateTime dateTime = new DateTime(startDate.getDate());

        note.setStartDate(dateTime);
        note.setEndDate(dateTime);
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

    public HibEventTimeRangeIndex calculateEventStampIndexes(Calendar calendar, VEvent event, HibBaseEventStamp stamp) {
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
            return null;
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

        HibEventTimeRangeIndex timeRangeIndex = new HibEventTimeRangeIndex();
        timeRangeIndex.setStartDate(fromDateToStringNoTimezone(startDate));
        stamp.setStartDate(fromDateToStringNoTimezone(startDate));


        // A null endDate equates to infinity, which is represented by
        // a String that will always come after any date when compared.
        if(endDate!=null) {
            timeRangeIndex.setEndDate(fromDateToStringNoTimezone(endDate));
            stamp.setEndDate(fromDateToStringNoTimezone(endDate));
        }
        else {
            timeRangeIndex.setEndDate(TIME_INFINITY);
            stamp.setEndDate(TIME_INFINITY);
        }

        timeRangeIndex.setIsFloating(isFloating);
        stamp.setIsFloating(isFloating);

        timeRangeIndex.setIsRecurring(isRecurring);
        stamp.setIsRecurring(isRecurring);

        return timeRangeIndex;
    }

    private String fromDateToStringNoTimezone(Date date) {
        if(date==null) {
            return null;
        }

        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            // If DateTime has a timezone, then convert to UTC before
            // serializing as String.
            if(dt.getTimeZone()!=null) {
                // clone instance first to prevent changes to original instance
                DateTime copy = new DateTime(dt);
                copy.setUtc(true);
                return copy.toString();
            } else {
                return dt.toString();
            }
        } else {
            return date.toString();
        }
    }
}
