package org.unitedinternet.cosmo.calendar.data;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpandRecurringEventsTest {

    private static final String BASE_DIR = "src/test/resources/testdata/";

    @Test
    void testExpandEvent() throws Exception {
        try (var fis = new FileInputStream(BASE_DIR + "expand_recurr_test1.ics")) {
            var cb = new CalendarBuilder();
            var calendar = cb.build(fis);

            assertEquals(1, calendar.getComponents().getComponents("VEVENT").size());

            var vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
            var tz = new TimeZone(vtz);
            var filter = new OutputFilter("test");
            var start = new DateTime("20060102T140000", tz);
            var end = new DateTime("20060105T140000", tz);
            var period = new Period(start, end);
            var buffer = new StringBuffer();

            start.setUtc(true);
            end.setUtc(true);

            filter.setExpand(period);
            filter.setAllSubComponents();
            filter.setAllProperties();
            filter.filter(calendar, buffer);

            var sr = new StringReader(buffer.toString());
            var filterCal = cb.build(sr);
            var comps = filterCal.getComponents().getComponents("VEVENT");

            // Should expand to 3 event components
            assertEquals(3, comps.size());

            Iterator<VEvent> it = comps.iterator();
            var event = it.next();

            assertEquals("event 6", event.getProperties().getProperty(Property.SUMMARY).getValue());
            assertEquals("20060102T190000Z", event.getStartDate().getDate().toString());
            assertEquals("20060102T190000Z", event.getRecurrenceId().getDate().toString());

            event = it.next();
            assertEquals("event 6", event.getProperties().getProperty(Property.SUMMARY).getValue());
            assertEquals("20060103T190000Z", event.getStartDate().getDate().toString());
            assertEquals("20060103T190000Z", event.getRecurrenceId().getDate().toString());

            event = it.next();
            assertEquals("event 6", event.getProperties().getProperty(Property.SUMMARY).getValue());
            assertEquals("20060104T190000Z", event.getStartDate().getDate().toString());
            assertEquals("20060104T190000Z", event.getRecurrenceId().getDate().toString());

            verifyExpandedCalendar(filterCal);
        }

    }

    @Test
    void testExpandEventWithOverrides() throws Exception {
        try (var fis = new FileInputStream(BASE_DIR + "expand_recurr_test2.ics")) {
            var cb = new CalendarBuilder();
            var calendar = cb.build(fis);
            var comps = calendar.getComponents().getComponents("VEVENT");

            assertEquals(5, comps.size());

            var vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
            var tz = new TimeZone(vtz);
            var filter = new OutputFilter("test");
            var start = new DateTime("20060102T140000", tz);
            var end = new DateTime("20060105T140000", tz);
            var period = new Period(start, end);
            var buffer = new StringBuffer();

            start.setUtc(true);
            end.setUtc(true);

            filter.setExpand(period);
            filter.setAllSubComponents();
            filter.setAllProperties();
            filter.filter(calendar, buffer);

            var sr = new StringReader(buffer.toString());
            var filterCal = cb.build(sr);

            comps = filterCal.getComponents().getComponents("VEVENT");

            // Should expand to 3 event components
            assertEquals(3, comps.size());

            Iterator<VEvent> it = comps.iterator();
            var event = it.next();

            assertEquals("event 6", event.getProperties().getProperty(Property.SUMMARY).getValue());
            assertEquals("20060102T190000Z", event.getStartDate().getDate().toString());
            assertEquals("20060102T190000Z", event.getRecurrenceId().getDate().toString());

            event = it.next();
            assertEquals("event 6", event.getProperties().getProperty(Property.SUMMARY).getValue());
            assertEquals("20060103T190000Z", event.getStartDate().getDate().toString());
            assertEquals("20060103T190000Z", event.getRecurrenceId().getDate().toString());

            event = it.next();
            assertEquals("event 6 changed", event.getProperties().getProperty(Property.SUMMARY).getValue());
            assertEquals("20060104T210000Z", event.getStartDate().getDate().toString());
            assertEquals("20060104T190000Z", event.getRecurrenceId().getDate().toString());

            verifyExpandedCalendar(filterCal);
        }
    }

    @Test
    void removedTestExpandNonRecurringEvent() throws Exception {
        try (var fis = new FileInputStream(BASE_DIR + "expand_nonrecurr_test3.ics")) {
            var cb = new CalendarBuilder();
            var calendar = cb.build(fis);

            assertEquals(1, calendar.getComponents().getComponents("VEVENT").size());

            var vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
            var tz = new TimeZone(vtz);
            var filter = new OutputFilter("test");
            var start = new DateTime("20060102T140000", tz);
            var end = new DateTime("20060105T140000", tz);
            var period = new Period(start, end);
            var buffer = new StringBuffer();

            start.setUtc(true);
            end.setUtc(true);

            filter.setExpand(period);
            filter.setAllSubComponents();
            filter.setAllProperties();
            filter.filter(calendar, buffer);

            var sr = new StringReader(buffer.toString());
            var filterCal = cb.build(sr);

            var comps = filterCal.getComponents().getComponents("VEVENT");

            // Should be the same component
            assertEquals(1, comps.size());

            Iterator<VEvent> it = comps.iterator();
            var event = it.next();

            assertEquals("event 6", event.getProperties().getProperty(Property.SUMMARY).getValue());
            assertEquals("20060102T190000Z", event.getStartDate().getDate().toString());
            assertNull(event.getRecurrenceId());

            verifyExpandedCalendar(filterCal);
        }
    }

    private void verifyExpandedCalendar(Calendar calendar) {
        // timezone should be stripped
        assertNull(calendar.getComponents().getComponent("VTIMEZONE"));

        var comps = calendar.getComponents().getComponents("VEVENT");

        for (var event : (Iterable<VEvent>) comps) {
            var dt = (DateTime) event.getStartDate().getDate();

            // verify start dates are UTC
            assertNull(event.getStartDate().getParameters().getParameter(Parameter.TZID));
            assertTrue(dt.isUtc());

            // verify no recurrence rules
            assertNull(event.getProperties().getProperty(Property.RRULE));
        }
    }
}
