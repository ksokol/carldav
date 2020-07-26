package org.unitedinternet.cosmo.calendar;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ICalendarUtilsTest {

    private static final TimeZoneRegistry TIMEZONE_REGISTRY = TimeZoneRegistryFactory.getInstance().createRegistry();

    @Test
    void testNormalizeUTCDateTimeToDate() throws Exception {
        var tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        var dt = new DateTime("20070201T070000Z");

        assertEquals("20070201", ICalendarUtils.normalizeUTCDateTimeToDate(dt, tz).toString());

        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        assertEquals("20070131", ICalendarUtils.normalizeUTCDateTimeToDate(dt, tz).toString());

        tz = TIMEZONE_REGISTRY.getTimeZone("Australia/Sydney");
        assertEquals("20070201", ICalendarUtils.normalizeUTCDateTimeToDate(dt, tz).toString());
    }

    @Test
    void testCompareDates() throws Exception {
        var tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");
        var dt = new DateTime("20070201T070000Z");
        var toTest = new Date("20070201");

        assertEquals(-1, ICalendarUtils.compareDates(toTest, dt, tz));

        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        assertEquals(1, ICalendarUtils.compareDates(toTest, dt, tz));
    }

    @Test
    void testPinFloatingTime() throws Exception {
        var tz1 = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");

        assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new Date("20070101"), tz1).toString());
        assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new DateTime("20070101T000000"), tz1).toString());

        var tz2 = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new Date("20070101"), tz1).toString());
        assertEquals("20070101T000000", ICalendarUtils.pinFloatingTime(new DateTime("20070101T000000"), tz1).toString());

        assertTrue(ICalendarUtils.pinFloatingTime(new Date("20070101"), tz1).before(ICalendarUtils.pinFloatingTime(new Date("20070101"), tz2)));
        assertTrue(ICalendarUtils.pinFloatingTime(new DateTime("20070101T000000"), tz1).before(ICalendarUtils.pinFloatingTime(new DateTime("20070101T000000"), tz2)));
    }

    @Test
    void testConvertToUTC() throws Exception {
        var tz = TIMEZONE_REGISTRY.getTimeZone("America/Chicago");

        assertEquals("20070101T060000Z", ICalendarUtils.convertToUTC(new Date("20070101"), tz).toString());
        assertEquals("20070101T160000Z", ICalendarUtils.convertToUTC(new DateTime("20070101T100000"), tz).toString());

        tz = TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles");
        assertEquals("20070101T080000Z", ICalendarUtils.convertToUTC(new Date("20070101"), tz).toString());
        assertEquals("20070101T180000Z", ICalendarUtils.convertToUTC(new DateTime("20070101T100000"), tz).toString());

    }
}
