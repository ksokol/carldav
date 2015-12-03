package dav;

import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.ContentUtil.xml;
import static util.FileUtil.file;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;
import static util.mockmvc.CustomRequestBuilders.propfind;

import org.junit.Test;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.TestUser;

/**
 * @author Kamill Sokol
 */
public class UsersCollectionTests extends IntegrationTestSupport {

    private final TestUser testUser = TEST01;

    @Test
    public void usersOptions() throws Exception {
        mockMvc.perform(options("/dav/users")
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, access-control, calendar-access, ticket"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT"));
    }

    @Test
    public void usersHead() throws Exception {
        mockMvc.perform(head("/dav/users")
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(xml(file("dav/users/usersHead_response.xml")));
    }

    @Test
    public void usersPost() throws Exception {
        mockMvc.perform(post("/dav/users")
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(xml(file("dav/users/usersPost_response.xml")));
    }

    @Test
    public void usersPropFindProp() throws Exception {
        mockMvc.perform(propfind("/dav/users")
                .contentType(TEXT_XML)
                .content(file("dav/users/usersPropFindProp_request.xml"))
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isInternalServerError())
                .andDo(print())
                .andExpect(xml(file("dav/users/usersPropFindProp_response.xml")));
    }

    @Test
    public void usersPropFindPropname() throws Exception {
        mockMvc.perform(propfind("/dav/users")
                .contentType(TEXT_XML)
                .content(file("dav/users/usersPropFindPropname_request.xml"))
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(xml(file("dav/users/usersPropFindPropname_response.xml")));
    }

    @Test
    public void usersPropFindAllprop() throws Exception {
        mockMvc.perform(propfind("/dav/users")
                .contentType(TEXT_XML)
                .content(file("dav/users/usersPropFindAllprop_request.xml"))
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(xml(file("dav/users/usersPropFindAllprop_response.xml")));
    }
}
