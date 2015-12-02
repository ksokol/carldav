package dav.user;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.ContentUtil.html;
import static util.ContentUtil.with;
import static util.ContentUtil.xml;
import static util.FileUtil.file;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;
import static util.mockmvc.CustomMediaTypes.TEXT_CALENDAR;
import static util.mockmvc.CustomRequestBuilders.mkcalendar;
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
}
