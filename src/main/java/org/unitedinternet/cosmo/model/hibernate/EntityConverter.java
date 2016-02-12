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
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Trigger;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.model.TriageStatusUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * A component that converts iCalendar objects to entities and vice versa.
 * Often this is not a straight one-to-one mapping, because recurring
 * iCalendar events are modeled as multiple events in a single
 * {@link Calendar}, whereas recurring items are modeled as a master
 * {@link HibNoteItem} with zero or more {@link HibNoteItem} modifications
 */
public class EntityConverter { 

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

    /**
     * Returns a calendar representing the note.
     * <p>
     * If the note is a modification, returns null. If the note has an event
     * stamp, returns a calendar containing the event and any exceptions. If
     * the note has a task stamp, returns a calendar containing the task.
     * Otherwise, returns a calendar containing a journal.
     * </p>
     * @param note The note item.
     * @return calendar The calendar.
     */
    public Calendar convertNote(HibNoteItem note) {
        HibBaseEventStamp event = note.getStamp(HibBaseEventStamp.class);
        if (event!=null) {
            return note.getStampCalendar();
        }

        return getCalendarFromNote(note);
    }

    /**
     * Gets calendar from note.
     * @param note The note item.
     * @return The calendar.
     */
    protected Calendar getCalendarFromNote(HibNoteItem note) {
        // Start with existing calendar if present
        Calendar calendar = note.getTaskCalendar();
        
        // otherwise, start with new calendar
        if (calendar == null) {
            calendar = ICalendarUtils.createBaseCalendar(new VToDo());
        }
        else {
            // use copy when merging calendar with item properties
            calendar = CalendarUtils.copyCalendar(calendar);
        }
        
        // merge in displayName,body
        VToDo task = (VToDo) calendar.getComponent(Component.VTODO);
        mergeCalendarProperties(task, note);
        
        return calendar;
    }

    /**
     * Merges calendar properties.
     * @param task The task.
     * @param note The note item.
     */
    private void mergeCalendarProperties(VToDo task, HibNoteItem note) {
        //uid = icaluid or uid
        //summary = displayName
        //description = body
        //dtstamp = clientModifiedDate/modifiedDate
        //completed = triageStatus==DONE/triageStatusRank
        
        String icalUid = note.getIcalUid();
        if (icalUid==null) {
            icalUid = note.getUid();
        }
        
        if (note.getClientModifiedDate()!=null) {
            ICalendarUtils.setDtStamp(note.getClientModifiedDate(), task);
        }
        else {
            ICalendarUtils.setDtStamp(note.getModifiedDate(), task);
        }
        
        ICalendarUtils.setUid(icalUid, task);
        ICalendarUtils.setSummary(note.getDisplayName(), task);
        ICalendarUtils.setDescription(note.getBody(), task);
        
        // Set COMPLETED/STATUS if triagestatus is DONE
        TriageStatus ts = note.getTriageStatus();
        DateTime completeDate = null;
        if(ts!=null && ts.getCode()==TriageStatusUtil.CODE_DONE) {
            ICalendarUtils.setStatus(Status.VTODO_COMPLETED, task);
            if (ts.getRank() != null) {
                completeDate =  new DateTime(TriageStatusUtil.getDateFromRank(ts.getRank()));
            }
        }
        
        ICalendarUtils.setCompleted(completeDate, task);
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
}
