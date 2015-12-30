package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.unitedinternet.cosmo.IntegrationTestSupport

import static com.google.common.net.HttpHeaders.AUTHORIZATION
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.TestUser.USER01_PASSWORD
import static testutil.helper.Base64Helper.user
import static testutil.mockmvc.CustomRequestBuilders.propfind
import static testutil.mockmvc.CustomResultMatchers.textXmlContentType
import static testutil.mockmvc.CustomResultMatchers.xml

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
                                            <CARD:addressbook-home-set xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                            <CARD:addressbook-description xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
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
                .andExpect(header().string("DAV", "1, 3, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, PUT, COPY, DELETE, MOVE, REPORT"))

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
                                                <C:comp name="VAVAILABILITY"/>
                                                <C:comp name="VFREEBUSY"/>
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response3))
    }
}
