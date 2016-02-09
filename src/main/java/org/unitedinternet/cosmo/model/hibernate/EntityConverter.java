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
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.TriageStatusUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 * A component that converts iCalendar objects to entities and vice versa.
 * Often this is not a straight one-to-one mapping, because recurring
 * iCalendar events are modeled as multiple events in a single
 * {@link Calendar}, whereas recurring items are modeled as a master
 * {@link HibNoteItem} with zero or more {@link HibNoteItem} modifications
 */
public class EntityConverter { 
    private static final TimeZoneRegistry TIMEZONE_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry();

    private final IdGenerator idGenerator;

    public EntityConverter(IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "idGenerator is null");
        this.idGenerator = idGenerator;
    }
    
    /**
     * Converts a single calendar containing many different
     * components and component types into a set of
     * {@link HibICalendarItem}.
     * 
     * @param calendar calendar containing any number and type
     *        of calendar components
     * @return set of ICalendarItems
     */
    public Set<HibICalendarItem> convertCalendar(Calendar calendar) {
        Set<HibICalendarItem> items = new LinkedHashSet<>();
        for(CalendarContext cc: splitCalendar(calendar)) {
            if(cc.type.equals(Component.VEVENT)) {
                items.addAll(convertEventCalendar(cc.calendar));
            }
            else if(cc.type.equals(Component.VTODO)) {
                items.add(convertTaskCalendar(cc.calendar));
            }
            else if(cc.type.equals(Component.VJOURNAL)) {
                items.add(convertJournalCalendar(cc.calendar));
            }
        }
        
        return items;
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
        HibEventStamp eventStamp = (HibEventStamp) note.getStamp(HibEventStamp.class);
        
        if (eventStamp == null) {
            eventStamp = new HibEventStamp(note);
            note.addStamp(eventStamp);
        }

        if (note.getUid() == null) {
            note.setUid(idGenerator.nextStringIdentifier());
        }

        updateEventInternal(note, calendar);

        LinkedHashSet<HibNoteItem> items = new LinkedHashSet<>();
        items.add(note);

        // add modifications to set of items
        for(Iterator<HibNoteItem> it = note.getModifications().iterator(); it.hasNext();) {
            HibNoteItem mod = it.next();
            items.add(mod);
        }

        return items;
    }
    
    /**
     * Expands an event calendar and returns a set of notes representing the
     * master and exception items.
     * @param calendar The calendar.
     * @return The convertion.
     */
    public Set<HibNoteItem> convertEventCalendar(Calendar calendar) {
        HibNoteItem note = new HibNoteItem();
        note.setUid(idGenerator.nextStringIdentifier());
        setBaseContentAttributes(note);
        return convertEventCalendar(note, calendar);
    }
    
    /**
     * Convert calendar containing single VJOURNAL into NoteItem
     * @param calendar calendar containing VJOURNAL
     * @return NoteItem representation of VJOURNAL
     */
    public HibJournalItem convertJournalCalendar(Calendar calendar) {
        HibJournalItem note = new HibJournalItem();
        note.setClientCreationDate(new Date());
        note.setClientModifiedDate(note.getClientCreationDate());
        return convertJournalCalendar(note, calendar);
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
     * Returns an icalendar representation of a calendar collection.  
     * @param collection calendar collection
     * @return icalendar representation of collection
     */
    public Calendar convertCollection(HibCollectionItem collection) {
        Calendar calendar = ICalendarUtils.createBaseCalendar();

        // extract the supported calendar components for each child item and
        // add them to the collection calendar object.
        // index the timezones by tzid so that we only include each tz
        // once. if for some reason different calendar items have
        // different tz definitions for a tzid, *shrug* last one wins
        // for this same reason, we use a single calendar builder/time
        // zone registry.
        Map<String, CalendarComponent> tzIdx = new HashMap<>();
        
        for (HibItem hibItem : collection.getItems()) {
           if (!(hibItem instanceof HibItem)) {
               continue;
           }

           HibItem hibContentItem = hibItem;
           Calendar childCalendar = convertContent(hibContentItem);
           
           // ignore items that can't be converted
           if (childCalendar == null) {
               continue;
           }
           
           // index VTIMEZONE and add all other components
           for (Object obj: childCalendar.getComponents()) {
               CalendarComponent comp = (CalendarComponent)obj; 
               if(Component.VTIMEZONE.equals(comp.getName())) {
                   Property tzId = comp.getProperties().getProperty(Property.TZID);
                   if (! tzIdx.containsKey(tzId.getValue())) {
                       tzIdx.put(tzId.getValue(), comp);
                   }
               } else {
                   calendar.getComponents().add(comp);
               }
           }
        }
        
        // add VTIMEZONEs
        for (CalendarComponent comp: tzIdx.values()) {
            calendar.getComponents().add(0,comp);
        }
       
        return calendar;
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

        // must be a master note
        if (note.getModifies()!=null) {
            return null;
        }

        HibBaseEventStamp event = (HibBaseEventStamp) note.getStamp(HibBaseEventStamp.class);
        if (event!=null) {
            return getCalendarFromEventStamp(event);
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
     * gets calendar from event stamp.
     * @param stamp The event stamp.
     * @return The calendar.
     */
    protected Calendar getCalendarFromEventStamp(HibBaseEventStamp stamp) {
        Calendar masterCal = CalendarUtils.copyCalendar(stamp.getEventCalendar());
        if (masterCal == null) {
            return null;
        }
       
        // the master calendar might not have any events; for
        // instance, a client might be trying to save a VTODO
        if (masterCal.getComponents(Component.VEVENT).isEmpty()) {
            return masterCal;
        }

        VEvent masterEvent = (VEvent) masterCal.getComponents(Component.VEVENT).get(0);
        VAlarm masterAlarm = getDisplayAlarm(masterEvent);
        String masterLocation = stamp.getLocation();
        
        // build timezone map that includes all timezones in master calendar
        ComponentList timezones = masterCal.getComponents(Component.VTIMEZONE);
        HashMap<String, VTimeZone> tzMap = new HashMap<String, VTimeZone>();
        for(Iterator it = timezones.iterator(); it.hasNext();) {
            VTimeZone vtz = (VTimeZone) it.next();
            tzMap.put(vtz.getTimeZoneId().getValue(), vtz);
        }
        
        // check start/end date tz is included, and add if it isn't
        String tzid = getTzId(stamp.getStartDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
            if(tz!=null) {
                VTimeZone vtz = tz.getVTimeZone();
                masterCal.getComponents().add(0, vtz);
                tzMap.put(tzid, vtz);
            }
        }
        
        tzid = getTzId(stamp.getEndDate());
        if(tzid!=null && !tzMap.containsKey(tzid)) {
            TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
            if(tz!=null) {
                VTimeZone vtz = tz.getVTimeZone();
                masterCal.getComponents().add(0, vtz);
                tzMap.put(tzid, vtz);
            }
        }
        
        // merge item properties to icalendar props
        mergeCalendarProperties(masterEvent, (HibNoteItem) stamp.getItem());
        
        // bug 9606: handle displayAlarm with no trigger by not including
        // in exported icalendar
        if(masterAlarm!=null && stamp.getDisplayAlarmTrigger()==null) {
            masterEvent.getAlarms().remove(masterAlarm);
            masterAlarm = null;
        }
        
        // If event is not recurring, skip all the event modification
        // processing
        if (!stamp.isRecurring()) {
            return masterCal;
        }
        
        // add all exception events
        HibNoteItem note = (HibNoteItem) stamp.getItem();
        TreeMap<String, VEvent> sortedMap = new TreeMap<>();
        for(HibNoteItem exception : note.getModifications()) {
            HibEventExceptionStamp exceptionStamp = exception.getEventException();
            
            // if modification isn't stamped as an event then ignore
            if (exceptionStamp==null) {
                continue;
            }
            
            // Get exception event copy
            VEvent exceptionEvent = (VEvent) CalendarUtils
                    .copyComponent(exceptionStamp.getExceptionEvent());

            // ensure DURATION or DTEND exists on modfication
            if (ICalendarUtils.getDuration(exceptionEvent) == null) {
                ICalendarUtils.setDuration(exceptionEvent, ICalendarUtils
                        .getDuration(masterEvent));
            }
            
            // merge item properties to icalendar props
            mergeCalendarProperties(exceptionEvent, exception);

            // Check for inherited displayAlarm, which is represented
            // by a valarm with no TRIGGER
            VAlarm displayAlarm = getDisplayAlarm(exceptionEvent);
            if(displayAlarm !=null && exceptionStamp.getDisplayAlarmTrigger()==null) {
                exceptionEvent.getAlarms().remove(displayAlarm);
                if (masterAlarm != null) {
                    exceptionEvent.getAlarms().add(masterAlarm);
                }
            }
            
            // Check for inherited LOCATION which is represented as null LOCATION
            // If inherited, and master event has a LOCATION, then add it to exception
            if(exceptionStamp.getLocation()==null && masterLocation!=null) {
                ICalendarUtils.setLocation(masterLocation, exceptionEvent);
            }
            
            sortedMap.put(exceptionStamp.getRecurrenceId().toString(), exceptionEvent);
            
            // verify that timezones are present for exceptions, and add if not
            tzid = getTzId(exceptionStamp.getStartDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
                if(tz!=null) {
                    VTimeZone vtz = tz.getVTimeZone();
                    masterCal.getComponents().add(0, vtz);
                    tzMap.put(tzid, vtz);
                }
            }
            
            tzid = getTzId(exceptionStamp.getEndDate());
            if(tzid!=null && !tzMap.containsKey(tzid)) {
                TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
                if(tz!=null) {
                    VTimeZone vtz = tz.getVTimeZone();
                    masterCal.getComponents().add(0, vtz);
                    tzMap.put(tzid, vtz);
                }
            }
        }
        
        masterCal.getComponents().addAll(sortedMap.values());
        
        return masterCal;
    }
    
    /**
     * Merges calendar properties.
     * @param event The event.
     * @param note The note item.
     */
    private void mergeCalendarProperties(VEvent event, HibNoteItem note) {
        //summary = displayName
        //description = body
        //uid = icalUid
        //dtstamp = clientModifiedDate/modifiedDate
        
        boolean isMod = note.getModifies()!=null;
        if (isMod) {
            ICalendarUtils.setUid(note.getModifies().getIcalUid(), event);
        }
        else {
            ICalendarUtils.setUid(note.getIcalUid(), event);
        }
        
        // inherited displayName and body should always be serialized
        if (event.getSummary() != null) {
            ICalendarUtils.setSummary(event.getSummary().getValue(), event);
        } else if (note.getDisplayName()==null && isMod) {
            ICalendarUtils.setSummary(note.getModifies().getDisplayName(), event);
        }
        else  {
            ICalendarUtils.setSummary(note.getDisplayName(), event);
        }
        if (event.getDescription() != null) {
            ICalendarUtils.setDescription(event.getDescription().getValue(), event);
        } else if (note.getBody()==null && isMod) {
            ICalendarUtils.setDescription(note.getModifies().getBody(), event);
        } else {
            ICalendarUtils.setDescription(note.getBody(), event);
        }
       
       
        if (note.getClientModifiedDate()!=null) {
            ICalendarUtils.setDtStamp(note.getClientModifiedDate(), event);
        }
        else {
            ICalendarUtils.setDtStamp(note.getModifiedDate(), event);
        }
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
     * Gets display alarm.
     * @param event The event.
     * @return The alarm.
     */
    private VAlarm getDisplayAlarm(VEvent event) {
        for(Iterator it = event.getAlarms().iterator();it.hasNext();) {
            VAlarm alarm = (VAlarm) it.next();
            if (alarm.getProperties().getProperty(Property.ACTION).equals(Action.DISPLAY)) {
                return alarm;
            }
        }
        
        return null;
    }
    
    /**
     * Gets timezone id.
     * @param date The date.
     * @return The id.
     */
    private String getTzId(Date date) {
        if(date instanceof DateTime) {
            DateTime dt = (DateTime) date;
            if (dt.getTimeZone()!=null) {
                return dt.getTimeZone().getID();
            }
        }
        
        return null;
    }

    /**
     * Updates event internal.
     * @param masterNote The master note.
     * @param calendar The calendar.
     */
    private void updateEventInternal(HibNoteItem masterNote, Calendar calendar) {
        final HashMap<Date, VEvent> exceptions = new HashMap<>();
        final PropertyList calendarProperties = (PropertyList) calendar.getProperties().clone();

        Calendar masterCalendar = calendar;
        
        ComponentList vevents = masterCalendar.getComponents().getComponents(Component.VEVENT);
        HibEventStamp eventStamp = (HibEventStamp) masterNote.getStamp(HibEventStamp.class);

        // get list of exceptions (VEVENT with RECURRENCEID)
        for (Iterator<VEvent> i = vevents.iterator(); i.hasNext();) {
            VEvent event = i.next();
            // make sure event has DTSTAMP, otherwise validation will fail
            if (event.getDateStamp()==null) {
                event.getProperties().add(new DtStamp(new DateTime()));
            }
            if (event.getRecurrenceId() != null) {
                Date recurrenceIdDate = event.getRecurrenceId().getDate();
                exceptions.put(recurrenceIdDate, event);
            }
        }
        
        // Remove all exceptions from master calendar as these
        // will be stored in each NoteItem modification's EventExceptionStamp
        for (Entry<Date, VEvent> entry : exceptions.entrySet()) {
            masterCalendar.getComponents().remove(entry.getValue());
        }

        // Master calendar includes everything in the original calendar minus
        // any exception events (VEVENT with RECURRENCEID)
        eventStamp.setEventCalendar(masterCalendar);
        compactTimezones(masterCalendar);
        
        VEvent event = eventStamp.getEvent();
        
        // verify master event exists
        if (event==null) {
            final ComponentList clonedComponentList = (ComponentList) vevents.clone();
            final Calendar newMasterCalendar = new Calendar(calendarProperties, clonedComponentList);

            eventStamp.setEventCalendar(newMasterCalendar);
            compactTimezones(masterCalendar);

            event = eventStamp.getEvent();

            if(event == null) {
                throw new ModelValidationException("no master calendar component found");
            }
        }
        
        setCalendarAttributes(masterNote, event);
        
        // synchronize exceptions with master NoteItem modifications
        syncExceptions(exceptions, masterNote);
    }

    /**
     * Compact timezones.
     * @param calendar The calendar.
     */
    private void compactTimezones(Calendar calendar) {
        
        if (calendar==null) {
            return;
        }

        // Get list of timezones in master calendar and remove all timezone
        // definitions that are in the registry.  The idea is to not store
        // extra data.  Instead, the timezones will be added to the calendar
        // by the getCalendar() api.
        ComponentList timezones = calendar.getComponents(Component.VTIMEZONE);
        ArrayList toRemove = new ArrayList();
        for(Iterator it = timezones.iterator();it.hasNext();) {
            VTimeZone vtz = (VTimeZone) it.next();
            String tzid = vtz.getTimeZoneId().getValue();
            TimeZone tz = TIMEZONE_REGISTRY.getTimeZone(tzid);
            //  Remove timezone iff it matches the one in the registry
            if(tz!=null && vtz.equals(tz.getVTimeZone())) {
                toRemove.add(vtz);
            }
        }

        // remove known timezones from master calendar
        calendar.getComponents().removeAll(toRemove);
    }

    /**
     * Sync exceptions.
     * @param exceptions The exceptions.
     * @param masterNote The master note.
     */
    private void syncExceptions(Map<Date, VEvent> exceptions,
                                HibNoteItem masterNote) {
        for (Entry<Date, VEvent> entry : exceptions.entrySet()) {
            syncException(entry.getValue(), masterNote);
        }
    }

    /**
     * Sync exception.
     * @param event The event.
     * @param masterNote The master note.
     */
    private void syncException(VEvent event, HibNoteItem masterNote) {
        HibNoteItem mod =
            getModification(masterNote, event.getRecurrenceId().getDate());

        if (mod == null) {
            // create if not present
            createNoteModification(masterNote, event);
        } else {
            // update existing mod
            updateNoteModification(mod, event);
        }
    }

    /**
     * Gets modification.
     * @param masterNote The master note.
     * @param recurrenceId The reccurence id.
     * @return The note item.
     */
    private HibNoteItem getModification(HibNoteItem masterNote,
                                     Date recurrenceId) {
        for (HibNoteItem mod : masterNote.getModifications()) {
            HibEventExceptionStamp exceptionStamp = mod.getEventException();
            // only interested in mods with event stamp
            if (exceptionStamp == null) {
                continue;
            }
            if (exceptionStamp.getRecurrenceId().equals(recurrenceId)) {
                return mod;
            }
        }

        return null;
    }
    
    /**
     * Creates note modification.
     * @param masterNote masterNote.
     * @event The event.
     */
    private void createNoteModification(HibNoteItem masterNote, VEvent event) {
        HibNoteItem noteMod = new HibNoteItem();
        Calendar exceptionCal = null;
        // a note modification should inherit the calendar product info as its master component.
        if(masterNote.getStamp(HibEventStamp.class) != null) {
            HibEventStamp masterStamp = (HibEventStamp) masterNote.getStamp(HibEventStamp.class);
            Calendar masterCal = masterStamp.getEventCalendar();
            if(masterCal != null && masterCal.getProductId() != null) {
                exceptionCal = new Calendar();
                exceptionCal.getProperties().add(masterCal.getProductId());
                exceptionCal.getProperties().add(masterCal.getVersion() != null? masterCal.getVersion(): Version.VERSION_2_0);
                exceptionCal.getProperties().add(masterCal.getCalendarScale() != null? masterCal.getCalendarScale(): CalScale.GREGORIAN);
                exceptionCal.getComponents().add(event);
            }
        }

        HibEventExceptionStamp exceptionStamp =new HibEventExceptionStamp(noteMod);
        exceptionStamp.setEventCalendar(exceptionCal);
        exceptionStamp.setExceptionEvent(event);
        noteMod.addStamp(exceptionStamp);

        noteMod.setUid(new ModificationUidImpl(masterNote, event.getRecurrenceId()
                .getDate()).toString());
        noteMod.setOwner(masterNote.getOwner());
        noteMod.setName(noteMod.getUid());
        
        // copy VTIMEZONEs to front if present
        HibEventStamp es = (HibEventStamp) masterNote.getStamp(HibEventStamp.class);
        ComponentList vtimezones = es.getEventCalendar().getComponents(Component.VTIMEZONE);
        for(Object obj : vtimezones) {
            VTimeZone vtimezone = (VTimeZone)obj;
            exceptionStamp.getEventCalendar().getComponents().add(0, vtimezone);
        }
        
        setBaseContentAttributes(noteMod);
        noteMod.setModifies(masterNote);
        masterNote.addModification(noteMod);
        
        setCalendarAttributes(noteMod, event);
    }

    /**
     * Updates note modification.
     * @param noteMod The note item modified.
     * @param event The event.
     */
    private void updateNoteModification(HibNoteItem noteMod, VEvent event) {
        HibEventExceptionStamp exceptionStamp = noteMod.getEventException();

        // copy VTIMEZONEs to front if present
        ComponentList vtimezones = exceptionStamp.getMasterNote().getComponents(Component.VTIMEZONE);
        for(Object obj: vtimezones) {
            VTimeZone vtimezone = (VTimeZone)obj;
            exceptionStamp.getEventCalendar().getComponents().add(0, vtimezone);
        }
        
        noteMod.setClientModifiedDate(new Date());
        
        setCalendarAttributes(noteMod, event);
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
     * @param event The event.
     */
    private void setCalendarAttributes(HibNoteItem note, VEvent event) {
        
        // UID (only set if master)
        if(event.getUid()!=null && note.getModifies()==null) {
            note.setIcalUid(event.getUid().getValue());
        }
        
        // for now displayName is limited to 1024 chars
        if (event.getSummary() != null) {
            note.setDisplayName(StringUtils.substring(event.getSummary()
                    .getValue(), 0, 1024));
        }

        if (event.getDescription() != null) {
            note.setBody(event.getDescription().getValue());
        }

        // look for DTSTAMP
        if(event.getDateStamp()!=null) {
            note.setClientModifiedDate(event.getDateStamp().getDate());
        }
        
        // look for absolute VALARM
        VAlarm va = ICalendarUtils.getDisplayAlarm(event);
        if (va != null && va.getTrigger()!=null) {
            Trigger trigger = va.getTrigger();
            Date reminderTime = trigger.getDateTime();
            if (reminderTime != null) {
                note.setRemindertime(reminderTime);
            }
        }

        // calculate triage status based on start date
        java.util.Date now =java.util.Calendar.getInstance().getTime();
        boolean later = event.getStartDate().getDate().after(now);
        int code = later ? TriageStatusUtil.CODE_LATER : TriageStatusUtil.CODE_DONE;
        
        TriageStatus triageStatus = note.getTriageStatus();
        
        // initialize TriageStatus if not present
        if (triageStatus == null) {
            triageStatus = TriageStatusUtil.initialize(new TriageStatus());
            note.setTriageStatus(triageStatus);
        }

        triageStatus.setCode(code);
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
    
    /**
     * Given a Calendar with no VTIMZONE components, go through
     * all other components and add all relevent VTIMEZONES.
     * @param calendar The calendar.
     */
    private void addTimezones(Calendar calendar) {
        ComponentList comps = calendar.getComponents();
        Set<VTimeZone> timezones = new HashSet<VTimeZone>();
        
        for(Iterator<Component> it = comps.iterator();it.hasNext();) {
            Component comp = it.next();
            PropertyList props = comp.getProperties();
            for(Iterator<Property> it2 = props.iterator();it2.hasNext();) {
                Property prop = it2.next();
                if(prop instanceof DateProperty) {
                    DateProperty dateProp = (DateProperty) prop;
                    Date d = dateProp.getDate();
                    if(d instanceof DateTime) {
                        DateTime dt = (DateTime)d;
                        if(dt.getTimeZone()!=null) {
                            timezones.add(dt.getTimeZone().getVTimeZone());
                        }
                    }
                } else if(prop instanceof DateListProperty) {
                    DateListProperty dateProp = (DateListProperty) prop;
                    if(dateProp.getDates().getTimeZone()!=null) {
                        timezones.add(dateProp.getDates().getTimeZone().getVTimeZone());
                    }
                }
            }
        }
        
        for(VTimeZone vtz: timezones) {
            calendar.getComponents().add(0, vtz);
        }
    }
    
    /**
     * Given a calendar with many different components, split into
     * separate calendars that contain only a single component type
     * and a single UID.
     * @param calendar The calendar.
     * @return The split calendar.
     */
    private CalendarContext[] splitCalendar(Calendar calendar) {
        Vector<CalendarContext> contexts = new Vector<CalendarContext>();
        Set<String> allComponents = new HashSet<String>();
        Map<String, ComponentList> componentMap = new HashMap<String, ComponentList>();
        
        ComponentList comps = calendar.getComponents();
        for(Iterator<Component> it = comps.iterator(); it.hasNext();) {
            Component comp = it.next();
            // ignore vtimezones for now
            if(comp instanceof VTimeZone) {
                continue;
            }
            
            Uid uid = (Uid) comp.getProperty(Property.UID);
            RecurrenceId rid = (RecurrenceId) comp.getProperty(Property.RECURRENCE_ID);
            
            String key = uid.getValue();
            if(rid!=null) {
                key+=rid.toString();
            }
            
            // ignore duplicates
            if(allComponents.contains(key)) {
                continue;
            }
            
            allComponents.add(key);
            
            ComponentList cl = componentMap.get(uid.getValue());
            
            if(cl==null) {
                cl = new ComponentList();
                componentMap.put(uid.getValue(), cl);
            }
            
            cl.add(comp);
        }
        
        for(Entry<String, ComponentList> entry : componentMap.entrySet()) {
           
            Component firstComp = (Component) entry.getValue().get(0);
            
            Calendar cal = ICalendarUtils.createBaseCalendar();
            cal.getComponents().addAll(entry.getValue());
            addTimezones(cal);
            
            CalendarContext cc = new CalendarContext();
            cc.calendar = cal;
            cc.type = firstComp.getName();
            
            contexts.add(cc);
        }
        
        return contexts.toArray(new CalendarContext[contexts.size()]);
    }

    /**
     * Container for a calendar containing single component type (can
     * be multiple components if the component is recurring and has
     * modifications), and the component type.
     */
    static class CalendarContext {
        String type;
        Calendar calendar;
    }
}
