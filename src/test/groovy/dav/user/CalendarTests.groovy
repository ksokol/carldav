package dav.user

import calendar.DavDroidData
import groovy.xml.XmlSlurper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.MvcResult
import org.unitedinternet.cosmo.IntegrationTestSupport
import util.builder.GeneralData

import java.text.DateFormat
import java.text.SimpleDateFormat

import static calendar.EvolutionData.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpHeaders.ETAG
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static util.TestUser.USER01
import static util.TestUser.USER02
import static util.builder.GeneralData.*
import static util.builder.GeneralResponse.NOT_FOUND
import static util.builder.MethodNotAllowedBuilder.notAllowed
import static util.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static util.mockmvc.CustomRequestBuilders.propfind
import static util.mockmvc.CustomRequestBuilders.report
import static util.mockmvc.CustomResultMatchers.*
import static util.xmlunit.XmlMatcher.equalXml

@WithUserDetails(USER01)
class CalendarTests extends IntegrationTestSupport {

  private final String uuid = GeneralData.UUID
  private final String uuid2 = UUID_EVENT2

  @Test
  void shouldReturnHtmlForUser() throws Exception {
    final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
      .contentType(TEXT_CALENDAR)
      .content(CALDAV_EVENT))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))
      .andReturn()

    final String eTag = mvcResult.getResponse().getHeader(ETAG)

    def getRequest = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                        </C:calendar-multiget>"""

    def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                            VERSION:2.0&#13;
                                            X-WR-CALNAME:Work&#13;
                                            PRODID:-//Apple Computer\\, Inc//iCal 2.0//EN&#13;
                                            X-WR-RELCALID:21654AA6-F774-4918-80B8-F0C8CABC7737&#13;
                                            X-WR-TIMEZONE:US/Pacific&#13;
                                            CALSCALE:GREGORIAN&#13;
                                            BEGIN:VTIMEZONE&#13;
                                            TZID:US/Pacific&#13;
                                            LAST-MODIFIED:20050812T212029Z&#13;
                                            BEGIN:DAYLIGHT&#13;
                                            DTSTART:20040404T100000&#13;
                                            TZOFFSETTO:-0700&#13;
                                            TZOFFSETFROM:+0000&#13;
                                            TZNAME:PDT&#13;
                                            END:DAYLIGHT&#13;
                                            BEGIN:STANDARD&#13;
                                            DTSTART:20041031T020000&#13;
                                            TZOFFSETTO:-0800&#13;
                                            TZOFFSETFROM:-0700&#13;
                                            TZNAME:PST&#13;
                                            END:STANDARD&#13;
                                            BEGIN:DAYLIGHT&#13;
                                            DTSTART:20050403T010000&#13;
                                            TZOFFSETTO:-0700&#13;
                                            TZOFFSETFROM:-0800&#13;
                                            TZNAME:PDT&#13;
                                            END:DAYLIGHT&#13;
                                            BEGIN:STANDARD&#13;
                                            DTSTART:20051030T020000&#13;
                                            TZOFFSETTO:-0800&#13;
                                            TZOFFSETFROM:-0700&#13;
                                            TZNAME:PST&#13;
                                            END:STANDARD&#13;
                                            END:VTIMEZONE&#13;
                                            BEGIN:VEVENT&#13;
                                            DTSTART;TZID=US/Pacific:20050602T120000&#13;
                                            LOCATION:Whoville&#13;
                                            SUMMARY:all entities meeting&#13;
                                            UID:59BC120D-E909-4A56-A70D-8E97914E51A3&#13;
                                            SEQUENCE:4&#13;
                                            DTSTAMP:20050520T014148Z&#13;
                                            DURATION:PT1H&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(getRequest)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response))
  }

  @Test
  void putCalendarItem() throws Exception {
    final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid2)
      .contentType(TEXT_CALENDAR)
      .content(CALDAV_EVENT2))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))
      .andReturn()

    final String eTag = mvcResult.getResponse().getHeader(ETAG)

    def getRequest = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/18f0e0e5-4e1e-4e0d-b317-0d861d3e575c.ics</D:href>
                        </C:calendar-multiget>"""

    def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/18f0e0e5-4e1e-4e0d-b317-0d861d3e575c.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                            PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                            VERSION:2.0&#13;
                                            BEGIN:VEVENT&#13;
                                            CREATED:20151215T212053Z&#13;
                                            LAST-MODIFIED:20151215T212127Z&#13;
                                            DTSTAMP:20151215T212127Z&#13;
                                            UID:18f0e0e5-4e1e-4e0d-b317-0d861d3e575c&#13;
                                            SUMMARY:title&#13;
                                            ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:kamill@sokol-web.de&#13;
                                            ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:attende&#13;
                                            RRULE:FREQ=DAILY&#13;
                                            X-MOZ-LASTACK:20151215T212127Z&#13;
                                            DTSTART;VALUE=DATE:20151206&#13;
                                            DTEND;VALUE=DATE:20151207&#13;
                                            TRANSP:TRANSPARENT&#13;
                                            LOCATION:location&#13;
                                            DESCRIPTION:description&#13;
                                            X-MOZ-SEND-INVITATIONS:TRUE&#13;
                                            X-MOZ-SEND-INVITATIONS-UNDISCLOSED:FALSE&#13;
                                            X-MOZ-GENERATION:1&#13;
                                            BEGIN:VALARM&#13;
                                            ACTION:DISPLAY&#13;
                                            TRIGGER;VALUE=DURATION:-PT15M&#13;
                                            DESCRIPTION:Default Mozilla Description&#13;
                                            END:VALARM&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(getRequest)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response))
  }

  @Test
  void calendarGetItem() {
    mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
      .contentType(TEXT_CALENDAR)
      .content(CALDAV_EVENT))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))
      .andReturn()

    mockMvc.perform(get("/dav/{email}/calendar/{uid}.ics", USER01, uuid)
      .contentType(TEXT_XML))
      .andExpect(textCalendarContentType())
      .andExpect(status().isOk())
      .andExpect(text(CALDAV_EVENT))
  }

  @Test
  void shouldReturnHtmlForUserAllProp() throws Exception {
    final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
      .contentType(TEXT_CALENDAR)
      .content(CALDAV_EVENT))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))
      .andReturn()

    final String eTag = mvcResult.getResponse().getHeader(ETAG)

    def request = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                            <D:allprop />
                        </C:calendar-multiget>"""

    def result1 = mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andReturn().getResponse().getContentAsString()

    def xml = new XmlSlurper().parseText(result1)
    String lastModified = xml.response.propstat.prop.getlastmodified.text()

    DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
    format.parse(lastModified)

    def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag}</D:getetag>
                                        <D:displayname>all entities meeting</D:displayname>
                                        <D:getlastmodified>${lastModified}</D:getlastmodified>
                                        <D:iscollection>0</D:iscollection>
                                        <D:getcontenttype>text/calendar;charset=UTF-8</D:getcontenttype>
                                        <D:supported-report-set>
                                            <D:supported-report>
                                              <D:report>
                                                <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                              </D:report>
                                            </D:supported-report>
                                            <D:supported-report>
                                              <D:report>
                                                <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                              </D:report>
                                            </D:supported-report>
                                        </D:supported-report-set>
                                        <D:getcontentlength>920</D:getcontentlength>
                                        <D:resourcetype/>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR&#13;
                                            VERSION:2.0&#13;
                                            X-WR-CALNAME:Work&#13;
                                            PRODID:-//Apple Computer\\, Inc//iCal 2.0//EN&#13;
                                            X-WR-RELCALID:21654AA6-F774-4918-80B8-F0C8CABC7737&#13;
                                            X-WR-TIMEZONE:US/Pacific&#13;
                                            CALSCALE:GREGORIAN&#13;
                                            BEGIN:VTIMEZONE&#13;
                                            TZID:US/Pacific&#13;
                                            LAST-MODIFIED:20050812T212029Z&#13;
                                            BEGIN:DAYLIGHT&#13;
                                            DTSTART:20040404T100000&#13;
                                            TZOFFSETTO:-0700&#13;
                                            TZOFFSETFROM:+0000&#13;
                                            TZNAME:PDT&#13;
                                            END:DAYLIGHT&#13;
                                            BEGIN:STANDARD&#13;
                                            DTSTART:20041031T020000&#13;
                                            TZOFFSETTO:-0800&#13;
                                            TZOFFSETFROM:-0700&#13;
                                            TZNAME:PST&#13;
                                            END:STANDARD&#13;
                                            BEGIN:DAYLIGHT&#13;
                                            DTSTART:20050403T010000&#13;
                                            TZOFFSETTO:-0700&#13;
                                            TZOFFSETFROM:-0800&#13;
                                            TZNAME:PDT&#13;
                                            END:DAYLIGHT&#13;
                                            BEGIN:STANDARD&#13;
                                            DTSTART:20051030T020000&#13;
                                            TZOFFSETTO:-0800&#13;
                                            TZOFFSETFROM:-0700&#13;
                                            TZNAME:PST&#13;
                                            END:STANDARD&#13;
                                            END:VTIMEZONE&#13;
                                            BEGIN:VEVENT&#13;
                                            DTSTART;TZID=US/Pacific:20050602T120000&#13;
                                            LOCATION:Whoville&#13;
                                            SUMMARY:all entities meeting&#13;
                                            UID:59BC120D-E909-4A56-A70D-8E97914E51A3&#13;
                                            SEQUENCE:4&#13;
                                            DTSTAMP:20050520T014148Z&#13;
                                            DURATION:PT1H&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                        <D:current-user-privilege-set>
                                          <D:privilege>
                                            <D:read/>
                                          </D:privilege>
                                          <D:privilege>
                                            <D:write/>
                                          </D:privilege>
                                        </D:current-user-privilege-set>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

    assertThat(result1, equalXml(response1))
  }

  @Test
  void addTodo() {
    mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, UUID_TODO)
      .content(CALDAV_TODO)
      .contentType(TEXT_CALENDAR))
      .andExpect(etag(notNullValue()))
      .andExpect(status().isCreated())

    mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, UUID_TODO)
      .contentType(TEXT_CALENDAR))
      .andExpect(textCalendarContentType())
      .andExpect(text(CALDAV_TODO))
  }

  @Test
  void shouldReturnHtmlForUserPropName() throws Exception {
    mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
      .contentType(TEXT_CALENDAR)
      .content(CALDAV_EVENT))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))

    def request = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                            <D:propname />
                        </C:calendar-multiget>"""

    def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag/>
                                        <D:displayname/>
                                        <D:getlastmodified/>
                                        <D:iscollection/>
                                        <D:getcontenttype/>
                                        <D:supported-report-set/>
                                        <D:getcontentlength/>
                                        <D:resourcetype/>
                                        <D:current-user-privilege-set />
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response))
  }

  @Test
  void calendarHead() throws Exception {
    mockMvc.perform(head("/dav/{email}/calendar/", USER01))
      .andExpect(status().isOk())
      .andExpect(etag(notNullValue()))
  }

  @Test
  void calendarPropFind() throws Exception {
    def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>"157565ba8b0d3652b027c868d554f914"</D:getetag>
                                        <C:supported-calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav">
                                            <C:calendar-data C:content-type="text/calendar" C:version="2.0"/>
                                        </C:supported-calendar-data>
                                        <D:getlastmodified>Sat, 21 Nov 2015 21:11:00 GMT</D:getlastmodified>
                                        <D:iscollection>1</D:iscollection>
                                        <D:supported-report-set>
                                            <D:supported-report>
                                              <D:report>
                                                <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                              </D:report>
                                            </D:supported-report>
                                            <D:supported-report>
                                              <D:report>
                                                <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                              </D:report>
                                            </D:supported-report>
                                        </D:supported-report-set>
                                        <D:resourcetype>
                                            <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            <D:collection/>
                                        </D:resourcetype>
                                        <C:supported-collation-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                            <C:supported-collation>i;ascii-casemap</C:supported-collation>
                                            <C:supported-collation>i;octet</C:supported-collation>
                                        </C:supported-collation-set>
                                        <D:displayname>calendarDisplayName</D:displayname>
                                        <CARD:addressbook-home-set xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                            <D:href>/carldav/dav/test01@localhost.de/contacts</D:href>
                                        </CARD:addressbook-home-set>
                                        <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                            <C:comp name="VEVENT"/>
                                            <C:comp name="VJOURNAL"/>
                                            <C:comp name="VTODO"/>
                                        </C:supported-calendar-component-set>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">157565ba8b0d3652b027c868d554f914</CS:getctag>
                                        <D:current-user-principal>
                                            <D:href>/carldav/principals/users/${USER01}</D:href>
                                        </D:current-user-principal>
                                        <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/">#000000</apple:calendar-color>
                                        <D:current-user-privilege-set>
                                          <D:privilege>
                                            <D:read/>
                                          </D:privilege>
                                          <D:privilege>
                                            <D:write/>
                                          </D:privilege>
                                        </D:current-user-privilege-set>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

    mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
      .contentType(TEXT_XML))
      .andExpect(status().isMultiStatus())
      .andExpect(textXmlContentType())
      .andExpect(xml(response))
  }

  @Test
  void calendarPost() throws Exception {
    mockMvc.perform(post("/dav/{email}/calendar/", USER01)
      .contentType(TEXT_XML))
      .andExpect(status().isMethodNotAllowed())
      .andExpect(textXmlContentType())
      .andExpect(xml(notAllowed(POST).onCollection()))
  }

  @Test
  void calendarDelete() throws Exception {
    mockMvc.perform(delete("/dav/{email}/calendar/", USER01)
      .contentType(TEXT_XML))
      .andExpect(status().isNoContent())

    mockMvc.perform(get("/dav/{email}/calendar/", USER01)
      .contentType(TEXT_XML))
      .andExpect(status().isNotFound())
      .andExpect(textXmlContentType())
      .andExpect(xml(NOT_FOUND))
  }

  @Test
  void updateCalendarEvent() {
    def uid = "9bb25dec-c1e5-468c-92ea-0152f9f4c1ee"

    def request1 = """\
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
                        BEGIN:VEVENT
                        CREATED:20151215T214602Z
                        LAST-MODIFIED:20151215T214606Z
                        DTSTAMP:20151215T214606Z
                        UID:${uid}
                        SUMMARY:event1
                        DTSTART;TZID=Europe/Berlin:20151201T230000
                        DTEND;TZID=Europe/Berlin:20151202T000000
                        TRANSP:OPAQUE
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    final MvcResult mvcResult1 = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uid)
      .contentType(TEXT_CALENDAR)
      .content(request1))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))
      .andReturn()

    final String eTag1 = mvcResult1.getResponse().getHeader(ETAG)

    def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/${uid}.ics</D:href>
                        </C:calendar-multiget>"""

    def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/9bb25dec-c1e5-468c-92ea-0152f9f4c1ee.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag1}</D:getetag>
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
                                            BEGIN:VEVENT&#13;
                                            CREATED:20151215T214602Z&#13;
                                            LAST-MODIFIED:20151215T214606Z&#13;
                                            DTSTAMP:20151215T214606Z&#13;
                                            UID:9bb25dec-c1e5-468c-92ea-0152f9f4c1ee&#13;
                                            SUMMARY:event1&#13;
                                            DTSTART;TZID=Europe/Berlin:20151201T230000&#13;
                                            DTEND;TZID=Europe/Berlin:20151202T000000&#13;
                                            TRANSP:OPAQUE&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response1))

    def request3 = """\
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
                        BEGIN:VEVENT
                        CREATED:20151215T214602Z
                        LAST-MODIFIED:20151215T214624Z
                        DTSTAMP:20151215T214624Z
                        UID:9bb25dec-c1e5-468c-92ea-0152f9f4c1ee
                        SUMMARY:event2
                        DTSTART;TZID=Europe/Berlin:20151201T230000
                        DTEND;TZID=Europe/Berlin:20151202T000000
                        TRANSP:OPAQUE
                        X-MOZ-GENERATION:1
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    final MvcResult mvcResult2 = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uid)
      .contentType(TEXT_CALENDAR)
      .content(request3))
      .andExpect(status().isNoContent())
      .andExpect(etag(notNullValue()))
      .andReturn()


    final String eTag2 = mvcResult2.getResponse().getHeader(ETAG)

    def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/9bb25dec-c1e5-468c-92ea-0152f9f4c1ee.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag2}</D:getetag>
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
                                            BEGIN:VEVENT&#13;
                                            CREATED:20151215T214602Z&#13;
                                            LAST-MODIFIED:20151215T214624Z&#13;
                                            DTSTAMP:20151215T214624Z&#13;
                                            UID:9bb25dec-c1e5-468c-92ea-0152f9f4c1ee&#13;
                                            SUMMARY:event2&#13;
                                            DTSTART;TZID=Europe/Berlin:20151201T230000&#13;
                                            DTEND;TZID=Europe/Berlin:20151202T000000&#13;
                                            TRANSP:OPAQUE&#13;
                                            X-MOZ-GENERATION:1&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response2))
  }

  @Test
  void updateCalendarTodo() {
    def uid = UUID_TODO

    def mvcResult1 = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uid)
      .content(CALDAV_TODO)
      .contentType(TEXT_CALENDAR))
      .andExpect(etag(notNullValue()))
      .andExpect(status().isCreated())
      .andReturn()

    final String eTag1 = mvcResult1.getResponse().getHeader(ETAG)

    def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/${uid}.ics</D:href>
                        </C:calendar-multiget>"""

    def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/f3bc6436-991a-4a50-88b1-f27838e615c1.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag1}</D:getetag>
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
                                            BEGIN:VTODO&#13;
                                            CREATED:20151213T203529Z&#13;
                                            LAST-MODIFIED:20151213T203552Z&#13;
                                            DTSTAMP:20151213T203552Z&#13;
                                            UID:f3bc6436-991a-4a50-88b1-f27838e615c1&#13;
                                            SUMMARY:test task&#13;
                                            STATUS:NEEDS-ACTION&#13;
                                            RRULE:FREQ=WEEKLY&#13;
                                            DTSTART;TZID=Europe/Berlin:20151213T220000&#13;
                                            DUE;TZID=Europe/Berlin:20151214T220000&#13;
                                            PERCENT-COMPLETE:25&#13;
                                            BEGIN:VALARM&#13;
                                            ACTION:DISPLAY&#13;
                                            TRIGGER;VALUE=DURATION:-PT15M&#13;
                                            DESCRIPTION:Default Mozilla Description&#13;
                                            END:VALARM&#13;
                                            END:VTODO&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response1))

    def request3 = """\
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
                        RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        TZOFFSETFROM:+0200
                        TZOFFSETTO:+0100
                        TZNAME:CET
                        DTSTART:19701025T030000
                        RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VTODO
                        CREATED:20151213T203529Z
                        LAST-MODIFIED:20151213T203552Z
                        DTSTAMP:20151213T203552Z
                        UID:f3bc6436-991a-4a50-88b1-f27838e615c1
                        SUMMARY:test task
                        STATUS:NEEDS-ACTION
                        RRULE:FREQ=WEEKLY
                        DTSTART;TZID=Europe/Berlin:20151213T220000
                        DUE;TZID=Europe/Berlin:20151214T220000
                        PERCENT-COMPLETE:75
                        BEGIN:VALARM
                        ACTION:DISPLAY
                        TRIGGER;VALUE=DURATION:-PT15M
                        DESCRIPTION:junit test
                        END:VALARM
                        END:VTODO
                        END:VCALENDAR
                        """.stripIndent()

    final MvcResult mvcResult2 = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uid)
      .contentType(TEXT_CALENDAR)
      .content(request3))
      .andExpect(status().isNoContent())
      .andExpect(etag(notNullValue()))
      .andReturn()

    def eTag2 = mvcResult2.getResponse().getHeader(ETAG)

    def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/f3bc6436-991a-4a50-88b1-f27838e615c1.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag2}</D:getetag>
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
                                            BEGIN:VTODO&#13;
                                            CREATED:20151213T203529Z&#13;
                                            LAST-MODIFIED:20151213T203552Z&#13;
                                            DTSTAMP:20151213T203552Z&#13;
                                            UID:f3bc6436-991a-4a50-88b1-f27838e615c1&#13;
                                            SUMMARY:test task&#13;
                                            STATUS:NEEDS-ACTION&#13;
                                            RRULE:FREQ=WEEKLY&#13;
                                            DTSTART;TZID=Europe/Berlin:20151213T220000&#13;
                                            DUE;TZID=Europe/Berlin:20151214T220000&#13;
                                            PERCENT-COMPLETE:75&#13;
                                            BEGIN:VALARM&#13;
                                            ACTION:DISPLAY&#13;
                                            TRIGGER;VALUE=DURATION:-PT15M&#13;
                                            DESCRIPTION:junit test&#13;
                                            END:VALARM&#13;
                                            END:VTODO&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response2))
  }

  @Test
  void deleteCalendarTodo() {
    def uid = UUID_TODO

    def mvcResult1 = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uid)
      .content(CALDAV_TODO)
      .contentType(TEXT_CALENDAR))
      .andExpect(etag(notNullValue()))
      .andExpect(status().isCreated())
      .andReturn()

    final String eTag1 = mvcResult1.getResponse().getHeader(ETAG)

    def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/${uid}.ics</D:href>
                        </C:calendar-multiget>"""

    def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/f3bc6436-991a-4a50-88b1-f27838e615c1.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag1}</D:getetag>
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
                                            BEGIN:VTODO&#13;
                                            CREATED:20151213T203529Z&#13;
                                            LAST-MODIFIED:20151213T203552Z&#13;
                                            DTSTAMP:20151213T203552Z&#13;
                                            UID:f3bc6436-991a-4a50-88b1-f27838e615c1&#13;
                                            SUMMARY:test task&#13;
                                            STATUS:NEEDS-ACTION&#13;
                                            RRULE:FREQ=WEEKLY&#13;
                                            DTSTART;TZID=Europe/Berlin:20151213T220000&#13;
                                            DUE;TZID=Europe/Berlin:20151214T220000&#13;
                                            PERCENT-COMPLETE:25&#13;
                                            BEGIN:VALARM&#13;
                                            ACTION:DISPLAY&#13;
                                            TRIGGER;VALUE=DURATION:-PT15M&#13;
                                            DESCRIPTION:Default Mozilla Description&#13;
                                            END:VALARM&#13;
                                            END:VTODO&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response1))

    mockMvc.perform(delete("/dav/{email}/calendar/{uuid}.ics", USER01, uid))
      .andExpect(status().isNoContent())

    def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/${uid}.ics</D:href>
                                <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:response>
                        </D:multistatus>"""

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response2))
  }

  @Test
  void deleteCalendarEvent() {
    def uid = "9bb25dec-c1e5-468c-92ea-0152f9f4c1ee"

    def request1 = """\
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
                        BEGIN:VEVENT
                        CREATED:20151215T214602Z
                        LAST-MODIFIED:20151215T214606Z
                        DTSTAMP:20151215T214606Z
                        UID:${uid}
                        SUMMARY:event1
                        DTSTART;TZID=Europe/Berlin:20151201T230000
                        DTEND;TZID=Europe/Berlin:20151202T000000
                        TRANSP:OPAQUE
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    final MvcResult mvcResult1 = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uid)
      .contentType(TEXT_CALENDAR)
      .content(request1))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))
      .andReturn()

    final String eTag1 = mvcResult1.getResponse().getHeader(ETAG)

    def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/carldav/dav/test01@localhost.de/calendar/${uid}.ics</D:href>
                        </C:calendar-multiget>"""

    def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/9bb25dec-c1e5-468c-92ea-0152f9f4c1ee.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eTag1}</D:getetag>
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
                                            BEGIN:VEVENT&#13;
                                            CREATED:20151215T214602Z&#13;
                                            LAST-MODIFIED:20151215T214606Z&#13;
                                            DTSTAMP:20151215T214606Z&#13;
                                            UID:9bb25dec-c1e5-468c-92ea-0152f9f4c1ee&#13;
                                            SUMMARY:event1&#13;
                                            DTSTART;TZID=Europe/Berlin:20151201T230000&#13;
                                            DTEND;TZID=Europe/Berlin:20151202T000000&#13;
                                            TRANSP:OPAQUE&#13;
                                            END:VEVENT&#13;
                                            END:VCALENDAR&#13;
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>
                        """

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(textXmlContentType())
      .andExpect(xml(response1))

    mockMvc.perform(delete("/dav/{email}/calendar/{uuid}.ics", USER01, uid))
      .andExpect(status().isNoContent())

    def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/${uid}.ics</D:href>
                                <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:response>
                        </D:multistatus>"""

    mockMvc.perform(report("/dav/{email}/calendar/", USER01)
      .content(request2)
      .contentType(TEXT_XML))
      .andExpect(status().isMultiStatus())
      .andExpect(textXmlContentType())
      .andExpect(xml(response2))
  }

  @Test
  void eventException() {
    mockMvc.perform(put("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01)
      .contentType(TEXT_CALENDAR)
      .content(ADD_VEVENT_RECURRENCE_REQUEST1))
      .andExpect(status().isCreated())

    def response2 = """\
                            BEGIN:VCALENDAR
                            CALSCALE:GREGORIAN
                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                            VERSION:2.0
                            BEGIN:VEVENT
                            UID:20160123T135858Z-25739-1000-1796-13@localhost
                            DTSTAMP:20160119T173941Z
                            DTSTART;TZID=Europe/Berlin:20160123T145800
                            DTEND;TZID=Europe/Berlin:20160123T155800
                            SEQUENCE:2
                            SUMMARY:event exception
                            CLASS:PUBLIC
                            TRANSP:OPAQUE
                            RRULE:FREQ=DAILY;COUNT=3
                            EXDATE;VALUE=DATE:20160124
                            CREATED:20160123T135931Z
                            LAST-MODIFIED:20160123T135931Z
                            END:VEVENT
                            END:VCALENDAR
                            """.stripIndent()

    mockMvc.perform(get("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01, uuid))
      .andExpect(textCalendarContentType())
      .andExpect(status().isOk())
      .andExpect(text(response2))
  }

  @Test
  void recurrenceId() {
    eventException()

    mockMvc.perform(put("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01)
      .contentType(TEXT_CALENDAR)
      .content(UPDATE_VEVENT_RECURRENCE_REQUEST1))
      .andExpect(status().isNoContent())

    def response2 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=Europe/Berlin:20160123T155900
                        DTEND;TZID=Europe/Berlin:20160123T165900
                        SEQUENCE:2
                        SUMMARY:event exception
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        RRULE;X-EVOLUTION-ENDDATE=20160125T145900Z:FREQ=DAILY;COUNT=3
                        EXDATE;VALUE=DATE:20160124
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T135931Z
                        END:VEVENT
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=Europe/Berlin:20160123T155900
                        DTEND;TZID=Europe/Berlin:20160123T165900
                        SEQUENCE:3
                        SUMMARY:event exception2
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T145948Z
                        RECURRENCE-ID;TZID=Europe/Berlin:20160123T155900
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    mockMvc.perform(get("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01, uuid))
      .andExpect(textCalendarContentType())
      .andExpect(status().isOk())
      .andExpect(text(response2))
  }

  @Test
  void recurrenceIdDifferentTimezone() {
    eventException()

    mockMvc.perform(put("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01)
      .contentType(TEXT_CALENDAR)
      .content(UPDATE_VEVENT_RECURRENCE_REQUEST2))
      .andExpect(status().isNoContent())

    def response2 = """\
                            BEGIN:VCALENDAR
                            CALSCALE:GREGORIAN
                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                            VERSION:2.0
                            BEGIN:VEVENT
                            UID:20160123T135858Z-25739-1000-1796-13@localhost
                            DTSTAMP:20160119T173941Z
                            DTSTART;TZID=Europe/Berlin:20160123T155900
                            DTEND;TZID=Europe/Berlin:20160123T165900
                            SEQUENCE:2
                            SUMMARY:event exception
                            CLASS:PUBLIC
                            TRANSP:OPAQUE
                            RRULE;X-EVOLUTION-ENDDATE=20160125T145900Z:FREQ=DAILY;COUNT=3
                            EXDATE;VALUE=DATE:20160124
                            CREATED:20160123T135931Z
                            LAST-MODIFIED:20160123T135931Z
                            END:VEVENT
                            BEGIN:VEVENT
                            UID:20160123T135858Z-25739-1000-1796-13@localhost
                            DTSTAMP:20160119T173941Z
                            DTSTART;TZID=Europe/Lisbon:20160121T155900
                            SEQUENCE:3
                            SUMMARY:event exception2
                            CLASS:PUBLIC
                            TRANSP:OPAQUE
                            CREATED:20160123T135931Z
                            LAST-MODIFIED:20160123T145948Z
                            RECURRENCE-ID;TZID=Europe/Berlin:20160123T155900
                            END:VEVENT
                            END:VCALENDAR
                            """.stripIndent()

    mockMvc.perform(get("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01, uuid))
      .andExpect(textCalendarContentType())
      .andExpect(status().isOk())
      .andExpect(text(response2))
  }

  @Test
  void removeFromRecurrenceVEventShouldHaveExdate20160125() {
    recurrenceId()

    def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T165900
                        SEQUENCE:2
                        SUMMARY:event exception
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        RRULE;X-EVOLUTION-ENDDATE=20160125T145900Z:FREQ=DAILY;COUNT=3
                        EXDATE;VALUE=DATE:20160124
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T135931Z
                        EXDATE:20160125T145900Z
                        END:VEVENT
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T165900
                        SEQUENCE:3
                        SUMMARY:event exception2
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T145948Z
                        RECURRENCE-ID;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    mockMvc.perform(put("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01)
      .contentType(TEXT_CALENDAR)
      .content(request1))
      .andExpect(status().isNoContent())

    def response2 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=Europe/Berlin:20160123T155900
                        DTEND;TZID=Europe/Berlin:20160123T165900
                        SEQUENCE:2
                        SUMMARY:event exception
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        RRULE;X-EVOLUTION-ENDDATE=20160125T145900Z:FREQ=DAILY;COUNT=3
                        EXDATE;VALUE=DATE:20160124
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T135931Z
                        EXDATE:20160125T145900Z
                        END:VEVENT
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=Europe/Berlin:20160123T155900
                        DTEND;TZID=Europe/Berlin:20160123T165900
                        SEQUENCE:3
                        SUMMARY:event exception2
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T145948Z
                        RECURRENCE-ID;TZID=Europe/Berlin:20160123T155900
                        END:VEVENT
                        END:VCALENDAR
                            """.stripIndent()

    mockMvc.perform(get("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01, uuid))
      .andExpect(textCalendarContentType())
      .andExpect(status().isOk())
      .andExpect(text(response2))
  }

  @Test
  void alarm() {
    def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T152950Z-30167-1000-1796-43@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160124T162900
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160124T172900
                        SEQUENCE:2
                        SUMMARY:complex alarm
                        CLASS:PUBLIC
                        TRANSP:TRANSPARENT
                        RRULE:FREQ=DAILY;COUNT=4;INTERVAL=4
                        EXDATE;VALUE=DATE:20160126
                        CREATED:20160123T153145Z
                        LAST-MODIFIED:20160123T153145Z
                        BEGIN:VALARM
                        X-EVOLUTION-ALARM-UID:20160123T153000Z-30167-1000-1796-50@localhost
                        ACTION:DISPLAY
                        TRIGGER;VALUE=DURATION;RELATED=START:-PT15M
                        DESCRIPTION:complex alarm
                        END:VALARM
                        BEGIN:VALARM
                        X-EVOLUTION-ALARM-UID:20160123T153107Z-30167-1000-1796-57@localhost
                        TRIGGER;VALUE=DURATION;RELATED=END:PT15M
                        ACTION:DISPLAY
                        REPEAT:2
                        DURATION:PT4M
                        DESCRIPTION:complex alarm
                        END:VALARM
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    mockMvc.perform(put("/dav/{email}/calendar/20160123T152950Z-30167-1000-1796-43_localhost-20160123T153145Z.ics", USER01)
      .contentType(TEXT_CALENDAR)
      .content(request1))
      .andExpect(status().isCreated())

    def response2 = """\
                            BEGIN:VCALENDAR
                            CALSCALE:GREGORIAN
                            PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                            VERSION:2.0
                            BEGIN:VEVENT
                            UID:20160123T152950Z-30167-1000-1796-43@localhost
                            DTSTAMP:20160119T173941Z
                            DTSTART;TZID=Europe/Berlin:20160124T162900
                            DTEND;TZID=Europe/Berlin:20160124T172900
                            SEQUENCE:2
                            SUMMARY:complex alarm
                            CLASS:PUBLIC
                            TRANSP:TRANSPARENT
                            RRULE:FREQ=DAILY;COUNT=4;INTERVAL=4
                            EXDATE;VALUE=DATE:20160126
                            CREATED:20160123T153145Z
                            LAST-MODIFIED:20160123T153145Z
                            BEGIN:VALARM
                            X-EVOLUTION-ALARM-UID:20160123T153000Z-30167-1000-1796-50@localhost
                            ACTION:DISPLAY
                            TRIGGER;VALUE=DURATION;RELATED=START:-PT15M
                            DESCRIPTION:complex alarm
                            END:VALARM
                            BEGIN:VALARM
                            X-EVOLUTION-ALARM-UID:20160123T153107Z-30167-1000-1796-57@localhost
                            TRIGGER;VALUE=DURATION;RELATED=END:PT15M
                            ACTION:DISPLAY
                            REPEAT:2
                            DURATION:PT4M
                            DESCRIPTION:complex alarm
                            END:VALARM
                            END:VEVENT
                            END:VCALENDAR
                            """.stripIndent()

    mockMvc.perform(get("/dav/{email}/calendar/20160123T152950Z-30167-1000-1796-43_localhost-20160123T153145Z.ics", USER01, uuid))
      .andExpect(textCalendarContentType())
      .andExpect(status().isOk())
      .andExpect(text(response2))
  }

  @Test
  void doubleDelete() {
    def uid = UUID_TODO

    mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uid)
      .content(CALDAV_TODO)
      .contentType(TEXT_CALENDAR))
      .andExpect(status().isCreated())

    mockMvc.perform(delete("/dav/{email}/calendar/{uuid}.ics", USER01, uid))
      .andExpect(status().isNoContent())

    mockMvc.perform(delete("/dav/{email}/calendar/{uuid}.ics", USER01, uid))
      .andExpect(status().isNoContent())
  }

  @Disabled("unsupported")
  @WithUserDetails(USER02)
  @Test
  void depthInfinity() {
    mockMvc.perform(put("/dav/{email}/calendar/subcalendar/{uuid}.ics", USER02, UUID_TODO)
      .content(CALDAV_TODO)
      .contentType(TEXT_CALENDAR))
      .andExpect(status().isCreated())

    def request2 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VTODO" />
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

    def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/test02@localhost.de/calendar/subcalendar/f3bc6436-991a-4a50-88b1-f27838e615c1.ics</D:href>
                                <D:status>HTTP/1.1 200 OK</D:status>
                              </D:response>
                            </D:multistatus>"""

    mockMvc.perform(report("/dav/{email}/calendar", USER02)
      .contentType(APPLICATION_XML)
      .content(request2)
      .header("Depth", "infinity"))
      .andExpect(status().isMultiStatus())
      .andExpect(textXmlContentType())
      .andExpect(xml(response2))
  }

  @Test
  void collectionResourceGet() {
    mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, UUID_TODO)
      .content(CALDAV_TODO)
      .contentType(TEXT_CALENDAR))
      .andExpect(status().isCreated())

    def result2 = mockMvc.perform(get("/dav/{email}/calendar", USER01))
      .andExpect(status().isOk())
      .andExpect(textHtmlContentType())
      .andReturn().getResponse()

    def lastModified = result2.getHeader("Last-Modified")
    def getctag = result2.getHeader("ETag").replaceAll('"', "")

    def response2 = """\
                            <html>
                            <head><title>calendarDisplayName</title></head>
                            <body>
                            <h1>calendarDisplayName</h1>
                            Parent: <a href="/carldav/dav/${USER01}/">homeCollection</a></li>
                            <h2>Members</h2>
                            <ul>
                            <li><a href="/carldav/dav/${USER01}/calendar/f3bc6436-991a-4a50-88b1-f27838e615c1.ics">test task</a></li>
                            </ul>
                            <h2>Properties</h2>
                            <dl>
                            <dt>{urn:ietf:params:xml:ns:carddav}addressbook-home-set</dt><dd>/carldav/dav/${USER01}/contacts</dd>
                            <dt>{http://apple.com/ns/ical/}calendar-color</dt><dd>#000000</dd>
                            <dt>{DAV:}current-user-principal</dt><dd>/carldav/principals/users/${USER01}</dd>
                            <dt>{DAV:}current-user-privilege-set</dt><dd>[{DAV:}read, {DAV:}write]</dd>
                            <dt>{DAV:}displayname</dt><dd>calendarDisplayName</dd>
                            <dt>{http://calendarserver.org/ns/}getctag</dt><dd>${getctag}</dd>
                            <dt>{DAV:}getetag</dt><dd>&quot;${getctag}&quot;</dd>
                            <dt>{DAV:}getlastmodified</dt><dd>${lastModified}</dd>
                            <dt>{DAV:}iscollection</dt><dd>1</dd>
                            <dt>{DAV:}resourcetype</dt><dd>{DAV:}collection, {urn:ietf:params:xml:ns:caldav}calendar</dd>
                            <dt>{urn:ietf:params:xml:ns:caldav}supported-calendar-component-set</dt><dd>VEVENT, VJOURNAL, VTODO</dd>
                            <dt>{urn:ietf:params:xml:ns:caldav}supported-calendar-data</dt><dd>-- no value --</dd>
                            <dt>{urn:ietf:params:xml:ns:caldav}supported-collation-set</dt><dd>i;ascii-casemap, i;octet</dd>
                            <dt>{DAV:}supported-report-set</dt><dd>{urn:ietf:params:xml:ns:caldav}calendar-multiget, {urn:ietf:params:xml:ns:caldav}calendar-query</dd>
                            </dl>
                            <p>
                            <a href="/carldav/principals/users/${USER01}">Principal resource</a><br>
                            <p>
                            <a href="/carldav/dav/${USER01}/">Home collection</a><br>
                            </body></html>
                            """.stripIndent()

    assertThat(result2.getContentAsString(), is(response2))
  }

  @Test
  void reportWithDepthZeroOnCalendarEventValidTimerange() {
    mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
      .contentType(TEXT_CALENDAR)
      .content(DavDroidData.ADD_VEVENT_REQUEST1)
      .header("If-None-Match", "*"))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))

    def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:time-range start="20101210T071757Z" end="20360218T071757Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

    def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/test01@localhost.de/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics</D:href>
                                <D:status>HTTP/1.1 200 OK</D:status>
                              </D:response>
                            </D:multistatus>"""

    mockMvc.perform(report("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
      .contentType(APPLICATION_XML)
      .content(request1)
      .header("Depth", "0"))
      .andExpect(status().isMultiStatus())
      .andExpect(textXmlContentType())
      .andExpect(xml(response1))
  }

  @Test
  void reportWithZeroDepthOnCalendarEventInvalidTimerange() {
    mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
      .contentType(TEXT_CALENDAR)
      .content(DavDroidData.ADD_VEVENT_REQUEST1)
      .header("If-None-Match", "*"))
      .andExpect(status().isCreated())
      .andExpect(etag(notNullValue()))

    def request1 = """\
                        <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav" xmlns:D="DAV:">
                          <C:filter>
                            <C:comp-filter name="VCALENDAR">
                              <C:comp-filter name="VEVENT">
                                <C:time-range start="19701210T071757Z" end="19710218T071757Z"/>
                              </C:comp-filter>
                            </C:comp-filter>
                          </C:filter>
                        </C:calendar-query>"""

    def response1 = """<D:multistatus xmlns:D="DAV:" />"""

    mockMvc.perform(report("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
      .contentType(APPLICATION_XML)
      .content(request1)
      .header("Depth", "0"))
      .andExpect(status().isMultiStatus())
      .andExpect(textXmlContentType())
      .andExpect(xml(response1))
  }

  @Disabled
  @Test
  void reportWithZeroDepthOnContactsItemValidTimerange() {
    Assertions.fail("not implemented yet")
  }
}
