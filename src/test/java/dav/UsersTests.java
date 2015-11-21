package dav;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.TestUser;

/**
 * @author Kamill Sokol
 */
public class UsersTests extends IntegrationTestSupport {

    @Test
    public void shouldReturnHtmlForUser() throws Exception {
        final TestUser testUser = TEST01;
        mockMvc.perform(get("/dav/users/" + testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(content().string(IOUtils.toString(new ClassPathResource("test01.html").getInputStream())));
    }
}
