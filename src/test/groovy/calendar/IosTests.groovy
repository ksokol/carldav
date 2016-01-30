package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomRequestBuilders.propfind
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.textXmlContentType
import static testutil.mockmvc.CustomResultMatchers.xml

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
        .andDo(MockMvcResultHandlers.print())
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
        .andDo(MockMvcResultHandlers.print())
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
}
