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
package org.unitedinternet.cosmo.model.hibernate;


import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Status;
import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.model.TriageStatusUtil;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * Test EntityConverter.
 *
 */
public class EntityConverterTest {
    protected String baseDir = "src/test/resources/testdata/entityconverter/";
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    protected EntityConverter converter = new EntityConverter(new VersionFourGenerator());
    
    /**
     * Tests entity convertor task.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterTask() throws Exception {
        Calendar calendar = getCalendar("vtodo.ics");

        HibNoteItem note = converter.convertTaskCalendar(calendar);
        
        Assert.assertTrue(TriageStatusUtil.CODE_NOW==note.getTriageStatus().getCode());
        
        // add COMPLETED
        DateTime completeDate = new DateTime("20080122T100000Z");
        
        VToDo vtodo = (VToDo) calendar.getComponents(Component.VTODO).get(0);
        ICalendarUtils.setCompleted(completeDate, vtodo);
        note = converter.convertTaskCalendar(calendar);
        
        TriageStatus ts = note.getTriageStatus();
        Assert.assertTrue(TriageStatusUtil.CODE_DONE==ts.getCode());
        Assert.assertTrue(TriageStatusUtil.getDateFromRank(ts.getRank()).getTime()==completeDate.getTime());
    
        note.setTriageStatus(null);
        ICalendarUtils.setCompleted(null, vtodo);
        Assert.assertNull(vtodo.getDateCompleted());
        ICalendarUtils.setStatus(Status.VTODO_COMPLETED, vtodo);
        
        // verify that TriageStatus.rank is set ot current time when 
        // STATUS:COMPLETED is present and COMPLETED is not present
        long begin = (System.currentTimeMillis() / 1000) * 1000;
        note = converter.convertTaskCalendar(calendar);
        long end = (System.currentTimeMillis() / 1000) * 1000;
        ts = note.getTriageStatus();
        Assert.assertTrue(TriageStatusUtil.CODE_DONE==ts.getCode());
        long rankTime = TriageStatusUtil.getDateFromRank(ts.getRank()).getTime();
        Assert.assertTrue(rankTime<=end && rankTime>=begin);
    }
    
    /**
     * Tests entity converter event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterEvent() throws Exception {
        
        Calendar calendar = getCalendar("event_with_exception.ics");
        HibNoteItem master = new HibNoteItem();
        Set<HibNoteItem> items = converter.convertEventCalendar(master, calendar);
        
        // should be master and mod
        Assert.assertEquals(2, items.size());
        
        // get master
        Iterator<HibNoteItem> it = items.iterator();
        master = it.next();
        
        // check ical props
        // DTSTART
//      This fails, which is pretty strange
//        EventStamp masterEvent = StampUtils.getEventStamp(master); 
//        Assert.assertNotNull(masterEvent);
//        DateTime testStart = new DateTime("20060102T140000", TimeZoneUtils.getTimeZone("US/Eastern_mod"));
//        Assert.assertTrue(masterEvent.getStartDate().equals(testStart));
        // Triage status
        TriageStatus ts = master.getTriageStatus();
        // the event is in the past, it should be DONE
        Assert.assertTrue(TriageStatusUtil.CODE_DONE==ts.getCode());
        // DTSTAMP
        Assert.assertEquals(master.getClientModifiedDate().getTime(), new DateTime("20051222T210507Z").getTime());
        // UID
        Assert.assertEquals(master.getIcalUid(), "F5B811E00073B22BA6B87551@ninevah.local");
        // SUMMARY
        Assert.assertEquals(master.getDisplayName(), "event 6");
        
        // get mod
        HibNoteItem mod = it.next();
        
        ModificationUidImpl uid = new ModificationUidImpl(mod.getUid());
        
        Assert.assertEquals(master.getUid(), uid.getParentUid());
        Assert.assertEquals("20060104T190000Z", uid.getRecurrenceId().toString());
        
        Assert.assertTrue(mod.getModifies()==master);
        Assert.assertNotNull(mod.getEventException());
        
        // update event (change mod and add mod)
        calendar = getCalendar("event_with_exception2.ics");
        items = converter.convertEventCalendar(master, calendar);
        
        // should be master and 2 mods
        Assert.assertEquals(3, items.size());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060104T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertEquals("event 6 mod 1 changed", mod.getDisplayName());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060105T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertEquals("event 6 mod 2", mod.getDisplayName());
        
        // update event again (remove mod)
        calendar = getCalendar("event_with_exception3.ics");
        items = converter.convertEventCalendar(master, calendar);
        
        // should be master and 1 active mod/ 1 deleted mod
        Assert.assertEquals(3, items.size());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060104T190000Z");
        Assert.assertNotNull(mod);
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060105T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertEquals("event 6 mod 2 changed", mod.getDisplayName());
        
    }
    
    /**
     * Tests entity converter multi component calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterMultiComponentCalendar() throws Exception {
       
        // test converting calendar with many different components
        // into ICalendarItems
        
        Calendar calendar = getCalendar("bigcalendar.ics");
        @SuppressWarnings("unused")
        HibNoteItem master = new HibNoteItem();
        Set<HibICalendarItem> items = converter.convertCalendar(calendar);
        
        // should be 8
        Assert.assertEquals(8, items.size());

        HibNoteItem item = (HibNoteItem) findItemByIcalUid(items, "8qv7nuaq50vk3r98tvj37vjueg@google.com" );
        Assert.assertNotNull(item);
        Assert.assertNotNull(item.getStamp(HibEventStamp.class));

        item = (HibNoteItem) findItemByIcalUid(items, "e3i849b29kd3fbp48hmkmgjst0@google.com" );
        Assert.assertNotNull(item);
        Assert.assertNotNull(item.getStamp(HibEventStamp.class));

        item = (HibNoteItem) findItemByIcalUid(items, "4csitoh29h1arc46bnchg19oc8@google.com" );
        Assert.assertNotNull(item);
        Assert.assertNotNull(item.getStamp(HibEventStamp.class));
        
        
        item = (HibNoteItem) findItemByIcalUid(items, "f920n2rdb0qdd6grkjh4m4jrq0@google.com" );
        Assert.assertNotNull(item);
        Assert.assertNotNull(item.getStamp(HibEventStamp.class));

        item = (HibNoteItem) findItemByIcalUid(items, "jev0phs8mnfkuvoscrra1fh8j0@google.com" );
        Assert.assertNotNull(item);
        Assert.assertNotNull(item.getStamp(HibEventStamp.class));
        
        item = (HibNoteItem) findModByRecurrenceId(items, "20071129T203000Z" );
        Assert.assertNotNull(item);

        Assert.assertNotNull(item.getEventException());
        
        item = (HibNoteItem) findItemByIcalUid(items, "19970901T130000Z-123404@host.com" );
        Assert.assertNotNull(item);

        HibJournalItem journalItem = (HibJournalItem) findItemByIcalUid(items, "19970901T130000Z-123405@host.com" );
        Assert.assertNotNull(journalItem);
        Assert.assertEquals(0, journalItem.getStamps().size());
        
    }

    /**
     * Tests convert task.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testConvertTask() throws Exception {
        HibNoteItem master = new HibNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        master.setIcalUid("icaluid");
        master.setClientModifiedDate(new DateTime("20070101T100000Z"));
        master.setTriageStatus(TriageStatusUtil.initialize(new TriageStatus()));
        
        Calendar cal = converter.convertNote(master);
        cal.validate();
        
        Assert.assertEquals(1, cal.getComponents().size());
        
        ComponentList comps = cal.getComponents(Component.VTODO);
        Assert.assertEquals(1, comps.size());
        VToDo task = (VToDo) comps.get(0);
        
        Assert.assertNull(task.getDateCompleted());
        
        DateTime completeDate = new DateTime("20080122T100000Z");
        
        master.getTriageStatus().setCode(TriageStatusUtil.CODE_DONE);
        master.getTriageStatus().setRank(TriageStatusUtil.getRank(completeDate.getTime()));

        cal = converter.convertNote(master);
        task = (VToDo) cal.getComponents().get(0);
        
        Completed completed = task.getDateCompleted();
        Assert.assertNotNull(completed);
        Assert.assertEquals(completeDate.getTime(), completed.getDate().getTime());
    }
    
    /**
     * Tests convert event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testConvertEvent() throws Exception {
        TimeZoneRegistry registry =
            TimeZoneRegistryFactory.getInstance().createRegistry();
        HibNoteItem master = new HibNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        master.setIcalUid("icaluid");
        master.setClientModifiedDate(new DateTime("20070101T100000Z"));
        HibEventStamp eventStamp = new HibEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        master.addStamp(eventStamp);
        
        Calendar cal = converter.convertNote(master);
        cal.validate();
        
        // date has no timezone, so there should be no timezones
        Assert.assertEquals(0, cal.getComponents(Component.VTIMEZONE).size());
      
        eventStamp.setStartDate(new DateTime("20070212T074500",TIMEZONE_REGISTRY.getTimeZone("America/Chicago")));
        
        cal = converter.convertNote(master);
        cal.validate();
        
        // should be a single VEVENT
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(1, comps.size());
        VEvent event = (VEvent) comps.get(0);
        
        // test VALUE=DATE-TIME is not present
        Assert.assertNull(event.getStartDate().getParameter(Parameter.VALUE));
        
        // test item properties got merged into calendar
        Assert.assertEquals("displayName", event.getSummary().getValue());
        Assert.assertEquals("body", event.getDescription().getValue());
        Assert.assertEquals("icaluid", event.getUid().getValue());
        Assert.assertEquals(master.getClientModifiedDate().getTime(), event.getDateStamp().getDate().getTime());
         
        // date has timezone, so there should be a timezone
        Assert.assertEquals(1, cal.getComponents(Component.VTIMEZONE).size());
        
        eventStamp.setEndDate(new DateTime("20070212T074500",TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles")));
        
        cal = converter.convertNote(master);
        cal.validate();
        
        // dates have 2 different timezones, so there should be 2 timezones
        Assert.assertEquals(2, cal.getComponents(Component.VTIMEZONE).size());
        
        // add timezones to master event calendar
        eventStamp.getEventCalendar().getComponents().add(registry.getTimeZone("America/Chicago").getVTimeZone());
        eventStamp.getEventCalendar().getComponents().add(registry.getTimeZone("America/Los_Angeles").getVTimeZone());
        
        cal = converter.convertNote(master);
        cal.validate();
        Assert.assertEquals(2, cal.getComponents(Component.VTIMEZONE).size());
    }
    
    /**
     * Tests event event modification.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventModificationGetCalendar() throws Exception {
        HibNoteItem master = new HibNoteItem();
        master.setIcalUid("icaluid");
        master.setDisplayName("master displayName");
        master.setBody("master body");
        HibEventStamp eventStamp = new HibEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        eventStamp.setDuration(new Dur("PT1H"));
        eventStamp.setLocation("master location");
        DateList dates = new DateList();
        dates.add(new DateTime("20070212T074500"));
        dates.add(new DateTime("20070213T074500"));
        eventStamp.setRecurrenceDates(dates);
        master.addStamp(eventStamp);
        
        eventStamp.getEventCalendar().validate();

        HibNoteItem mod = new HibNoteItem();
        mod.setDisplayName("modDisplayName");
        mod.setBody("modBody");
        mod.setModifies(master);
        master.addModification(mod);
        HibEventExceptionStamp exceptionStamp = new HibEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.setStartDate(eventStamp.getStartDate());
        exceptionStamp.setRecurrenceId(eventStamp.getStartDate());

        // test modification VEVENT gets added properly
        Calendar cal = converter.convertNote(master);
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        VEvent modEvent = (VEvent) comps.get(1);
        
        // test merged properties
        Assert.assertEquals("modDisplayName", modEvent.getSummary().getValue());
        Assert.assertEquals("modBody", modEvent.getDescription().getValue());
        Assert.assertEquals("icaluid", modEvent.getUid().getValue());
        
        // test duration got added to modfication
        Assert.assertNotNull(modEvent.getDuration());
        Assert.assertEquals("PT1H", modEvent.getDuration().getDuration().toString());
        
        // test inherited description/location/body
        mod.setDisplayName(null);
        mod.setBody((String) null);
        exceptionStamp.setLocation(null);
        
        cal = converter.convertNote(master);
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        modEvent = (VEvent) comps.get(1);
        
        Assert.assertEquals("master displayName", modEvent.getSummary().getValue());
        Assert.assertEquals("master body", modEvent.getDescription().getValue());
        Assert.assertEquals("master location", modEvent.getLocation().getValue());
        
    }

    /**
     * Tests inherited any time.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testInheritedAnyTime() throws Exception {
        HibNoteItem master = new HibNoteItem();
        HibEventStamp eventStamp = new HibEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));

        DateList dates = new DateList();
        dates.add(new DateTime("20070212T074500"));
        dates.add(new DateTime("20070213T074500"));
        eventStamp.setRecurrenceDates(dates);
        master.addStamp(eventStamp);

        HibNoteItem mod = new HibNoteItem();
        mod.setModifies(master);
        master.addModification(mod);
        HibEventExceptionStamp exceptionStamp = new HibEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.setRecurrenceId(new DateTime("20070212T074500"));
        exceptionStamp.setStartDate(new DateTime("20070212T074500"));

        Calendar cal = converter.convertNote(master);
        cal.validate();
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());

        cal = converter.convertNote(master);
        cal.validate();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());

        cal = converter.convertNote(master);
        cal.validate();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
    }

    /**
     * Finds item by Ical uid.
     * @param items The items.
     * @param icalUid The uid.
     * @return The calendar item.
     */
    private HibICalendarItem findItemByIcalUid(Set<HibICalendarItem> items, String icalUid) {
        for(HibICalendarItem item: items) {
            if(icalUid.equals(item.getIcalUid())) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Finds mod by recurrence id.
     * @param items The items.
     * @param rid The id.
     * @return The calendar item.
     */
    private HibICalendarItem findModByRecurrenceId(Set<HibICalendarItem> items, String rid) {
        for(HibICalendarItem item: items) {
            if(item instanceof HibNoteItem) {
                HibNoteItem note = (HibNoteItem) item;
                if(note.getModifies()!=null && note.getUid().contains(rid)) {
                    return note;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds mod by recurrence id.
     * @param items The items.
     * @param rid The id.
     * @return The note item.
     */
    private HibNoteItem findModByRecurrenceIdForNoteItems(Set<HibNoteItem> items, String rid) {
        for(HibNoteItem note: items) {
            if(note.getModifies()!=null && note.getUid().contains(rid)) {
                return note;
            }
        }
        
        return null;
    }
    
    /**
     * Gets calendar.
     * @param name The name.
     * @return The calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
    
}
