package org.unitedinternet.cosmo;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.TestDispatcherServlet;
import org.springframework.web.context.WebApplicationContext;
import util.TestData;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {CarldavApplication.class, TestData.class})
@WebIntegrationTest("server.port:0")
public class IntegrationTestSupport {

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Before
    public void beforeAnyOther() throws Exception {
        this.mockMvc = webAppContextSetup(this.wac)
				.dispatcherServlet(new CustomTestDispatcherServlet())
				.addFilters(springSecurityFilterChain).build();
    }

    private static class CustomTestDispatcherServlet extends TestDispatcherServlet {
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            processRequest(request, response);
        }
    }

}
