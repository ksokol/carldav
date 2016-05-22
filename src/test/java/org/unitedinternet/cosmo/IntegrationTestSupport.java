package org.unitedinternet.cosmo;

import carldav.CarldavApplication;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import util.TestData;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {CarldavApplication.class, TestData.class})
@WebIntegrationTest
@Transactional
@Rollback
public abstract class IntegrationTestSupport {

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Value("${server.context-path}")
    private String contextPath;

    @Before
    public void beforeAnyOther() throws Exception {
        this.mockMvc = webAppContextSetup(this.wac)
                .defaultRequest(get("/").with(new ContextPathRequestPostProcessor()))
                .apply(springSecurity()).build();
    }

    private class ContextPathRequestPostProcessor implements RequestPostProcessor {
        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
            mockHttpServletRequest.setRequestURI(contextPath + mockHttpServletRequest.getRequestURI());
            mockHttpServletRequest.setPathInfo(contextPath + mockHttpServletRequest.getPathInfo());
            mockHttpServletRequest.setServletPath(contextPath);
            return mockHttpServletRequest;
        }
    }
}
