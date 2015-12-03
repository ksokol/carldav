package dav;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static util.ContentUtil.html;
import static util.ContentUtil.xml;
import static util.FileUtil.file;
import static util.HeaderUtil.user;
import static util.TestUser.TEST01;
import static util.mockmvc.CustomResultMatchers.contentType;

import org.junit.Test;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import util.TestUser;

/**
 * @author Kamill Sokol
 */
public class CollectionTests extends IntegrationTestSupport {

    private final TestUser testUser = TEST01;

    @Test
    public void collectionGetNotFound() throws Exception {
        mockMvc.perform(get("/dav/collection/{uid}", "1")
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(contentType(is("text/xml; charset=UTF-8")))
                .andExpect(xml(file("dav/collection/collectionGetNotFound_response.xml")));
    }

    @Test
    public void collectionGet() throws Exception {
        mockMvc.perform(get("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(contentType(is("text/html; charset=UTF-8")))
                .andExpect(html(file("dav/collection/collectionGet_response.html")));
    }
}