package dav.user

import org.junit.Test
import org.unitedinternet.cosmo.IntegrationTestSupport
import util.TestUser

import static carldav.util.builder.GeneralResponse.NOT_FOUND
import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static util.mockmvc.CustomResultMatchers.html
import static util.mockmvc.CustomResultMatchers.xml
import static util.HeaderUtil.user
import static util.TestUser.TEST01
import static util.mockmvc.CustomResultMatchers.textHtmlContentType
import static util.mockmvc.CustomResultMatchers.textXmlContentType

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

        mockMvc.perform(get("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header(AUTHORIZATION, user(testUser)))
                .andExpect(status().isOk())
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }
}