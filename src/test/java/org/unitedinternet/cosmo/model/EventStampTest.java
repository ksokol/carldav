/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateList;
import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.model.hibernate.HibBaseEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import java.io.FileInputStream;

public class EventStampTest {
   
    protected String baseDir = "src/test/resources/testdata/";

    @Test
    public void testExDates() throws Exception {
        HibNoteItem master = new HibNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        HibBaseEventStamp eventStamp = new HibBaseEventStamp(master);
        
        eventStamp.setEventCalendar(getCalendar("recurring_with_exdates.ics"));
        
        DateList exdates = eventStamp.getExceptionDates();
        
        Assert.assertNotNull(exdates);
        Assert.assertTrue(2==exdates.size());
        Assert.assertNotNull(exdates.getTimeZone());
    }

    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
}
