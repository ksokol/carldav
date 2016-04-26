package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static calendar.DavDroidData.ADD_VEVENT_REQUEST1
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
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
                                <cosmo:bad-request>Element type "D:pro" must be followed by either attribute specifications, "&gt;" or "/&gt;".</cosmo:bad-request>
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
                                <cosmo:unsupported-media-type>Expected Content-Type application/xml or text/xml</cosmo:unsupported-media-type>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType("application/json")
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isUnsupportedMediaType())
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
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    void calendarQueryWithInvalidCollation() {
        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:prop-filter name="UID">
                                  <C:text-match collation="i;unknown">20151230T132406Z-27136-1000-3483-35_localhost</C:text-match>
                                </C:prop-filter>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:supported-collation>Collation must be one of i;ascii-casemap, i;octet</C:supported-collation>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }


    @Test
    void addUnknownCalendarComponent() {
        def request1 = """\
                    BEGIN:VCALENDAR
                    VERSION:2.0
                    PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x
                    BEGIN:VUNKNOWN
                    DTSTAMP:20151231T115937Z
                    UID:6f490b02-77d7-442e-abd3-1e0bb14c3259
                    CREATED:20151231T115922Z
                    LAST-MODIFIED:20151231T115922Z
                    SUMMARY:add vtodo
                    STATUS:NEEDS-ACTION
                    END:VUNKNOWN
                    END:VCALENDAR
                    """.stripIndent()

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:supported-calendar-component>Calendar object must contain at least one of VEVENT, VTODO, VJOURNAL</C:supported-calendar-component>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    void calendarQueryWithInvalidFilter() {
        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                            <C:calendar-data/>
                          </D:prop>
                          <C:filter />
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:valid-filter>CALDAV:filter must contain a comp-filter</C:valid-filter>
                             </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    void getWithXmlBody() {
        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                            <C:calendar-data/>
                          </D:prop>
                          <C:filter />
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:unsupported-media-type>Body not expected for method GET</cosmo:unsupported-media-type>
                            </D:error>"""

        mockMvc.perform(get("/dav/{email}/calendar", USER01)
                .contentType(APPLICATION_XML)
                .content(request1))
                .andExpect(xml(response1))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(textXmlContentType())
    }

    @Test
    void deleteWithXmlBody() {
         mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1))
                .andExpect(status().isCreated())

        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                            <C:calendar-data/>
                          </D:prop>
                          <C:filter />
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:unsupported-media-type>Body not expected for method DELETE</cosmo:unsupported-media-type>
                            </D:error>"""

        mockMvc.perform(delete("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(APPLICATION_XML)
                .content(request1))
                .andExpect(xml(response1))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(textXmlContentType())
    }

    @Test
    void deleteWithMalformedXmlBody() {
        mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())

        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <C:calendar-data/
                          </D:prop>
                          <C:filter />
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>Element type "C:calendar-data" must be followed by either attribute specifications, ">" or "/>".</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(delete("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(APPLICATION_XML)
                .content(request1))
                .andExpect(xml(response1))
                .andExpect(status().isBadRequest())
                .andExpect(textXmlContentType())
    }

    @Test
    void calendarDataQueryWithWrongVersion() {
        def request1 = """\
                        <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                            <prop>
                                <CAL:calendar-data CAL:content-type="text/calendar" CAL:version="42.0" />
                            </prop>
                        </CAL:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:supported-calendar-data>Calendar data must be of media type text/calendar, version 2.0</C:supported-calendar-data>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(xml(response1))
                .andExpect(textXmlContentType())
    }

    @Test
    void calendarDataQueryWithWrongContentType() {
        def request1 = """\
                        <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                            <prop>
                                <CAL:calendar-data CAL:content-type="text/card" CAL:version="2.0" />
                            </prop>
                        </CAL:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:supported-calendar-data>Calendar data of type text/card not allowed; only text/calendar or text/vcard</C:supported-calendar-data>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(xml(response1))
                .andExpect(textXmlContentType())
    }

    @Test
    void calendarDataQueryWithInvalidTimeRanges() {
        def request = { attributes ->
            """\
                <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                    <prop>
                        <CAL:calendar-data CAL:content-type="text/calendar" CAL:version="2.0">
                            <CAL:expand ${attributes} />
                        </CAL:calendar-data>
                    </prop>
                </CAL:calendar-query>"""
        }

        def response = { message ->
            xml("""\
                <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                    <cosmo:bad-request>${message}</cosmo:bad-request>
                </D:error>""")
        }

        def assertTimerange = { attributes, message ->
            mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                    .contentType(APPLICATION_XML)
                    .content(request(attributes)))
                    .andExpect(status().isBadRequest())
                    .andExpect(response(message))
                    .andExpect(textXmlContentType())
        }

        assertTimerange("", "Expected timerange attribute start")
        assertTimerange('start="1"', 'Timerange start not parseable: Unparseable date: "1"')
        assertTimerange('start="20160132T115937Z"', 'Timerange start must be UTC')
        assertTimerange('start="20160119T115937Z"', 'Expected timerange attribute end')
        assertTimerange('start="20160119T115937Z" end="1"', 'Timerange end not parseable: Unparseable date: "1"')
        assertTimerange('start="20160119T115937Z" end="20160132T115937Z"', 'Timerange end must be UTC')
    }

    @Test
    void calendarDataDuplicatedComp() {
        def request1 = """\
                <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                    <prop>
                        <CAL:calendar-data CAL:content-type="text/calendar" CAL:version="2.0">
                            <CAL:comp name="VCALENDAR" />
                            <CAL:comp name="VCALENDAR" />
                        </CAL:calendar-data>
                    </prop>
                </CAL:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:internal-server-error>only one top-level component supported</cosmo:internal-server-error>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1))
                .andExpect(xml(response1))
                .andExpect(status().isInternalServerError())
    }

    @Test
    void calendarDataInvalidCompName() {
        def request1 = """\
                <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                    <prop>
                        <CAL:calendar-data CAL:content-type="text/calendar" CAL:version="2.0">
                            <CAL:comp name="UNKNOWN" />
                        </CAL:calendar-data>
                    </prop>
                </CAL:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:internal-server-error>only top-level comp name VCALENDAR supported</cosmo:internal-server-error>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1))
                .andExpect(xml(response1))
                .andExpect(status().isInternalServerError())
    }

    @Test
    void textMatchWrongCollation() {
        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:prop-filter name="UID">
                                  <C:text-match collation="i;unknown">e94d89d2-b195-4128-a9a8-be83a873deae</C:text-match>
                                </C:prop-filter>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:supported-collation>Collation must be one of i;ascii-casemap, i;octet</C:supported-collation>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isPreconditionFailed())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    public void multipleCompFilter() {
        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR" />
                            <C:comp-filter name="IRRELEVANT" />
                          </C:filter>
                        </C:calendar-query>"""

        def response1 = """\
                            <D:error xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <C:valid-filter>CALDAV:filter can contain only one comp-filter</C:valid-filter>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    public void missingHrefInCalendarMultiget() {
        def request1 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                            </D:prop>
                        </C:calendar-multiget>"""

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>Expected at least one href</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .content(request1)
                .contentType(TEXT_XML))
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }
}
