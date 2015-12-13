package org.unitedinternet.cosmo.dav.acegisecurity

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.FilterInvocation
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator
import org.unitedinternet.cosmo.model.mock.MockUser
import org.unitedinternet.cosmo.service.UserService


import static org.mockito.Mockito.*

/**
 * @author Kamill Sokol
 */
class DavAccessDecisionManagerTests {

    def UserService userService = mock(UserService.class)
    def PrincipalEvaluator userPrincipalEvaluator = mock(PrincipalEvaluator.class)
    def PrincipalEvaluator userPrincipalCollectionEvaluator = mock(PrincipalEvaluator.class)
    def DavAccessDecisionManager uut = new DavAccessDecisionManager(userService, userPrincipalEvaluator, userPrincipalCollectionEvaluator)
    def token = new UsernamePasswordAuthenticationToken(new CosmoUserDetails("user", "password", new MockUser()), "password")
    def invocation = mock(FilterInvocation.class)

    @Rule
    public ExpectedException expectedException = ExpectedException.none()

    @Before
    public void before() {
        reset(userService, userPrincipalCollectionEvaluator, invocation);
    }

    @Test
    void supportsClass() {
        assertThat(uut.supports(FilterInvocation.class), is(true))
    }

    @Test
    void supportsConfigAttribute() {
        assertThat(uut.supports(null), is(true))
    }

    @Test
    void didNotMatchUri() {
        uut.match("/test", "POST", new UserAclEvaluator(new MockUser(admin: true)))
    }

    @Test
    void matchUsers() {
        def evaluator = new UserAclEvaluator(new MockUser(admin: true))
        uut.match("/users", "POST", evaluator)
    }

    @Test
    void matchSpecificUnknownUser() {
        def evaluator = new UserAclEvaluator(new MockUser(admin: true))
        uut.match("/users/user", "POST", evaluator)
    }

    @Test
    void matchSpecificKnownUser() {
        def user = new MockUser(admin: true)
        when(userService.getUser("user")).thenReturn(user)
        def evaluator = new UserAclEvaluator(user)
        uut.match("/users/user", "POST", evaluator)
    }

    @Test
    void expectInsufficientAuthenticationException() {
        expectedException.expect(InsufficientAuthenticationException.class)
        expectedException.expectMessage("Unrecognized authentication token")
        uut.decide(null, null, null)
    }

    @Test
    void decideWithUsernamePasswordAuthenticationToken() {
        def request = new MockHttpServletRequest()
        when(invocation.getHttpRequest()).thenReturn(request)

        uut.decide(token, invocation, null)

        verify(userPrincipalEvaluator, never()).evaluate(anyObject(), anyObject())
        verify(userPrincipalCollectionEvaluator, never()).evaluate(anyObject(), anyObject())
    }

    @Test
    void decideWithPreAuthenticatedAuthenticationToken() {
        def request = new MockHttpServletRequest()
        when(invocation.getHttpRequest()).thenReturn(request)

        uut.decide(new PreAuthenticatedAuthenticationToken("user", "password"), invocation, null)

        verify(userPrincipalEvaluator, never()).evaluate(anyObject(), anyObject())
        verify(userPrincipalCollectionEvaluator, never()).evaluate(anyObject(), anyObject())
    }

    @Test
    void decideDifferentPathInfo() {
        def request = new MockHttpServletRequest()
        request.setPathInfo("/test/")
        request.setMethod("POST")
        when(invocation.getHttpRequest()).thenReturn(request)

        uut.decide(token, invocation, null)

        verify(userPrincipalEvaluator, never()).evaluate(anyObject(), anyObject())
        verify(userPrincipalCollectionEvaluator, never()).evaluate(anyObject(), anyObject())
    }

    @Test(expected = DavAccessDeniedException.class)
    void expectDavAccessDeniedException() {
        def request = new MockHttpServletRequest()
        request.setPathInfo("/users")
        when(invocation.getHttpRequest()).thenReturn(request)
        when(userPrincipalCollectionEvaluator.evaluate(anyObject(), anyObject())).thenThrow(new AclEvaluationException(null))

        uut.decide(token, invocation, null)

        verify(userPrincipalEvaluator, never()).evaluate(anyObject(), anyObject())
        verify(userPrincipalCollectionEvaluator, never()).evaluate(anyObject(), anyObject())
    }

}
