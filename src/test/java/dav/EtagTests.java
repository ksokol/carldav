package dav;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.builder.GeneralData;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.TestUser.USER01;
import static util.builder.GeneralData.CALDAV_EVENT;
import static util.builder.GeneralResponse.PRECONDITION_FAILED_RESPONSE;
import static util.mockmvc.CustomMediaTypes.TEXT_CALENDAR;
import static util.mockmvc.CustomResultMatchers.etag;
import static util.mockmvc.CustomResultMatchers.xml;

@WithUserDetails(USER01)
class EtagTests extends IntegrationTestSupport {

    private static final String uuid = GeneralData.UUID;

    private String etag;

    @BeforeEach
    void before() throws Exception {
        etag = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG);
    }

    @Test
    void user01IfMatchWildcardIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-Match", '*'))
                .andExpect(status().isOk())
                .andExpect(etag(is(etag)));
    }

    @Test
    void user01IfMatchEtagIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-Match", etag))
                .andExpect(status().isOk())
                .andExpect(etag(is(etag)));
    }

    @Test
    void user01IfNoneMatchGetIsNotModified() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", etag))
                .andExpect(status().isNotModified());
    }

    @Test
    void user01IfNoneMatchOptionsIsPrecodnitionFailed() throws Exception {
        mockMvc.perform(options("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", etag))
                .andExpect(status().isPreconditionFailed())
                .andExpect(xml(PRECONDITION_FAILED_RESPONSE));
    }

    @Test
    void user01IfNoneMatchHeadIsNotModified() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", etag))
                .andExpect(status().isNotModified());
    }
}
