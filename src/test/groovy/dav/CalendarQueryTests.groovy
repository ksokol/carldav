package dav

import org.junit.Before
import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.unitedinternet.cosmo.IntegrationTestSupport

import static calendar.DavDroidData.ADD_VEVENT_REQUEST1
import static calendar.EvolutionData.ADD_VEVENT_RECURRENCE_REQUEST1
import static calendar.EvolutionData.UPDATE_VEVENT_RECURRENCE_REQUEST1
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpHeaders.ETAG
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.etag
import static testutil.mockmvc.CustomResultMatchers.xml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class CalendarQueryTests extends IntegrationTestSupport {

    def currentTodoEtag;
    def currentEventEtag;

    @Before
    void setup() {
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

        currentTodoEtag = result1.getResponse().getHeader(ETAG)

        def result2 = mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEventEtag = result2.getResponse().getHeader(ETAG)
    }

    @Test
    void calendarDataAllProp() {
        def request1 = """\
                        <CAL:comp name="VCALENDAR">
                            <allprop />
                        </CAL:comp>"""

        def response1 = """\
                            BEGIN:VCALENDAR
                            VERSION:2.0&#13;
                            PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x&#13;
                            END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request1))
                .header("Depth", "1"))
                .andExpect(todoResponse(response1))
    }

    @Test
    void calendarDataAllComp() {
        def request1 = """\
                        <CAL:comp name="VCALENDAR">
                            <allcomp />
                        </CAL:comp>"""

        def response1 = """\
                            BEGIN:VCALENDAR
                            BEGIN:VTODO&#13;
                            DTSTAMP:20151231T115937Z&#13;
                            UID:6f490b02-77d7-442e-abd3-1e0bb14c3259&#13;
                            CREATED:20151231T115922Z&#13;
                            LAST-MODIFIED:20151231T115922Z&#13;
                            SUMMARY:add vtodo&#13;
                            STATUS:NEEDS-ACTION&#13;
                            END:VTODO&#13;
                            END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request1))
                .header("Depth", "1"))
                .andExpect(todoResponse(response1))
    }

    @Test
    void calendarDataVTodoUid() {
        def request1 = """\
                        <CAL:comp name="VCALENDAR">
                            <CAL:comp name="VTODO">
                                <prop name="UID" />
                            </CAL:comp>
                        </CAL:comp>"""

        def response1 = """\
                        BEGIN:VCALENDAR
                        BEGIN:VTODO
                        UID:6f490b02-77d7-442e-abd3-1e0bb14c3259&#13;
                        END:VTODO
                        END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request1))
                .header("Depth", "1"))
                .andExpect(todoResponse(response1))
    }

    @Test
    void expand() {
        def request1 = """\
                        <CAL:comp name="VCALENDAR">
                            <CAL:comp name="VEVENT">
                                <prop name="DTSTART" />
                            </CAL:comp>
                        </CAL:comp>
                        <CAL:expand start="30121221T115937Z" end="30131231T235937Z" />"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request1, "VEVENT"))
                .header("Depth", "1"))
                .andExpect(eventResponse("BEGIN:VCALENDAR END:VCALENDAR"))

        def request2 = """\
                        <CAL:comp name="VCALENDAR">
                            <CAL:comp name="VEVENT">
                                <prop name="DTSTART" />
                            </CAL:comp>
                        </CAL:comp>
                        <CAL:expand start="20160119T115937Z" end="20160128T235937Z" />"""

        def response2 = """\
                            BEGIN:VCALENDAR
                            BEGIN:VEVENT
                            DTSTART:20160120T183027Z&#13;
                            END:VEVENT
                            BEGIN:VEVENT
                            DTSTART:20160122T183027Z&#13;
                            END:VEVENT
                            BEGIN:VEVENT
                            DTSTART:20160127T183027Z&#13;
                            END:VEVENT
                            END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request2, "VEVENT"))
                .header("Depth", "1"))
                .andExpect(eventResponse(response2))
    }

    @Test
    void expandWithoutComp() {
        def request2 = """<CAL:expand start="20160119T115937Z" end="20160128T235937Z" />"""

        def response2 = """\
                            BEGIN:VCALENDAR
                            VERSION:2.0&#13;
                            PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x&#13;
                            BEGIN:VEVENT&#13;
                            DTSTAMP:20151230T185918Z&#13;
                            UID:e94d89d2-b195-4128-a9a8-be83a873deae&#13;
                            DURATION:PT5443200S&#13;
                            SUMMARY:add VEvent&#13;
                            LOCATION:Location&#13;
                            DESCRIPTION:DESCRIPTION&#13;
                            STATUS:CONFIRMED&#13;
                            ORGANIZER:mailto:test01@localhost.de&#13;
                            CLASS:PRIVATE&#13;
                            RECURRENCE-ID:20160120T183027Z&#13;
                            DTSTART:20160120T183027Z&#13;
                            BEGIN:VALARM&#13;
                            TRIGGER:-PT1440M&#13;
                            ACTION:DISPLAY&#13;
                            DESCRIPTION:add VEvent&#13;
                            END:VALARM&#13;
                            END:VEVENT&#13;
                            BEGIN:VEVENT&#13;
                            DTSTAMP:20151230T185918Z&#13;
                            UID:e94d89d2-b195-4128-a9a8-be83a873deae&#13;
                            DURATION:PT5443200S&#13;
                            SUMMARY:add VEvent&#13;
                            LOCATION:Location&#13;
                            DESCRIPTION:DESCRIPTION&#13;
                            STATUS:CONFIRMED&#13;
                            ORGANIZER:mailto:test01@localhost.de&#13;
                            CLASS:PRIVATE&#13;
                            RECURRENCE-ID:20160122T183027Z&#13;
                            DTSTART:20160122T183027Z&#13;
                            BEGIN:VALARM&#13;
                            TRIGGER:-PT1440M&#13;
                            ACTION:DISPLAY&#13;
                            DESCRIPTION:add VEvent&#13;
                            END:VALARM&#13;
                            END:VEVENT&#13;
                            BEGIN:VEVENT&#13;
                            DTSTAMP:20151230T185918Z&#13;
                            UID:e94d89d2-b195-4128-a9a8-be83a873deae&#13;
                            DURATION:PT5443200S&#13;
                            SUMMARY:add VEvent&#13;
                            LOCATION:Location&#13;
                            DESCRIPTION:DESCRIPTION&#13;
                            STATUS:CONFIRMED&#13;
                            ORGANIZER:mailto:test01@localhost.de&#13;
                            CLASS:PRIVATE&#13;
                            RECURRENCE-ID:20160127T183027Z&#13;
                            DTSTART:20160127T183027Z&#13;
                            BEGIN:VALARM&#13;
                            TRIGGER:-PT1440M&#13;
                            ACTION:DISPLAY&#13;
                            DESCRIPTION:add VEvent&#13;
                            END:VALARM&#13;
                            END:VEVENT&#13;
                            END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request2, "VEVENT"))
                .header("Depth", "1"))
                .andExpect(eventResponse(response2))
    }

    @Test
    void limitRecurrenceSet() {
        mockMvc.perform(put("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_RECURRENCE_REQUEST1))
                .andExpect(status().isCreated())

        def result2 = mockMvc.perform(put("/dav/{email}/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(UPDATE_VEVENT_RECURRENCE_REQUEST1))
                .andExpect(status().isNoContent())
                .andReturn()

        def eventWithRecurrenceIdEtag = result2.getResponse().getHeader(ETAG)

        def request3 = """\
                        <CAL:comp name="VCALENDAR">
                            <CAL:comp name="VEVENT">
                                <prop name="UID" />
                                <prop name="RECURRENCE-ID" />
                            </CAL:comp>
                        </CAL:comp>
                        <CAL:limit-recurrence-set start="30121221T115937Z" end="30131231T235937Z" />"""

        def response3 = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${eventWithRecurrenceIdEtag}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR
                                            BEGIN:VEVENT
                                            UID:20160123T135858Z-25739-1000-1796-13@localhost&#13;
                                            END:VEVENT
                                            END:VCALENDAR
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                            <D:response>
                                <D:href>/dav/test01@localhost.de/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:getetag>${currentEventEtag}</D:getetag>
                                        <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR
                                            BEGIN:VEVENT
                                            UID:e94d89d2-b195-4128-a9a8-be83a873deae&#13;
                                            END:VEVENT
                                            END:VCALENDAR
                                        </C:calendar-data>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request3, "VEVENT"))
                .header("Depth", "1"))
                .andExpect(xml(response3))

        def request4 = """\
                        <CAL:comp name="VCALENDAR">
                            <CAL:comp name="VEVENT">
                                <prop name="UID" />
                                  <prop name="RECURRENCE-ID" />
                            </CAL:comp>
                        </CAL:comp>
                        <CAL:limit-recurrence-set start="10160119T115937Z" end="30160128T235937Z" />"""

        def response4 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/20160123T135858Z-25739-1000-1796-13_localhost-20160123T135931Z.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${eventWithRecurrenceIdEtag}</D:getetag>
                                            <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR
                                                BEGIN:VEVENT
                                                UID:20160123T135858Z-25739-1000-1796-13@localhost&#13;
                                                END:VEVENT
                                                BEGIN:VEVENT
                                                UID:20160123T135858Z-25739-1000-1796-13@localhost&#13;
                                                RECURRENCE-ID;TZID=Europe/Berlin:20160123T155900&#13;
                                                END:VEVENT
                                                END:VCALENDAR
                                            </C:calendar-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                                <D:response>
                                    <D:href>/dav/test01@localhost.de/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <D:getetag>${currentEventEtag}</D:getetag>
                                            <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">BEGIN:VCALENDAR
                                                BEGIN:VEVENT
                                                UID:e94d89d2-b195-4128-a9a8-be83a873deae&#13;
                                                END:VEVENT
                                                END:VCALENDAR
                                            </C:calendar-data>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request4, "VEVENT"))
                .header("Depth", "1"))
                .andExpect(xml(response4))
    }

    @Test
    void limitRecurrenceSetWithoutComp() {
        def request1 = """<CAL:limit-recurrence-set start="10160119T115937Z" end="30160128T235937Z" />"""

        def response1 = """\
                            BEGIN:VCALENDAR
                            VERSION:2.0&#13;
                            PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x&#13;
                            BEGIN:VEVENT&#13;
                            DTSTAMP:20151230T185918Z&#13;
                            UID:e94d89d2-b195-4128-a9a8-be83a873deae&#13;
                            DTSTART;TZID=America/New_York:20160120T133027&#13;
                            DURATION:PT5443200S&#13;
                            RRULE:FREQ=WEEKLY;WKST=SU;UNTIL=20160629T183027Z;BYDAY=WE,FR&#13;
                            SUMMARY:add VEvent&#13;
                            LOCATION:Location&#13;
                            DESCRIPTION:DESCRIPTION&#13;
                            STATUS:CONFIRMED&#13;
                            ORGANIZER:mailto:test01@localhost.de&#13;
                            CLASS:PRIVATE&#13;
                            BEGIN:VALARM&#13;
                            TRIGGER:-PT1440M&#13;
                            ACTION:DISPLAY&#13;
                            DESCRIPTION:add VEvent&#13;
                            END:VALARM&#13;
                            END:VEVENT&#13;
                            BEGIN:VTIMEZONE&#13;
                            TZID:America/New_York&#13;
                            TZURL:http://tzurl.org/zoneinfo/America/New_York&#13;
                            X-LIC-LOCATION:America/New_York&#13;
                            BEGIN:DAYLIGHT&#13;
                            TZOFFSETFROM:-0500&#13;
                            TZOFFSETTO:-0400&#13;
                            TZNAME:EDT&#13;
                            DTSTART:20070311T020000&#13;
                            RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU&#13;
                            END:DAYLIGHT&#13;
                            BEGIN:STANDARD&#13;
                            TZOFFSETFROM:-0400&#13;
                            TZOFFSETTO:-0500&#13;
                            TZNAME:EST&#13;
                            DTSTART:20071104T020000&#13;
                            RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU&#13;
                            END:STANDARD&#13;
                            BEGIN:STANDARD&#13;
                            TZOFFSETFROM:-045602&#13;
                            TZOFFSETTO:-0500&#13;
                            TZNAME:EST&#13;
                            DTSTART:18831118T120358&#13;
                            RDATE:18831118T120358&#13;
                            END:STANDARD&#13;
                            BEGIN:DAYLIGHT&#13;
                            TZOFFSETFROM:-0500&#13;
                            TZOFFSETTO:-0400&#13;
                            TZNAME:EDT&#13;
                            DTSTART:19180331T030000&#13;
                            RDATE:19180331T030000&#13;
                            RDATE:19190330T030000&#13;
                            RDATE:19200328T030000&#13;
                            RDATE:19210424T030000&#13;
                            RDATE:19220430T030000&#13;
                            RDATE:19230429T030000&#13;
                            RDATE:19240427T030000&#13;
                            RDATE:19250426T030000&#13;
                            RDATE:19260425T030000&#13;
                            RDATE:19270424T030000&#13;
                            RDATE:19280429T030000&#13;
                            RDATE:19290428T030000&#13;
                            RDATE:19300427T030000&#13;
                            RDATE:19310426T030000&#13;
                            RDATE:19320424T030000&#13;
                            RDATE:19330430T030000&#13;
                            RDATE:19340429T030000&#13;
                            RDATE:19350428T030000&#13;
                            RDATE:19360426T030000&#13;
                            RDATE:19370425T030000&#13;
                            RDATE:19380424T030000&#13;
                            RDATE:19390430T030000&#13;
                            RDATE:19400428T030000&#13;
                            RDATE:19410427T030000&#13;
                            RDATE:19460428T030000&#13;
                            RDATE:19470427T030000&#13;
                            RDATE:19480425T030000&#13;
                            RDATE:19490424T030000&#13;
                            RDATE:19500430T030000&#13;
                            RDATE:19510429T030000&#13;
                            RDATE:19520427T030000&#13;
                            RDATE:19530426T030000&#13;
                            RDATE:19540425T030000&#13;
                            RDATE:19550424T030000&#13;
                            RDATE:19560429T030000&#13;
                            RDATE:19570428T030000&#13;
                            RDATE:19580427T030000&#13;
                            RDATE:19590426T030000&#13;
                            RDATE:19600424T030000&#13;
                            RDATE:19610430T030000&#13;
                            RDATE:19620429T030000&#13;
                            RDATE:19630428T030000&#13;
                            RDATE:19640426T030000&#13;
                            RDATE:19650425T030000&#13;
                            RDATE:19660424T030000&#13;
                            RDATE:19670430T030000&#13;
                            RDATE:19680428T030000&#13;
                            RDATE:19690427T030000&#13;
                            RDATE:19700426T020000&#13;
                            RDATE:19710425T020000&#13;
                            RDATE:19720430T020000&#13;
                            RDATE:19730429T020000&#13;
                            RDATE:19740106T020000&#13;
                            RDATE:19750223T020000&#13;
                            RDATE:19760425T020000&#13;
                            RDATE:19770424T020000&#13;
                            RDATE:19780430T020000&#13;
                            RDATE:19790429T020000&#13;
                            RDATE:19800427T020000&#13;
                            RDATE:19810426T020000&#13;
                            RDATE:19820425T020000&#13;
                            RDATE:19830424T020000&#13;
                            RDATE:19840429T020000&#13;
                            RDATE:19850428T020000&#13;
                            RDATE:19860427T020000&#13;
                            RDATE:19870405T020000&#13;
                            RDATE:19880403T020000&#13;
                            RDATE:19890402T020000&#13;
                            RDATE:19900401T020000&#13;
                            RDATE:19910407T020000&#13;
                            RDATE:19920405T020000&#13;
                            RDATE:19930404T020000&#13;
                            RDATE:19940403T020000&#13;
                            RDATE:19950402T020000&#13;
                            RDATE:19960407T020000&#13;
                            RDATE:19970406T020000&#13;
                            RDATE:19980405T020000&#13;
                            RDATE:19990404T020000&#13;
                            RDATE:20000402T020000&#13;
                            RDATE:20010401T020000&#13;
                            RDATE:20020407T020000&#13;
                            RDATE:20030406T020000&#13;
                            RDATE:20040404T020000&#13;
                            RDATE:20050403T020000&#13;
                            RDATE:20060402T020000&#13;
                            END:DAYLIGHT&#13;
                            BEGIN:STANDARD&#13;
                            TZOFFSETFROM:-0400&#13;
                            TZOFFSETTO:-0500&#13;
                            TZNAME:EST&#13;
                            DTSTART:19181027T020000&#13;
                            RDATE:19181027T020000&#13;
                            RDATE:19191026T020000&#13;
                            RDATE:19201031T020000&#13;
                            RDATE:19210925T020000&#13;
                            RDATE:19220924T020000&#13;
                            RDATE:19230930T020000&#13;
                            RDATE:19240928T020000&#13;
                            RDATE:19250927T020000&#13;
                            RDATE:19260926T020000&#13;
                            RDATE:19270925T020000&#13;
                            RDATE:19280930T020000&#13;
                            RDATE:19290929T020000&#13;
                            RDATE:19300928T020000&#13;
                            RDATE:19310927T020000&#13;
                            RDATE:19320925T020000&#13;
                            RDATE:19330924T020000&#13;
                            RDATE:19340930T020000&#13;
                            RDATE:19350929T020000&#13;
                            RDATE:19360927T020000&#13;
                            RDATE:19370926T020000&#13;
                            RDATE:19380925T020000&#13;
                            RDATE:19390924T020000&#13;
                            RDATE:19400929T020000&#13;
                            RDATE:19410928T020000&#13;
                            RDATE:19450930T020000&#13;
                            RDATE:19460929T020000&#13;
                            RDATE:19470928T020000&#13;
                            RDATE:19480926T020000&#13;
                            RDATE:19490925T020000&#13;
                            RDATE:19500924T020000&#13;
                            RDATE:19510930T020000&#13;
                            RDATE:19520928T020000&#13;
                            RDATE:19530927T020000&#13;
                            RDATE:19540926T020000&#13;
                            RDATE:19551030T020000&#13;
                            RDATE:19561028T020000&#13;
                            RDATE:19571027T020000&#13;
                            RDATE:19581026T020000&#13;
                            RDATE:19591025T020000&#13;
                            RDATE:19601030T020000&#13;
                            RDATE:19611029T020000&#13;
                            RDATE:19621028T020000&#13;
                            RDATE:19631027T020000&#13;
                            RDATE:19641025T020000&#13;
                            RDATE:19651031T020000&#13;
                            RDATE:19661030T020000&#13;
                            RDATE:19671029T020000&#13;
                            RDATE:19681027T020000&#13;
                            RDATE:19691026T020000&#13;
                            RDATE:19701025T020000&#13;
                            RDATE:19711031T020000&#13;
                            RDATE:19721029T020000&#13;
                            RDATE:19731028T020000&#13;
                            RDATE:19741027T020000&#13;
                            RDATE:19751026T020000&#13;
                            RDATE:19761031T020000&#13;
                            RDATE:19771030T020000&#13;
                            RDATE:19781029T020000&#13;
                            RDATE:19791028T020000&#13;
                            RDATE:19801026T020000&#13;
                            RDATE:19811025T020000&#13;
                            RDATE:19821031T020000&#13;
                            RDATE:19831030T020000&#13;
                            RDATE:19841028T020000&#13;
                            RDATE:19851027T020000&#13;
                            RDATE:19861026T020000&#13;
                            RDATE:19871025T020000&#13;
                            RDATE:19881030T020000&#13;
                            RDATE:19891029T020000&#13;
                            RDATE:19901028T020000&#13;
                            RDATE:19911027T020000&#13;
                            RDATE:19921025T020000&#13;
                            RDATE:19931031T020000&#13;
                            RDATE:19941030T020000&#13;
                            RDATE:19951029T020000&#13;
                            RDATE:19961027T020000&#13;
                            RDATE:19971026T020000&#13;
                            RDATE:19981025T020000&#13;
                            RDATE:19991031T020000&#13;
                            RDATE:20001029T020000&#13;
                            RDATE:20011028T020000&#13;
                            RDATE:20021027T020000&#13;
                            RDATE:20031026T020000&#13;
                            RDATE:20041031T020000&#13;
                            RDATE:20051030T020000&#13;
                            RDATE:20061029T020000&#13;
                            END:STANDARD&#13;
                            BEGIN:STANDARD&#13;
                            TZOFFSETFROM:-0500&#13;
                            TZOFFSETTO:-0500&#13;
                            TZNAME:EST&#13;
                            DTSTART:19200101T000000&#13;
                            RDATE:19200101T000000&#13;
                            RDATE:19420101T000000&#13;
                            RDATE:19460101T000000&#13;
                            RDATE:19670101T000000&#13;
                            END:STANDARD&#13;
                            BEGIN:DAYLIGHT&#13;
                            TZOFFSETFROM:-0500&#13;
                            TZOFFSETTO:-0400&#13;
                            TZNAME:EWT&#13;
                            DTSTART:19420209T030000&#13;
                            RDATE:19420209T030000&#13;
                            END:DAYLIGHT&#13;
                            BEGIN:DAYLIGHT&#13;
                            TZOFFSETFROM:-0400&#13;
                            TZOFFSETTO:-0400&#13;
                            TZNAME:EPT&#13;
                            DTSTART:19450814T190000&#13;
                            RDATE:19450814T190000&#13;
                            END:DAYLIGHT&#13;
                            END:VTIMEZONE&#13;
                            END:VCALENDAR"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request1, "VEVENT"))
                .header("Depth", "1"))
                .andExpect(eventResponse(response1))
    }

    @Test
    void unknownElement() {
        def request1 = """<CAL:unknown />"""

        def response1 = """\
                        BEGIN:VCALENDAR&#13;
                        VERSION:2.0&#13;
                        PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x&#13;
                        BEGIN:VTODO&#13;
                        DTSTAMP:20151231T115937Z&#13;
                        UID:6f490b02-77d7-442e-abd3-1e0bb14c3259&#13;
                        CREATED:20151231T115922Z&#13;
                        LAST-MODIFIED:20151231T115922Z&#13;
                        SUMMARY:add vtodo&#13;
                        STATUS:NEEDS-ACTION&#13;
                        END:VTODO&#13;
                        END:VCALENDAR&#13;"""

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request(request1))
                .header("Depth", "1"))
                .andExpect(todoResponse(response1))
    }

    String request(String xmlFragment) {
        return request(xmlFragment, "VTODO")
    }

    String request(String xmlFragment, String component) {
        return """\
                    <CAL:calendar-query xmlns="DAV:" xmlns:CAL="urn:ietf:params:xml:ns:caldav">
                        <prop>
                            <getetag/>
                            <CAL:calendar-data CAL:content-type="text/calendar" CAL:version="2.0">
                                ${xmlFragment}
                            </CAL:calendar-data>
                        </prop>
                        <CAL:filter>
                            <CAL:comp-filter name="VCALENDAR">
                                <CAL:comp-filter name="${component}"/>
                            </CAL:comp-filter>
                        </CAL:filter>
                    </CAL:calendar-query>"""
    }

    ResultMatcher todoResponse(String calendarData) {
        return response(calendarData, currentTodoEtag, "6f490b02-77d7-442e-abd3-1e0bb14c3259")
    }

    ResultMatcher eventResponse(String calendarData) {
        return response(calendarData, currentEventEtag, "e94d89d2-b195-4128-a9a8-be83a873deae")
    }

    ResultMatcher response(String calendarData, String etag, String uuid) {
        return xml("""\
                    <D:multistatus xmlns:D="DAV:">
                        <D:response>
                            <D:href>/dav/test01@localhost.de/calendar/${uuid}.ics</D:href>
                            <D:propstat>
                                <D:prop>
                                    <D:getetag>${etag}</D:getetag>
                                    <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">${calendarData}
                                    </C:calendar-data>
                                </D:prop>
                                <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                        </D:response>
                    </D:multistatus>""")
    }
}
