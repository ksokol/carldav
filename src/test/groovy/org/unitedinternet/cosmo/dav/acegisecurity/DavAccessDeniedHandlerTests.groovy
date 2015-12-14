package org.unitedinternet.cosmo.dav.acegisecurity

import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException

import static org.hamcrest.MatcherAssert.assertThat
import static testutil.xmlunit.XmlMatcher.equalXml

/**
 * @author Kamill Sokol
 */
class DavAccessDeniedHandlerTests extends GroovyTestCase {

    def DavAccessDeniedHandler uut = new DavAccessDeniedHandler();

    void testAccessDeniedException() {
        def response = new MockHttpServletResponse()
        uut.handle(null,response, new AccessDeniedException("test"))
        def actual = response.getContentAsString()
        def expected = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <D:needs-privileges>test</D:needs-privileges>
                        </D:error>"""

        assert response.getStatus() == 403
        assertThat(actual, equalXml(expected))
    }

    void testDavAccessDeniedException() {
        def response = new MockHttpServletResponse()
        uut.handle(null,response, new DavAccessDeniedException("href"))
        def actual = response.getContentAsString()
        def expected = """\
                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                            <D:needs-privileges>href</D:needs-privileges>
                        </D:error>""";

        assert response.getStatus() == 403
        assertThat(actual, equalXml(expected))
    }
}
