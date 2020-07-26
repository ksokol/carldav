package org.unitedinternet.cosmo.calendar;

import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarQueryFilterTest {

    private static final String BASE_DIR = "src/test/resources/testdata/queries";

    @Test
    void testComponentFilterBasic() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/test1.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());

        var timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());
    }

    @Test
    void testIphoneios7bugNoEndDate() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/testIphoneios7bugNoEndDate.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());

        var timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20050902T000000Z", timeRange.getUTCEnd());
    }

    @Test
    void testComponentFilterIsNotDefined() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/test4.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getIsNotDefinedFilter());
    }

    @Test
    void testPropertyFilterBasic() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/test2.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());

        var timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        assertEquals(1, compFilter.getPropFilters().size());
        var propFilter = compFilter.getPropFilters().get(0);

        assertEquals("SUMMARY", propFilter.getName());
        var textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());
    }

    @Test
    void testPropertyFilterIsNotDefined() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/test5.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());

        var timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        assertEquals(1, compFilter.getPropFilters().size());
        var propFilter = compFilter.getPropFilters().get(0);

        assertEquals("SUMMARY", propFilter.getName());
        assertNotNull(propFilter.getIsNotDefinedFilter());
    }

    @Test
    void testParamFilterBasic() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/test3.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());

        var timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        assertEquals(1, compFilter.getPropFilters().size());
        var propFilter = compFilter.getPropFilters().get(0);

        assertEquals("SUMMARY", propFilter.getName());
        var textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());

        assertEquals(1, propFilter.getParamFilters().size());
        var paramFilter = propFilter.getParamFilters().get(0);
        assertEquals("PARAM1", paramFilter.getName());

        textMatch = paramFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("DEF", textMatch.getValue());
        assertTrue(textMatch.isCaseless());
    }

    @Test
    void testParamFilterIsNotDefined() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/test6.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());

        var timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        assertEquals(1, compFilter.getPropFilters().size());
        var propFilter = compFilter.getPropFilters().get(0);

        assertEquals("SUMMARY", propFilter.getName());
        var textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());

        assertEquals(1, propFilter.getParamFilters().size());
        var paramFilter = propFilter.getParamFilters().get(0);
        assertEquals("PARAM1", paramFilter.getName());

        assertNotNull(paramFilter.getIsNotDefinedFilter());
    }

    @Test
    void testMultiplePropFilters() throws Exception {
        var element = parseFile(new File(BASE_DIR + "/test7.xml"));
        var filter = new CalendarFilter(element);
        var compFilter = filter.getFilter();

        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());

        compFilter = compFilter.getComponentFilters().get(0);

        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());

        var timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());

        assertEquals(2, compFilter.getPropFilters().size());
        var propFilter = compFilter.getPropFilters().get(0);

        assertEquals("SUMMARY", propFilter.getName());
        var textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());

        propFilter = compFilter.getPropFilters().get(1);
        assertEquals("DESCRIPTION", propFilter.getName());
        assertNotNull(propFilter.getIsNotDefinedFilter());
    }

    @Test
    void testComponentFilterError() {
        assertThrows(
                ParseException.class,
                () -> new CalendarFilter(parseFile(new File(BASE_DIR + "/error-test4.xml")))
        );

        assertThrows(
                ParseException.class,
                () -> new CalendarFilter(parseFile(new File(BASE_DIR + "/error-test5.xml")))
        );

        assertThrows(
                ParseException.class,
                () -> new CalendarFilter(parseFile(new File(BASE_DIR + "/error-test6.xml")))
        );

        assertThrows(
                ParseException.class,
                () -> new CalendarFilter(parseFile(new File(BASE_DIR + "/error-test7.xml")))
        );

        assertThrows(
                ParseException.class,
                () -> new CalendarFilter(parseFile(new File(BASE_DIR + "/error-test8.xml")))
        );
    }

    @Test
    void testPropertyFilterError() {
        assertThrows(
                ParseException.class,
                () -> new CalendarFilter(parseFile(new File(BASE_DIR + "/error-test9.xml")))
        );
    }

    @Test
    void testParamFilterError() {
        assertThrows(
                ParseException.class,
                () -> new CalendarFilter(parseFile(new File(BASE_DIR + "/error-test10.xml")))
        );
    }

    private Element parseFile(File file) throws Exception {
        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        var db = dbf.newDocumentBuilder();
        var dom = db.parse(file);
        return (Element) dom.getFirstChild();
    }
}
