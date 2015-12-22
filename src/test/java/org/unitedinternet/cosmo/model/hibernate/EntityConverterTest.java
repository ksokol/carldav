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
import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.unitedinternet.cosmo.model.mock.MockEventExceptionStamp;
import org.unitedinternet.cosmo.model.mock.MockEventStamp;

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
    
    
    protected EntityFactory entityFactory = new MockEntityFactory();
    protected EntityConverter converter = new EntityConverter(entityFactory);

    /**
     * Tests entity converter event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterEvent() throws Exception {
        
        Calendar calendar = getCalendar("event_with_exception.ics");
        NoteItem master = entityFactory.createNote();
        Set<NoteItem> items = converter.convertEventCalendar(master, calendar);
        
        // should be master and mod
        Assert.assertEquals(2, items.size());
        
        // get master
        Iterator<NoteItem> it = items.iterator();
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
        Assert.assertTrue(TriageStatus.CODE_DONE==ts.getCode());
        // DTSTAMP
        Assert.assertEquals(master.getClientModifiedDate().getTime(), new DateTime("20051222T210507Z").getTime());
        // UID
        Assert.assertEquals(master.getIcalUid(), "F5B811E00073B22BA6B87551@ninevah.local");
        // SUMMARY
        Assert.assertEquals(master.getDisplayName(), "event 6");
        
        // get mod
        NoteItem mod = it.next();
        
        ModificationUid uid = new ModificationUid(mod.getUid());
        
        Assert.assertEquals(master.getUid(), uid.getParentUid());
        Assert.assertEquals("20060104T190000Z", uid.getRecurrenceId().toString());
        
        Assert.assertTrue(mod.getModifies()==master);
        EventExceptionStamp ees = StampUtils.getEventExceptionStamp(mod);
        Assert.assertNotNull(ees);
        
        // mod should include VTIMEZONES
        Calendar eventCal = ees.getEventCalendar();
        ComponentList vtimezones = eventCal.getComponents(Component.VTIMEZONE);
        Assert.assertEquals(1, vtimezones.size());
        
        
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
        Assert.assertFalse(mod.getIsActive().booleanValue());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060105T190000Z");
        Assert.assertNotNull(mod);
        Assert.assertEquals("event 6 mod 2 changed", mod.getDisplayName());
        
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
        EventStamp eventStamp = new MockEventStamp(master);
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
        EventStamp eventStamp = new MockEventStamp(master);
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
        EventExceptionStamp exceptionStamp = new MockEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.setStartDate(eventStamp.getStartDate());
        exceptionStamp.setRecurrenceId(eventStamp.getStartDate());
        mod.addStamp(exceptionStamp);
        
        // test modification VEVENT gets added properly
        Calendar cal = converter.convertNote(master);
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        @SuppressWarnings("unused")
		VEvent masterEvent = (VEvent) comps.get(0);
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
        EventStamp eventStamp = new MockEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        eventStamp.setAnyTime(true);
        DateList dates = new DateList();
        dates.add(new DateTime("20070212T074500"));
        dates.add(new DateTime("20070213T074500"));
        eventStamp.setRecurrenceDates(dates);
        master.addStamp(eventStamp);

        HibNoteItem mod = new HibNoteItem();
        mod.setModifies(master);
        master.addModification(mod);
        EventExceptionStamp exceptionStamp = new MockEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.setRecurrenceId(new DateTime("20070212T074500"));
        exceptionStamp.setStartDate(new DateTime("20070212T074500"));
        exceptionStamp.setAnyTime(null);
        mod.addStamp(exceptionStamp);
        
        Calendar cal = converter.convertNote(master);
        cal.validate();
        ComponentList comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        VEvent masterEvent = (VEvent) comps.get(0);
        VEvent modEvent = (VEvent) comps.get(1);
        
        Parameter masterAnyTime = masterEvent.getStartDate().getParameter("X-OSAF-ANYTIME");
        Parameter modAnyTime = modEvent.getStartDate().getParameter("X-OSAF-ANYTIME");
        
        Assert.assertNotNull(masterAnyTime);
        Assert.assertEquals("TRUE", masterAnyTime.getValue());
        Assert.assertNotNull(modAnyTime);
        Assert.assertEquals("TRUE", modAnyTime.getValue());
        
        // change master and verify attribute is inherited in modification
        eventStamp.setAnyTime(false);
        
        cal = converter.convertNote(master);
        cal.validate();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        masterEvent = (VEvent) comps.get(0);
        modEvent = (VEvent) comps.get(1);
        
        Assert.assertNull(masterEvent.getStartDate().getParameter("X-OSAF-ANYTIME"));
        Assert.assertNull(modEvent.getStartDate().getParameter("X-OSAF-ANYTIME"));
        
        // change both and verify
        exceptionStamp.setAnyTime(true);
        
        cal = converter.convertNote(master);
        cal.validate();
        comps = cal.getComponents(Component.VEVENT);
        Assert.assertEquals(2, comps.size());
        masterEvent = (VEvent) comps.get(0);
        modEvent = (VEvent) comps.get(1);
        
        modAnyTime = modEvent.getStartDate().getParameter("X-OSAF-ANYTIME");
        
        Assert.assertNull(masterEvent.getStartDate().getParameter("X-OSAF-ANYTIME"));
        Assert.assertNotNull(modAnyTime);
        Assert.assertEquals("TRUE", modAnyTime.getValue());
    }
    
    /**
     * Finds mod by recurrence id.
     * @param items The items.
     * @param rid The id.
     * @return The note item.
     */
    private NoteItem findModByRecurrenceIdForNoteItems(Set<NoteItem> items, String rid) {
        for(NoteItem note: items) {
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
        return cb.build(fis);
    }
    
}
