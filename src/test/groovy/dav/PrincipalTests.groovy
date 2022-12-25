package dav

import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static util.TestUser.USER01
import static util.mockmvc.CustomResultMatchers.textHtmlContentType

@WithUserDetails(USER01)
class PrincipalTests extends IntegrationTestSupport {

  @Test
  void get() {
    def response = """\
                            <html>
                            <head><title>${USER01}</title></head>
                            <body>
                            <h1>test01@localhost.de</h1>
                            <h2>Properties</h2>
                            <dl>
                            <dt>{urn:ietf:params:xml:ns:carddav}addressbook-home-set</dt><dd><a href="/carldav/dav/${USER01}/contacts">/carldav/dav/${USER01}/contacts</a>
                            </dd>
                            <dt>{urn:ietf:params:xml:ns:caldav}calendar-home-set</dt><dd><a href="/carldav/dav/${USER01}">/carldav/dav/${USER01}</a>
                            </dd>
                            <dt>{DAV:}current-user-privilege-set</dt><dd>[{DAV:}read, {DAV:}write]</dd>
                            <dt>{DAV:}displayname</dt><dd>${USER01}</dd>
                            <dt>{DAV:}iscollection</dt><dd>0</dd>
                            <dt>{DAV:}principal-URL</dt><dd><a href="/carldav/principals/users/${USER01}">/carldav/principals/users/${USER01}</a>
                            </dd>
                            <dt>{DAV:}resourcetype</dt><dd></dd>
                            <dt>{DAV:}supported-report-set</dt><dd></dd>
                            </dl>
                            <p>
                            <a href="/carldav/dav/${USER01}/">Home collection</a><br>
                            </body></html>
                            """.stripIndent()

    def result = mockMvc.perform(get("/principals/users/{email}", USER01))
      .andExpect(textHtmlContentType())
      .andExpect(status().isOk())
      .andReturn().getResponse()

    assertThat(result.getContentAsString(), is(response))
  }
}
