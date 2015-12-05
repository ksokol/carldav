package dav

import carldav.util.builder.MethodNotAllowedBuilder
import org.junit.Test
import org.springframework.http.HttpMethod
import org.unitedinternet.cosmo.IntegrationTestSupport
import util.TestUser

import static carldav.util.builder.GeneralResponse.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static util.mockmvc.CustomResultMatchers.xml
import static util.HeaderUtil.user
import static util.TestUser.TEST01
import static util.mockmvc.CustomRequestBuilders.propfind
import static util.mockmvc.CustomResultMatchers.textXmlContentType

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
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void usersPost() throws Exception {
        mockMvc.perform(post("/dav/users")
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(MethodNotAllowedBuilder.notAllowed(HttpMethod.POST).onCollection()));
    }

    @Test
    public void usersPropFindProp() throws Exception {
        def request = """\
                        <D:propfind xmlns:D="DAV:">
                            <D:prop>
                                <D:displayname />
                            </D:prop>
                        </D:propfind>"""

        mockMvc.perform(propfind("/dav/users")
                .contentType(TEXT_XML)
                .content(request)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void usersPropFindPropname() throws Exception {
        def request = """\
                        <D:propfind xmlns:D="DAV:">
                            <D:propname />
                        </D:propfind>"""

        mockMvc.perform(propfind("/dav/users")
                .contentType(TEXT_XML)
                .content(request)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void usersPropFindAllprop() throws Exception {
        def request = """\
                        <D:propfind xmlns:D="DAV:">
                            <D:allprop />
                        </D:propfind>"""

        mockMvc.perform(propfind("/dav/users")
                .contentType(TEXT_XML)
                .content(request)
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR));
    }
}
