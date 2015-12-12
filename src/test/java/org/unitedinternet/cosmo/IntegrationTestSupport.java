package org.unitedinternet.cosmo;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.TestDispatcherServlet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import util.MockBeans;
import util.TestData;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {CarldavApplication.class, TestData.class, MockBeans.class})
@WebIntegrationTest("server.port:0")
@Transactional
public abstract class IntegrationTestSupport {

    protected MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void beforeAnyOther() throws Exception {
        this.mockMvc = webAppContextSetup(this.wac)
				.dispatcherServlet(new CustomTestDispatcherServlet())
                .apply(springSecurity()).build();
    }

    private static class CustomTestDispatcherServlet extends TestDispatcherServlet {
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            processRequest(request, response);
        }
    }

}
