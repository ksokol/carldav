package dav.user;

import static org.hamcrest.Matchers.notNullValue;
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

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.TestUser;
/**
 * @author Kamill Sokol
 */
public class CalendarTests extends IntegrationTestSupport {

    @Test
    public void shouldReturnHtmlForUser() throws Exception {
        final TestUser testUser = TEST01;
        final String uuid = "59BC120D-E909-4A56-A70D-8E97914E51A3";

        final MvcResult mvcResult = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", testUser.getUid(), uuid)
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/calendar; charset=utf-8")
                .content(fromFile("event1.ics")))
                .andExpect(status().isCreated())
                .andExpect(header().string(ETAG, notNullValue()))
                .andReturn();

        final String eTag = mvcResult.getResponse().getHeader(ETAG);

        mockMvc.perform(request("REPORT", "/dav/{email}/calendar/", testUser.getUid())
                .content(fromFile("test.xml"))
                .header(AUTHORIZATION, user(testUser))
                .header(CONTENT_TYPE, "text/xml; charset=utf-8"))
              .andExpect(reportWithEtag(fromFile("shouldReturnHtmlForUser_response.xml"), eTag));
    }
}
