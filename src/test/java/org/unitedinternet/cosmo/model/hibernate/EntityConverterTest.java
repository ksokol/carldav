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
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.Status;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.model.TriageStatusUtil;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

import java.io.FileInputStream;

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
    @Ignore
    @Test
    public void testConvertEvent() throws Exception {
        TimeZoneRegistry registry =
            TimeZoneRegistryFactory.getInstance().createRegistry();
        HibNoteItem master = new HibNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        master.setIcalUid("icaluid");
        master.setClientModifiedDate(new DateTime("20070101T100000Z"));
        HibBaseEventStamp eventStamp = new HibBaseEventStamp(master);
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
