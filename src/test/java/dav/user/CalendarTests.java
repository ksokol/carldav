package dav.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.ContentUtil.html;
import static util.ContentUtil.with;
import static util.ContentUtil.xml;
import static util.FileUtil.file;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;
import static util.mockmvc.CustomMediaTypes.TEXT_CALENDAR;
import static util.mockmvc.CustomRequestBuilders.acl;
import static util.mockmvc.CustomRequestBuilders.copy;
import static util.mockmvc.CustomRequestBuilders.delticket;
import static util.mockmvc.CustomRequestBuilders.mkcalendar;
import static util.mockmvc.CustomRequestBuilders.mkticket;
import static util.mockmvc.CustomRequestBuilders.move;
import static util.mockmvc.CustomRequestBuilders.propfind;
import static util.mockmvc.CustomRequestBuilders.proppatch;
import static util.mockmvc.CustomRequestBuilders.report;
import static util.mockmvc.CustomResultMatchers.etag;

import carldav.service.generator.IdGenerator;
import carldav.service.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.TestUser;
import util.mockmvc.CustomRequestBuilders;

import java.util.Date;

/**
 * @author Kamill Sokol
 */
public class CalendarTests extends IntegrationTestSupport {

    private final TestUser testUser = TEST01;
    private final String uuid = "59BC120D-E909-4A56-A70D-8E97914E51A3";

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
                .content(file("dav/caldav/event1.ics")))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        mockMvc.perform(report("/dav/{email}/calendar/", testUser.getUid())
                .content(file("dav/user/shouldReturnHtmlForUser_request.xml"))
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(xml(with().etag(eTag).in(file("dav/user/shouldReturnHtmlForUser_response.xml"))));
    }

    @Test
    public void shouldReturnHtmlForUserAllProp() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(testUser))
                .content(file("dav/caldav/event1.ics")))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        mockMvc.perform(report("/dav/{email}/calendar/", testUser.getUid())
                .content(file("dav/user/shouldReturnHtmlForUserAllProp_request.xml"))
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(xml(with().etag(eTag).in(file("dav/user/shouldReturnHtmlForUserAllProp_response.xml"))));
    }

    @Test
    public void shouldReturnHtmlForUserPropName() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(testUser))
                .content(file("dav/caldav/event1.ics")))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        mockMvc.perform(report("/dav/{email}/calendar/", testUser.getUid())
                .content(file("dav/user/shouldReturnHtmlForUserPropName_request.xml"))
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(xml(with().etag(eTag).in(file("dav/user/shouldReturnHtmlForUserPropName_response.xml"))));
    }

    @Test
    public void shouldForbidSameCalendar() throws Exception {
        mockMvc.perform(CustomRequestBuilders.mkcalendar("/dav/{email}/calendar/", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(xml(file("dav/user/shouldForbidSameCalendar_request.xml")));
    }

    @Test
    public void shouldCreateCalendar() throws Exception {
        mockMvc.perform(get("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isNotFound());

        mockMvc.perform(mkcalendar("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/dav/{email}/newcalendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(html(file("dav/user/shouldCreateCalendar_response.html")));
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
                .andExpect(xml(file("dav/user/calendarAcl_response.xml")));
    }

    @Test
    public void calendarPropFind() throws Exception {
        mockMvc.perform(propfind("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(file("dav/user/calendarPropFind_response.xml")));
    }

    @Test
    public void calendarPost() throws Exception {
        mockMvc.perform(post("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(xml(file("dav/user/calendarPost_response.xml")));
    }

    @Test
    public void calendarPropPatch() throws Exception {
        mockMvc.perform(proppatch("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .content(file("dav/user/calendarPropPatch_request.xml"))
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(file("dav/user/calendarPropPatch_response.xml")));
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
                .andExpect(status().isNotFound());
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
                .andExpect(status().isNotFound());
    }

    @Test
    public void calendarMkticket() throws Exception {
        final MvcResult result = mockMvc.perform(mkticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .content(file("dav/user/calendarMkticket_request.xml"))
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andReturn();

        final String ticket = result.getResponse().getHeader("Ticket");
        final String content = result.getResponse().getContentAsString();

        assertThat(content, is(with().ticket(ticket).in(file("dav/user/calendarMkticket_response.xml"))));
    }

    @Test
    public void calendarDelticket() throws Exception {
        final MvcResult result = mockMvc.perform(mkticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .content(file("dav/user/calendarMkticket_request.xml"))
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andReturn();

        final String ticket = result.getResponse().getHeader("Ticket");

        mockMvc.perform(delticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser))
                .header("ticket", ticket))
                .andExpect(status().isNoContent())
                .andReturn();

        mockMvc.perform(delticket("/dav/{email}/calendar/", testUser.getUid())
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(testUser))
                .header("ticket", ticket))
                .andExpect(status().isPreconditionFailed())
                .andReturn();
    }
}
