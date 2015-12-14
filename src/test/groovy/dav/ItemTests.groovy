package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport
import testutil.TestUser

import static org.hamcrest.Matchers.notNullValue
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.POST
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.builder.GeneralRequest.PROPFIND_DISPLAYNAME_REQUEST
import static testutil.builder.MethodNotAllowedBuilder.notAllowed
import static testutil.mockmvc.CaldavHttpMethod.COPY
import static testutil.mockmvc.CaldavHttpMethod.MOVE
import static testutil.mockmvc.CustomRequestBuilders.*
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
@WithUserDetails(TestUser.USER01)
public class ItemTests extends IntegrationTestSupport {

    @Test
    public void itemOptions() throws Exception {
        mockMvc.perform(options("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isOk())
                .andExpect(header().string("DAV", "1, 3, calendar-access"))
                .andExpect(header().string(ALLOW, "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH"));
    }

    @Test
    public void itemGet() {
        def response  = """\
                        <html>
                        <head><title>no name</title></head>
                        <body>
                        <h1>no name</h1>
                        <h2>Members</h2>
                        <ul>
                        <li><a href="/dav/item/de359448-1ee0-4151-872d-eea0ee462bc6/calendar/">calendarDisplayName</a></li>
                        </ul>
                        <h2>Properties</h2>
                        <dl>
                        <dt>{DAV:}creationdate</dt><dd>2015-11-21T21:11:00Z</dd>
                        <dt>{DAV:}current-user-privilege-set</dt><dd>{DAV:}read, {DAV:}read-current-user-privilege-set, {DAV:}write, {urn:ietf:params:xml:ns:caldav}read-free-busy</dd>
                        <dt>{DAV:}displayname</dt><dd>-- no value --</dd>
                        <dt>{http://osafoundation.org/cosmo/DAV}exclude-free-busy-rollup</dt><dd>false</dd>
                        <dt>{DAV:}getetag</dt><dd>&quot;ghFexXxxU+9KC/of1jmJ82wMFig=&quot;</dd>
                        <dt>{DAV:}getlastmodified</dt><dd>Sat, 21 Nov 2015 21:11:00 GMT</dd>
                        <dt>{DAV:}iscollection</dt><dd>1</dd>
                        <dt>{DAV:}owner</dt><dd>/dav/users/test01@localhost.de</dd>
                        <dt>{DAV:}resourcetype</dt><dd>{DAV:}collection</dd>
                        <dt>{DAV:}supported-report-set</dt><dd>{urn:ietf:params:xml:ns:caldav}calendar-multiget, {urn:ietf:params:xml:ns:caldav}calendar-query, {urn:ietf:params:xml:ns:caldav}free-busy-query</dd>
                        <dt>{http://osafoundation.org/cosmo/DAV}uuid</dt><dd>de359448-1ee0-4151-872d-eea0ee462bc6</dd>
                        </dl>
                        <p>
                        <a href="/dav/users/test01@localhost.de">Principal resource</a><br>
                        </body></html>
                        """.stripIndent()

        mockMvc.perform(get("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isOk())
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }

    @Test
    public void itemHead() throws Exception {
        mockMvc.perform(head("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isOk())
                .andExpect(etag(notNullValue()));
    }

    @Test
    public void itemPost() throws Exception {
        mockMvc.perform(post("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(xml(notAllowed(POST).onCollection()));
    }

    @Test
    public void itemPropfind() throws Exception {
        def response = """\
                        <D:multistatus xmlns:D="DAV:">
                            <D:response>
                                <D:href>/dav/item/de359448-1ee0-4151-872d-eea0ee462bc6/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                            <D:response>
                                <D:href>/dav/item/de359448-1ee0-4151-872d-eea0ee462bc6/calendar/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname>calendarDisplayName</D:displayname>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(propfind("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(PROPFIND_DISPLAYNAME_REQUEST))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response));
    }

    @Test
    public void itemPropPatchSet() throws Exception {
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
                                <D:href>/dav/item/de359448-1ee0-4151-872d-eea0ee462bc6/</D:href>
                                <D:propstat>
                                    <D:prop>
                                        <D:displayname/>
                                    </D:prop>
                                    <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                            </D:response>
                        </D:multistatus>"""

        mockMvc.perform(proppatch("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }

    @Test
    public void itemDelete() throws Exception {
        mockMvc.perform(delete("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(DELETE).onHomeCollection()))
    }

    @Test
    public void itemCopy() throws Exception {
        mockMvc.perform(copy("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(COPY).onHomeCollection()))
    }

    @Test
    public void itemMove() throws Exception {
        mockMvc.perform(move("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(textXmlContentType())
                .andExpect(xml(notAllowed(MOVE).onHomeCollection()))
    }

    @Test
    public void itemReport() throws Exception {
        def request = '''\
                        <D:principal-match xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50/">
                            <D:principal-property>
                                <D:displayname/>
                            </D:principal-property>
                        </D:principal-match>'''

        def response = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:"><cosmo:unprocessable-entity>Unknown report {DAV:}principal-match</cosmo:unprocessable-entity></D:error>"""

        mockMvc.perform(report("/dav/item/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .contentType(TEXT_XML)
                .content(request))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(textXmlContentType())
                .andExpect(xml(response));
    }
}
