package dav.user

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static carldav.CaldavHttpMethod.*
import static testutil.builder.GeneralResponse.NOT_SUPPORTED_PRIVILEGE
import static testutil.builder.MethodNotAllowedBuilder.notAllowed
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpMethod.*
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static util.mockmvc.CustomRequestBuilders.*
import static util.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class UsersTests extends IntegrationTestSupport {

    @Test
    public void userGet() throws Exception {
        def response = '''\
                        <html>
                        <head><title>test01@localhost.de</title></head>
                        <body>
                        <h1>test01@localhost.de</h1>
                        <h2>Properties</h2>
                        <dl>
                        <dt>{DAV:}acl</dt><dd>not implemented yet</dd>
                        <dt>{DAV:}alternate-URI-set</dt><dd></dd>
                        <dt>{urn:ietf:params:xml:ns:caldav}calendar-home-set</dt><dd>/dav/test01@localhost.de</dd>
                        <dt>{DAV:}creationdate</dt><dd>2015-11-16T15:35:16Z</dd>
                        <dt>{DAV:}current-user-privilege-set</dt><dd>{DAV:}read</dd>
                        <dt>{DAV:}displayname</dt><dd>test01@localhost.de</dd>
                        <dt>{DAV:}getetag</dt><dd>q0leu+2ctlWs3jLUakICskqYGms=</dd>
                        <dt>{DAV:}getlastmodified</dt><dd>Mon, 16 Nov 2015 15:35:16 GMT</dd>
                        <dt>{DAV:}group-membership</dt><dd></dd>
                        <dt>{DAV:}iscollection</dt><dd>0</dd>
                        <dt>{DAV:}principal-URL</dt><dd>/dav/users/test01@localhost.de</dd>
                        <dt>{DAV:}resourcetype</dt><dd>{DAV:}principal</dd>
                        <dt>{DAV:}supported-report-set</dt><dd>{DAV:}principal-match</dd>
                        </dl>
                        <a href="/dav/users/">User Principals</a></li>
                        <p>
                        <a href="/dav/test01@localhost.de/">Home collection</a><br>
                        </body></html>
                        '''.stripIndent()

        mockMvc.perform(get("/dav/users/{uid}", USER01))
                .andExpect(status().isOk())
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }

    @Test
    public void userPut() throws Exception {
        mockMvc.perform(put("/dav/users/{uid}", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(PUT).onUserPrincipal()));
    }

    @Test
    public void userOptions() throws Exception {
        mockMvc.perform(options("/dav/users/{uid}", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, access-control, calendar-access, ticket"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT"));
    }

    @Test
    public void userHead() throws Exception {
        mockMvc.perform(head("/dav/users/{uid}", USER01))
                .andExpect(status().isOk())
                .andExpect(etag(notNullValue()))
                .andExpect(lastModified(is("Mon, 16 Nov 2015 15:35:16 GMT")));
    }

    @Test
    public void userPost() throws Exception {
        mockMvc.perform(post("/dav/users/{uid}", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(POST).onCollection()));
    }

    @Test
    public void userPropFind() throws Exception {
        def response = '''\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                          <cosmo:bad-request>Depth must be 0 for non-collection resources</cosmo:bad-request>
                        </D:error>'''

        mockMvc.perform(propfind("/dav/users/{uid}", USER01))
                .andExpect(status().isBadRequest())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void userPropPatchSet() throws Exception {
        def request = '''\
                        <D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:set>
                                <D:prop>
                                    <D:displayname></D:displayname>
                                </D:prop>
                            </D:set>
                        </D:propertyupdate>'''

        def respone = '''\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/users/test01@localhost.de</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 403 Forbidden</D:status>
                                </D:propstat>
                                <D:responsedescription>Property {DAV:}displayname is protected</D:responsedescription>
                            </D:response>
                        </D:multistatus>'''

        mockMvc.perform(proppatch("/dav/users/{uid}", USER01)
                .content(request)
                .contentType(TEXT_XML))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(respone));
    }

    @Test
    public void userPropPatchRemoveDeadProperty() throws Exception {
        def request = '''\
                        <D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:remove>
                                <D:prop><Z:Copyright-Owner/></D:prop>
                            </D:remove>
                        </D:propertyupdate>'''

        def response = '''\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/users/test01@localhost.de</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <Z:Copyright-Owner xmlns:Z="http://www.w3.com/standards/z39.50/"/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 403 Forbidden</D:status>
                                </D:propstat>
                                <D:responsedescription>Dead properties are not supported on this resource</D:responsedescription>
                            </D:response>
                        </D:multistatus>'''

        mockMvc.perform(proppatch("/dav/users/{uid}", USER01)
                .content(request)
                .contentType(TEXT_XML))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void userPropPatchRemove() throws Exception {
        def request = '''\
                        <D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:remove>
                                <D:prop><D:displayname/></D:prop>
                            </D:remove>
                        </D:propertyupdate>'''

        def response = '''\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/users/test01@localhost.de</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 403 Forbidden</D:status>
                                </D:propstat>
                                <D:responsedescription>Property {DAV:}displayname is protected</D:responsedescription>
                            </D:response>
                        </D:multistatus>'''

        mockMvc.perform(proppatch("/dav/users/{uid}", USER01)
                .content(request)
                .contentType(TEXT_XML))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void userDelete() throws Exception {
        mockMvc.perform(delete("/dav/users/{uid}", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(DELETE).onUserPrincipal()));
    }

    @Test
    public void userCopy() throws Exception {
        mockMvc.perform(copy("/dav/users/{uid}", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(COPY).onUserPrincipal()));
    }

    @Test
    public void userMove() throws Exception {
        mockMvc.perform(move("/dav/users/{uid}", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(MOVE).onUserPrincipal()));
    }

    @Test
    public void userReportUnprocessable() throws Exception {
        def request = '''\
                        <D:principal-match xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:remove>
                                <D:prop><D:displayname/></D:prop>
                            </D:remove>
                        </D:principal-match>'''

        def response = '''\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <cosmo:unprocessable-entity>Expected either {DAV:}self or {DAV:}principal-property child of {DAV:}principal-match</cosmo:unprocessable-entity>
                        </D:error>'''

        mockMvc.perform(report("/dav/users/{uid}", USER01)
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void userReport() throws Exception {
        def request = '''\
                        <D:principal-match xmlns:D="DAV:">
                            <D:principal-property>
                                <D:owner/>
                            </D:principal-property>
                        </D:principal-match>'''

        def response = '<D:multistatus xmlns:D="DAV:"/>'

        mockMvc.perform(report("/dav/users/{uid}", USER01)
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void userMkticket() throws Exception {
        mockMvc.perform(mkticket("/dav/users/{uid}", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(MKTICKET).onUserPrincipal()));
    }

    @Test
    public void userDelticket() throws Exception {
        mockMvc.perform(delticket("/dav/users/{uid}", USER01))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(DELTICKET).onUserPrincipal()));
    }

    @Test
    public void userAcl() throws Exception {
        mockMvc.perform(acl("/dav/users/{uid}", USER01))
                .andExpect(status().isForbidden())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_SUPPORTED_PRIVILEGE));
    }
}
