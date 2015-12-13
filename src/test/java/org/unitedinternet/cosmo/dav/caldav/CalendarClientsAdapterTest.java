/*
 * CalendarClientsAdapterTest.java Nov 14, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dav.caldav;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

public class CalendarClientsAdapterTest {
    
    /**
     * MKCALENDAR in icalIOS7 provides a timezone without prodid
     * @throws IOException
     * @throws ParserException
     * @throws ValidationException
     */
    @Test
    public void icalIOS7_missingTimezoneProductIdIsAdded() throws IOException, ParserException, ValidationException{
        Calendar calendar = new CalendarBuilder().build(new ByteArrayInputStream(geticalIOS7Calendar()));
        CalendarClientsAdapter.adaptTimezoneCalendarComponent(calendar);
        calendar.validate(true);//must not throw exceptions
        assertThat(calendar.getProductId().getValue(), is("UNKNOWN_PRODID"));
    }

    @Test
    public void timezoneProductIdNotOverriden() throws IOException, ParserException, ValidationException {
        Calendar calendar = new CalendarBuilder().build(new ByteArrayInputStream(geticalIOS7Calendar("test")));
        new CalendarClientsAdapter().adaptTimezoneCalendarComponent(calendar);
        calendar.validate(true);//must not throw exceptions
        assertThat(calendar.getProductId().getValue(), is("test"));
    }

    private byte[] geticalIOS7Calendar() {
        return geticalIOS7Calendar(null);
    }

    private byte[] geticalIOS7Calendar(String productId) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\n");
        sb.append("VERSION:2.0\n");
        sb.append("CALSCALE:GREGORIAN\n");
        if(productId != null) {
            sb.append("PRODID:" + productId + "\n");
        }
        sb.append("BEGIN:VTIMEZONE\n");
        sb.append("TZID:Europe/Bucharest\n");
        sb.append("BEGIN:DAYLIGHT\n");
        sb.append("TZOFFSETFROM:+0200\n");
        sb.append("TZNAME:GMT+3\n");
        sb.append("TZOFFSETTO:+0300\n");
        sb.append("RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n");
        sb.append("DTSTART:19970330T030000\n");
        sb.append("END:DAYLIGHT\n");
        sb.append("BEGIN:STANDARD\n");
        sb.append("TZOFFSETFROM:+0300\n");
        sb.append("TZNAME:GMT+2\n");
        sb.append("TZOFFSETTO:+0200\n");
        sb.append("RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n");
        sb.append("DTSTART:19971026T040000\n");
        sb.append("END:STANDARD\n");
        sb.append("END:VTIMEZONE\n");
        sb.append("END:VCALENDAR\n");
        return sb.toString().getBytes();
    }
}
