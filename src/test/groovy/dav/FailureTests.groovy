package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.textXmlContentType
import static testutil.mockmvc.CustomResultMatchers.xml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class FailureTests extends IntegrationTestSupport {

    @Test
    void malformedXmlBody() {
        def request1 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:pro
                        </C:calendar-multiget>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>Unknown error parsing request document</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .content(request1)
                .contentType(TEXT_XML))
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
                .andExpect(status().isBadRequest())
    }

    @Test
    void malformedVEvent() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        VERSION:2.0
                        PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x
                        BEGIN:VEVENT
                        DTSTAMP:20151230T185918Z
                        """.stripIndent()

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:valid-calendar-data>Failed to parse calendar object: Error at line 6:Unexpected end of file</C:valid-calendar-data>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(xml(response1))
                .andExpect(textXmlContentType())
                .andExpect(status().isBadRequest())
    }

    @Test
    void invalidContentTypeForCalendarQuery() {
        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                            <C:calendar-data/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VJOURNAL">
                                <C:prop-filter name="UID">
                                  <C:text-match collation="i;octet">20160114T072824Z-8357-1000-1795-3@localhost</C:text-match>
                                </C:prop-filter>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>Unknown error parsing request document</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType("application/json")
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    void addVTodoWithInvalidDtstamp() {
        def request1 = """\
                    BEGIN:VCALENDAR
                    VERSION:2.0
                    PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x
                    BEGIN:VTODO
                    DTSTAMP:20151231T115937
                    UID:6f490b02-77d7-442e-abd3-1e0bb14c3259
                    CREATED:20151231T115922Z
                    LAST-MODIFIED:20151231T115922Z
                    SUMMARY:add vtodo
                    STATUS:NEEDS-ACTION
                    END:VTODO
                    END:VCALENDAR
                    """.stripIndent()

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:valid-calendar-data>Invalid calendar object: DTSTAMP: DATE-TIME value must be specified in UTC time</C:valid-calendar-data>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(status().isBadRequest())
                .andExpect(xml(response1))
    }
}
