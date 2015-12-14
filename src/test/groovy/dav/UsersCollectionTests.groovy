package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.builder.GeneralRequest.PROPFIND_DISPLAYNAME_REQUEST
import static testutil.builder.GeneralResponse.INTERNAL_SERVER_ERROR
import static testutil.builder.MethodNotAllowedBuilder.notAllowed
import static testutil.mockmvc.CaldavHttpMethod.COPY
import static testutil.mockmvc.CaldavHttpMethod.MOVE
import static testutil.mockmvc.CustomRequestBuilders.*
import static testutil.mockmvc.CustomResultMatchers.textXmlContentType
import static testutil.mockmvc.CustomResultMatchers.xml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
public class UsersCollectionTests extends IntegrationTestSupport {

    @Test
    public void usersGet() throws Exception {
        mockMvc.perform(get("/dav/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(xml(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void usersOptions() throws Exception {
        mockMvc.perform(options("/dav/users"))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT"));
    }

    @Test
    public void usersHead() throws Exception {
        mockMvc.perform(head("/dav/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void usersPost() throws Exception {
        mockMvc.perform(post("/dav/users"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(POST).onCollection()));
    }

    @Test
    public void usersPropFindProp() throws Exception {
        mockMvc.perform(propfind("/dav/users")
                .contentType(TEXT_XML)
                .content(PROPFIND_DISPLAYNAME_REQUEST))
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
                .content(request))
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
                .content(request))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void usersPropPatchSet() throws Exception {
        def request = '''\
                        <D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:set>
                                <D:prop>
                                    <D:displayname></D:displayname>
                                </D:prop>
                            </D:set>
                        </D:propertyupdate>'''

        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/users/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 403 Forbidden</D:status>
                                </D:propstat>
                                <D:responsedescription>Property {DAV:}displayname is protected</D:responsedescription>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(proppatch("/dav/users")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void usersPropPatchRemoveDeadProperty() throws Exception {
        def request = '''\
                        <D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:remove>
                                <D:prop><Z:Copyright-Owner/></D:prop>
                            </D:remove>
                        </D:propertyupdate>'''

        def response = '''\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/users/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <Z:Copyright-Owner xmlns:Z="http://www.w3.com/standards/z39.50/"/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 403 Forbidden</D:status>
                                </D:propstat>
                                <D:responsedescription>Dead properties are not supported on this collection</D:responsedescription>
                            </D:response>
                        </D:multistatus>'''

        mockMvc.perform(proppatch("/dav/users")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void usersDelete() throws Exception {
        mockMvc.perform(delete("/dav/users")
                .contentType(TEXT_XML))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(DELETE).onUserPrincipalCollection()));
    }

    @Test
    public void usersCopy() throws Exception {
        mockMvc.perform(copy("/dav/users"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(COPY).onUserPrincipalCollection()));
    }

    @Test
    public void usersMove() throws Exception {
        mockMvc.perform(move("/dav/users"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(MOVE).onUserPrincipalCollection()));
    }

    @Test
    public void usersReport() throws Exception {
        def request = '''\
                        <D:principal-match xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:principal-property>
                                <D:displayname/>
                            </D:principal-property>
                        </D:principal-match>'''

        def response = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:"><cosmo:unprocessable-entity>Unknown report {DAV:}principal-match</cosmo:unprocessable-entity></D:error>
                        """

        mockMvc.perform(report("/dav/users")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void userSearch() throws Exception {
        def request = """\
                        <D:principal-property-search xmlns:D="DAV:">
                             <D:property-search>
                               <D:prop>
                                 <D:displayname/>
                               </D:prop>
                               <D:match>Laurie</D:match>
                             </D:property-search>
                             <D:prop>
                               <C:calendar-home-set
                                  xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                               <D:displayname/>
                             </D:prop>
                        </D:principal-property-search>"""

        def response = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <cosmo:unprocessable-entity>Unknown report {DAV:}principal-property-search</cosmo:unprocessable-entity>
                        </D:error>"""

        mockMvc.perform(report("/dav/users")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }
}
