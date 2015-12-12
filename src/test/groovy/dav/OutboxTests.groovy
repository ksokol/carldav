package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.hamcrest.Matchers.is
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.builder.GeneralRequest.PROPFIND_DISPLAYNAME_REQUEST
import static testutil.builder.GeneralResponse.INTERNAL_SERVER_ERROR
import static testutil.mockmvc.CustomRequestBuilders.*
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class OutboxTests extends IntegrationTestSupport {

    @Test
    public void outboxOptions() throws Exception {
        mockMvc.perform(options("/dav/{uid}/Outbox", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string(ALLOW, is("OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPFIND, PROPPATCH, LOCK, UNLOCK, REPORT, ACL")))
                .andExpect(header().string("DAV", is("1, 3, access-control, calendar-access, calendar-schedule, calendar-auto-schedule")))
    }

    @Test
    public void outboxHead() throws Exception {
        mockMvc.perform(head("/dav/{uid}/Outbox", USER01))
                .andExpect(status().isOk())
                .andExpect(textHtmlContentType())
    }

    @Test
    public void outboxGet() {
        def response = """\
                        <html>
                        <head><title>Outbox</title></head>
                        <body>
                        <h1>Outbox</h1>
                        <h2>Properties</h2>
                        <dl>
                        <dt>{DAV:}acl</dt><dd>not implemented yet</dd>
                        <dt>{DAV:}current-user-privilege-set</dt><dd>{DAV:}read</dd>
                        <dt>{DAV:}displayname</dt><dd>Outbox</dd>
                        <dt>{DAV:}getetag</dt><dd></dd>
                        <dt>{DAV:}iscollection</dt><dd>1</dd>
                        <dt>{DAV:}resourcetype</dt><dd>{DAV:}collection, {urn:ietf:params:xml:ns:caldav}schedule-outbox</dd>
                        <dt>{DAV:}supported-report-set</dt><dd></dd>
                        </dl>
                        <p>
                        <a href="/dav/test01@localhost.de/">Home collection</a><br>
                        <a href="/dav/users/test01@localhost.de">Principal resource</a><br>
                        </body></html>
                        """.stripIndent()

        mockMvc.perform(get("/dav/{uid}/Outbox", USER01))
                .andExpect(status().isOk())
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }

    @Test
    public void outboxPost() {
        def response = """\
                        <C:schedule-response xmlns:C="urn:ietf:params:xml:ns:caldav"/>"""

        mockMvc.perform(post("/dav/{uid}/Outbox", USER01))
        .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void outboxPropfind() throws Exception {
        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/Outbox/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname>Outbox</D:displayname>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{uid}/Outbox", USER01)
                .contentType(TEXT_XML)
                .content(PROPFIND_DISPLAYNAME_REQUEST))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response));
    }

    @Test
    public void outboxPropPatchSet() throws Exception {
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
                                <D:href>/dav/test01@localhost.de/Outbox/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 403 Forbidden</D:status>
                                </D:propstat>
                                <D:responsedescription>Property {DAV:}displayname is protected</D:responsedescription>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(proppatch("/dav/{uid}/Outbox", USER01)
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void outboxDelete() throws Exception {
        def response = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <cosmo:internal-server-error>Expected 'member' as instance of: [org.unitedinternet.cosmo.dav.impl.DavItemResource]</cosmo:internal-server-error>
                        </D:error>"""

        mockMvc.perform(delete("/dav/{uid}/Outbox", USER01))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(response))
    }

    @Test
    public void outboxCopy() throws Exception {
        mockMvc.perform(copy("/dav/{uid}/Outbox", USER01)
                .header("Destination", "/dav/" + USER01 + "/newOutbox"))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR))
    }

    @Test
    public void outboxMove() throws Exception {
        mockMvc.perform(move("/dav/{uid}/Outbox", USER01)
                .header("Destination", "/dav/" + USER01 + "/newOutbox"))
                .andExpect(status().isInternalServerError())
                .andExpect(textXmlContentType())
                .andExpect(xml(INTERNAL_SERVER_ERROR))
    }

    @Test
    public void outboxReport() throws Exception {
        def request = '''\
                        <D:principal-match xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:principal-property>
                                <D:displayname/>
                            </D:principal-property>
                        </D:principal-match>'''

        def response = '''\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <cosmo:unprocessable-entity>Unknown report {DAV:}principal-match</cosmo:unprocessable-entity>
                        </D:error>'''

        mockMvc.perform(report("/dav/{uid}/Outbox", USER01)
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void outboxPropPatchRemoveDeadProperty() throws Exception {
        def request = '''\
                        <D:propertyupdate xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:remove>
                                <D:prop><Z:Copyright-Owner/></D:prop>
                            </D:remove>
                        </D:propertyupdate>'''

        def response = '''\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/test01@localhost.de/Outbox/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <Z:Copyright-Owner xmlns:Z="http://www.w3.com/standards/z39.50/"/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 403 Forbidden</D:status>
                                </D:propstat>
                                <D:responsedescription>Dead properties are not supported on this collection</D:responsedescription>
                            </D:response>
                        </D:multistatus>'''

        mockMvc.perform(proppatch("/dav/{uid}/Outbox", USER01)
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }
}
