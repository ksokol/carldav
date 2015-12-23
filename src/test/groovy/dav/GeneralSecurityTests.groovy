package dav

import carldav.service.generator.IdGenerator
import carldav.service.time.TimeService
import org.apache.commons.codec.binary.Base64
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.builder.GeneralResponse

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.mockito.Mockito.when
import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.*
import static testutil.builder.GeneralData.CALDAV_EVENT
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
public class GeneralSecurityTests extends IntegrationTestSupport {

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
    public void unauthorized() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, UUID)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    @Test
    public void authorizedShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, UUID)
                .header(AUTHORIZATION, user(UNKNOWN, UNKNOWN_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    @Test
    public void calendarPutSameUser() {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, UUID)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD))
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
        mockMvc.perform(get("/dav/{email}/calendar/{uid}.ics", USER01, UUID)
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD)))
                .andExpect(textCalendarContentType())
                .andExpect(status().isOk())
                .andExpect(text(CALDAV_EVENT));

        mockMvc.perform(get("/dav/{email}/calendar/{uid}.ics", USER02, UUID)
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(textXmlContentType())
                .andExpect(status().isNotFound())
                .andExpect(xml(GeneralResponse.NOT_FOUND));
    }

    @Test
    public void calendarPutDifferentUser() {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, UUID)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD))
                .content(CALDAV_EVENT))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    private static String user(String username, String password) {
        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes());
    }
}
