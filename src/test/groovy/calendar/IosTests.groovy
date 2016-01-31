package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.hamcrest.Matchers.*
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpHeaders.ETAG
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomRequestBuilders.propfind
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class IosTests extends IntegrationTestSupport {

    def currentEtag

    @Test
    public void fetchingEmptyCalendarFirstTime() {
        def request1 = """\
                        <x0:propfind xmlns:x0="DAV:" xmlns:CAL="http://calendarserver.org/ns/" xmlns:x1="urn:ietf:params:xml:ns:caldav" xmlns:n0="http://cal.me.com/_namespace/">
                         <x0:prop>
                          <x0:principal-collection-set/>
                          <x1:calendar-home-set/>
                          <x1:calendar-user-address-set/>
                          <x1:schedule-inbox-URL/>
                          <x1:schedule-outbox-URL/>
                          <CAL:dropbox-home-URL/>
                          <CAL:notification-URL/>
                          <x0:displayname/>
                          <n0:user-state/>
                          <x0:principal-URL/>
                          <x0:supported-report-set/>
                         </x0:prop>
                        </x0:propfind>"""

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/principals/users/test01@localhost.de</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:principal-collection-set/>
                                            <CAL:dropbox-home-URL xmlns:CAL="http://calendarserver.org/ns/"/>
                                            <D:principal-URL/>
                                            <x1:schedule-outbox-URL xmlns:x1="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:calendar-user-address-set xmlns:x1="urn:ietf:params:xml:ns:caldav"/>
                                            <n0:user-state xmlns:n0="http://cal.me.com/_namespace/"/>
                                            <x1:schedule-inbox-URL xmlns:x1="urn:ietf:params:xml:ns:caldav"/>
                                            <CAL:notification-URL xmlns:CAL="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <C:calendar-home-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                                <D:href>/dav/test01@localhost.de</D:href>
                                            </C:calendar-home-set>
                                            <D:displayname>test01@localhost.de</D:displayname>
                                            <D:supported-report-set/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/principals/users/{email}/", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        mockMvc.perform(options("/dav/principals/users/{email}/", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, addressbook, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT"))

        def request3 = """\
                        <x0:propfind xmlns:x0="DAV:" xmlns:x1="http://calendarserver.org/ns/" xmlns:CAL="urn:ietf:params:xml:ns:caldav" xmlns:n0="http://apple.com/ns/ical/">
                         <x0:prop>
                          <x1:getctag/>
                          <x0:displayname/>
                          <CAL:calendar-description/>
                          <n0:calendar-color/>
                          <n0:calendar-order/>
                          <CAL:supported-calendar-component-set/>
                          <x0:resourcetype/>
                          <CAL:calendar-free-busy-set/>
                          <CAL:schedule-calendar-transp/>
                          <CAL:schedule-default-calendar-URL/>
                          <x0:quota-available-bytes/>
                          <x0:quota-used-bytes/>
                          <CAL:calendar-timezone/>
                          <x0:current-user-privilege-set/>
                          <x1:subscribed-strip-todos/>
                          <x1:subscribed-strip-alarms/>
                          <x1:subscribed-strip-attachments/>
                          <x1:source/>
                          <x1:pushkey/>
                          <x1:push-transports/>
                          <x0:owner/>
                         </x0:prop>
                        </x0:propfind>"""

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <x1:pushkey xmlns:x1="http://calendarserver.org/ns/"/>
                                            <D:quota-used-bytes/>
                                            <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                            <D:quota-available-bytes/>
                                            <D:owner/>
                                            <n0:calendar-order xmlns:n0="http://apple.com/ns/ical/"/>
                                            <CAL:schedule-default-calendar-URL xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <CAL:calendar-timezone xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:source xmlns:x1="http://calendarserver.org/ns/"/>
                                            <CAL:schedule-calendar-transp xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:subscribed-strip-todos xmlns:x1="http://calendarserver.org/ns/"/>
                                            <CAL:calendar-free-busy-set xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <D:current-user-privilege-set/>
                                            <x1:push-transports xmlns:x1="http://calendarserver.org/ns/"/>
                                            <x1:subscribed-strip-alarms xmlns:x1="http://calendarserver.org/ns/"/>
                                            <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                            <CAL:calendar-description xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:subscribed-strip-attachments xmlns:x1="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:displayname/>
                                            <D:resourcetype>
                                                <D:collection/>
                                            </D:resourcetype>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <x1:pushkey xmlns:x1="http://calendarserver.org/ns/"/>
                                            <D:quota-used-bytes/>
                                            <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                            <D:quota-available-bytes/>
                                            <D:owner/>
                                            <n0:calendar-order xmlns:n0="http://apple.com/ns/ical/"/>
                                            <CAL:schedule-default-calendar-URL xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <CAL:calendar-timezone xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:source xmlns:x1="http://calendarserver.org/ns/"/>
                                            <CAL:schedule-calendar-transp xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:subscribed-strip-todos xmlns:x1="http://calendarserver.org/ns/"/>
                                            <CAL:calendar-free-busy-set xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <D:current-user-privilege-set/>
                                            <x1:push-transports xmlns:x1="http://calendarserver.org/ns/"/>
                                            <x1:subscribed-strip-alarms xmlns:x1="http://calendarserver.org/ns/"/>
                                            <CAL:calendar-description xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:subscribed-strip-attachments xmlns:x1="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:displayname>calendarDisplayName</D:displayname>
                                            <D:resourcetype>
                                                <D:collection/>
                                                <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            </D:resourcetype>
                                            <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                                <C:comp name="VEVENT"/>
                                                <C:comp name="VJOURNAL"/>
                                                <C:comp name="VTODO"/>
                                            </C:supported-calendar-component-set>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/">NVy57RJot0LhdYELkMDJ9gQZjOM=</CS:getctag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/contacts/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <x1:pushkey xmlns:x1="http://calendarserver.org/ns/"/>
                                            <D:quota-used-bytes/>
                                            <n0:calendar-color xmlns:n0="http://apple.com/ns/ical/"/>
                                            <D:quota-available-bytes/>
                                            <D:owner/>
                                            <n0:calendar-order xmlns:n0="http://apple.com/ns/ical/"/>
                                            <CAL:schedule-default-calendar-URL xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <CAL:calendar-timezone xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:source xmlns:x1="http://calendarserver.org/ns/"/>
                                            <CAL:schedule-calendar-transp xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:subscribed-strip-todos xmlns:x1="http://calendarserver.org/ns/"/>
                                            <CAL:calendar-free-busy-set xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <D:current-user-privilege-set/>
                                            <x1:push-transports xmlns:x1="http://calendarserver.org/ns/"/>
                                            <x1:subscribed-strip-alarms xmlns:x1="http://calendarserver.org/ns/"/>
                                            <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            <CS:getctag xmlns:CS="http://calendarserver.org/ns/"/>
                                            <CAL:calendar-description xmlns:CAL="urn:ietf:params:xml:ns:caldav"/>
                                            <x1:subscribed-strip-attachments xmlns:x1="http://calendarserver.org/ns/"/>
                                        </D:prop>
                                        <D:status>HTTP/1.1 404 Not Found</D:status>
                                    </D:propstat>
                                    <D:propstat>
                                        <D:prop>
                                            <D:displayname>contactDisplayName</D:displayname>
                                            <D:resourcetype>
                                                <D:collection/>
                                                <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                            </D:resourcetype>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/", USER01)
                .contentType(TEXT_XML)
                .content(request3)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """\
                        <x0:calendar-query xmlns:x1="DAV:" xmlns:x0="urn:ietf:params:xml:ns:caldav">
                            <x1:prop>
                                <x1:getetag/>
                                <x1:resourcetype/>
                            </x1:prop>
                            <x0:filter>
                                <x0:comp-filter name="VCALENDAR">
                                    <x0:comp-filter name="VEVENT">
                                        <x0:time-range start="20151228T230000Z"/>
                                    </x0:comp-filter>
                                </x0:comp-filter>
                            </x0:filter>
                        </x0:calendar-query>"""

        def response4 = """<D:multistatus xmlns:D="DAV:"/>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request4)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response4))
    }

    @Test
    void addVEvent() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Apple Inc.//iCal 3.0m//EN
                        VERSION:2.0
                        BEGIN:VTIMEZONE
                        TZID:Europe/Berlin
                        BEGIN:DAYLIGHT
                        DTSTART:19810329T020000
                        RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU
                        TZNAME:GMT+02:00
                        TZOFFSETFROM:+0100
                        TZOFFSETTO:+0200
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        DTSTART:19961027T030000
                        RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU
                        TZNAME:GMT+01:00
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VEVENT
                        DESCRIPTION:iOS Note
                        DTEND;TZID=Europe/Berlin:20160203T220000
                        DTSTAMP:20160131T170937Z
                        DTSTART;TZID=Europe/Berlin:20160203T190000
                        LAST-MODIFIED:20160131T170937Z
                        LOCATION:iOS Location
                        RRULE:FREQ=WEEKLY;UNTIL=20170309T225959Z
                        SEQUENCE:0
                        SUMMARY:iOS title
                        TRANSP:OPAQUE
                        UID:BC9458C9-C221-4E23-BA24-1E3D4EDBE65B
                        BEGIN:VALARM
                        ACTION:DISPLAY
                        DESCRIPTION:Event reminder
                        TRIGGER:-PT15M
                        X-WR-ALARMUID:0C27CAB2-7842-40C6-93AF-C12C09A4F88B
                        END:VALARM
                        BEGIN:VALARM
                        ACTION:DISPLAY
                        DESCRIPTION:Event reminder
                        TRIGGER:-PT5M
                        X-WR-ALARMUID:F06D1833-D68F-4141-94C6-537C4FE48232
                        END:VALARM
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        mockMvc.perform(get("/dav/{email}/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(text(request1))
    }

    @Test
    void deleteVEvent() {
        addVEvent()

        mockMvc.perform(delete("/dav/{email}/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics", USER01))
                .andExpect(status().isNoContent())
                .andExpect(etag(nullValue()))

        mockMvc.perform(get("/dav/{email}/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void updateVEvent() {
        addVEvent()

        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Apple Inc.//iCal 3.0m//EN
                        VERSION:2.0
                        BEGIN:VTIMEZONE
                        TZID:Europe/Berlin
                        BEGIN:DAYLIGHT
                        DTSTART:19810329T020000
                        RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU
                        TZNAME:GMT+02:00
                        TZOFFSETFROM:+0100
                        TZOFFSETTO:+0200
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        DTSTART:19961027T030000
                        RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU
                        TZNAME:GMT+01:00
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VEVENT
                        DESCRIPTION:iOS Note
                        DTEND;TZID=Europe/Berlin:20160203T220000
                        DTSTAMP:20160131T173030Z
                        DTSTART;TZID=Europe/Berlin:20160203T190000
                        EXDATE;TZID=Europe/Berlin:20160203T190000
                        EXDATE;TZID=Europe/Berlin:20160316T190000
                        LAST-MODIFIED:20160131T173030Z
                        LOCATION:iOS Location
                        RRULE:FREQ=WEEKLY;UNTIL=20170309T225959Z
                        SEQUENCE:0
                        SUMMARY:iOS title
                        TRANSP:OPAQUE
                        UID:BC9458C9-C221-4E23-BA24-1E3D4EDBE65B
                        BEGIN:VALARM
                        ACTION:DISPLAY
                        DESCRIPTION:Event reminder
                        TRIGGER:-PT15M
                        X-WR-ALARMUID:1BD5C979-1CFC-4B60-A704-998C952FAAD1
                        END:VALARM
                        BEGIN:VALARM
                        ACTION:DISPLAY
                        DESCRIPTION:Event reminder
                        TRIGGER:-PT5M
                        X-WR-ALARMUID:42511428-4548-4C71-A066-E074773D87C4
                        END:VALARM
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1))
                .andExpect(status().isNoContent())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        mockMvc.perform(get("/dav/{email}/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(text(request1))
    }

    @Test
    void fetchCalendar() {
        addVEvent()

        def request1 = """\
                        <x0:calendar-query xmlns:x1="DAV:" xmlns:x0="urn:ietf:params:xml:ns:caldav">
                            <x1:prop>
                                <x1:getetag/>
                                <x1:resourcetype/>
                            </x1:prop>
                            <x0:filter>
                                <x0:comp-filter name="VCALENDAR">
                                    <x0:comp-filter name="VEVENT">
                                        <x0:time-range start="20151228T230000Z"/>
                                    </x0:comp-filter>
                                </x0:comp-filter>
                            </x0:filter>
                        </x0:calendar-query>"""

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${currentEtag}</D:getetag>
                                    <D:resourcetype/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

    }
}
