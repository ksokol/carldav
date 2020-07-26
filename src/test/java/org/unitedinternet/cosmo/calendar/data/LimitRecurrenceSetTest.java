package org.unitedinternet.cosmo.calendar.data;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class LimitRecurrenceSetTest {

    private static final String BASE_DIR = "src/test/resources/testdata/";

    @Test
    void testLimitRecurrenceSet() throws Exception {
        try (var fis = new FileInputStream(BASE_DIR + "limit_recurr_test.ics")) {
            var cb = new CalendarBuilder();
            var calendar = cb.build(fis);

            assertEquals(5, calendar.getComponents().getComponents("VEVENT").size());

            var vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
            var tz = new TimeZone(vtz);
            var filter = new OutputFilter("test");
            var start = new DateTime("20060104T010000", tz);
            var end = new DateTime("20060106T010000", tz);
            var period = new Period(start, end);
            var buffer = new StringBuffer();

            start.setUtc(true);
            end.setUtc(true);

            filter.setLimit(period);
            filter.setAllSubComponents();
            filter.setAllProperties();
            filter.filter(calendar, buffer);

            var sr = new StringReader(buffer.toString());
            var filterCal = cb.build(sr);

            var comps = filterCal.getComponents();
            assertEquals(3, comps.getComponents("VEVENT").size());
            assertEquals(1, comps.getComponents("VTIMEZONE").size());

            // Make sure 3rd and 4th override are dropped
            for (var c : (Iterable<Component>) comps.getComponents("VEVENT")) {
                assertNotSame("event 6 changed 3", c.getProperties().getProperty("SUMMARY").getValue());
                assertNotSame("event 6 changed 4", c.getProperties().getProperty("SUMMARY").getValue());
            }
        }
    }

    @Test
    void testLimitFloatingRecurrenceSet() throws Exception {
        try (var fis = new FileInputStream(BASE_DIR + "limit_recurr_float_test.ics")) {
            var cb = new CalendarBuilder();
            var calendar = cb.build(fis);

            assertEquals(3, calendar.getComponents().getComponents("VEVENT").size());

            var filter = new OutputFilter("test");
            var start = new DateTime("20060102T170000");
            var end = new DateTime("20060104T170000");
            var period = new Period(start, end);
            var buffer = new StringBuffer();

            start.setUtc(true);
            end.setUtc(true);

            filter.setLimit(period);
            filter.setAllSubComponents();
            filter.setAllProperties();
            filter.filter(calendar, buffer);

            var sr = new StringReader(buffer.toString());
            var filterCal = cb.build(sr);

            assertEquals(2, filterCal.getComponents().getComponents("VEVENT").size());
            // Make sure 2nd override is dropped
            var vevents = filterCal.getComponents().getComponents(VEvent.VEVENT);
            for (var c : (Iterable<Component>) vevents) {
                assertNotSame("event 6 changed 2", c.getProperties().getProperty("SUMMARY").getValue());
            }
        }
    }

    @Test
    void testLimitRecurrenceSetThisAndFuture() throws Exception {
        try (var fis = new FileInputStream(BASE_DIR + "limit_recurr_taf_test.ics")) {
            var cb = new CalendarBuilder();
            var calendar = cb.build(fis);

            assertEquals(4, calendar.getComponents().getComponents("VEVENT").size());

            var vtz = (VTimeZone) calendar.getComponents().getComponent("VTIMEZONE");
            var tz = new TimeZone(vtz);
            var filter = new OutputFilter("test");
            var start = new DateTime("20060108T170000", tz);
            var end = new DateTime("20060109T170000", tz);
            var period = new Period(start, end);
            var buffer = new StringBuffer();

            start.setUtc(true);
            end.setUtc(true);

            filter.setLimit(period);
            filter.setAllSubComponents();
            filter.setAllProperties();
            filter.filter(calendar, buffer);

            var sr = new StringReader(buffer.toString());
            var filterCal = cb.build(sr);

            assertEquals(2, filterCal.getComponents().getComponents("VEVENT").size());
            // Make sure 2nd and 3rd override are dropped
            var vevents = filterCal.getComponents().getComponents(VEvent.VEVENT);
            for (var c : (Iterable<Component>) vevents) {
                assertNotSame("event 6 changed", c.getProperties().getProperty("SUMMARY").getValue());
                assertNotSame("event 6 changed 2", c.getProperties().getProperty("SUMMARY").getValue());
            }
        }
    }
}
