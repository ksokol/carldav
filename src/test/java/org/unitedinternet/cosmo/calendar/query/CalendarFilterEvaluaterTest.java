package org.unitedinternet.cosmo.calendar.query;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalendarFilterEvaluaterTest {

    @Test
    void testEvaluateFilterPropFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("cal1.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VEVENT");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        var propFilter = new PropertyFilter("SUMMARY");
        var textFilter = new TextMatchFilter("Visible");

        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("ViSiBle");
        textFilter.setCollation(TextMatchFilter.COLLATION_OCTET);
        assertFalse(evaluater.evaluate(calendar, filter));

        textFilter.setCollation(null);
        assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("XXX");
        textFilter.setNegateCondition(true);
        assertTrue(evaluater.evaluate(calendar, filter));

        propFilter.setTextMatchFilter(null);
        assertTrue(evaluater.evaluate(calendar, filter));

        propFilter.setName("RRULE");
        assertFalse(evaluater.evaluate(calendar, filter));

        propFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        assertTrue(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateFilterParamFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("cal1.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VEVENT");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        var propFilter = new PropertyFilter("DTSTART");
        var paramFilter = new ParamFilter("VALUE");
        var textFilter = new TextMatchFilter("DATE-TIME");

        paramFilter.setTextMatchFilter(textFilter);
        propFilter.getParamFilters().add(paramFilter);
        eventFilter.getPropFilters().add(propFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("XXX");
        assertFalse(evaluater.evaluate(calendar, filter));

        textFilter.setNegateCondition(true);
        assertTrue(evaluater.evaluate(calendar, filter));

        paramFilter.setTextMatchFilter(null);
        assertTrue(evaluater.evaluate(calendar, filter));

        paramFilter.setName("BOGUS");
        assertFalse(evaluater.evaluate(calendar, filter));

        paramFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        assertTrue(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateFilterEventTimeRangeFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("cal1.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VEVENT");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        var start = new DateTime("20050816T115000Z");
        var end = new DateTime("20050916T115000Z");
        var period = new Period(start, end);
        var timeRangeFilter = new TimeRangeFilter(period);

        eventFilter.setTimeRangeFilter(timeRangeFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        start = new DateTime("20050818T115000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateFilterRecurringEventTimeRangeFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("eventwithtimezone1.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VEVENT");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        var start = new DateTime("20070514T115000Z");
        var end = new DateTime("20070516T115000Z");
        var period = new Period(start, end);
        var timeRangeFilter = new TimeRangeFilter(period);

        eventFilter.setTimeRangeFilter(timeRangeFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        start = new DateTime("20070515T205000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateFilterPropertyTimeRangeFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("cal1.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VEVENT");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        var propFilter = new PropertyFilter("DTSTAMP");
        var start = new DateTime("20060517T115000Z");
        var end = new DateTime("20060717T115000Z");
        var period = new Period(start, end);
        var timeRangeFilter = new TimeRangeFilter(period);

        propFilter.setTimeRangeFilter(timeRangeFilter);
        eventFilter.getPropFilters().add(propFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        start = new DateTime("20060717T115000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateComplicated() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("cal1.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VEVENT");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        var start = new DateTime("20050816T115000Z");
        var end = new DateTime("20050916T115000Z");
        var period = new Period(start, end);
        var timeRangeFilter = new TimeRangeFilter(period);

        eventFilter.setTimeRangeFilter(timeRangeFilter);

        var propFilter = new PropertyFilter("SUMMARY");
        var textFilter = new TextMatchFilter("Visible");

        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        var propFilter2 = new PropertyFilter("DTSTART");
        var paramFilter2 = new ParamFilter("VALUE");
        var textFilter2 = new TextMatchFilter("DATE-TIME");

        paramFilter2.setTextMatchFilter(textFilter2);
        propFilter2.getParamFilters().add(paramFilter2);

        var period2 = new Period(start, end);
        var timeRangeFilter2 = new TimeRangeFilter(period2);

        propFilter2.setTimeRangeFilter(timeRangeFilter2);
        eventFilter.getPropFilters().add(propFilter2);

        assertTrue(evaluater.evaluate(calendar, filter));

        // change one thing
        paramFilter2.setName("XXX");
        assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateVAlarmFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("event_with_alarm.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");

        filter.setFilter(compFilter);

        var eventFilter = new ComponentFilter("VEVENT");
        var alarmFilter = new ComponentFilter("VALARM");
        var propFilter = new PropertyFilter("ACTION");
        var textMatch = new TextMatchFilter("AUDIO");

        propFilter.setTextMatchFilter(textMatch);
        compFilter.getComponentFilters().add(eventFilter);
        eventFilter.getComponentFilters().add(alarmFilter);
        alarmFilter.getPropFilters().add(propFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        textMatch.setValue("EMAIL");
        assertFalse(evaluater.evaluate(calendar, filter));

        alarmFilter.getPropFilters().clear();
        assertTrue(evaluater.evaluate(calendar, filter));

        // time-range filter on VALARM

        // find alarm relative to start
        var start = new DateTime("20060101T220000Z");
        var end = new DateTime("20060101T230000Z");
        var period = new Period(start, end);
        var timeRangeFilter = new TimeRangeFilter(period);

        alarmFilter.setTimeRangeFilter(timeRangeFilter);
        assertTrue(evaluater.evaluate(calendar, filter));

        // find alarm relative to end
        start = new DateTime("20060101T050000Z");
        end = new DateTime("20060101T190000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertTrue(evaluater.evaluate(calendar, filter));

        // find absolute repeating alarm
        start = new DateTime("20051230T050000Z");
        end = new DateTime("20051230T080000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertTrue(evaluater.evaluate(calendar, filter));

        // find no alarms
        start = new DateTime("20060101T020000Z");
        end = new DateTime("20060101T030000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertFalse(evaluater.evaluate(calendar, filter));

        alarmFilter.setTimeRangeFilter(null);
        alarmFilter.setIsNotDefinedFilter(new IsNotDefinedFilter());
        assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateFilterPropFilterAgainstException() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("event_with_exception.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VEVENT");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        var propFilter = new PropertyFilter("DESCRIPTION");

        eventFilter.getPropFilters().add(propFilter);

        assertTrue(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateVJournalFilterPropFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("vjournal.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VJOURNAL");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        var propFilter = new PropertyFilter("SUMMARY");
        var textFilter = new TextMatchFilter("Staff");

        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("bogus");
        assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateVToDoFilterPropFilter() throws Exception {
        var evaluater = new CalendarFilterEvaluater();
        var calendar = getCalendar("vtodo.ics");
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var eventFilter = new ComponentFilter("VTODO");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(eventFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        var propFilter = new PropertyFilter("SUMMARY");
        var textFilter = new TextMatchFilter("Income");

        propFilter.setTextMatchFilter(textFilter);
        eventFilter.getPropFilters().add(propFilter);

        assertTrue(evaluater.evaluate(calendar, filter));

        textFilter.setValue("bogus");
        assertFalse(evaluater.evaluate(calendar, filter));
    }

    @Test
    void testEvaluateVToDoTimeRangeFilter() throws Exception {
        var calendar1 = getCalendar("vtodo/vtodo.ics");
        var calendar2 = getCalendar("vtodo/vtodo_due_only.ics");
        var evaluater = new CalendarFilterEvaluater();
        var filter = new CalendarFilter();
        var compFilter = new ComponentFilter("VCALENDAR");
        var vtodoFilter = new ComponentFilter("VTODO");

        filter.setFilter(compFilter);
        compFilter.getComponentFilters().add(vtodoFilter);

        // Verify VTODO that has DTSTART matches
        var start = new DateTime("19970414T133000Z");
        var end = new DateTime("19970416T133000Z");
        var period = new Period(start, end);
        var timeRangeFilter = new TimeRangeFilter(period);

        vtodoFilter.setTimeRangeFilter(timeRangeFilter);

        assertTrue(evaluater.evaluate(calendar1, filter));

        // Verify VTODO that has DTSTART doesn't match
        start = new DateTime("19970420T133000Z");
        end = new DateTime("19970421T133000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertFalse(evaluater.evaluate(calendar1, filter));

        // Verify VTODO that has DUE doesn't match
        assertFalse(evaluater.evaluate(calendar2, filter));

        // Verify VTODO that has DUE matches
        start = new DateTime("20080401T133000Z");
        end = new DateTime("20080421T133000Z");
        period = new Period(start, end);
        timeRangeFilter.setPeriod(period);
        assertTrue(evaluater.evaluate(calendar2, filter));
    }

    @Test
    void testNotVCalendar() {
        var uut = new CalendarFilterEvaluater();

        var componentFilter = mock(ComponentFilter.class);
        var calendarFilter = mock(CalendarFilter.class);

        when(calendarFilter.getFilter()).thenReturn(componentFilter);
        when(componentFilter.getName()).thenReturn("NOT_VCALENDAR");

        uut.evaluate(null, calendarFilter);

        assertFalse(uut.evaluate(null, calendarFilter));
    }

    private Calendar getCalendar(String name) throws Exception {
        var cb = new CalendarBuilder();
        var in = getClass().getClassLoader().getResourceAsStream("testdata/" + name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return cb.build(in);
    }
}
