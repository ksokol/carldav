package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.MvcResult
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.helper.XmlHelper

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpHeaders.ETAG
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import static testutil.TestUser.USER01
import static testutil.helper.XmlHelper.getetag
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
class ThunderbirdTests extends IntegrationTestSupport {

    def VEVENT = """\
                        BEGIN:VCALENDAR
                        PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
                        VERSION:2.0
                        BEGIN:VTIMEZONE
                        TZID:Europe/Stockholm
                        BEGIN:DAYLIGHT
                        TZOFFSETFROM:+0100
                        TZOFFSETTO:+0200
                        TZNAME:CEST
                        DTSTART:19700329T020000
                        RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        TZNAME:CET
                        DTSTART:19701025T030000
                        RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VEVENT
                        CREATED:20151225T180011Z
                        LAST-MODIFIED:20151225T180151Z
                        DTSTAMP:20151225T180151Z
                        UID:0c3112fa-ba2b-4cb4-b495-1b842e3f3b77
                        SUMMARY:VEvent add
                        ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:kamill@test01@localhost.d
                         e
                        ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:attendee1
                        RRULE:FREQ=DAILY;UNTIL=20160226T190000Z;INTERVAL=3
                        CATEGORIES:Business
                        DTSTART;TZID=Europe/Stockholm:20151209T200000
                        DTEND;TZID=Europe/Stockholm:20151209T215500
                        TRANSP:OPAQUE
                        LOCATION:location
                        DESCRIPTION:description
                        X-MOZ-SEND-INVITATIONS:TRUE
                        X-MOZ-SEND-INVITATIONS-UNDISCLOSED:FALSE
                        BEGIN:VALARM
                        ACTION:DISPLAY
                        TRIGGER;VALUE=DATE-TIME:20151225T190000Z
                        DESCRIPTION:Default Mozilla Description
                        END:VALARM
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    def VTODO = """\
                        BEGIN:VCALENDAR
                        PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
                        VERSION:2.0
                        BEGIN:VTIMEZONE
                        TZID:Europe/Berlin
                        BEGIN:DAYLIGHT
                        TZOFFSETFROM:+0100
                        TZOFFSETTO:+0200
                        TZNAME:CEST
                        DTSTART:19700329T020000
                        RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        TZNAME:CET
                        DTSTART:19701025T030000
                        RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VTIMEZONE
                        TZID:Europe/Stockholm
                        BEGIN:DAYLIGHT
                        TZOFFSETFROM:+0100
                        TZOFFSETTO:+0200
                        TZNAME:CEST
                        DTSTART:19700329T020000
                        RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        TZNAME:CET
                        DTSTART:19701025T030000
                        RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VTODO
                        CREATED:20151225T184045Z
                        LAST-MODIFIED:20151225T184131Z
                        DTSTAMP:20151225T184131Z
                        UID:00396957-a9f9-482e-8c51-96d20889ab56
                        SUMMARY:add task
                        STATUS:IN-PROCESS
                        RRULE:FREQ=WEEKLY;UNTIL=20151231T222500Z
                        CATEGORIES:Customer
                        DTSTART;TZID=Europe/Berlin:20151201T232500
                        DUE;TZID=Europe/Stockholm:20151224T232500
                        LOCATION:location
                        PERCENT-COMPLETE:25
                        DESCRIPTION:description
                        BEGIN:VALARM
                        ACTION:DISPLAY
                        TRIGGER;VALUE=DURATION:-PT30M
                        DESCRIPTION:Default Mozilla Description
                        END:VALARM
                        END:VTODO
                        END:VCALENDAR
                        """.stripIndent()

    def currentEtag

    @Test
    void fetchingEmptyCalendarFirstTime() {
        def request1 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:resourcetype/>
                                <D:owner/>
                                <D:current-user-principal/>
                                <D:supported-report-set/>
                                <C:supported-calendar-component-set/>
                                <CS:getctag/>
                            </D:prop>
                        </D:propfind>"""

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:owner/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                                <D:propstat>
                                    <D:prop>
                                        <D:supported-report-set>
                                            <D:supported-report>
                                                <D:report>
                                                  <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                </D:report>
                                              </D:supported-report>
                                              <D:supported-report>
                                                <D:report>
                                                  <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                </D:report>
                                              </D:supported-report>
                                        </D:supported-report-set>
                                        <D:resourcetype>
                                            <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            <D:collection/>
                                        </D:resourcetype>
                                        <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                          <C:comp name="VEVENT"/>
                                          <C:comp name="VJOURNAL"/>
                                          <C:comp name="VTODO"/>
                                        </C:supported-calendar-component-set>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">157565ba8b0d3652b027c868d554f914</CS:getctag>
                                        <D:current-user-principal>
                                          <D:href>/carldav/principals/users/${USER01}</D:href>
                                        </D:current-user-principal>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """\
                        <D:propfind xmlns:D="DAV:">
                            <D:prop>
                                <D:getcontenttype/>
                                <D:resourcetype/>
                                <D:getetag/>
                            </D:prop>
                        </D:propfind>"""

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/calendar/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getcontenttype/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>"157565ba8b0d3652b027c868d554f914"</D:getetag>
                                            <D:resourcetype>
                                                <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                <D:collection/>
                                            </D:resourcetype>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        mockMvc.perform(options("/dav/{email}/", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, addressbook, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, PROPFIND"))
    }

    @Test
    void addVEvent() {
        MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(VEVENT)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = mvcResult.getResponse().getHeader(ETAG)

        def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag/>
                                <C:calendar-data/>
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                        </C:calendar-multiget>"""

        def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEtag}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                            PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                            VERSION:2.0&#13;
                                            BEGIN:VTIMEZONE&#13;
                                            TZID:Europe/Stockholm&#13;
                                            BEGIN:DAYLIGHT&#13;
                                            TZOFFSETFROM:+0100&#13;
                                            TZOFFSETTO:+0200&#13;
                                            TZNAME:CEST&#13;
                                            DTSTART:19700329T020000&#13;
                                            RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                            END:DAYLIGHT&#13;
                                            BEGIN:STANDARD&#13;
                                            TZOFFSETFROM:+0200&#13;
                                            TZOFFSETTO:+0100&#13;
                                            TZNAME:CET&#13;
                                            DTSTART:19701025T030000&#13;
                                            RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                            END:STANDARD&#13;
                                            END:VTIMEZONE&#13;
                                            BEGIN:VEVENT&#13;
                                            CREATED:20151225T180011Z&#13;
                                            LAST-MODIFIED:20151225T180151Z&#13;
                                            DTSTAMP:20151225T180151Z&#13;
                                            UID:0c3112fa-ba2b-4cb4-b495-1b842e3f3b77&#13;
                                            SUMMARY:VEvent add&#13;
                                            ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:kamill@test01@localhost.de&#13;
                                            ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:attendee1&#13;
                                            RRULE:FREQ=DAILY;UNTIL=20160226T190000Z;INTERVAL=3&#13;
                                            CATEGORIES:Business&#13;
                                            DTSTART;TZID=Europe/Stockholm:20151209T200000&#13;
                                            DTEND;TZID=Europe/Stockholm:20151209T215500&#13;
                                            TRANSP:OPAQUE&#13;
                                            LOCATION:location&#13;
                                            DESCRIPTION:description&#13;
                                            X-MOZ-SEND-INVITATIONS:TRUE&#13;
                                            X-MOZ-SEND-INVITATIONS-UNDISCLOSED:FALSE&#13;
                                            BEGIN:VALARM&#13;
                                            ACTION:DISPLAY&#13;
                                            TRIGGER;VALUE=DATE-TIME:20151225T190000Z&#13;
                                            DESCRIPTION:Default Mozilla Description&#13;
                                            END:VALARM&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .content(request2)
                .contentType(TEXT_XML)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))
    }

    @Test
    void propfindGetctagAfterAddVEvent() {
        addVEvent()

        def request1 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/">
                            <D:prop>
                                <CS:getctag/>
                            </D:prop>
                        </D:propfind>"""

        def result1 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def ctag = XmlHelper.getctag(result1)

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">${ctag}</CS:getctag>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result1, equalXml(response1))
    }

    @Test
    void propfindGetcontenttypeResourcetypeGetetagAfterAddVEvent() {
        addVEvent()

        def request1 = """\
                        <D:propfind xmlns:D="DAV:">
                            <D:prop>
                                <D:getcontenttype/>
                                <D:resourcetype/>
                                <D:getetag/>
                            </D:prop>
                        </D:propfind>"""

        def result1 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def etag = getetag(result1)

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getcontenttype/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${etag}</D:getetag>
                                        <D:resourcetype>
                                            <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            <D:collection/>
                                        </D:resourcetype>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEtag}</D:getetag>
                                        <D:getcontenttype>text/calendar;charset=UTF-8</D:getcontenttype>
                                        <D:resourcetype/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result1, equalXml(response1))
    }

    @Test
    void addVTodo() {
        MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(VTODO)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = mvcResult.getResponse().getHeader(ETAG)

        def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag/>
                                <C:calendar-data/>
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics</D:href>
                        </C:calendar-multiget>"""

        def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEtag}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                            PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                            VERSION:2.0&#13;
                                            BEGIN:VTIMEZONE&#13;
                                            TZID:Europe/Berlin&#13;
                                            BEGIN:DAYLIGHT&#13;
                                            TZOFFSETFROM:+0100&#13;
                                            TZOFFSETTO:+0200&#13;
                                            TZNAME:CEST&#13;
                                            DTSTART:19700329T020000&#13;
                                            RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                            END:DAYLIGHT&#13;
                                            BEGIN:STANDARD&#13;
                                            TZOFFSETFROM:+0200&#13;
                                            TZOFFSETTO:+0100&#13;
                                            TZNAME:CET&#13;
                                            DTSTART:19701025T030000&#13;
                                            RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                            END:STANDARD&#13;
                                            END:VTIMEZONE&#13;
                                            BEGIN:VTIMEZONE&#13;
                                            TZID:Europe/Stockholm&#13;
                                            BEGIN:DAYLIGHT&#13;
                                            TZOFFSETFROM:+0100&#13;
                                            TZOFFSETTO:+0200&#13;
                                            TZNAME:CEST&#13;
                                            DTSTART:19700329T020000&#13;
                                            RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                            END:DAYLIGHT&#13;
                                            BEGIN:STANDARD&#13;
                                            TZOFFSETFROM:+0200&#13;
                                            TZOFFSETTO:+0100&#13;
                                            TZNAME:CET&#13;
                                            DTSTART:19701025T030000&#13;
                                            RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                            END:STANDARD&#13;
                                            END:VTIMEZONE&#13;
                                            BEGIN:VTODO&#13;
                                            CREATED:20151225T184045Z&#13;
                                            LAST-MODIFIED:20151225T184131Z&#13;
                                            DTSTAMP:20151225T184131Z&#13;
                                            UID:00396957-a9f9-482e-8c51-96d20889ab56&#13;
                                            SUMMARY:add task&#13;
                                            STATUS:IN-PROCESS&#13;
                                            RRULE:FREQ=WEEKLY;UNTIL=20151231T222500Z&#13;
                                            CATEGORIES:Customer&#13;
                                            DTSTART;TZID=Europe/Berlin:20151201T232500&#13;
                                            DUE;TZID=Europe/Stockholm:20151224T232500&#13;
                                            LOCATION:location&#13;
                                            PERCENT-COMPLETE:25&#13;
                                            DESCRIPTION:description&#13;
                                            BEGIN:VALARM&#13;
                                            ACTION:DISPLAY&#13;
                                            TRIGGER;VALUE=DURATION:-PT30M&#13;
                                            DESCRIPTION:Default Mozilla Description&#13;
                                            END:VALARM&#13;
                                            END:VTODO&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .content(request2)
                .contentType(TEXT_XML)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))
    }

    @Test
    void addSameVEvent() {
        addVEvent()

        mockMvc.perform(put("/dav/{email}/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(VEVENT)
                .header("If-Match", "${currentEtag}"))
                .andExpect(status().isNoContent())
                .andExpect(etag(notNullValue()))
                .andExpect(etag(not(currentEtag)))
    }

    @Test
    void addSameVTodo() {
        addVTodo()

        mockMvc.perform(put("/dav/{email}/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(VTODO)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(notNullValue()))
                .andExpect(etag(not(currentEtag)))
    }

    @Test
    void deleteVEvent() {
        addVEvent()

        mockMvc.perform(delete("/dav/{email}/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
    }

    @Test
    void deleteVTodo() {
        addVTodo()

        mockMvc.perform(delete("/dav/{email}/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
    }

    @Test
    void addAndUpdateVEvent() {
        addVEvent()

        def vevent = VEVENT.replace('LOCATION:location', 'LOCATION:newlocation')

        mockMvc.perform(put("/dav/{email}/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(vevent)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(notNullValue()))
                .andExpect(etag(not(currentEtag)))
    }

    @Test
    void calendarQueryVEventWithTimeRange() {
        calendarQueryVEvent("""\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:time-range start="20141120T181910Z" end="20990129T181910Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>""")
    }

    @Test
    void calendarQueryVEventWithoutTimeRange() {
        calendarQueryVEvent("""\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>""")
    }

    void calendarQueryVEvent(String request2) {
        addVEvent();

        def request1 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/">
                            <D:prop>
                                <D:getetag/>
                            </D:prop>
                        </D:propfind>"""

        def result1 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def etag = getetag(result1, 1)

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${etag}</D:getetag>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar", USER01)
                .contentType(TEXT_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    void fetchingCalendarFirstTime() {
        addVEvent()
        def getetag1 = currentEtag

        addVTodo()
        def getetag2 = currentEtag

        def request1 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:resourcetype/>
                                <D:getcontenttype />
                                <CS:getctag/>
                            </D:prop>
                        </D:propfind>"""

        def result1 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def getctag = XmlHelper.getctag(result1)

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/calendar/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getcontenttype/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:resourcetype>
                                                <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                <D:collection/>
                                            </D:resourcetype>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/">${getctag}</CS:getctag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getcontenttype>text/calendar;charset=UTF-8</D:getcontenttype>
                                            <D:resourcetype/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getcontenttype>text/calendar;charset=UTF-8</D:getcontenttype>
                                            <D:resourcetype/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        assertThat(result1, equalXml(response1))

        def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01%40localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics</D:href>
                        </C:calendar-multiget>"""

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/calendar/00396957-a9f9-482e-8c51-96d20889ab56.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${getetag2}</D:getetag>
                                            <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                                PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                                VERSION:2.0&#13;
                                                BEGIN:VTIMEZONE&#13;
                                                TZID:Europe/Berlin&#13;
                                                BEGIN:DAYLIGHT&#13;
                                                TZOFFSETFROM:+0100&#13;
                                                TZOFFSETTO:+0200&#13;
                                                TZNAME:CEST&#13;
                                                DTSTART:19700329T020000&#13;
                                                RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                                END:DAYLIGHT&#13;
                                                BEGIN:STANDARD&#13;
                                                TZOFFSETFROM:+0200&#13;
                                                TZOFFSETTO:+0100&#13;
                                                TZNAME:CET&#13;
                                                DTSTART:19701025T030000&#13;
                                                RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                                END:STANDARD&#13;
                                                END:VTIMEZONE&#13;
                                                BEGIN:VTIMEZONE&#13;
                                                TZID:Europe/Stockholm&#13;
                                                BEGIN:DAYLIGHT&#13;
                                                TZOFFSETFROM:+0100&#13;
                                                TZOFFSETTO:+0200&#13;
                                                TZNAME:CEST&#13;
                                                DTSTART:19700329T020000&#13;
                                                RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                                END:DAYLIGHT&#13;
                                                BEGIN:STANDARD&#13;
                                                TZOFFSETFROM:+0200&#13;
                                                TZOFFSETTO:+0100&#13;
                                                TZNAME:CET&#13;
                                                DTSTART:19701025T030000&#13;
                                                RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                                END:STANDARD&#13;
                                                END:VTIMEZONE&#13;
                                                BEGIN:VTODO&#13;
                                                CREATED:20151225T184045Z&#13;
                                                LAST-MODIFIED:20151225T184131Z&#13;
                                                DTSTAMP:20151225T184131Z&#13;
                                                UID:00396957-a9f9-482e-8c51-96d20889ab56&#13;
                                                SUMMARY:add task&#13;
                                                STATUS:IN-PROCESS&#13;
                                                RRULE:FREQ=WEEKLY;UNTIL=20151231T222500Z&#13;
                                                CATEGORIES:Customer&#13;
                                                DTSTART;TZID=Europe/Berlin:20151201T232500&#13;
                                                DUE;TZID=Europe/Stockholm:20151224T232500&#13;
                                                LOCATION:location&#13;
                                                PERCENT-COMPLETE:25&#13;
                                                DESCRIPTION:description&#13;
                                                BEGIN:VALARM&#13;
                                                ACTION:DISPLAY&#13;
                                                TRIGGER;VALUE=DURATION:-PT30M&#13;
                                                DESCRIPTION:Default Mozilla Description&#13;
                                                END:VALARM&#13;
                                                END:VTODO&#13;
                                                END:VCALENDAR&#13;
                                            </C:calendar-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${getetag1}</D:getetag>
                                            <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                                PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                                VERSION:2.0&#13;
                                                BEGIN:VTIMEZONE&#13;
                                                TZID:Europe/Stockholm&#13;
                                                BEGIN:DAYLIGHT&#13;
                                                TZOFFSETFROM:+0100&#13;
                                                TZOFFSETTO:+0200&#13;
                                                TZNAME:CEST&#13;
                                                DTSTART:19700329T020000&#13;
                                                RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                                END:DAYLIGHT&#13;
                                                BEGIN:STANDARD&#13;
                                                TZOFFSETFROM:+0200&#13;
                                                TZOFFSETTO:+0100&#13;
                                                TZNAME:CET&#13;
                                                DTSTART:19701025T030000&#13;
                                                RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                                END:STANDARD&#13;
                                                END:VTIMEZONE&#13;
                                                BEGIN:VEVENT&#13;
                                                CREATED:20151225T180011Z&#13;
                                                LAST-MODIFIED:20151225T180151Z&#13;
                                                DTSTAMP:20151225T180151Z&#13;
                                                UID:0c3112fa-ba2b-4cb4-b495-1b842e3f3b77&#13;
                                                SUMMARY:VEvent add&#13;
                                                ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:kamill@test01@localhost.de&#13;
                                                ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:attendee1&#13;
                                                RRULE:FREQ=DAILY;UNTIL=20160226T190000Z;INTERVAL=3&#13;
                                                CATEGORIES:Business&#13;
                                                DTSTART;TZID=Europe/Stockholm:20151209T200000&#13;
                                                DTEND;TZID=Europe/Stockholm:20151209T215500&#13;
                                                TRANSP:OPAQUE&#13;
                                                LOCATION:location&#13;
                                                DESCRIPTION:description&#13;
                                                X-MOZ-SEND-INVITATIONS:TRUE&#13;
                                                X-MOZ-SEND-INVITATIONS-UNDISCLOSED:FALSE&#13;
                                                BEGIN:VALARM&#13;
                                                ACTION:DISPLAY&#13;
                                                TRIGGER;VALUE=DATE-TIME:20151225T190000Z&#13;
                                                DESCRIPTION:Default Mozilla Description&#13;
                                                END:VALARM&#13;
                                                END:VEVENT&#13;
                                                END:VCALENDAR&#13;
                                            </C:calendar-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .content(request2)
                .contentType(TEXT_XML)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        def request3 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:time-range start="20151125T151138Z" end="20160203T151138Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${getetag1}</D:getetag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar", USER01)
                .contentType(TEXT_XML)
                .content(request3)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        mockMvc.perform(report("/dav/{email}/calendar", USER01)
                .contentType(TEXT_XML)
                .content(request4)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))
    }

    @Test
    void fetchingEmptyContactsCollectionFirstTime() {
        def request1 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/">
                            <D:prop>
                                <D:resourcetype/>
                                <D:supported-report-set/>
                                <CS:getctag/>
                            </D:prop>
                        </D:propfind>"""

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/contacts/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:supported-report-set>
                                                <D:supported-report>
                                                    <D:report>
                                                        <CARD:addressbook-multiget xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                                    </D:report>
                                                </D:supported-report>
                                                <D:supported-report>
                                                    <D:report>
                                                        <CARD:addressbook-query xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                                    </D:report>
                                                </D:supported-report>
                                            </D:supported-report-set>
                                            <D:resourcetype>
                                                <D:collection/>
                                                <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                            </D:resourcetype>
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
                        <D:propfind xmlns:D="DAV:">
                            <D:prop>
                                <D:getcontenttype/>
                                <D:getetag/>
                            </D:prop>
                        </D:propfind>"""

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/contacts/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getcontenttype/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>"6ae40e84e2caf8e8fea53ce5396de66f"</D:getetag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
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
                        VERSION:3.0
                        PRODID:-//Inverse inc.//SOGo Connector 1.0//EN
                        UID:C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf
                        N:last;first1
                        FN:first last
                        ORG:organization;department
                        NICKNAME:nickname
                        CATEGORIES:Colleague,Family
                        ADR;TYPE=work:;address 2;address 1;city;state;zip;country
                        ADR;TYPE=home:;address 2;address 1;city;state;zip;country
                        TEL;TYPE=work:work
                        TEL;TYPE=home:home
                        TEL;TYPE=cell:mobile
                        TEL;TYPE=fax:fax
                        TEL;TYPE=pager:pager
                        X-MOZILLA-HTML:FALSE
                        EMAIL;TYPE=work:email@localhost
                        EMAIL;TYPE=home:additrional@localhost
                        URL;TYPE=work:web page
                        URL;TYPE=home:web page
                        TITLE:title
                        BDAY:1979-06-05
                        CUSTOM1:custom 1
                        CUSTOM2:custom 2
                        CUSTOM3:custom 3
                        CUSTOM4:custom 4
                        NOTE:notes
                        X-AIM:aim
                        END:VCARD
                        """.stripIndent()

        MvcResult result1 = mockMvc.perform(put("/dav/{email}/contacts/C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                        <D:propfind xmlns:D="DAV:">
                            <D:prop>
                                <D:getcontenttype/>
                                <D:getetag/>
                            </D:prop>
                        </D:propfind>"""

        def result2 = mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/contacts/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getcontenttype/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${getetag(result2)}</D:getetag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                <D:response>
                                    <D:href>/carldav/dav/test01@localhost.de/contacts/C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${currentEtag}</D:getetag>
                                            <D:getcontenttype>text/vcard</D:getcontenttype>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        assertThat(result2, equalXml(response2))

        mockMvc.perform(get("/dav/{email}/contacts/C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(text(request1))
    }

    @Test
    void deleteVCard() {
        addVCard()

        mockMvc.perform(delete("/dav/{email}/contacts/C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf", USER01))
                .andExpect(status().isNoContent())

        mockMvc.perform(get("/dav/{email}/contacts/C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void addAndUpdateVCard() {
        addVCard()

        def request1 = """\
                        BEGIN:VCARD
                        VERSION:3.0
                        PRODID:-//Inverse inc.//SOGo Connector 1.0//EN
                        UID:C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf
                        N:last;first1
                        FN:first last
                        ORG:organization;department
                        NICKNAME:nickname
                        CATEGORIES:Colleague,Family
                        ADR;TYPE=work:;address 2;address 1;city;state;zip;country
                        ADR;TYPE=home:;address 2;address 1;city;state;zip;country
                        X-MOZILLA-HTML:FALSE
                        EMAIL;TYPE=work:email@localhost
                        URL;TYPE=work:web page
                        URL;TYPE=home:web page
                        TITLE:title
                        BDAY:1979-06-05
                        NOTE:notes
                        PHOTO;ENCODING=b;TYPE=PNG:iVBORw0KGgoAAAANSUhEUgAAAsAAAAGMAQMAAADuk4YmAAAAA
                         1BMVEX///+nxBvIAAAAAXRSTlMAQObYZgAAADlJREFUeF7twDEBAAAAwiD7p7bGDlgYAAAAAAA
                         AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwAGJrAABgPqdWQAAAABJRU5ErkJggg==
                        END:VCARD
                        """.stripIndent()

        MvcResult result1 = mockMvc.perform(put("/dav/{email}/contacts/C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        mockMvc.perform(get("/dav/{email}/contacts/C6E77DAD-3F00-0001-37D0-10022300A3C0.vcf", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(text(request1))
    }

    @Test
    void preconditionFailed() {
        addVEvent()

        def request1 = VEVENT.replaceAll("DESCRIPTION:description", "DESCRIPTION:description altered")

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:precondition-failed>If-Match disallows conditional request</cosmo:precondition-failed>
                            </D:error>"""

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-Match", '"d9bdbd8c948962820b9f8c9733eaecd1"'))
                .andExpect(status().isPreconditionFailed())
                .andExpect(etag(is(currentEtag)))
                .andExpect(xml(response1))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        MvcResult result2 = mockMvc.perform(put("/dav/{email}/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result2.getResponse().getHeader(ETAG)

        mockMvc.perform(get("/dav/{email}/calendar/0c3112fa-ba2b-4cb4-b495-1b842e3f3b77.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(content().string(containsString("DESCRIPTION:description altered")))
    }
}
