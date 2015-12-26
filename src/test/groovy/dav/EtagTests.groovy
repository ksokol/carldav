package dav

import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.builder.GeneralData

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.builder.GeneralData.CALDAV_EVENT
import static testutil.builder.GeneralResponse.PRECONDITION_FAILED_RESPONSE
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomResultMatchers.etag
import static testutil.mockmvc.CustomResultMatchers.xml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class EtagTests extends IntegrationTestSupport {

    private static final String ETAG = '"1d21bc1d460b1085d53e3def7f7380f6"'
    private static final String uuid = GeneralData.UUID

    def etag;

    @Before
    public void before() {
        etag = mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG)
    }

    @Test
    public void user01IfMatchWildcardIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-Match", '*'))
                .andExpect(status().isOk())
                .andExpect(etag(is(etag)));
    }

    @Test
    public void user01IfMatchEtagIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-Match", etag))
                .andExpect(status().isOk())
                .andExpect(etag(is(etag)));
    }

    @Test
    public void user01IfNoneMatchGetIsNotModified() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", etag))
                .andExpect(status().isNotModified())
    }

    @Test
    public void user01IfNoneMatchOptionsIsPrecodnitionFailed() throws Exception {
        mockMvc.perform(options("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", etag))
                .andExpect(status().isPreconditionFailed())
                .andExpect(xml(PRECONDITION_FAILED_RESPONSE))
    }

    @Test
    public void user01IfNoneMatchHeadIsNotModified() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", etag))
                .andExpect(status().isNotModified())
    }
}
