package dav;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;
import static util.TestUser.UNKNOWN;

import org.junit.Test;
import org.unitedinternet.cosmo.IntegrationTestSupport;

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
}
