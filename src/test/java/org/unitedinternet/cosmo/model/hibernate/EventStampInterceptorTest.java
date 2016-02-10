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

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test EventStampHandler
 */
public class EventStampInterceptorTest {
   
    EventStampInterceptor interceptor = new EventStampInterceptor();
    TimeZoneRegistry registry =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    /**
     * Tests event stamp handler.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampHandler() throws Exception {
        
        HibNoteItem master = new HibNoteItem();
        HibBaseEventStamp eventStamp = new HibBaseEventStamp(master);
        eventStamp.createCalendar();
        eventStamp.setStartDate(new DateTime("20070212T074500"));
        eventStamp.setEndDate(new DateTime("20070212T094500"));
        master.addStamp(eventStamp);
        
        HibEventTimeRangeIndex index = interceptor.calculateEventStampIndexes(eventStamp);
        
        Assert.assertEquals("20070212T074500", index.getStartDate());
        Assert.assertEquals("20070212T094500", index.getEndDate());
        Assert.assertTrue(index.getIsFloating().booleanValue());
        
        TimeZone ctz = registry.getTimeZone("America/Chicago");
        DateTime start = new DateTime("20070212T074500", ctz);
        eventStamp.setStartDate(start);
        
        DateTime end = new DateTime("20070212T094500", ctz);
        eventStamp.setEndDate(end);
        
        String recur1 = "FREQ=DAILY;";
        
        ArrayList<Recur> recursList = new ArrayList<Recur>();
        if (recur1 != null) {
            for (String s : recur1.split(":")) {
                try {
                    recursList.add(new Recur(s));
                } catch (ParseException e) {
                   
                }
            }
        }
        
		List<Recur> recurs = recursList;
        eventStamp.setRecurrenceRules(recurs);
        
        index = interceptor.calculateEventStampIndexes(eventStamp);
        
        Assert.assertEquals("20070212T134500Z", index.getStartDate());
        Assert.assertEquals(HibBaseEventStamp.TIME_INFINITY, index.getEndDate());
        Assert.assertFalse(index.getIsFloating().booleanValue());
    }
}
