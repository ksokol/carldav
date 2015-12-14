package dav

import carldav.service.generator.IdGenerator
import carldav.service.time.TimeService
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.builder.GeneralData

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.mockito.Mockito.when
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

    @Autowired
    private TimeService timeService;

    @Autowired
    private IdGenerator idGenerator;

    @Before
    public void before() {
        when(timeService.getCurrentTime()).thenReturn(new Date(3600));
        when(idGenerator.nextStringIdentifier()).thenReturn("1");

         mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .contentType(TEXT_CALENDAR)
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn();
    }

    @Test
    public void user01IfMatchWildcardIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-Match", '*'))
                .andExpect(status().isOk())
                .andExpect(etag(is(ETAG)));
    }

    @Test
    public void user01IfMatchEtagIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-Match", ETAG))
                .andExpect(status().isOk())
                .andExpect(etag(is(ETAG)));
    }

    @Test
    public void user01IfNoneMatchGetIsNotModified() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", ETAG))
                .andExpect(status().isNotModified())
    }

    @Test
    public void user01IfNoneMatchOptionsIsPrecodnitionFailed() throws Exception {
        mockMvc.perform(options("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", ETAG))
                .andExpect(status().isPreconditionFailed())
                .andExpect(xml(PRECONDITION_FAILED_RESPONSE))
    }

    @Test
    public void user01IfNoneMatchHeadIsNotModified() throws Exception {
        mockMvc.perform(head("/dav/{email}/calendar/{uuid}.ics", USER01, uuid)
                .header("If-None-Match", ETAG))
                .andExpect(status().isNotModified())
    }
}
