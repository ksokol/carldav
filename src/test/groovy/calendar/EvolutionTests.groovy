package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.MvcResult
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.xml.XmlHelper

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat
import static org.springframework.http.HttpHeaders.*
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomRequestBuilders.propfind
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.*
import static testutil.xml.XmlHelper.getctag
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
                .andExpect(header().string("DAV", "1, 3, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, PUT, COPY, DELETE, MOVE, REPORT"))

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
                                <D:href>/dav/test01%40localhost.de/calendar/20151230T132406Z-27136-1000-3483-35_localhost-20151230T132510Z.ics</D:href>
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
                            END:VEVENT
                            END:VCALENDAR
                            """.stripIndent()

        mockMvc.perform(get("/dav/{email}/calendar/20151230T141828Z-27136-1000-3483-localhost-20151230T141924Z.ics", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(is(currentEtag)))
                .andExpect(textCalendarContentType())
                .andExpect(header().string(LAST_MODIFIED, notNullValue()))
                .andExpect(header().string(CONTENT_LENGTH, is("398")))
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
}
