package dav.user

import carldav.service.generator.IdGenerator
import carldav.service.time.TimeService
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.MvcResult
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.builder.GeneralData

import static org.hamcrest.Matchers.notNullValue
import static org.mockito.Mockito.when
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpHeaders.ETAG
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.builder.GeneralData.*
import static testutil.builder.GeneralResponse.NOT_FOUND
import static testutil.builder.GeneralResponse.RESOURCE_MUST_BE_NULL
import static testutil.builder.MethodNotAllowedBuilder.notAllowed
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomRequestBuilders.*
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
public class CalendarTests extends IntegrationTestSupport {

    private final String uuid = GeneralData.UUID;
    private final String uuid2 = GeneralData.UUID_EVENT2;

    @Autowired
    private TimeService timeService;

    @Autowired
    private IdGenerator idGenerator;

    @Before
    public void before() {
        when(timeService.getCurrentTime()).thenReturn(new Date(3600));
        when(idGenerator.nextStringIdentifier()).thenReturn("1");
    }

    @Test
    public void shouldReturnHtmlForUser() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        def getRequest = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/dav/test01%40localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                        </C:calendar-multiget>"""

        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01%40localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
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
                .andExpect(xml(response));
    }

    @Test
    public void putCalendarItem() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid2)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT2))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        def getRequest = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/dav/test01%40localhost.de/calendar/18f0e0e5-4e1e-4e0d-b317-0d861d3e575c.ics</D:href>
                        </C:calendar-multiget>"""

        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01%40localhost.de/calendar/18f0e0e5-4e1e-4e0d-b317-0d861d3e575c.ics</D:href>
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
                .andExpect(xml(response));
    }

    @Test
    public void calendarGetItem() {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();

        mockMvc.perform(get("/dav/{email}/calendar/{uid}.ics", USER01, uuid)
                .contentType(TEXT_XML))
                .andExpect(textCalendarContentType())
                .andExpect(status().isOk())
                .andExpect(text(CALDAV_EVENT));
    }

    @Test
    public void shouldReturnHtmlForUserAllProp() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        def request = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/dav/test01%40localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                            <D:allprop />
                        </C:calendar-multiget>"""

        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01%40localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:creationdate>1970-01-01T00:00:03Z</D:creationdate>
                                        <D:getetag>${eTag}</D:getetag>
                                        <D:getlastmodified>Thu, 01 Jan 1970 00:00:03 GMT</D:getlastmodified>
                                        <D:iscollection>0</D:iscollection>
                                        <D:supported-report-set>
                                            <D:supported-report>
                                                <D:report>
                                                    <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                </D:report>
                                            </D:supported-report>
                                        </D:supported-report-set>
                                        <D:getcontentlength>920</D:getcontentlength>
                                        <D:resourcetype/>
                                        <cosmo:uuid xmlns:cosmo="http://osafoundation.org/cosmo/DAV">1</cosmo:uuid>
                                        <D:displayname>all entities meeting</D:displayname>
                                        <D:getcontenttype>text/calendar; charset=UTF-8</D:getcontenttype>
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
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .content(request)
                .contentType(TEXT_XML))
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void addTodo() {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, UUID_TODO)
                .content(CALDAV_TODO)
                .contentType(TEXT_CALENDAR))
                .andExpect(etag(notNullValue()))
                .andExpect(status().isCreated())

        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, UUID_TODO)
                .contentType(TEXT_CALENDAR))
                .andExpect(textCalendarContentType())
                .andExpect(text(CALDAV_TODO));
    }

    @Test
    public void shouldReturnHtmlForUserPropName() throws Exception {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()));

        def request = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/dav/test01%40localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                            <D:propname />
                        </C:calendar-multiget>"""

        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01%40localhost.de/calendar/59BC120D-E909-4A56-A70D-8E97914E51A3.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:creationdate/>
                                        <D:getetag/>
                                        <D:getlastmodified/>
                                        <D:iscollection/>
                                        <D:supported-report-set/>
                                        <D:getcontentlength/>
                                        <D:resourcetype/>
                                        <cosmo:uuid xmlns:cosmo="http://osafoundation.org/cosmo/DAV"/>
                                        <D:displayname/>
                                        <D:getcontenttype/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .content(request)
                .contentType(TEXT_XML))
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void shouldForbidSameCalendar() throws Exception {
        mockMvc.perform(mkcalendar("/dav/{email}/calendar/", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(RESOURCE_MUST_BE_NULL));
    }

    @Test
    public void shouldCreateCalendar() throws Exception {
        mockMvc.perform(get("/dav/{email}/newcalendar/", USER01)
                .contentType(TEXT_XML))
                .andExpect(textXmlContentType())
                .andExpect(status().isNotFound())
                .andExpect(xml(NOT_FOUND));

        mockMvc.perform(mkcalendar("/dav/{email}/newcalendar/", USER01)
                .contentType(TEXT_XML))
                .andExpect(status().isCreated());

        def response = """\
                        <html>
                        <head><title>newcalendar</title></head>
                        <body>
                        <h1>newcalendar</h1>
                        Parent: <a href="/dav/test01@localhost.de/">no name</a></li>
                        <h2>Members</h2>
                        <ul>
                        </ul>
                        <h2>Properties</h2>
                        <dl>
                        <dt>{urn:ietf:params:xml:ns:caldav}calendar-description</dt><dd>newcalendar</dd>
                        <dt>{urn:ietf:params:xml:ns:xcaldavoneandone}calendar-visible</dt><dd>false</dd>
                        <dt>{DAV:}creationdate</dt><dd>1970-01-01T00:00:03Z</dd>
                        <dt>{DAV:}displayname</dt><dd>newcalendar</dd>
                        <dt>{http://calendarserver.org/ns/}getctag</dt><dd>1d21bc1d460b1085d53e3def7f7380f6</dd>
                        <dt>{DAV:}getetag</dt><dd>&quot;1d21bc1d460b1085d53e3def7f7380f6&quot;</dd>
                        <dt>{DAV:}getlastmodified</dt><dd>Thu, 01 Jan 1970 00:00:03 GMT</dd>
                        <dt>{DAV:}iscollection</dt><dd>1</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}max-resource-size</dt><dd>10485760</dd>
                        <dt>{DAV:}resourcetype</dt><dd>{DAV:}collection, {urn:ietf:params:xml:ns:caldav}calendar</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}supported-calendar-component-set</dt><dd>VAVAILABILITY, VEVENT, VFREEBUSY, VJOURNAL, VTODO</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}supported-calendar-data</dt><dd>-- no value --</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}supported-collation-set</dt><dd>i;ascii-casemap, i;octet</dd>
                        <dt>{DAV:}supported-report-set</dt><dd>{urn:ietf:params:xml:ns:caldav}calendar-multiget</dd>
                        <dt>{http://osafoundation.org/cosmo/DAV}uuid</dt><dd>1</dd>
                        </dl>
                        </body></html>
                        """.stripIndent()

        mockMvc.perform(get("/dav/{email}/newcalendar/", USER01)
                .contentType(TEXT_XML))
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }

    @Test
    public void calendarOptions() throws Exception {
        mockMvc.perform(options("/dav/{email}/calendar/", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, PUT, DELETE, REPORT"));
    }

    @Test
    public void calendarHead() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(notNullValue()));
    }

    @Test
    public void calendarPropFind() throws Exception {
        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:creationdate>2015-11-21T21:11:00Z</D:creationdate>
                                        <D:getetag>"NVy57RJot0LhdYELkMDJ9gQZjOM="</D:getetag>
                                        <C:supported-calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav">
                                            <C:calendar-data C:content-type="text/calendar" C:version="2.0"/>
                                        </C:supported-calendar-data>
                                        <C:calendar-color xmlns:C="urn:ietf:params:xml:ns:xcaldavoneandone">#f0f0f0</C:calendar-color>
                                        <D:getlastmodified>Sat, 21 Nov 2015 21:11:00 GMT</D:getlastmodified>
                                        <D:iscollection>1</D:iscollection>
                                        <D:supported-report-set>
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
                                        <C:max-resource-size xmlns:C="urn:ietf:params:xml:ns:caldav">10485760</C:max-resource-size>
                                        <cosmo:uuid xmlns:cosmo="http://osafoundation.org/cosmo/DAV">a172ed34-0106-4616-bb40-a416a8305465</cosmo:uuid>
                                        <C:calendar-visible xmlns:C="urn:ietf:params:xml:ns:xcaldavoneandone">true</C:calendar-visible>
                                        <D:displayname>calendarDisplayName</D:displayname>
                                        <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                            <C:comp name="VEVENT"/>
                                            <C:comp name="VAVAILABILITY"/>
                                            <C:comp name="VFREEBUSY"/>
                                            <C:comp name="VJOURNAL"/>
                                            <C:comp name="VTODO"/>
                                        </C:supported-calendar-component-set>
                                        <CS:getctag xmlns:CS="http://calendarserver.org/ns/">NVy57RJot0LhdYELkMDJ9gQZjOM=</CS:getctag>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void calendarPost() throws Exception {
        mockMvc.perform(post("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(POST).onCollection()));
    }

    @Test
    public void calendarPropPatch() throws Exception {
        def request = """\
                        <D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:set>
                                <D:prop>
                                    <Z:authors>
                                        <Z:Author>Jim Whitehead</Z:Author>
                                        <Z:Author>Roy Fielding</Z:Author>
                                    </Z:authors>
                                </D:prop>
                            </D:set>
                            <D:remove>
                                <D:prop><Z:Copyright-Owner/></D:prop>
                            </D:remove>
                        </D:propertyupdate>"""

        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <Z:Copyright-Owner xmlns:Z="http://www.w3.com/standards/z39.50/"/>
                                        <Z:authors xmlns:Z="http://www.w3.com/standards/z39.50/"/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(proppatch("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void calendarDelete() throws Exception {
        mockMvc.perform(delete("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML))
                .andExpect(status().isNotFound())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_FOUND))
    }

    @Test
    public void updateCalendarEvent() {
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
                .andReturn();

        final String eTag1 = mvcResult1.getResponse().getHeader(ETAG);

        def request2 = """\
                        <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                            <D:prop>
                                <D:getetag />
                                <C:calendar-data />
                            </D:prop>
                            <D:href>/dav/test01%40localhost.de/calendar/${uid}.ics</D:href>
                        </C:calendar-multiget>"""

        def response1 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01%40localhost.de/calendar/9bb25dec-c1e5-468c-92ea-0152f9f4c1ee.ics</D:href>
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
                .andReturn();


        final String eTag2 = mvcResult2.getResponse().getHeader(ETAG);

        def response2 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01%40localhost.de/calendar/9bb25dec-c1e5-468c-92ea-0152f9f4c1ee.ics</D:href>
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
}
