package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomResultMatchers.text
import static testutil.mockmvc.CustomResultMatchers.textHtmlContentType

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class PrincipalTests extends IntegrationTestSupport {

    @Test
    void get() {
        def response1 = """\
                            <html>
                            <head><title>test01@localhost.de</title></head>
                            <body>
                            <h1>test01@localhost.de</h1>
                            <h2>Properties</h2>
                            <dl>
                            <dt>{urn:ietf:params:xml:ns:carddav}addressbook-home-set</dt><dd><a href="/carldav/dav/test01@localhost.de/contacts">/carldav/dav/test01@localhost.de/contacts</a>
                            </dd>
                            <dt>{urn:ietf:params:xml:ns:caldav}calendar-home-set</dt><dd><a href="/carldav/dav/test01@localhost.de">/carldav/dav/test01@localhost.de</a>
                            </dd>
                            <dt>{DAV:}displayname</dt><dd>test01@localhost.de</dd>
                            <dt>{DAV:}iscollection</dt><dd>0</dd>
                            <dt>{DAV:}principal-URL</dt><dd><a href="/carldav/principals/users/test01@localhost.de">/carldav/principals/users/test01@localhost.de</a>
                            </dd>
                            <dt>{DAV:}resourcetype</dt><dd></dd>
                            <dt>{DAV:}supported-report-set</dt><dd></dd>
                            </dl>
                            <p>
                            <a href="/carldav/dav/test01@localhost.de/">Home collection</a><br>
                            </body></html>
                            """.stripIndent()

       mockMvc.perform(get("/carldav/principals/users/{email}", USER01))
                .andExpect(textHtmlContentType())
                .andExpect(status().isOk())
                .andExpect(text(response1))
    }
}
