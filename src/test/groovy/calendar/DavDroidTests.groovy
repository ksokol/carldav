package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.helper.XmlHelper

import static calendar.DavDroidData.ADD_AND_UPDATE_VEVENT_REQUEST4
import static calendar.DavDroidData.ADD_VEVENT_REQUEST1
import static com.google.common.net.HttpHeaders.AUTHORIZATION
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.springframework.http.HttpHeaders.*
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.TestUser.USER01_PASSWORD
import static testutil.helper.Base64Helper.user
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomMediaTypes.TEXT_VCARD
import static testutil.mockmvc.CustomRequestBuilders.propfind
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.*
import static testutil.xmlunit.XmlMatcher.equalXml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class DavDroidTests extends IntegrationTestSupport {

    def currentEtag

    @Test
    public void fetchingEmptyCalendarFirstTime() {
        def request1 = """\
                            <propfind xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                <prop>
                                    <CARD:addressbook-home-set/>
                                    <resourcetype/>
                                    <displayname/>
                                    <CARD:addressbook-description/>
                                    <current-user-privilege-set/>
                                    <current-user-principal/>
                                </prop>
                            </propfind>"""

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:current-user-privilege-set/>
                                            <D:current-user-principal/>
                                            <CARD:addressbook-description xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:displayname>calendarDisplayName</D:displayname>
                                            <CARD:addressbook-home-set xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                                <D:href>/dav/test01@localhost.de/contacts</D:href>
                                            </CARD:addressbook-home-set>
                                            <D:resourcetype>
                                                <D:collection/>
                                                <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            </D:resourcetype>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/calendar", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        mockMvc.perform(options("/dav/{email}/calendar", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, addressbook, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PUT, DELETE, REPORT"))

        def request2 = """\
                        <propfind xmlns="DAV:">
                            <prop>
                                <current-user-principal/>
                            </prop>
                        </propfind>"""

        mockMvc.perform(propfind("/.well-known/carddav")
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "0"))
                .andExpect(status().isUnauthorized())

        def request3 = """\
                        <propfind xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                            <prop>
                                <CAL:calendar-home-set/>
                                <CAL:supported-calendar-component-set/>
                                <resourcetype/>
                                <displayname/>
                                <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                <CAL:calendar-description/>
                                <CAL:calendar-timezone/>
                                <current-user-privilege-set/>
                                <current-user-principal/>
                            </prop>
                        </propfind>"""

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:current-user-privilege-set/>
                                            <C:calendar-home-set xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            <D:current-user-principal/>
                                            <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                            <C:calendar-timezone xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            <C:calendar-description xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:displayname>calendarDisplayName</D:displayname>
                                            <D:resourcetype>
                                                <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                <D:collection/>
                                            </D:resourcetype>
                                            <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                                <C:comp name="VEVENT"/>
                                                <C:comp name="VJOURNAL"/>
                                                <C:comp name="VTODO"/>
                                            </C:supported-calendar-component-set>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/calendar", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("Depth", "0")
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD)))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response3))

        def request4 = """\
                        <propfind xmlns="DAV:">
                            <prop>
                                <displayname/>
                                <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                <n1:getctag xmlns:n1="http://calendarserver.org/ns/"/>
                            </prop>
                        </propfind>"""

        def response4 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname>calendarDisplayName</D:displayname>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">NVy57RJot0LhdYELkMDJ9gQZjOM=</CS:getctag>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""


        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request4)
                .header("Depth", "0")
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD)))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response4))

        def request5 = """\
                        <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                            <prop>
                                <getetag/>
                            </prop>
                            <CAL:filter>
                                <CAL:comp-filter name="VCALENDAR">
                                    <CAL:comp-filter name="VEVENT"/>
                                </CAL:comp-filter>
                            </CAL:filter>
                        </CAL:calendar-query>"""

        def response5 = """<D:multistatus xmlns:D="DAV:"/>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request5)
                .header("Depth", "1")
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD)))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response5))
    }

    @Test
    void addVEvent() {
        def result1 = mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                        <propfind xmlns="DAV:">
                            <prop>
                                <displayname/>
                                <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                <n1:getctag xmlns:n1="http://calendarserver.org/ns/"/>
                            </prop>
                        </propfind>"""

        def result2 = mockMvc.perform(propfind("/dav/{email}/calendar", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def getctag = XmlHelper.getctag(result2)

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:displayname>calendarDisplayName</D:displayname>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/">${getctag}</CS:getctag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        assertThat(result2, equalXml(response2))

        def request3 = """\
                        <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                            <prop>
                                <getetag/>
                            </prop>
                            <CAL:filter>
                                <CAL:comp-filter name="VCALENDAR">
                                    <CAL:comp-filter name="VEVENT"/>
                                </CAL:comp-filter>
                            </CAL:filter>
                        </CAL:calendar-query>"""

        def result3 = mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def getetag = XmlHelper.getetag(result3)

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${getetag}</D:getetag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        assertThat(result3, equalXml(response3))
    }

    @Test
    void deleteVEvent() {
        addVEvent()

        mockMvc.perform(delete("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andReturn()

        mockMvc.perform(get("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void addAndUpdateVEvent() {
        def veventRequest1 = ADD_VEVENT_REQUEST1.replace("DESCRIPTION:DESCRIPTION", "DESCRIPTION:DESCRIPTION update")

        addVEvent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(veventRequest1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                    <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                        <prop>
                            <getetag/>
                        </prop>
                        <CAL:filter>
                            <CAL:comp-filter name="VCALENDAR">
                                <CAL:comp-filter name="VEVENT"/>
                            </CAL:comp-filter>
                        </CAL:filter>
                    </CAL:calendar-query>"""

        def response3 = """\
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/dav/test01@localhost.de/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:getetag>${currentEtag}</D:getetag>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response3))

        mockMvc.perform(get("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("5716")))
                .andExpect(content().string(containsString("DESCRIPTION:DESCRIPTION update")))
    }

    @Test
    void addVTodo() {
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

        def request2 = """\
                        <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                            <prop>
                                <getetag/>
                            </prop>
                            <CAL:filter>
                                <CAL:comp-filter name="VCALENDAR">
                                    <CAL:comp-filter name="VTODO"/>
                                </CAL:comp-filter>
                            </CAL:filter>
                        </CAL:calendar-query>"""

        def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEtag}</D:getetag>
                                    </D:prop>
                                <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response2))

        mockMvc.perform(get("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("303")))
                .andExpect(text(request1))
    }

    @Test
    void deleteVTodo() {
        addVTodo()

        mockMvc.perform(delete("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andReturn()

        mockMvc.perform(get("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void addAndUpdateVTodo() {
        addVTodo()

        def request1 = """\
                        BEGIN:VCALENDAR
                        VERSION:2.0
                        PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x
                        BEGIN:VTODO
                        DTSTAMP:20151231T120107Z
                        UID:6f490b02-77d7-442e-abd3-1e0bb14c3259
                        SEQUENCE:1
                        CREATED:20151231T115922Z
                        LAST-MODIFIED:20151231T120035Z
                        SUMMARY:add vtodo
                        LOCATION:location
                        DESCRIPTION:description\\n[ ] check1\\n[x] check2
                        URL:http://google.de
                        PRIORITY:5
                        CLASS:PRIVATE
                        STATUS:CANCELLED
                        DUE;TZID=Africa/Windhoek:20160131T130000
                        DTSTART;TZID=Africa/Windhoek:20151223T060000
                        PERCENT-COMPLETE:50
                        END:VTODO
                        BEGIN:VTIMEZONE
                        TZID:Africa/Windhoek
                        TZURL:http://tzurl.org/zoneinfo/Africa/Windhoek
                        X-LIC-LOCATION:Africa/Windhoek
                        BEGIN:DAYLIGHT
                        TZOFFSETFROM:+0100
                        TZOFFSETTO:+0200
                        TZNAME:WAST
                        DTSTART:19940904T020000
                        RRULE:FREQ=YEARLY;BYMONTH=9;BYDAY=1SU
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        TZNAME:WAT
                        DTSTART:19950402T020000
                        RRULE:FREQ=YEARLY;BYMONTH=4;BYDAY=1SU
                        END:STANDARD
                        BEGIN:STANDARD
                        TZOFFSETFROM:+010824
                        TZOFFSETTO:+0130
                        TZNAME:SWAT
                        DTSTART:18920208T000000
                        RDATE:18920208T000000
                        END:STANDARD
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0130
                        TZOFFSETTO:+0200
                        TZNAME:SAST
                        DTSTART:19030301T000000
                        RDATE:19030301T000000
                        END:STANDARD
                        BEGIN:DAYLIGHT
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0300
                        TZNAME:SAST
                        DTSTART:19420920T020000
                        RDATE:19420920T020000
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0300
                        TZOFFSETTO:+0200
                        TZNAME:SAST
                        DTSTART:19430321T020000
                        RDATE:19430321T020000
                        END:STANDARD
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0200
                        TZNAME:CAT
                        DTSTART:19900321T000000
                        RDATE:19900321T000000
                        END:STANDARD
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        TZNAME:WAT
                        DTSTART:19940403T000000
                        RDATE:19940403T000000
                        END:STANDARD
                        END:VTIMEZONE
                        END:VCALENDAR
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-Match", "*"))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                    <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                        <prop>
                            <getetag/>
                        </prop>
                        <CAL:filter>
                            <CAL:comp-filter name="VCALENDAR">
                                <CAL:comp-filter name="VTODO"/>
                            </CAL:comp-filter>
                        </CAL:filter>
                    </CAL:calendar-query>"""

        def response3 = """\
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/dav/test01@localhost.de/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:getetag>${currentEtag}</D:getetag>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response3))

        def response4 = """\
                            BEGIN:VCALENDAR
                            VERSION:2.0
                            PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x
                            BEGIN:VTODO
                            DTSTAMP:20151231T120107Z
                            UID:6f490b02-77d7-442e-abd3-1e0bb14c3259
                            SEQUENCE:1
                            CREATED:20151231T115922Z
                            LAST-MODIFIED:20151231T120035Z
                            SUMMARY:add vtodo
                            LOCATION:location
                            DESCRIPTION:description\\n[ ] check1\\n[x] check2
                            URL:http://google.de
                            PRIORITY:5
                            CLASS:PRIVATE
                            STATUS:CANCELLED
                            DUE;TZID=Africa/Windhoek:20160131T130000
                            DTSTART;TZID=Africa/Windhoek:20151223T060000
                            PERCENT-COMPLETE:50
                            END:VTODO
                            BEGIN:VTIMEZONE
                            TZID:Africa/Windhoek
                            TZURL:http://tzurl.org/zoneinfo/Africa/Windhoek
                            X-LIC-LOCATION:Africa/Windhoek
                            BEGIN:DAYLIGHT
                            TZOFFSETFROM:+0100
                            TZOFFSETTO:+0200
                            TZNAME:WAST
                            DTSTART:19940904T020000
                            RRULE:FREQ=YEARLY;BYMONTH=9;BYDAY=1SU
                            END:DAYLIGHT
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0200
                            TZOFFSETTO:+0100
                            TZNAME:WAT
                            DTSTART:19950402T020000
                            RRULE:FREQ=YEARLY;BYMONTH=4;BYDAY=1SU
                            END:STANDARD
                            BEGIN:STANDARD
                            TZOFFSETFROM:+010824
                            TZOFFSETTO:+0130
                            TZNAME:SWAT
                            DTSTART:18920208T000000
                            RDATE:18920208T000000
                            END:STANDARD
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0130
                            TZOFFSETTO:+0200
                            TZNAME:SAST
                            DTSTART:19030301T000000
                            RDATE:19030301T000000
                            END:STANDARD
                            BEGIN:DAYLIGHT
                            TZOFFSETFROM:+0200
                            TZOFFSETTO:+0300
                            TZNAME:SAST
                            DTSTART:19420920T020000
                            RDATE:19420920T020000
                            END:DAYLIGHT
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0300
                            TZOFFSETTO:+0200
                            TZNAME:SAST
                            DTSTART:19430321T020000
                            RDATE:19430321T020000
                            END:STANDARD
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0200
                            TZOFFSETTO:+0200
                            TZNAME:CAT
                            DTSTART:19900321T000000
                            RDATE:19900321T000000
                            END:STANDARD
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0200
                            TZOFFSETTO:+0100
                            TZNAME:WAT
                            DTSTART:19940403T000000
                            RDATE:19940403T000000
                            END:STANDARD
                            END:VTIMEZONE
                            END:VCALENDAR
                            """.stripIndent()

        mockMvc.perform(get("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("1736")))
                .andExpect(text(response4))
    }


    @Test
    void fetchEmptyAddressbookFirstTime() {
        def request1 = """\
                        <propfind xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                            <prop>
                                <CARD:supported-address-data/>
                                <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                            </prop>
                        </propfind>"""

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/contacts/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <CARD:supported-address-data xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                                <CARD:address-data-type CARD:content-type="text/vcard" CARD:version="4.0"/>
                                            </CARD:supported-address-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """\
                        <CARD:addressbook-query xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                            <prop>
                                <getetag/>
                            </prop>
                            <CARD:filter/>
                        </CARD:addressbook-query>"""

        def response2 = """<D:multistatus xmlns:D="DAV:"/>"""

        mockMvc.perform(report("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))
    }

    @Test
    void addVCard() {
        def request1 = """\
                        BEGIN:VCARD
                        VERSION:4.0
                        UID:d0f1d24e-2f4b-4318-b38c-92c6a0130c6a
                        PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 vcard4android ez-vcard/0.9.6
                        FN:Name Prefix Name Middle Name Last Name\\, Name Suffix
                        N:Suffix;Name Prefix Name Middle Name Last Name;Name;;
                        X-PHONETIC-FIRST-NAME:name
                        X-PHONETIC-LAST-NAME:phonetic
                        TEL;TYPE=cell:746-63
                        TEL;TYPE=work:1111-1
                        EMAIL;TYPE=home:email@localhost
                        ORG:Company
                        TITLE:Title
                        IMPP:sip:sip
                        NICKNAME:Nickname
                        NOTE:Notes
                        REV:20160109T131938Z
                        END:VCARD
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                            <CARD:addressbook-query xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                <prop>
                                    <getetag/>
                                </prop>
                                <CARD:filter/>
                            </CARD:addressbook-query>"""

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${currentEtag}</D:getetag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        mockMvc.perform(get("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCardContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("476")))
                .andExpect(text(request1))
    }

    @Test
    void deleteVCard() {
        addVCard()

        mockMvc.perform(delete("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())

        def request2 = """\
                            <CARD:addressbook-query xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                <prop>
                                    <getetag/>
                                </prop>
                                <CARD:filter/>
                            </CARD:addressbook-query>"""

        def response2 = """<D:multistatus xmlns:D="DAV:"/>"""

        mockMvc.perform(report("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        mockMvc.perform(get("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void addAndUpdateVCard() {
        addVCard()

        def request1 = """\
                        BEGIN:VCARD
                        VERSION:4.0
                        UID:d0f1d24e-2f4b-4318-b38c-92c6a0130c6a
                        PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 vcard4android ez-vcard/0.9.6
                        FN:Name Prefix Name Middle Name Last Name\\, Name Suffix
                        REV:20160109T131938Z
                        END:VCARD
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()


        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                            <CARD:addressbook-query xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                <prop>
                                    <getetag/>
                                </prop>
                                <CARD:filter/>
                            </CARD:addressbook-query>"""

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${currentEtag}</D:getetag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        mockMvc.perform(get("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCardContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("224")))
                .andExpect(text(request1))
    }

    @Test
    void fetchAddressbookFirstTime() {
        addVCard()

        def request1 = """\
                        <propfind xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                            <prop>
                                <CARD:supported-address-data/>
                                <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                            </prop>
                        </propfind>"""

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/contacts/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <CARD:supported-address-data xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                                <CARD:address-data-type CARD:content-type="text/vcard" CARD:version="4.0"/>
                                            </CARD:supported-address-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """\
                        <CARD:addressbook-query xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                            <prop>
                                <getetag/>
                            </prop>
                            <CARD:filter/>
                        </CARD:addressbook-query>"""

        def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEtag}</D:getetag>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))
    }
}
