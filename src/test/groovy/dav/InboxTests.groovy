package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomResultMatchers.html
import static testutil.mockmvc.CustomResultMatchers.textHtmlContentType

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class InboxTests extends IntegrationTestSupport {

    @Test
    public void inboxGet() {
        def response = """\
                        <html>
                        <head><title>Inbox</title></head>
                        <body>
                        <h1>Inbox</h1>
                        <h2>Properties</h2>
                        <dl>
                        <dt>{DAV:}acl</dt><dd>not implemented yet</dd>
                        <dt>{DAV:}current-user-privilege-set</dt><dd>{DAV:}read</dd>
                        <dt>{DAV:}displayname</dt><dd>Inbox</dd>
                        <dt>{DAV:}getetag</dt><dd></dd>
                        <dt>{DAV:}iscollection</dt><dd>1</dd>
                        <dt>{DAV:}resourcetype</dt><dd>{DAV:}collection, {urn:ietf:params:xml:ns:caldav}schedule-inbox</dd>
                        <dt>{DAV:}supported-report-set</dt><dd></dd>
                        </dl>
                        <p>
                        <a href="/dav/test01@localhost.de/">Home collection</a><br>
                        <a href="/dav/users/test01@localhost.de">Principal resource</a><br>
                        </body></html>
                        """.stripIndent()

        mockMvc.perform(get("/dav/{uid}/Inbox", USER01))
                .andExpect(status().isOk())
                .andExpect(textHtmlContentType())
                .andExpect(html(response));
    }
}
