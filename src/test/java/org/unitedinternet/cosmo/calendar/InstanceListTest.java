package org.unitedinternet.cosmo.calendar;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstanceListTest {

    private static final TimeZoneRegistry TIMEZONE_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry();

    @Test
    void testFloatingRecurring() throws Exception {
        var calendar = getCalendar("floating_recurr_event.ics");
        var instances = new InstanceList();
        var start = new DateTime("20060101T140000");
        var end = new DateTime("20060108T140000");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(5, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20060102T140000", key);
        assertEquals("20060102T140000", instance.getStart().toString());
        assertEquals("20060102T150000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060103T140000", key);
        assertEquals("20060103T140000", instance.getStart().toString());
        assertEquals("20060103T150000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060104T140000", key);
        assertEquals("20060104T160000", instance.getStart().toString());
        assertEquals("20060104T170000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060105T140000", key);
        assertEquals("20060105T160000", instance.getStart().toString());
        assertEquals("20060105T170000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060106T140000", key);
        assertEquals("20060106T140000", instance.getStart().toString());
        assertEquals("20060106T150000", instance.getEnd().toString());
    }

    @Test
    void testUTCInstanceList() throws Exception {
        var calendar = getCalendar("floating_recurr_event.ics");
        var tz = TIMEZONE_REGISTRY.getTimeZone("America/New_York");
        var instances = new InstanceList();

        instances.setUTC(true);
        instances.setTimezone(tz);

        var start = new DateTime("20060101T190000Z");
        var end = new DateTime("20060108T190000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(5, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20060102T190000Z", key);
        assertEquals("20060102T190000Z", instance.getStart().toString());
        assertEquals("20060102T200000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060103T190000Z", key);
        assertEquals("20060103T190000Z", instance.getStart().toString());
        assertEquals("20060103T200000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060104T190000Z", key);
        assertEquals("20060104T210000Z", instance.getStart().toString());
        assertEquals("20060104T220000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060105T190000Z", key);
        assertEquals("20060105T210000Z", instance.getStart().toString());
        assertEquals("20060105T220000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060106T190000Z", key);
        assertEquals("20060106T190000Z", instance.getStart().toString());
        assertEquals("20060106T200000Z", instance.getEnd().toString());
    }

    @Test
    void testUTCInstanceListAllDayEvent() throws Exception {
        var calendar = getCalendar("allday_weekly_recurring.ics");
        var instances = new InstanceList();

        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));

        var start = new DateTime("20070103T090000Z");
        var end = new DateTime("20070117T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(2, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070108T060000Z", key);
        assertEquals("20070108T060000Z", instance.getStart().toString());
        assertEquals("20070109T060000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070115T060000Z", key);
        assertEquals("20070115T060000Z", instance.getStart().toString());
        assertEquals("20070116T060000Z", instance.getEnd().toString());
    }

    @Test
    void testUTCInstanceListAllDayWithExDates() throws Exception {
        var calendar = getCalendar("allday_recurring_with_exdates.ics");
        var instances = new InstanceList();

        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));

        var start = new DateTime("20070101T090000Z");
        var end = new DateTime("20070106T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(3, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070101T060000Z", key);
        assertEquals("20070101T060000Z", instance.getStart().toString());
        assertEquals("20070102T060000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070102T060000Z", key);
        assertEquals("20070102T060000Z", instance.getStart().toString());
        assertEquals("20070103T060000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070106T060000Z", key);
        assertEquals("20070106T060000Z", instance.getStart().toString());
        assertEquals("20070107T060000Z", instance.getEnd().toString());
    }

    @Test
    void testUTCInstanceListAllDayEventWithMods() throws Exception {
        var calendar = getCalendar("allday_weekly_recurring_with_mods.ics");
        var instances = new InstanceList();

        instances.setUTC(true);
        instances.setTimezone(TIMEZONE_REGISTRY.getTimeZone("America/Chicago"));

        var start = new DateTime("20070101T090000Z");
        var end = new DateTime("20070109T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(2, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070101T060000Z", key);
        assertEquals("20070101T060000Z", instance.getStart().toString());
        assertEquals("20070102T060000Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070108T060000Z", key);
        assertEquals("20070109T060000Z", instance.getStart().toString());
        assertEquals("20070110T060000Z", instance.getEnd().toString());
    }

    @Test
    void testInstanceListInstanceBeforeStartRange() throws Exception {
        var calendar = getCalendar("eventwithtimezone3.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070509T090000Z");
        var end = new DateTime("20070511T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(3, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070509T081500Z", key);
        assertEquals("20070509T031500", instance.getStart().toString());
        assertEquals("20070509T041500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070510T081500Z", key);
        assertEquals("20070510T031500", instance.getStart().toString());
        assertEquals("20070510T041500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070511T081500Z", key);
        assertEquals("20070511T031500", instance.getStart().toString());
        assertEquals("20070511T041500", instance.getEnd().toString());
    }

    @Test
    void testFloatingWithSwitchingTimezoneInstanceList() throws Exception {
        var calendar = getCalendar("floating_recurr_event.ics");
        var tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        var instances = new InstanceList();

        instances.setTimezone(tz);

        var start = new DateTime("20060102T220000Z");
        var end = new DateTime("20060108T190000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(5, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20060102T220000Z", key);
        assertEquals("20060102T140000", instance.getStart().toString());
        assertEquals("20060102T150000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060103T220000Z", key);
        assertEquals("20060103T140000", instance.getStart().toString());
        assertEquals("20060103T150000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060104T220000Z", key);
        assertEquals("20060104T160000", instance.getStart().toString());
        assertEquals("20060104T170000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060105T220000Z", key);
        assertEquals("20060105T160000", instance.getStart().toString());
        assertEquals("20060105T170000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20060106T220000Z", key);
        assertEquals("20060106T140000", instance.getStart().toString());
        assertEquals("20060106T150000", instance.getEnd().toString());
    }

    @Test
    void testExdateWithTimezone() throws Exception {
        var calendar = getCalendar("recurring_with_exdates.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070509T090000Z");
        var end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(2, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070529T101500Z", key);
        assertEquals("20070529T051500", instance.getStart().toString());
        assertEquals("20070529T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070605T101500Z", key);
        assertEquals("20070605T051500", instance.getStart().toString());
        assertEquals("20070605T061500", instance.getEnd().toString());
    }

    @Test
    void testExdateUtc() throws Exception {
        var calendar = getCalendar("recurring_with_exdates_utc.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070509T090000Z");
        var end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(2, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070529T101500Z", key);
        assertEquals("20070529T051500", instance.getStart().toString());
        assertEquals("20070529T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070605T101500Z", key);
        assertEquals("20070605T051500", instance.getStart().toString());
        assertEquals("20070605T061500", instance.getEnd().toString());
    }

    @Test
    void testExdateNoTimezone() throws Exception {
        var calendar = getCalendar("recurring_with_exdates_floating.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070509T040000");
        var end = new DateTime("20070609T040000");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(2, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070529T051500", key);
        assertEquals("20070529T051500", instance.getStart().toString());
        assertEquals("20070529T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070605T051500", key);
        assertEquals("20070605T051500", instance.getStart().toString());
        assertEquals("20070605T061500", instance.getEnd().toString());
    }

    @Test
    void testRdateWithTimezone() throws Exception {
        var calendar = getCalendar("recurring_with_rdates.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070509T090000Z");
        var end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(7, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070515T101500Z", key);
        assertEquals("20070515T051500", instance.getStart().toString());
        assertEquals("20070515T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070516T101500Z", key);
        assertEquals("20070516T051500", instance.getStart().toString());
        assertEquals("20070516T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070517T101500Z", key);
        assertEquals("20070517T101500Z", instance.getStart().toString());
        assertEquals("20070517T131500Z", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070522T101500Z", key);
        assertEquals("20070522T051500", instance.getStart().toString());
        assertEquals("20070522T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070523T101500Z", key);
        assertEquals("20070523T051500", instance.getStart().toString());
        assertEquals("20070523T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070529T101500Z", key);
        assertEquals("20070529T051500", instance.getStart().toString());
        assertEquals("20070529T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070605T101500Z", key);
        assertEquals("20070605T051500", instance.getStart().toString());
        assertEquals("20070605T061500", instance.getEnd().toString());
    }

    @Test
    void testExruleWithTimezone() throws Exception {
        var calendar = getCalendar("recurring_with_exrule.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070509T090000Z");
        var end = new DateTime("20070609T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(2, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070515T101500Z", key);
        assertEquals("20070515T051500", instance.getStart().toString());
        assertEquals("20070515T061500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070529T101500Z", key);
        assertEquals("20070529T051500", instance.getStart().toString());
        assertEquals("20070529T061500", instance.getEnd().toString());
    }

    @Test
    void testAllDayRecurring() throws Exception {
        var calendar = getCalendar("allday_recurring.ics");
        var instances = new InstanceList();

        // need to normalize to local timezone to get test to pass in mutliple timezones
        var start = new DateTime(new Date("20070101").getTime() + 1000 * 60);
        var end = new DateTime(new Date("20070103").getTime() + 1000 * 60);

        start.setUtc(true);
        end.setUtc(true);

        addToInstanceList(calendar, instances, start, end);

        assertEquals(3, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070101", key);
        assertEquals("20070101", instance.getStart().toString());
        assertEquals("20070102", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070102", key);
        assertEquals("20070102", instance.getStart().toString());
        assertEquals("20070103", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070103", key);
        assertEquals("20070103", instance.getStart().toString());
        assertEquals("20070104", instance.getEnd().toString());
    }

    @Test
    void testAllDayRecurringWithExDates() throws Exception {
        var calendar = getCalendar("allday_recurring_with_exdates.ics");
        var instances = new InstanceList();
        var tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");

        instances.setTimezone(tz);

        var start = new DateTime("20070101T090000Z");
        var end = new DateTime("20070106T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(3, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070101", key);
        assertEquals("20070101", instance.getStart().toString());
        assertEquals("20070102", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070102", key);
        assertEquals("20070102", instance.getStart().toString());
        assertEquals("20070103", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070106", key);
        assertEquals("20070106", instance.getStart().toString());
        assertEquals("20070107", instance.getEnd().toString());
    }

    @Test
    void testAllDayRecurringWithMods() throws Exception {
        var calendar = getCalendar("allday_weekly_recurring_with_mods.ics");
        var instances = new InstanceList();
        var tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");

        instances.setTimezone(tz);

        var start = new DateTime("20070101T090000Z");
        var end = new DateTime("20070109T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(2, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070101", key);
        assertEquals("20070101", instance.getStart().toString());
        assertEquals("20070102", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070108", key);
        assertEquals("20070109", instance.getStart().toString());
        assertEquals("20070110", instance.getEnd().toString());
    }

    @Test
    void testAllDayRecurringWithTimeZone() throws Exception {
        var calendar = getCalendar("allday_recurring.ics");
        var instances = new InstanceList();
        var tz = TIMEZONE_REGISTRY.getTimeZone("Australia/Sydney");

        instances.setTimezone(tz);

        var start = new DateTime("20070102T140000Z");
        var end = new DateTime("20070104T140000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(3, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070103", key);
        assertEquals("20070103", instance.getStart().toString());
        assertEquals("20070104", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070104", key);
        assertEquals("20070104", instance.getStart().toString());
        assertEquals("20070105", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070105", key);
        assertEquals("20070105", instance.getStart().toString());
        assertEquals("20070106", instance.getEnd().toString());
    }

    @Test
    void testInstanceStartBeforeRange() throws Exception {
        var calendar = getCalendar("recurring_with_exdates.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070529T110000Z");
        var end = new DateTime("20070530T051500Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(1, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070529T101500Z", key);
        assertEquals("20070529T051500", instance.getStart().toString());
        assertEquals("20070529T061500", instance.getEnd().toString());
    }

    @Test
    void testComplicatedRecurringWithTimezone() throws Exception {
        var calendar = getCalendar("complicated_recurring.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070101T090000Z");
        var end = new DateTime("20070201T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(4, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070102T161500Z", key);
        assertEquals("20070102T101500", instance.getStart().toString());
        assertEquals("20070102T111500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070104T161500Z", key);
        assertEquals("20070104T101500", instance.getStart().toString());
        assertEquals("20070104T111500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070116T161500Z", key);
        assertEquals("20070116T101500", instance.getStart().toString());
        assertEquals("20070116T111500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070118T161500Z", key);
        assertEquals("20070118T101500", instance.getStart().toString());
        assertEquals("20070118T111500", instance.getEnd().toString());
    }

    @Test
    void testComplicatedRecurringAllDay() throws Exception {
        var calendar = getCalendar("complicated_allday_recurring.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070101T090000Z");
        var end = new DateTime("20071201T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(5, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070105", key);
        assertEquals("20070105", instance.getStart().toString());
        assertEquals("20070106", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070202", key);
        assertEquals("20070202", instance.getStart().toString());
        assertEquals("20070203", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070302", key);
        assertEquals("20070302", instance.getStart().toString());
        assertEquals("20070303", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070406", key);
        assertEquals("20070406", instance.getStart().toString());
        assertEquals("20070407", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070504", key);
        assertEquals("20070504", instance.getStart().toString());
        assertEquals("20070505", instance.getEnd().toString());
    }

    @Test
    void testRecurringWithUntil() throws Exception {
        var calendar = getCalendar("recurring_until.ics");
        var instances = new InstanceList();
        var start = new DateTime("20070101T090000Z");
        var end = new DateTime("20070201T090000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(3, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20070102T161500Z", key);
        assertEquals("20070102T101500", instance.getStart().toString());
        assertEquals("20070102T111500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070103T161500Z", key);
        assertEquals("20070103T101500", instance.getStart().toString());
        assertEquals("20070103T111500", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20070104T161500Z", key);
        assertEquals("20070104T101500", instance.getStart().toString());
        assertEquals("20070104T111500", instance.getEnd().toString());
    }

    @Test
    void testRecurrenceExpanderByDay() throws Exception {
        var calendar = getCalendar("recurring_by_day.ics");
        var instances = new InstanceList();
        var start = new DateTime("20080720T170000Z");
        var end = new DateTime("20080726T200000Z");

        addToInstanceList(calendar, instances, start, end);

        assertEquals(5, instances.size());

        var entrySets = instances.entrySet().iterator();
        var nextEntry = entrySets.next();
        var key = nextEntry.getKey();
        var instance = nextEntry.getValue();

        assertEquals("20080721T180000Z", key);
        assertEquals("20080721T110000", instance.getStart().toString());
        assertEquals("20080721T113000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20080722T180000Z", key);
        assertEquals("20080722T110000", instance.getStart().toString());
        assertEquals("20080722T113000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20080723T180000Z", key);
        assertEquals("20080723T110000", instance.getStart().toString());
        assertEquals("20080723T113000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20080724T180000Z", key);
        assertEquals("20080724T110000", instance.getStart().toString());
        assertEquals("20080724T113000", instance.getEnd().toString());

        nextEntry = entrySets.next();
        key = nextEntry.getKey();
        instance = nextEntry.getValue();

        assertEquals("20080725T180000Z", key);
        assertEquals("20080725T110000", instance.getStart().toString());
        assertEquals("20080725T113000", instance.getEnd().toString());
    }

    private static void addToInstanceList(Calendar calendar, InstanceList instances, Date start, Date end) {
        ComponentList vevents = calendar.getComponents().getComponents(VEvent.VEVENT);
        Iterator<VEvent> it = vevents.iterator();
        boolean addedMaster = false;
        while (it.hasNext()) {
            VEvent event = it.next();
            if (event.getRecurrenceId() == null) {
                addedMaster = true;
                instances.addComponent(event, start, end);
            } else {
                assertTrue(addedMaster);
                instances.addOverride(event, start, end);
            }
        }
    }

    private Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream("testdata/instancelist/" + name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return cb.build(in);
    }
}
