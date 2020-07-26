package org.unitedinternet.cosmo;

import carldav.CarldavApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import util.TestData;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CarldavApplication.class, TestData.class})
@Transactional
@Rollback
public abstract class IntegrationTestSupport {

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @BeforeEach
    public void beforeAnyOther() {
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
