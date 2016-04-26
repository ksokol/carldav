package dav

import org.junit.Test
import org.unitedinternet.cosmo.SecurityTestSupport
import testutil.builder.GeneralData
import testutil.builder.GeneralResponse

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.*
import static testutil.builder.GeneralData.CALDAV_EVENT
import static testutil.helper.Base64Helper.user
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
public class GeneralSecurityTests extends SecurityTestSupport {

    @Test
    public void unauthorizedAccessResourceOfOtherUser() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    @Test
    public void unknownUserAccessResourceOfKnownUser() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .header(AUTHORIZATION, user(UNKNOWN, UNKNOWN_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    @Test
    public void calendarPutSameUser() {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD))
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
        mockMvc.perform(get("/dav/{email}/calendar/{uid}.ics", USER01, GeneralData.UUID)
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD)))
                .andExpect(textCalendarContentType())
                .andExpect(status().isOk())
                .andExpect(text(CALDAV_EVENT));

        mockMvc.perform(get("/dav/{email}/calendar/{uid}.ics", USER02, GeneralData.UUID)
                .contentType(TEXT_XML)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(textXmlContentType())
                .andExpect(status().isNotFound())
                .andExpect(xml(GeneralResponse.NOT_FOUND));
    }

    @Test
    public void calendarPutDifferentUser() {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD))
                .content(CALDAV_EVENT))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    @Test
    public void list() {
        mockMvc.perform(get("/user")
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    @Test
    public void createUser() {
        mockMvc.perform(post("/user")
                .contentType(APPLICATION_JSON)
                .content("{}")
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }
}
