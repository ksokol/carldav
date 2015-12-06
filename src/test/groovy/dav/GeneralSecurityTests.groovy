package dav

import org.apache.commons.codec.binary.Base64
import org.junit.Test
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.hamcrest.Matchers.is
import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.*

/**
 * @author Kamill Sokol
 */
public class GeneralSecurityTests extends IntegrationTestSupport {

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get("/dav/users")
                .header(AUTHORIZATION, user(USER01, USER01_PASSWORD)))
                .andExpect(status().isInternalServerError())
    }

    @Test
    public void testAuthorizedShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/dav/users")
                .header(AUTHORIZATION, user(UNKNOWN, UNKNOWN_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, is('Basic realm="carldav"')))
    }

    private static String user(String username, String password) {
        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes());
    }
}
