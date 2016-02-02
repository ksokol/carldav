package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.helper.XmlHelper

import static XmlHelper.getctag
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.springframework.http.HttpHeaders.*
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
class EvolutionTests extends IntegrationTestSupport {

    def currentEtag

    @Test
    public void fetchingEmptyCalendarFirstTime() {
        mockMvc.perform(options("/dav/{email}/calendar/", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, addressbook, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PUT, DELETE, REPORT"))

        def request1 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <CS:getctag/>
                            </D:prop>
                        </D:propfind>"""

        def result1 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">${getctag(result1)}</CS:getctag>
                                    </D:prop>
                                <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result1, equalXml(response1))

        def request2 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:time-range start="20151125T121145Z" end="20160203T121145Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response2 = """<D:multistatus xmlns:D="DAV:" />"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
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
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response3 = """<D:multistatus xmlns:D="DAV:" />"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))
    }

    @Test
    void addVEvent() {
        def veventRequest1 = new File('src/test/resources/calendar/evolution/addvevent_request1.ics').getText('UTF-8')
        def veventResponse1 = new File('src/test/resources/calendar/evolution/addvevent_response1.ics').getText('UTF-8')
        def veventResponse4 = new File('src/test/resources/calendar/evolution/addvevent_response4.txt').getText('UTF-8')

        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                            <C:calendar-data/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:prop-filter name="UID">
                                  <C:text-match collation="i;octet">20151230T132406Z-27136-1000-3483-35_localhost</C:text-match>
                                </C:prop-filter>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response1 = """<D:multistatus xmlns:D="DAV:" />"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        MvcResult result1 = mockMvc.perform(put("/dav/{email}/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(veventRequest1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        mockMvc.perform(get("/dav/{email}/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("13347")))
                .andExpect(text(veventResponse1))
                .andReturn()

        def request2 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <CS:getctag/>
                            </D:prop>
                        </D:propfind>"""

        def result2 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">${getctag(result2)}</CS:getctag>
                                    </D:prop>
                                <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result2, equalXml(response2))

        def request3 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:time-range start="20151125T121145Z" end="20160203T121145Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response3 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics</D:href>
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
                .content(request3)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """\
                        <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                            <C:calendar-data/>
                          </D:prop>
                          <D:href>/dav/test01%40localhost.de/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics</D:href>
                        </C:calendar-multiget>"""

        def result4 = mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request4)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def response4 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${XmlHelper.getetag(result4)}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">${veventResponse4}</C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result4, equalXml(response4));
    }

    @Test
    void addVEventWithAttachment() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20151230T141828Z-27136-1000-3483-70@localhost
                        DTSTAMP:20151230T121137Z
                        DTSTART;VALUE=DATE:20151216
                        DTEND;VALUE=DATE:20151217
                        SEQUENCE:2
                        SUMMARY:attachment
                        CLASS:PUBLIC
                        TRANSP:TRANSPARENT
                        CREATED:20151230T141924Z
                        LAST-MODIFIED:20151230T141924Z
                        ATTACH;VALUE=BINARY;ENCODING=BASE64;
                         X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T141828Z-27136-1000-3483-70@loc
                         alhost-file.txt:ZW1wdHkgZmlsZQo=
                        ATTACH;VALUE=BINARY;ENCODING=BASE64;
                         X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T141828Z-27136-1000-3483-70@loc
                         alhost-file 2.txt:ZW1wdHkgZmlsZQo=
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

        MvcResult result1 = mockMvc.perform(put("/dav/{email}/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def response1 = """\
                            BEGIN:VCALENDAR
                            CALSCALE:GREGORIAN
                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                            VERSION:2.0
                            BEGIN:VEVENT
                            UID:20151230T141828Z-27136-1000-3483-70@localhost
                            DTSTAMP:20151230T121137Z
                            DTSTART;VALUE=DATE:20151216
                            DTEND;VALUE=DATE:20151217
                            SEQUENCE:2
                            SUMMARY:attachment
                            CLASS:PUBLIC
                            TRANSP:TRANSPARENT
                            CREATED:20151230T141924Z
                            LAST-MODIFIED:20151230T141924Z
                            ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T141828Z-27136-1000-3483-70@localhost-file.txt:ZW1wdHkgZmlsZQo=
                            ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T141828Z-27136-1000-3483-70@localhost-file 2.txt:ZW1wdHkgZmlsZQo=
                            END:VEVENT
                            END:VCALENDAR
                            """.stripIndent()

        mockMvc.perform(get("/dav/{email}/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("688")))
                .andExpect(text(response1))
    }

    @Test
    void addAndUpdateVEvent() {
        addVEventWithAttachment()

        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20151230T141828Z-27136-1000-3483-70@localhost
                        DTSTAMP:20151230T121137Z
                        DTSTART;VALUE=DATE:20151216
                        DTEND;VALUE=DATE:20151217
                        SEQUENCE:2
                        SUMMARY:attachment
                        CLASS:PUBLIC
                        TRANSP:TRANSPARENT
                        CREATED:20151230T141924Z
                        LAST-MODIFIED:20151230T141924Z
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

        MvcResult result1 = mockMvc.perform(put("/dav/{email}/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def response1 = """\
                            BEGIN:VCALENDAR
                            CALSCALE:GREGORIAN
                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                            VERSION:2.0
                            BEGIN:VEVENT
                            UID:20151230T141828Z-27136-1000-3483-70@localhost
                            DTSTAMP:20151230T121137Z
                            DTSTART;VALUE=DATE:20151216
                            DTEND;VALUE=DATE:20151217
                            SEQUENCE:2
                            SUMMARY:attachment
                            CLASS:PUBLIC
                            TRANSP:TRANSPARENT
                            CREATED:20151230T141924Z
                            LAST-MODIFIED:20151230T141924Z
                            END:VEVENT
                            END:VCALENDAR
                            """.stripIndent()

        mockMvc.perform(get("/dav/{email}/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("398")))
                .andExpect(etag(is(currentEtag)))
                .andExpect(text(response1))
    }

    @Test
    void deleteVEvent() {
        addVEventWithAttachment()

        mockMvc.perform(delete("/dav/{email}/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andReturn()

        mockMvc.perform(get("/dav/{email}/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    public void fetchingCalendarFirstTime() {
        def veventResponse4 = new File('src/test/resources/calendar/evolution/addvevent_response4.txt').getText('UTF-8')

        addVEvent()
        def getetag1 = currentEtag

        addVEventWithAttachment()
        def getetag2 = currentEtag

        def request1 = """\
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <CS:getctag/>
                            </D:prop>
                        </D:propfind>"""

        def result1 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">${getctag(result1)}</CS:getctag>
                                    </D:prop>
                                <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result1, equalXml(response1))

        def request2 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:time-range start="20151125T121145Z" end="20160203T121145Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${getetag1}</D:getetag>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${getetag2}</D:getetag>
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
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        def request3 = """\
                        <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                            <C:calendar-data/>
                          </D:prop>
                          <D:href>/dav/test01@localhost.de/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics</D:href>
                          <D:href>/dav/test01@localhost.de/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics</D:href>
                        </C:calendar-multiget>"""

        def response3 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${getetag1}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">${veventResponse4}
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${getetag2}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                            CALSCALE:GREGORIAN&#13;
                                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN&#13;
                                            VERSION:2.0&#13;
                                            BEGIN:VEVENT&#13;
                                            UID:20151230T141828Z-27136-1000-3483-70@localhost&#13;
                                            DTSTAMP:20151230T121137Z&#13;
                                            DTSTART;VALUE=DATE:20151216&#13;
                                            DTEND;VALUE=DATE:20151217&#13;
                                            SEQUENCE:2&#13;
                                            SUMMARY:attachment&#13;
                                            CLASS:PUBLIC&#13;
                                            TRANSP:TRANSPARENT&#13;
                                            CREATED:20151230T141924Z&#13;
                                            LAST-MODIFIED:20151230T141924Z&#13;
                                            ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T141828Z-27136-1000-3483-70@localhost-file.txt:ZW1wdHkgZmlsZQo=&#13;
                                            ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T141828Z-27136-1000-3483-70@localhost-file
                                            2.txt:ZW1wdHkgZmlsZQo=&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))
    }

    @Test
    void addVTodo() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VTODO
                        UID:20151230T170626Z-21291-1000-3483-0@localhost
                        DTSTAMP:20151224T092905Z
                        SUMMARY:add VTodo
                        DESCRIPTION:description
                        DUE;VALUE=DATE:20161231
                        DTSTART;VALUE=DATE:20161216
                        CLASS:CONFIDENTIAL
                        CATEGORIES:Business
                        CATEGORIES:International
                        PERCENT-COMPLETE:93
                        STATUS:IN-PROCESS
                        PRIORITY:7
                        URL:http://www.google.de/
                        SEQUENCE:1
                        CREATED:20151230T170748Z
                        LAST-MODIFIED:20151230T170748Z
                        ATTACH;VALUE=BINARY;ENCODING=BASE64;
                         X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T170626Z-21291-1000-3483-0@loca
                         lhost-file.txt:ZW1wdHkgZmlsZQo=
                        END:VTODO
                        END:VCALENDAR
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/20151230T170626Z-21291-1000-3483-0_localhost-20151230T170748Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def response2 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VTODO
                        UID:20151230T170626Z-21291-1000-3483-0@localhost
                        DTSTAMP:20151224T092905Z
                        SUMMARY:add VTodo
                        DESCRIPTION:description
                        DUE;VALUE=DATE:20161231
                        DTSTART;VALUE=DATE:20161216
                        CLASS:CONFIDENTIAL
                        CATEGORIES:Business
                        CATEGORIES:International
                        PERCENT-COMPLETE:93
                        STATUS:IN-PROCESS
                        PRIORITY:7
                        URL:http://www.google.de/
                        SEQUENCE:1
                        CREATED:20151230T170748Z
                        LAST-MODIFIED:20151230T170748Z
                        ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20151230T170626Z-21291-1000-3483-0@localhost-file.txt:ZW1wdHkgZmlsZQo=
                        END:VTODO
                        END:VCALENDAR
                        """.stripIndent()

        mockMvc.perform(get("/dav/{email}/calendar/20151230T170626Z-21291-1000-3483-0_localhost-20151230T170748Z.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("672")))
                .andExpect(text(response2))
                .andReturn()
    }

    @Test
    void addAndUpdateVTodo() {
        addVTodo()

        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VTODO
                        UID:20151230T170626Z-21291-1000-3483-0@localhost
                        DTSTAMP:20151224T092905Z
                        SUMMARY:add VTodo
                        DESCRIPTION:description
                        DUE;VALUE=DATE:20161231
                        DTSTART;VALUE=DATE:20161216
                        PERCENT-COMPLETE:93
                        STATUS:IN-PROCESS
                        PRIORITY:7
                        SEQUENCE:1
                        CREATED:20151230T170748Z
                        LAST-MODIFIED:20151230T170748Z
                        END:VTODO
                        END:VCALENDAR
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/20151230T170626Z-21291-1000-3483-0_localhost-20151230T170748Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def response2 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VTODO
                        UID:20151230T170626Z-21291-1000-3483-0@localhost
                        DTSTAMP:20151224T092905Z
                        SUMMARY:add VTodo
                        DESCRIPTION:description
                        DUE;VALUE=DATE:20161231
                        DTSTART;VALUE=DATE:20161216
                        PERCENT-COMPLETE:93
                        STATUS:IN-PROCESS
                        PRIORITY:7
                        SEQUENCE:1
                        CREATED:20151230T170748Z
                        LAST-MODIFIED:20151230T170748Z
                        END:VTODO
                        END:VCALENDAR
                        """.stripIndent()

        mockMvc.perform(get("/dav/{email}/calendar/20151230T170626Z-21291-1000-3483-0_localhost-20151230T170748Z.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("435")))
                .andExpect(text(response2))
    }

    @Test
    void deleteVTodo() {
        addVTodo()

        mockMvc.perform(delete("/dav/{email}/calendar/20151230T170626Z-21291-1000-3483-0_localhost-20151230T170748Z.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andReturn()

        mockMvc.perform(get("/dav/{email}/calendar/20151230T170626Z-21291-1000-3483-0_localhost-20151230T170748Z.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void calendarQueryVTodoWithTimeRange() {
        calendarQueryVTodo("""\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VTODO">
                                <C:time-range start="20141120T181910Z" end="20990129T181910Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>""")
    }

    @Test
    void calendarQueryVTodoWithoutTimeRange() {
        calendarQueryVTodo("""\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VTODO">
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>""")
    }

    void calendarQueryVTodo(String request2) {
        addVTodo();

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

        def etag = XmlHelper.getetag(result1, 1)

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/20151230T170626Z-21291-1000-3483-0_localhost-20151230T170748Z.ics</D:href>
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
    void addVCard() {
        def request1 = """\
                        BEGIN:VCARD
                        VERSION:3.0
                        URL:home page
                        TITLE:
                        ROLE:
                        X-EVOLUTION-MANAGER:manager
                        X-EVOLUTION-ASSISTANT:assistant
                        NICKNAME:Nickname
                        BDAY:1992-05-13
                        X-EVOLUTION-ANNIVERSARY:2016-01-14
                        X-EVOLUTION-SPOUSE:
                        NOTE:notes
                        FN:Mr. First Middle Last II
                        N:Last;First;Middle;Mr.;II
                        X-EVOLUTION-FILE-AS:Last\\, First
                        CATEGORIES:Birthday,Business
                        X-EVOLUTION-BLOG-URL:blog
                        CALURI:calendar
                        FBURL:free/busy
                        X-EVOLUTION-VIDEO-URL:video chat
                        X-MOZILLA-HTML:TRUE
                        EMAIL;TYPE=WORK:work@email
                        EMAIL;TYPE=HOME:home@email
                        EMAIL;TYPE=OTHER:other@email
                        TEL;TYPE=WORK,VOICE:business pohne
                        TEL;TYPE=HOME,VOICE:home phone
                        TEL;TYPE=CAR:car phone
                        TEL;TYPE=VOICE:other phone
                        X-SIP;TYPE=WORK:work sip
                        X-SIP;TYPE=HOME:home sip
                        X-SIP;TYPE=OTHER:other sip
                        X-AIM;X-EVOLUTION-UI-SLOT=1:aim
                        X-SKYPE;X-EVOLUTION-UI-SLOT=2:skype
                        ADR;TYPE=WORK:;;address work;;;;country
                        LABEL;TYPE=WORK:address work\\ncountry
                        ADR;TYPE=HOME:;;address home;city;;;
                        LABEL;TYPE=HOME:address home\\ncity
                        UID:EE0F1E48-114E3062-76210FF9
                        END:VCARD
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/contacts/BA9B77D0-87105168-1311D5B6.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                        <propfind xmlns="DAV:">
                            <prop>
                                <getetag/>
                            </prop>
                        </propfind>"""

        def result2 = mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
                .contentType(TEXT_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(textXmlContentType())
                .andExpect(status().isMultiStatus())
                .andReturn().getResponse().getContentAsString()

        def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/contacts/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${getetag(result2)}</D:getetag>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                            <D:response>
                                <D:href>/dav/test01@localhost.de/contacts/BA9B77D0-87105168-1311D5B6.vcf</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEtag}</D:getetag>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result2, equalXml(response2))

        mockMvc.perform(get("/dav/{email}/contacts/BA9B77D0-87105168-1311D5B6.vcf", USER01))
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(ETAG, currentEtag))
                .andExpect(textCardContentType())
                .andExpect(status().isOk())
                .andExpect(text(request1))
    }

    @Test
    void deleteVCard() {
        addVCard()

        mockMvc.perform(delete("/dav/{email}/contacts/BA9B77D0-87105168-1311D5B6.vcf", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())

        mockMvc.perform(get("/dav/{email}/contacts/BA9B77D0-87105168-1311D5B6.vcf", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void updateVCard() {
        addVCard()

        def request1 = """\
                        BEGIN:VCARD
                        VERSION:3.0
                        URL:home page
                        UID:EE0F1E48-114E3062-76210FF9
                        END:VCARD
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/contacts/BA9B77D0-87105168-1311D5B6.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        mockMvc.perform(get("/dav/{email}/contacts/BA9B77D0-87105168-1311D5B6.vcf", USER01))
                .andExpect(status().isOk())
                .andExpect(text(request1))
    }

    @Test
    void fetchingMemosFirstTime() {
        def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VJOURNAL">
                                <C:time-range start="20151210T071757Z" end="20160218T071757Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def response1 = """<D:multistatus xmlns:D="DAV:"/>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VJOURNAL">
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))
    }

    @Test
    void addVJournal() {
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

        def response1 = """<D:multistatus xmlns:D="DAV:"/>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VJOURNAL
                        UID:20160114T072824Z-8357-1000-1795-3@localhost
                        DTSTAMP:20160114T065614Z
                        SUMMARY:summary
                        DESCRIPTION:description
                        DTSTART;VALUE=DATE:20160114
                        CLASS:PUBLIC
                        CATEGORIES:Favorites
                        CATEGORIES:Gifts
                        SEQUENCE:1
                        CREATED:20160114T072907Z
                        LAST-MODIFIED:20160114T072907Z
                        ATTACH;VALUE=BINARY;ENCODING=BASE64;
                         X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20160114T072824Z-8357-1000-1795-3@local
                         host-file.txt:ZGVtbwo=
                        END:VJOURNAL
                        END:VCALENDAR
                        """.stripIndent()

        def result2 = mockMvc.perform(put("/dav/{email}/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request2)
                .header(IF_NONE_MATCH, "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result2.getResponse().getHeader(ETAG)

        def response3 = """\
                            BEGIN:VCALENDAR
                            CALSCALE:GREGORIAN
                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                            VERSION:2.0
                            BEGIN:VJOURNAL
                            UID:20160114T072824Z-8357-1000-1795-3@localhost
                            DTSTAMP:20160114T065614Z
                            SUMMARY:summary
                            DESCRIPTION:description
                            DTSTART;VALUE=DATE:20160114
                            CLASS:PUBLIC
                            CATEGORIES:Favorites
                            CATEGORIES:Gifts
                            SEQUENCE:1
                            CREATED:20160114T072907Z
                            LAST-MODIFIED:20160114T072907Z
                            ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20160114T072824Z-8357-1000-1795-3@localhost-file.txt:ZGVtbwo=
                            END:VJOURNAL
                            END:VCALENDAR
                            """.stripIndent()

        mockMvc.perform(get("/dav/{email}/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, is("text/calendar; charset=UTF-8")))
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, "549"))
                .andExpect(etag(is(currentEtag)))
                .andExpect(text(response3))
                .andReturn()

        def response4 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/20160114T072824Z-8357-1000-1795-3%40localhost.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${currentEtag}</D:getetag>
                                            <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR CALSCALE:GREGORIAN PRODID:-//Ximian//NONSGML Evolution Calendar//EN VERSION:2.0 BEGIN:VJOURNAL UID:20160114T072824Z-8357-1000-1795-3@localhost DTSTAMP:20160114T065614Z SUMMARY:summary DESCRIPTION:description DTSTART;VALUE=DATE:20160114 CLASS:PUBLIC CATEGORIES:Favorites CATEGORIES:Gifts SEQUENCE:1 CREATED:20160114T072907Z LAST-MODIFIED:20160114T072907Z ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20160114T072824Z-8357-1000-1795-3@localhost-file.txt:ZGVtbwo= END:VJOURNAL END:VCALENDAR</C:calendar-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response4))
    }

    @Test
    void deleteVJournal() {
        addVJournal()

        mockMvc.perform(delete("/dav/{email}/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andReturn()

        mockMvc.perform(get("/dav/{email}/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void addAndUpdateVJournal() {
        addVJournal()

        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VJOURNAL
                        UID:20160114T072824Z-8357-1000-1795-3@localhost
                        DTSTAMP:20160114T065614Z
                        SUMMARY:summary2
                        DESCRIPTION:description
                        DTSTART;VALUE=DATE:20160114
                        CLASS:PUBLIC
                        CATEGORIES:Favorites
                        CATEGORIES:Gifts
                        SEQUENCE:1
                        CREATED:20160114T072907Z
                        LAST-MODIFIED:20160114T072907Z
                        END:VJOURNAL
                        END:VCALENDAR
                        """.stripIndent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        mockMvc.perform(get("/dav/{email}/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(text(request1))
    }

    @Test
    void fetchingMemos() {
        addVJournal()

        def request1 = """\
                        <propfind xmlns="DAV:" xmlns:CS="http://calendarserver.org/ns/">
                          <prop>
                            <CS:getctag/>
                          </prop>
                        </propfind>"""

        def result1 = mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">${getctag(result1)}</CS:getctag>
                                    </D:prop>
                                <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        assertThat(result1, equalXml(response1))

        def request2 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VJOURNAL">
                                <C:time-range start="20151210T205925Z" end="20160218T205925Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        def result2 = mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()


        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/20160114T072824Z-8357-1000-1795-3%40localhost.ics</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${XmlHelper.getetag(result2)}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>"""

        assertThat(result2, equalXml(response2))

        def request3 = """\
                        <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/dav/test01@localhost.de/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics</D:href>
                        </C:calendar-multiget>"""

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/20160114T072824Z-8357-1000-1795-3@localhost.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${XmlHelper.getetag(result2)}</D:getetag>
                                            <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                                CALSCALE:GREGORIAN&#13;
                                                PRODID:-//Ximian//NONSGML Evolution Calendar//EN&#13;
                                                VERSION:2.0&#13;
                                                BEGIN:VJOURNAL&#13;
                                                UID:20160114T072824Z-8357-1000-1795-3@localhost&#13;
                                                DTSTAMP:20160114T065614Z&#13;
                                                SUMMARY:summary&#13;
                                                DESCRIPTION:description&#13;
                                                DTSTART;VALUE=DATE:20160114&#13;
                                                CLASS:PUBLIC&#13;
                                                CATEGORIES:Favorites&#13;
                                                CATEGORIES:Gifts&#13;
                                                SEQUENCE:1&#13;
                                                CREATED:20160114T072907Z&#13;
                                                LAST-MODIFIED:20160114T072907Z&#13;
                                                ATTACH;VALUE=BINARY;ENCODING=BASE64;X-EVOLUTION-CALDAV-ATTACHMENT-NAME=20160114T072824Z-8357-1000-1795-3@localhost-file.txt:ZGVtbwo=&#13;
                                                END:VJOURNAL&#13;
                                                END:VCALENDAR&#13;
                                            </C:calendar-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <D:prop>
                            <D:getetag/>
                          </D:prop>
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VJOURNAL">
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request4)
                .header("Depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(xml(response2))
    }

    @Test
    void calendarQueryForVJournal() {
        addVEvent()
        fetchingMemos()
    }

    @Test
    void calendarQueryForVEvent() {
        addVJournal()
        fetchingCalendarFirstTime()
    }

    @Test
    void calendarQueryForVTodo() {
        addVTodo()
        fetchingCalendarFirstTime()
    }

    @Test
    void calendarQueryForVCard() {
        addVCard()
        fetchingCalendarFirstTime()
    }

    @Test
    void preconditionFailed() {
        addVEvent()

        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20151230T132406Z-27136-1000-3483-35@localhost
                        DTSTAMP:20151230T121137Z
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:precondition-failed>If-Match disallows conditional request</cosmo:precondition-failed>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header("If-Match", '"d9bdbd8c948962820b9f8c9733eaecd1"'))
                .andExpect(status().isPreconditionFailed())
                .andExpect(etag(is(currentEtag)))
                .andExpect(xml(response1))

    }

    @Test
    public void addVEventContainingRecurrenceId() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T145918Z-30167-1000-1796-12@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T165900
                        SEQUENCE:5
                        SUMMARY:event exception23
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        CREATED:20160123T160450Z
                        LAST-MODIFIED:20160123T160450Z
                        RECURRENCE-ID;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

        mockMvc.perform(put("/dav/test01@localhost.de/calendar/20160123T145918Z-30167-1000-1796-12_localhost-20160123T160450Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1))
                .andExpect(status().isCreated())

        def response2 = """\
                            BEGIN:VCALENDAR
                            CALSCALE:GREGORIAN
                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                            VERSION:2.0
                            BEGIN:VTIMEZONE
                            TZID:Europe/Berlin
                            TZURL:http://tzurl.org/zoneinfo/Europe/Berlin
                            X-LIC-LOCATION:Europe/Berlin
                            BEGIN:DAYLIGHT
                            TZOFFSETFROM:+0100
                            TZOFFSETTO:+0200
                            TZNAME:CEST
                            DTSTART:19810329T020000
                            RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU
                            END:DAYLIGHT
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0200
                            TZOFFSETTO:+0100
                            TZNAME:CET
                            DTSTART:19961027T030000
                            RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU
                            END:STANDARD
                            BEGIN:STANDARD
                            TZOFFSETFROM:+005328
                            TZOFFSETTO:+0100
                            TZNAME:CET
                            DTSTART:18930401T000000
                            RDATE:18930401T000000
                            END:STANDARD
                            BEGIN:DAYLIGHT
                            TZOFFSETFROM:+0100
                            TZOFFSETTO:+0200
                            TZNAME:CEST
                            DTSTART:19160430T230000
                            RDATE:19160430T230000
                            RDATE:19170416T020000
                            RDATE:19180415T020000
                            RDATE:19400401T020000
                            RDATE:19430329T020000
                            RDATE:19440403T020000
                            RDATE:19450402T020000
                            RDATE:19460414T020000
                            RDATE:19470406T030000
                            RDATE:19480418T020000
                            RDATE:19490410T020000
                            RDATE:19800406T020000
                            END:DAYLIGHT
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0200
                            TZOFFSETTO:+0100
                            TZNAME:CET
                            DTSTART:19161001T010000
                            RDATE:19161001T010000
                            RDATE:19170917T030000
                            RDATE:19180916T030000
                            RDATE:19421102T030000
                            RDATE:19431004T030000
                            RDATE:19441002T030000
                            RDATE:19451118T030000
                            RDATE:19461007T030000
                            RDATE:19471005T030000
                            RDATE:19481003T030000
                            RDATE:19491002T030000
                            RDATE:19800928T030000
                            RDATE:19810927T030000
                            RDATE:19820926T030000
                            RDATE:19830925T030000
                            RDATE:19840930T030000
                            RDATE:19850929T030000
                            RDATE:19860928T030000
                            RDATE:19870927T030000
                            RDATE:19880925T030000
                            RDATE:19890924T030000
                            RDATE:19900930T030000
                            RDATE:19910929T030000
                            RDATE:19920927T030000
                            RDATE:19930926T030000
                            RDATE:19940925T030000
                            RDATE:19950924T030000
                            END:STANDARD
                            BEGIN:DAYLIGHT
                            TZOFFSETFROM:+0200
                            TZOFFSETTO:+0300
                            TZNAME:CEMT
                            DTSTART:19450524T020000
                            RDATE:19450524T020000
                            RDATE:19470511T030000
                            END:DAYLIGHT
                            BEGIN:DAYLIGHT
                            TZOFFSETFROM:+0300
                            TZOFFSETTO:+0200
                            TZNAME:CEST
                            DTSTART:19450924T030000
                            RDATE:19450924T030000
                            RDATE:19470629T030000
                            END:DAYLIGHT
                            BEGIN:STANDARD
                            TZOFFSETFROM:+0100
                            TZOFFSETTO:+0100
                            TZNAME:CET
                            DTSTART:19460101T000000
                            RDATE:19460101T000000
                            RDATE:19800101T000000
                            END:STANDARD
                            END:VTIMEZONE
                            BEGIN:VEVENT
                            UID:20160123T145918Z-30167-1000-1796-12@localhost
                            DTSTAMP:20160119T173941Z
                            DTSTART;TZID=Europe/Berlin:20160123T155900
                            DTEND;TZID=Europe/Berlin:20160123T165900
                            SEQUENCE:5
                            SUMMARY:event exception23
                            CLASS:PUBLIC
                            TRANSP:OPAQUE
                            CREATED:20160123T160450Z
                            LAST-MODIFIED:20160123T160450Z
                            RECURRENCE-ID;TZID=Europe/Berlin:20160123T155900
                            END:VEVENT
                            END:VCALENDAR
                            """.stripIndent()

        mockMvc.perform(get("/dav/test01@localhost.de/calendar/20160123T145918Z-30167-1000-1796-12_localhost-20160123T160450Z.ics", USER01))
                .andExpect(textCalendarContentType())
                .andExpect(status().isOk())
                .andExpect(text(response2))

        mockMvc.perform(delete("/dav/test01@localhost.de/calendar/20160123T145918Z-30167-1000-1796-12_localhost-20160123T160450Z.ics", USER01))
                .andExpect(status().isNoContent())

        mockMvc.perform(get("/dav/test01@localhost.de/calendar/20160123T145918Z-30167-1000-1796-12_localhost-20160123T160450Z.ics", USER01))
                .andExpect(status().isNotFound())
    }
}
