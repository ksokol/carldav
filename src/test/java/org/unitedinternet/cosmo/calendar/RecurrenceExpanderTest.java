package org.unitedinternet.cosmo.calendar;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecurrenceExpanderTest {

    private static final TimeZoneRegistry TIMEZONE_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry();

    @Test
    void testRecurrenceExpanderAllDay() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("allday_recurring1.ics");
        var range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20070101", range[0].toString());
        assertEquals("20070120", range[1].toString());

        calendar = getCalendar("allday_recurring2.ics");

        range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20070101", range[0].toString());
        assertNull(range[1]);
    }

    @Test
    void testRecurrenceExpanderFloating() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("floating_recurring1.ics");
        var range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20070101T100000", range[0].toString());
        assertEquals("20070119T120000", range[1].toString());

        calendar = getCalendar("floating_recurring2.ics");
        range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20070101T100000", range[0].toString());
        assertNull(range[1]);
    }

    @Test
    void testRecurrenceExpanderWithExceptions() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("withExceptions.ics");
        var range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20131223", range[0].toString());
        assertEquals("20140111T075100Z", range[1].toString());
    }

    @Test
    void testRecurrenceExpanderTimezone() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("tz_recurring1.ics");
        var range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20070101T100000", range[0].toString());
        assertEquals("20070119T120000", range[1].toString());

        assertEquals("America/Chicago", ((DateTime) range[0]).getTimeZone().getID());
        assertEquals("America/Chicago", ((DateTime) range[1]).getTimeZone().getID());

        calendar = getCalendar("tz_recurring2.ics");

        range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20070101T100000", range[0].toString());
        assertNull(range[1]);

        assertEquals("America/Chicago", ((DateTime) range[0]).getTimeZone().getID());
    }

    @Test
    void testRecurrenceExpanderLongEvent() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("tz_recurring3.ics");
        var range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20070101T100000", range[0].toString());
        assertEquals("20091231T120000", range[1].toString());
    }

    @Test
    void testRecurrenceExpanderRDates() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("floating_recurring3.ics");
        var range = expander.calculateRecurrenceRange(calendar);

        assertEquals("20061212T100000", range[0].toString());
        assertEquals("20101212T120000", range[1].toString());
    }

    @Test
    void testRecurrenceExpanderSingleOccurrence() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("floating_recurring4.ics");
        var instances = expander.getOcurrences(calendar, new DateTime("20080101T100000"), new DateTime("20080101T100001"), null);

        assertEquals(1, instances.size());
    }

    @Test
    void testIsOccurrence() throws Exception {
        var expander = new RecurrenceExpander();
        var calendar = getCalendar("floating_recurring3.ics");

        assertTrue(expander.isOccurrence(calendar, new DateTime("20070102T100000")));
        assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T110000")));
        assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T100001")));

        // test DATE
        calendar = getCalendar("allday_recurring3.ics");

        assertTrue(expander.isOccurrence(calendar, new Date("20070101")));
        assertFalse(expander.isOccurrence(calendar, new Date("20070102")));
        assertTrue(expander.isOccurrence(calendar, new Date("20070108")));

        // test DATETIME with timezone
        calendar = getCalendar("tz_recurring3.ics");
        var ctz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");

        assertTrue(expander.isOccurrence(calendar, new DateTime("20070102T100000", ctz)));
        assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T110000", ctz)));
        assertFalse(expander.isOccurrence(calendar, new DateTime("20070102T100001", ctz)));
    }

    private Calendar getCalendar(String name) throws Exception {
        var cb = new CalendarBuilder();
        var in = getClass().getClassLoader().getResourceAsStream("testdata/expander/" + name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return cb.build(in);
    }
}
