package org.unitedinternet.cosmo.dav.acegisecurity

import static org.hamcrest.Matchers.is
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator
import org.unitedinternet.cosmo.model.mock.MockUser
import org.unitedinternet.cosmo.service.UserService


import static org.mockito.Mockito.*
import static org.unitedinternet.cosmo.util.UriTemplate.Match

/**
 * @author Kamill Sokol
 */
class DavAccessDecisionManagerTest {

    def DavAccessDecisionManager uut = new DavAccessDecisionManager()
    def UserService userService = mock(UserService.class)

    @Rule
    public ExpectedException expectedException = ExpectedException.none()

    @Before
    public void before() {
        reset(userService);
        uut.setUserService(userService);
    }

    @Test
    void testEvaluateUserPrincipal() {
        def match = new Match("/test")

        when(userService.getUser("user")).thenReturn(new MockUser());

        uut.evaluateUserPrincipal(match, "POST", new UserAclEvaluator())
    }

    @Test
    void testEvaluateUserPrincipal2() {
        def match = new Match("/test")
        match.put("username", "user")

        def user = new MockUser()
        user.setAdmin(false)

        def evaluator = new UserAclEvaluator(user)

        when(userService.getUser("user")).thenReturn(user)
        expectedException.expect(is(DavAccessDecisionManager.AclEvaluationException))

        uut.evaluateUserPrincipal(match, "POST", evaluator)
    }
}
