package dav.user

import carldav.service.generator.IdGenerator
import carldav.service.time.TimeService
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MvcResult
import org.unitedinternet.cosmo.IntegrationTestSupport
import util.TestUser

import static carldav.util.builder.GeneralResponse.*
import static carldav.util.builder.MethodNotAllowedBuilder.notAllowed
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.notNullValue
import static org.mockito.Mockito.when
import static org.springframework.http.HttpHeaders.*
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static util.mockmvc.CustomResultMatchers.html
import static util.mockmvc.CustomResultMatchers.xml
import static util.HeaderUtil.user
import static util.TestUser.TEST01
import static util.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static util.mockmvc.CustomRequestBuilders.*
import static util.mockmvc.CustomResultMatchers.*
import static util.xmlunit.XmlMatcher.equalXml

/**
 * @author Kamill Sokol
 */
public class CalendarTests extends IntegrationTestSupport {

    private final TestUser testUser = TEST01;
    private final String uuid = "59BC120D-E909-4A56-A70D-8E97914E51A3";

    private final String CALDAV_EVENT = """\
                        BEGIN:VCALENDAR
                        VERSION:2.0
                        X-WR-CALNAME:Work
                        PRODID:-//Apple Computer\\, Inc//iCal 2.0//EN
                        X-WR-RELCALID:21654AA6-F774-4918-80B8-F0C8CABC7737
                        X-WR-TIMEZONE:US/Pacific
                        CALSCALE:GREGORIAN
                        BEGIN:VTIMEZONE
                        TZID:US/Pacific
                        LAST-MODIFIED:20050812T212029Z
                        BEGIN:DAYLIGHT
                        DTSTART:20040404T100000
                        TZOFFSETTO:-0700
                        TZOFFSETFROM:+0000
                        TZNAME:PDT
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        DTSTART:20041031T020000
                        TZOFFSETTO:-0800
                        TZOFFSETFROM:-0700
                        TZNAME:PST
                        END:STANDARD
                        BEGIN:DAYLIGHT
                        DTSTART:20050403T010000
                        TZOFFSETTO:-0700
                        TZOFFSETFROM:-0800
                        TZNAME:PDT
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        DTSTART:20051030T020000
                        TZOFFSETTO:-0800
                        TZOFFSETFROM:-0700
                        TZNAME:PST
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VEVENT
                        DTSTART;TZID=US/Pacific:20050602T120000
                        LOCATION:Whoville
                        SUMMARY:all entities meeting
                        UID:59BC120D-E909-4A56-A70D-8E97914E51A3
                        SEQUENCE:4
                        DTSTAMP:20050520T014148Z
                        DURATION:PT1H
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

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
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(testUser))
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

        mockMvc.perform(report("/dav/{email}/calendar/", testUser.getUid())
                .content(getRequest)
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void shouldReturnHtmlForUserAllProp() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(testUser))
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
                                        <D:principal-collection-set>
                                            <D:href>/dav/users</D:href>
                                        </D:principal-collection-set>
                                        <D:getlastmodified>Thu, 01 Jan 1970 00:00:03 GMT</D:getlastmodified>
                                        <D:iscollection>0</D:iscollection>
                                        <D:owner>
                                            <D:href>/dav/users/test01@localhost.de</D:href>
                                        </D:owner>
                                        <D:supported-report-set>
                                            <D:supported-report>
                                                <D:report>
                                                    <C:free-busy-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                </D:report>
                                            </D:supported-report>
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
                                        <ticket:ticketdiscovery xmlns:ticket="http://www.xythos.com/namespaces/StorageServer"/>
                                        <D:acl>
                                            <D:ace>
                                                <D:principal>
                                                    <D:unauthenticated/>
                                                </D:principal>
                                                <D:deny>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:all/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:deny>
                                                <D:protected/>
                                            </D:ace>
                                            <D:ace>
                                                <D:principal>
                                                    <D:property>
                                                        <D:owner/>
                                                    </D:property>
                                                </D:principal>
                                                <D:grant>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:all/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:grant>
                                                <D:protected/>
                                            </D:ace>
                                            <D:ace>
                                                <D:principal>
                                                    <D:all/>
                                                </D:principal>
                                                <D:grant>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:read-current-user-privilege-set/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:grant>
                                                <D:protected/>
                                            </D:ace>
                                            <D:ace>
                                                <D:principal>
                                                    <D:all/>
                                                </D:principal>
                                                <D:deny>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:all/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:deny>
                                                <D:protected/>
                                            </D:ace>
                                        </D:acl>
                                        <D:getcontentlength>920</D:getcontentlength>
                                        <D:resourcetype/>
                                        <cosmo:uuid xmlns:cosmo="http://osafoundation.org/cosmo/DAV">1</cosmo:uuid>
                                        <D:current-user-privilege-set>
                                            <D:privilege>
                                                <D:read/>
                                            </D:privilege>
                                            <D:privilege>
                                                <D:write/>
                                            </D:privilege>
                                            <D:privilege>
                                                <D:read-current-user-privilege-set/>
                                            </D:privilege>
                                            <D:privilege>
                                                <C:read-free-busy xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            </D:privilege>
                                        </D:current-user-privilege-set>
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

        mockMvc.perform(report("/dav/{email}/calendar/", testUser.getUid())
                .content(request)
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void shouldReturnHtmlForUserPropName() throws Exception {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(testUser))
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
                                        <D:principal-collection-set/>
                                        <D:getlastmodified/>
                                        <D:iscollection/>
                                        <D:owner/>
                                        <D:supported-report-set/>
                                        <ticket:ticketdiscovery xmlns:ticket="http://www.xythos.com/namespaces/StorageServer"/>
                                        <D:acl/>
                                        <D:getcontentlength/>
                                        <D:resourcetype/>
                                        <cosmo:uuid xmlns:cosmo="http://osafoundation.org/cosmo/DAV"/>
                                        <D:current-user-privilege-set/>
                                        <D:displayname/>
                                        <D:getcontenttype/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", testUser.getUid())
                .content(request)
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void shouldForbidSameCalendar() throws Exception {
        mockMvc.perform(mkcalendar("/dav/{email}/calendar/", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(RESOURCE_MUST_BE_NULL));
    }

    @Test
    public void shouldCreateCalendar() throws Exception {
        mockMvc.perform(get("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(textXmlContentType())
                .andExpect(status().isNotFound())
                .andExpect(xml(NOT_FOUND));

        mockMvc.perform(mkcalendar("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
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
                        <dt>{DAV:}acl</dt><dd>not implemented yet</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}calendar-description</dt><dd>newcalendar</dd>
                        <dt>{urn:ietf:params:xml:ns:xcaldavoneandone}calendar-visible</dt><dd>false</dd>
                        <dt>{DAV:}creationdate</dt><dd>1970-01-01T00:00:03Z</dd>
                        <dt>{DAV:}current-user-privilege-set</dt><dd>{DAV:}read, {DAV:}read-current-user-privilege-set, {DAV:}write, {urn:ietf:params:xml:ns:caldav}read-free-busy</dd>
                        <dt>{DAV:}displayname</dt><dd>newcalendar</dd>
                        <dt>{http://osafoundation.org/cosmo/DAV}exclude-free-busy-rollup</dt><dd>false</dd>
                        <dt>{http://calendarserver.org/ns/}getctag</dt><dd>W5kE5atb5Q+0h5Ll3tlZKTW7S2o=</dd>
                        <dt>{DAV:}getetag</dt><dd>&quot;W5kE5atb5Q+0h5Ll3tlZKTW7S2o=&quot;</dd>
                        <dt>{DAV:}getlastmodified</dt><dd>Thu, 01 Jan 1970 00:00:03 GMT</dd>
                        <dt>{DAV:}iscollection</dt><dd>1</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}max-resource-size</dt><dd>10485760</dd>
                        <dt>{DAV:}owner</dt><dd>/dav/users/test01@localhost.de</dd>
                        <dt>{DAV:}principal-collection-set</dt><dd>/dav/users</dd>
                        <dt>{DAV:}resourcetype</dt><dd>{DAV:}collection, {urn:ietf:params:xml:ns:caldav}calendar</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}supported-calendar-component-set</dt><dd>VAVAILABILITY, VEVENT, VFREEBUSY, VJOURNAL, VTODO</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}supported-calendar-data</dt><dd>-- no value --</dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}supported-collation-set</dt><dd>i;ascii-casemap, i;octet</dd>
                        <dt>{DAV:}supported-report-set</dt><dd>{DAV:}principal-match, {DAV:}principal-property-search, {urn:ietf:params:xml:ns:caldav}calendar-multiget, {urn:ietf:params:xml:ns:caldav}calendar-query, {urn:ietf:params:xml:ns:caldav}free-busy-query</dd>
                        <dt>{http://www.xythos.com/namespaces/StorageServer}ticketdiscovery</dt><dd></dd>
                        <dt>{http://osafoundation.org/cosmo/DAV}uuid</dt><dd>1</dd>
                        </dl>
                        <p>
                        <a href="/dav/test01@localhost.de/">Home collection</a><br>
                        <a href="/dav/users/test01@localhost.de">Principal resource</a><br>
                        </body></html>
                        """.stripIndent()

        mockMvc.perform(get("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }

    @Test
    public void calendarOptions() throws Exception {
        mockMvc.perform(options("/dav/{email}/calendar/", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, access-control, calendar-access, ticket"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, PUT, COPY, DELETE, MOVE, MKTICKET, DELTICKET, REPORT"));
    }

    @Test
    public void calendarHead() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(etag(notNullValue()));
    }

    @Test
    public void calendarAcl() throws Exception {
        mockMvc.perform(acl("/dav/{email}/calendar/", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isForbidden())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_SUPPORTED_PRIVILEGE));
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
                                        <D:principal-collection-set>
                                            <D:href>/dav/users</D:href>
                                        </D:principal-collection-set>
                                        <C:supported-calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav">
                                            <C:calendar-data C:content-type="text/calendar" C:version="2.0"/>
                                        </C:supported-calendar-data>
                                        <C:calendar-color xmlns:C="urn:ietf:params:xml:ns:xcaldavoneandone">#f0f0f0</C:calendar-color>
                                        <D:getlastmodified>Sat, 21 Nov 2015 21:11:00 GMT</D:getlastmodified>
                                        <D:iscollection>1</D:iscollection>
                                        <D:owner>
                                            <D:href>/dav/users/test01@localhost.de</D:href>
                                        </D:owner>
                                        <D:supported-report-set>
                                            <D:supported-report>
                                                <D:report>
                                                    <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                </D:report>
                                            </D:supported-report>
                                            <D:supported-report>
                                                <D:report>
                                                    <D:principal-match/>
                                                </D:report>
                                            </D:supported-report>
                                            <D:supported-report>
                                                <D:report>
                                                    <D:principal-property-search/>
                                                </D:report>
                                            </D:supported-report>
                                            <D:supported-report>
                                                <D:report>
                                                    <C:free-busy-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                </D:report>
                                            </D:supported-report>
                                            <D:supported-report>
                                                <D:report>
                                                    <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                                </D:report>
                                            </D:supported-report>
                                        </D:supported-report-set>
                                        <ticket:ticketdiscovery xmlns:ticket="http://www.xythos.com/namespaces/StorageServer"/>
                                        <D:acl>
                                            <D:ace>
                                                <D:principal>
                                                    <D:unauthenticated/>
                                                </D:principal>
                                                <D:deny>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:all/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:deny>
                                                <D:protected/>
                                            </D:ace>
                                            <D:ace>
                                                <D:principal>
                                                    <D:property>
                                                        <D:owner/>
                                                    </D:property>
                                                </D:principal>
                                                <D:grant>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:all/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:grant>
                                                <D:protected/>
                                            </D:ace>
                                            <D:ace>
                                                <D:principal>
                                                    <D:all/>
                                                </D:principal>
                                                <D:grant>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:read-current-user-privilege-set/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:grant>
                                                <D:protected/>
                                            </D:ace>
                                            <D:ace>
                                                <D:principal>
                                                    <D:all/>
                                                </D:principal>
                                                <D:deny>
                                                    <D:privilege>
                                                        <D:privilege>
                                                            <D:all/>
                                                        </D:privilege>
                                                    </D:privilege>
                                                </D:deny>
                                                <D:protected/>
                                            </D:ace>
                                        </D:acl>
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
                                        <D:current-user-privilege-set>
                                            <D:privilege>
                                                <D:read/>
                                            </D:privilege>
                                            <D:privilege>
                                                <D:write/>
                                            </D:privilege>
                                            <D:privilege>
                                                <D:read-current-user-privilege-set/>
                                            </D:privilege>
                                            <D:privilege>
                                                <C:read-free-busy xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            </D:privilege>
                                        </D:current-user-privilege-set>
                                        <C:calendar-visible xmlns:C="urn:ietf:params:xml:ns:xcaldavoneandone">true</C:calendar-visible>
                                        <D:displayname>calendarDisplayName</D:displayname>
                                        <cosmo:exclude-free-busy-rollup xmlns:cosmo="http://osafoundation.org/cosmo/DAV">false</cosmo:exclude-free-busy-rollup>
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

        mockMvc.perform(propfind("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void calendarPost() throws Exception {
        mockMvc.perform(post("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
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

        mockMvc.perform(proppatch("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .content(request)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void calendarDelete() throws Exception {
        mockMvc.perform(delete("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_FOUND))
    }

    @Test
    public void calendarCopy() throws Exception {
        mockMvc.perform(copy("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser))
                .header("Destination", "/dav/" + testUser.getUid() + "/newcalendar/"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk());
    }

    @Test
    public void calendarMove() throws Exception {
        mockMvc.perform(move("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser))
                .header("Destination", "/dav/" + testUser.getUid() + "/newcalendar/"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_FOUND));
    }

    @Test
    public void calendarMkticket() throws Exception {
        def request = """\
                        <C:ticketinfo xmlns:C="http://www.xythos.com/namespaces/StorageServer">
                            <D:privilege xmlns:D="DAV:"><D:read/></D:privilege>
                            <C:timeout>Second-3600</C:timeout>
                            <C:visits>1</C:visits>
                        </C:ticketinfo>"""

        final MvcResult result = mockMvc.perform(mkticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .content(request)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(textXmlContentType())
                .andReturn();

        final String ticket = result.getResponse().getHeader("Ticket");
        final String content = result.getResponse().getContentAsString();

        def response = """\
                        <D:prop xmlns:D="DAV:">
                            <ticket:ticketdiscovery xmlns:ticket="http://www.xythos.com/namespaces/StorageServer">
                                <ticket:ticketinfo>
                                    <ticket:id>${ticket}</ticket:id>
                                    <D:owner>
                                        <D:href>/dav/users/test01@localhost.de</D:href>
                                    </D:owner>
                                    <ticket:timeout>Second-3600</ticket:timeout>
                                    <ticket:visits>infinity</ticket:visits>
                                    <D:privilege>
                                        <D:privilege>
                                            <D:read/>
                                        </D:privilege>
                                    </D:privilege>
                                </ticket:ticketinfo>
                            </ticket:ticketdiscovery>
                        </D:prop>"""

        assertThat(content, equalXml(response));
    }

    @Test
    public void calendarDelticket() throws Exception {
        def request = """\
                        <C:ticketinfo xmlns:C="http://www.xythos.com/namespaces/StorageServer">
                            <D:privilege xmlns:D="DAV:"><D:read/></D:privilege>
                            <C:timeout>Second-3600</C:timeout>
                            <C:visits>1</C:visits>
                        </C:ticketinfo>"""

        final MvcResult result = mockMvc.perform(mkticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .content(request)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andReturn();

        final String ticket = result.getResponse().getHeader("Ticket");

        def delResponse1 = """\
                        <D:prop xmlns:D="DAV:">
                            <ticket:ticketdiscovery xmlns:ticket="http://www.xythos.com/namespaces/StorageServer">
                                <ticket:ticketinfo>
                                    <ticket:id>
                                        ${ticket}
                                    </ticket:id>
                                    <D:owner>
                                        <D:href>/dav/users/test01@localhost.de</D:href>
                                    </D:owner>
                                    <ticket:timeout>Second-3600</ticket:timeout>
                                    <ticket:visits>infinity</ticket:visits>
                                    <D:privilege>
                                        <D:privilege>
                                            <D:read/>
                                        </D:privilege>
                                    </D:privilege>
                                </ticket:ticketinfo>
                            </ticket:ticketdiscovery>
                        </D:prop>"""

        assertThat(result.getResponse().getContentAsString(), equalXml(delResponse1));

        mockMvc.perform(delticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser))
                .header("ticket", ticket))
                .andExpect(status().isNoContent())

        def delResponse2 = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <cosmo:precondition-failed>Ticket
                                ${ticket}
                                does not exist
                            </cosmo:precondition-failed>
                        </D:error>"""

        mockMvc.perform(delticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser))
                .header("ticket", ticket))
                .andExpect(status().isPreconditionFailed())
                .andExpect(textXmlContentType())
                .andExpect(xml(delResponse2))
    }
}
