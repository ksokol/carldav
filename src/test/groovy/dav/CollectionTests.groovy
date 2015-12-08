package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.TestUser

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.builder.GeneralRequest.PROPFIND_DISPLAYNAME_REQUEST
import static testutil.builder.GeneralResponse.NOT_FOUND
import static testutil.builder.GeneralResponse.NOT_SUPPORTED_PRIVILEGE
import static testutil.builder.MethodNotAllowedBuilder.notAllowed
import static testutil.mockmvc.CaldavHttpMethod.COPY
import static testutil.mockmvc.CaldavHttpMethod.MOVE
import static testutil.mockmvc.CustomRequestBuilders.*
import static testutil.mockmvc.CustomResultMatchers.*
import static testutil.xmlunit.XmlMatcher.equalXml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(TestUser.USER01)
public class CollectionTests extends IntegrationTestSupport {

    @Test
    public void collectionOptionsNotFound() throws Exception {
        mockMvc.perform(options("/dav/collection/{uid}", "1"))
                .andExpect(status().isNotFound())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_FOUND));
    }

    @Test
    public void collectionOptions() throws Exception {
        mockMvc.perform(options("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, access-control, calendar-access, ticket"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, MKTICKET, DELTICKET"));
    }

    @Test
    public void collectionGetNotFound() throws Exception {
        mockMvc.perform(get("/dav/collection/{uid}", "1"))
                .andExpect(status().isNotFound())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_FOUND));
    }

    @Test
    public void collectionGet() throws Exception {
        def response  = """\
                        <html>
                        <head><title>no name</title></head>
                        <body>
                        <h1>no name</h1>
                        <h2>Members</h2>
                        <ul>
                        <li><a href="/dav/collection/de359448-1ee0-4151-872d-eea0ee462bc6/calendar/">calendarDisplayName</a></li>
                        </ul>
                        <h2>Properties</h2>
                        <dl>
                        <dt>{DAV:}acl</dt><dd>not implemented yet</dd>
                        <dt>{DAV:}creationdate</dt><dd>2015-11-21T21:11:00Z</dd>
                        <dt>{DAV:}current-user-privilege-set</dt><dd>{DAV:}read, {DAV:}read-current-user-privilege-set, {DAV:}write, {urn:ietf:params:xml:ns:caldav}read-free-busy</dd>
                        <dt>{DAV:}displayname</dt><dd>-- no value --</dd>
                        <dt>{http://osafoundation.org/cosmo/DAV}exclude-free-busy-rollup</dt><dd>false</dd>
                        <dt>{DAV:}getetag</dt><dd>&quot;ghFexXxxU+9KC/of1jmJ82wMFig=&quot;</dd>
                        <dt>{DAV:}getlastmodified</dt><dd>Sat, 21 Nov 2015 21:11:00 GMT</dd>
                        <dt>{DAV:}iscollection</dt><dd>1</dd>
                        <dt>{DAV:}owner</dt><dd>/dav/users/test01@localhost.de</dd>
                        <dt>{DAV:}principal-collection-set</dt><dd>/dav/users</dd>
                        <dt>{DAV:}resourcetype</dt><dd>{DAV:}collection</dd>
                        <dt>{DAV:}supported-report-set</dt><dd>{DAV:}principal-match, {DAV:}principal-property-search, {urn:ietf:params:xml:ns:caldav}calendar-multiget, {urn:ietf:params:xml:ns:caldav}calendar-query, {urn:ietf:params:xml:ns:caldav}free-busy-query</dd>
                        <dt>{http://www.xythos.com/namespaces/StorageServer}ticketdiscovery</dt><dd></dd>
                        <dt>{http://osafoundation.org/cosmo/DAV}uuid</dt><dd>de359448-1ee0-4151-872d-eea0ee462bc6</dd>
                        </dl>
                        <p>
                        <a href="/dav/users/test01@localhost.de">Principal resource</a><br>
                        </body></html>
                        """.stripIndent()

        mockMvc.perform(get("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isOk())
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }

    @Test
    public void collectionHead() throws Exception {
        mockMvc.perform(head("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isOk())
                .andExpect(etag(notNullValue()));
    }

    @Test
    public void collectionPost() throws Exception {
        mockMvc.perform(post("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(xml(notAllowed(POST).onCollection()));
    }

    @Test
    public void collectionPropfind() throws Exception {
        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/collection/de359448-1ee0-4151-872d-eea0ee462bc6/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                            <D:response>
                                <D:href>/dav/collection/de359448-1ee0-4151-872d-eea0ee462bc6/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname>calendarDisplayName</D:displayname>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(propfind("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(PROPFIND_DISPLAYNAME_REQUEST))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response));
    }

    @Test
    public void collectionPropPatchSet() throws Exception {
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
                                <D:href>/dav/collection/de359448-1ee0-4151-872d-eea0ee462bc6/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(proppatch("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void collectionDelete() throws Exception {
        mockMvc.perform(delete("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(DELETE).onHomeCollection()))
    }

    @Test
    public void collectionCopy() throws Exception {
        mockMvc.perform(copy("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(COPY).onHomeCollection()))
    }

    @Test
    public void collectionMove() throws Exception {
        mockMvc.perform(move("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(MOVE).onHomeCollection()))
    }

    @Test
    public void collectionReport() throws Exception {
        def request = '''\
                        <D:principal-match xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:principal-property>
                                <D:displayname/>
                            </D:principal-property>
                        </D:principal-match>'''

        def response = """\
                        <D:multistatus xmlns:D="DAV:"/>"""

        mockMvc.perform(report("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void collectionMkticket() throws Exception {
        def request = """\
                        <C:ticketinfo xmlns:C="http://www.xythos.com/namespaces/StorageServer">
                            <D:privilege xmlns:D="DAV:"><D:read/></D:privilege>
                            <C:timeout>Second-3600</C:timeout>
                            <C:visits>1</C:visits>
                        </C:ticketinfo>"""


        def result = mockMvc.perform(mkticket("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(textXmlContentType())
                .andReturn()

        final String ticket = result.getResponse().getHeader("Ticket")
        final String content = result.getResponse().getContentAsString()

        def response = """\
                        <D:prop xmlns:D="DAV:">
                            <ticket:ticketdiscovery xmlns:ticket="http://www.xythos.com/namespaces/StorageServer">
                                <ticket:ticketinfo>
                                    <ticket:id>${ticket}</ticket:id>
                                    <D:owner>
                                        <D:href>/dav/users/test01@localhost.de</D:href>
                                    </D:owner>
                                    <ticket:timeout>Second-3600</ticket:timeout>
                                    <ticket:visits>infinity</ticket:visits>
                                    <D:privilege>
                                        <D:privilege>
                                            <D:read/>
                                        </D:privilege>
                                    </D:privilege>
                                </ticket:ticketinfo>
                            </ticket:ticketdiscovery>
                        </D:prop>"""

        assertThat(content, equalXml(response))
    }

    @Test
    public void collectionDelticket() throws Exception {
        def request = """\
                        <C:ticketinfo xmlns:C="http://www.xythos.com/namespaces/StorageServer">
                            <D:privilege xmlns:D="DAV:"><D:read/></D:privilege>
                            <C:timeout>Second-3600</C:timeout>
                            <C:visits>1</C:visits>
                        </C:ticketinfo>"""


        def result = mockMvc.perform(mkticket("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(textXmlContentType())
                .andReturn()

        final String ticket = result.getResponse().getHeader("Ticket")

        def delResponse1 = """\
                        <D:prop xmlns:D="DAV:">
                            <ticket:ticketdiscovery xmlns:ticket="http://www.xythos.com/namespaces/StorageServer">
                                <ticket:ticketinfo>
                                    <ticket:id>
                                        ${ticket}
                                    </ticket:id>
                                    <D:owner>
                                        <D:href>/dav/users/test01@localhost.de</D:href>
                                    </D:owner>
                                    <ticket:timeout>Second-3600</ticket:timeout>
                                    <ticket:visits>infinity</ticket:visits>
                                    <D:privilege>
                                        <D:privilege>
                                            <D:read/>
                                        </D:privilege>
                                    </D:privilege>
                                </ticket:ticketinfo>
                            </ticket:ticketdiscovery>
                        </D:prop>"""

        assertThat(result.getResponse().getContentAsString(), equalXml(delResponse1))

        mockMvc.perform(delticket("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .header("ticket", ticket))
                .andExpect(status().isNoContent())

        def delResponse2 = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <cosmo:precondition-failed>Ticket
                                ${ticket}
                                does not exist
                            </cosmo:precondition-failed>
                        </D:error>"""

        mockMvc.perform(delticket("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .header("ticket", ticket))
                .andExpect(status().isPreconditionFailed())
                .andExpect(textXmlContentType())
                .andExpect(xml(delResponse2))
    }

    @Test
    public void collectionAcl() throws Exception {
        mockMvc.perform(acl("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isForbidden())
                .andExpect(textXmlContentType())
                .andExpect(xml(NOT_SUPPORTED_PRIVILEGE));
    }
}