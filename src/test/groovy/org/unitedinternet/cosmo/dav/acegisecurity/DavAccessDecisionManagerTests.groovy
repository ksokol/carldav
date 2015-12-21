package org.unitedinternet.cosmo.dav.acegisecurity

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.FilterInvocation
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails
import org.unitedinternet.cosmo.model.hibernate.User

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 * @author Kamill Sokol
 */
class DavAccessDecisionManagerTests {

    def DavAccessDecisionManager uut = new DavAccessDecisionManager()
    def token = new UsernamePasswordAuthenticationToken(new CosmoUserDetails("user", "password", new User()), "password")
    def invocation = mock(FilterInvocation.class)

    @Rule
    public ExpectedException expectedException = ExpectedException.none()

    @Test
    void supportsClass() {
        assertThat(uut.supports(FilterInvocation.class), is(true))
    }

    @Test
    void supportsConfigAttribute() {
        assertThat(uut.supports((ConfigAttribute) null), is(true))
    }

    @Test
    void didNotMatchUri() {
        expectedException.expect(DavAccessDeniedException.class)
        expectedException.expectMessage("access denied for /dav")
        uut.match("user1", "/dav")
    }

    @Test
    void matchUsers() {
        uut.match("user1", "/dav/user1")
    }

    @Test
    void matchUsers2() {
        uut.match("user1", "/dav/user1/level1/level2")
    }

    @Test
    void matchSpecificUnknownUser() {
        expectedException.expect(DavAccessDeniedException.class)
        expectedException.expectMessage("access denied for /dav/user2")
        uut.match("user1", "/dav/user2")
    }

    @Test
    void matchSpecificKnownUser() {
        uut.match("user", "dav/user")
    }

    @Test
    void expectInsufficientAuthenticationException() {
        expectedException.expect(InsufficientAuthenticationException.class)
        expectedException.expectMessage("Unrecognized authentication token")
        uut.decide(null, null, null)
    }

    @Test
    void decideDifferentPathInfo() {
        def request = new MockHttpServletRequest()
        request.setRequestURI("/test/")
        when(invocation.getHttpRequest()).thenReturn(request)
        expectedException.expect(DavAccessDeniedException.class)
        expectedException.expectMessage("access denied for /test/")

        uut.decide(token, invocation, null)
    }
}
