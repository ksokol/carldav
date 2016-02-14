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
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Status;
import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.model.TriageStatusUtil;

import java.io.FileInputStream;

/**
 * Test EntityConverter.
 *
 */
public class EntityConverterTest {
    protected String baseDir = "src/test/resources/testdata/entityconverter/";

    protected EntityConverter converter = new EntityConverter();

    /**
     * Tests entity convertor task.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterTask() throws Exception {
        Calendar calendar = getCalendar("vtodo.ics");

        HibICalendarItem note = converter.convertTaskCalendar(calendar);

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
