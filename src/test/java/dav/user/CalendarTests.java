package dav.user;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.ContentUtil.reportWithEtag;
import static util.FileUtil.fromFile;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;

import carldav.service.generator.IdGenerator;
import carldav.service.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.TestUser;

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
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/calendar; charset=utf-8")
                .content(fromFile("dav/caldav/event1.ics")))
                .andExpect(status().isCreated())
                .andExpect(header().string(ETAG, notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        mockMvc.perform(request("REPORT", "/dav/{email}/calendar/", testUser.getUid())
                .content(fromFile("dav/user/shouldReturnHtmlForUser_request.xml"))
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/xml; charset=utf-8"))
                .andExpect(reportWithEtag(fromFile("dav/user/shouldReturnHtmlForUser_response.xml"), eTag));
    }

    @Test
    public void shouldReturnHtmlForUserAllProp() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/calendar; charset=utf-8")
                .content(fromFile("dav/caldav/event1.ics")))
                .andExpect(status().isCreated())
                .andExpect(header().string(ETAG, notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        mockMvc.perform(request("REPORT", "/dav/{email}/calendar/", testUser.getUid())
                .content(fromFile("dav/user/shouldReturnHtmlForUserAllProp_request.xml"))
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/xml; charset=utf-8"))
                .andExpect(reportWithEtag(fromFile("dav/user/shouldReturnHtmlForUserAllProp_response.xml"), eTag));
    }

    @Test
    public void shouldReturnHtmlForUserPropName() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/calendar; charset=utf-8")
                .content(fromFile("dav/caldav/event1.ics")))
                .andExpect(status().isCreated())
                .andExpect(header().string(ETAG, notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        mockMvc.perform(request("REPORT", "/dav/{email}/calendar/", testUser.getUid())
                .content(fromFile("dav/user/shouldReturnHtmlForUserPropName_request.xml"))
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/xml; charset=utf-8"))
                .andExpect(reportWithEtag(fromFile("dav/user/shouldReturnHtmlForUserPropName_response.xml"), eTag));
    }
}
