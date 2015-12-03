package dav;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.ContentUtil.xml;
import static util.CustomResultMatcher.contentFromFile;
import static util.FileUtil.file;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;
import static util.mockmvc.CustomRequestBuilders.propfind;
import static util.mockmvc.CustomRequestBuilders.proppatch;
import static util.mockmvc.CustomResultMatchers.contentType;
import static util.mockmvc.CustomResultMatchers.etag;
import static util.mockmvc.CustomResultMatchers.lastModified;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.TestUser;

/**
 * @author Kamill Sokol
 */
public class UsersTests extends IntegrationTestSupport {

    private final TestUser testUser = TEST01;

    @Test
    public void userGet() throws Exception {
        mockMvc.perform(get("/dav/users/{uid}", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(contentType(is("text/html; charset=UTF-8")))
                .andExpect(contentFromFile("dav/users/userGet_response.html"));
    }

    @Test
    public void userPut() throws Exception {
        mockMvc.perform(put("/dav/users/{uid}", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(contentType(is("text/xml; charset=UTF-8")))
                .andExpect(xml(file("dav/users/userPut_response.xml")));
    }

    @Test
    public void userOptions() throws Exception {
        mockMvc.perform(options("/dav/users/{uid}", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, access-control, calendar-access, ticket"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT"));
    }

    @Test
    public void userHead() throws Exception {
        mockMvc.perform(head("/dav/users/{uid}", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(etag(notNullValue()))
                .andExpect(lastModified(is("Mon, 16 Nov 2015 15:35:16 GMT")));
    }

    @Test
    public void userPost() throws Exception {
        mockMvc.perform(post("/dav/users/{uid}", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print())
                .andExpect(contentType(is("text/xml; charset=UTF-8")))
                .andExpect(xml(file("dav/users/userPost_response.xml")));
    }

    @Test
    public void userPropFind() throws Exception {
        mockMvc.perform(propfind("/dav/users/{uid}", testUser.getUid())
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(contentType(is("text/xml; charset=UTF-8")))
                .andExpect(xml(file("dav/users/userPropFind_response.xml")));
    }

    @Test
    public void userPropPatchSet() throws Exception {
        mockMvc.perform(proppatch("/dav/users/{uid}", testUser.getUid())
                .content(file("dav/users/userPropPatchSet_request.xml"))
                .contentType(MediaType.TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMultiStatus())
                .andExpect(contentType(is("text/xml; charset=UTF-8")))
                .andExpect(xml(file("dav/users/userPropPatchSet_response.xml")));
    }

    @Test
    public void userPropPatchRemoveDeadProperty() throws Exception {
        mockMvc.perform(proppatch("/dav/users/{uid}", testUser.getUid())
                .content(file("dav/users/userPropPatchRemoveDeadProperty_request.xml"))
                .contentType(MediaType.TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMultiStatus())
                .andExpect(contentType(is("text/xml; charset=UTF-8")))
                .andExpect(xml(file("dav/users/userPropPatchRemoveDeadProperty_response.xml")));
    }

    @Test
    public void userPropPatchRemove() throws Exception {
        mockMvc.perform(proppatch("/dav/users/{uid}", testUser.getUid())
                .content(file("dav/users/userPropPatchRemove_request.xml"))
                .contentType(MediaType.TEXT_XML)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMultiStatus())
                .andExpect(contentType(is("text/xml; charset=UTF-8")))
                .andExpect(xml(file("dav/users/userPropPatchRemove_response.xml")));
    }
}
