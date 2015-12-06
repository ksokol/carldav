package dav

import org.apache.commons.codec.binary.Base64
import org.junit.Test
import org.unitedinternet.cosmo.IntegrationTestSupport
import util.TestUser

import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static util.TestUser.TEST01
import static util.TestUser.UNKNOWN

/**
 * @author Kamill Sokol
 */
public class GeneralSecurityTests extends IntegrationTestSupport {

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get("/dav/users")
                .header(AUTHORIZATION, user(UNKNOWN)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAuthorizedShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/dav/users")
                .header(AUTHORIZATION, user(TEST01)))
                .andExpect(status().isInternalServerError());
    }

    private static String user(final TestUser testUser) {
        return "Basic " + Base64.encodeBase64String((testUser.getUid() + ":" + testUser.getPassword()).getBytes());
    }
}
