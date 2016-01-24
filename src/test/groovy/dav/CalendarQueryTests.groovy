package dav

import org.junit.Before
import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.ResultMatcher
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpHeaders.ETAG
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.etag
import static testutil.mockmvc.CustomResultMatchers.xml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class CalendarQueryTests extends IntegrationTestSupport {

    def currentEtag;

    @Before
    void setup() {
        def request1 = """\
                    BEGIN:VCALENDAR
                    VERSION:2.0
                    PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x
                    BEGIN:VTODO
                    DTSTAMP:20151231T115937Z
                    UID:6f490b02-77d7-442e-abd3-1e0bb14c3259
                    CREATED:20151231T115922Z
                    LAST-MODIFIED:20151231T115922Z
                    SUMMARY:add vtodo
                    STATUS:NEEDS-ACTION
                    END:VTODO
                    END:VCALENDAR
                    """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)
    }

    @Test
    void calendarDataAllProp() {
        def response1 = """\
                            BEGIN:VCALENDAR
                            VERSION:2.0&#13;
                            PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x&#13;
                            END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request("<allprop />"))
                .header("Depth", "1"))
                .andExpect(response(response1))
    }

    @Test
    void calendarDataAllComp() {
        def response1 = """\
                            BEGIN:VCALENDAR
                            BEGIN:VTODO&#13;
                            DTSTAMP:20151231T115937Z&#13;
                            UID:6f490b02-77d7-442e-abd3-1e0bb14c3259&#13;
                            CREATED:20151231T115922Z&#13;
                            LAST-MODIFIED:20151231T115922Z&#13;
                            SUMMARY:add vtodo&#13;
                            STATUS:NEEDS-ACTION&#13;
                            END:VTODO&#13;
                            END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request("<allcomp />"))
                .header("Depth", "1"))
                .andExpect(response(response1))
    }

    @Test
    void calendarDataVTodoUid() {
        def request1 = """\
                        <CAL:comp name="VTODO">
                            <prop name="UID" />
                        </CAL:comp>"""

        def response1 = """\
                        BEGIN:VCALENDAR
                        BEGIN:VTODO
                        UID:6f490b02-77d7-442e-abd3-1e0bb14c3259&#13;
                        END:VTODO
                        END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request1))
                .header("Depth", "1"))
                .andExpect(response(response1))
    }

    String request(String xmlFragment) {
        return """\
                        <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                            <prop>
                                <getetag/>
                                <CAL:calendar-data CAL:content-type="text/calendar" CAL:version="2.0">
                                    <CAL:comp name="VCALENDAR">
                                        ${xmlFragment}
                                    </CAL:comp>
                                </CAL:calendar-data>
                            </prop>
                            <CAL:filter>
                                <CAL:comp-filter name="VCALENDAR">
                                    <CAL:comp-filter name="VTODO"/>
                                </CAL:comp-filter>
                            </CAL:filter>
                        </CAL:calendar-query>"""
    }

    ResultMatcher response(String calendarData) {
        return xml("""\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEtag}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">${calendarData}
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>""")
    }
}
