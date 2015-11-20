package org.unitedinternet.cosmo.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.unitedinternet.cosmo.IntegrationTestSupport;

/**
 * @author Kamill Sokol
 */
public class GeneralSecurityTests extends IntegrationTestSupport {

    @Test
    public void testInternalServerError() throws Exception {
        mockMvc.perform(get("/dav/users"))
                .andExpect(status().isInternalServerError());
    }
}
