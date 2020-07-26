package dav;

import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.SecurityTestSupport;
import util.builder.GeneralData;
import util.builder.GeneralResponse;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.TestUser.UNKNOWN;
import static util.TestUser.UNKNOWN_PASSWORD;
import static util.TestUser.USER01;
import static util.TestUser.USER01_PASSWORD;
import static util.TestUser.USER02;
import static util.TestUser.USER02_PASSWORD;
import static util.builder.GeneralData.CALDAV_EVENT;
import static util.helper.Base64Helper.user;
import static util.mockmvc.CustomMediaTypes.TEXT_CALENDAR;
import static util.mockmvc.CustomResultMatchers.etag;
import static util.mockmvc.CustomResultMatchers.text;
import static util.mockmvc.CustomResultMatchers.textCalendarContentType;
import static util.mockmvc.CustomResultMatchers.textXmlContentType;
import static util.mockmvc.CustomResultMatchers.xml;

class GeneralSecurityTests extends SecurityTestSupport {

    private static final String EXPECTED_REALM = "Basic realm=\"carldav\"";

    @Test
    void unauthorizedAccessResourceOfOtherUser() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is(EXPECTED_REALM)));
    }

    @Test
    void unknownUserAccessResourceOfKnownUser() throws Exception {
        mockMvc.perform(get("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .header(AUTHORIZATION, user(UNKNOWN, UNKNOWN_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is(EXPECTED_REALM)));
    }

    @Test
    void calendarPutSameUser() throws Exception {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD))
                .content(CALDAV_EVENT))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()));

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
    void calendarPutDifferentUser() throws Exception {
        mockMvc.perform(put("/dav/{email}/calendar/{uuid}.ics", USER01, GeneralData.UUID)
                .contentType(TEXT_CALENDAR)
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD))
                .content(CALDAV_EVENT))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is(EXPECTED_REALM)));
    }

    @Test
    void list() throws Exception {
        mockMvc.perform(get("/user")
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is(EXPECTED_REALM)));
    }

    @Test
    void createUser() throws Exception {
        mockMvc.perform(post("/user")
                .contentType(APPLICATION_JSON)
                .content("{}")
                .header(AUTHORIZATION, user(USER02, USER02_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is(EXPECTED_REALM)));
    }
}
