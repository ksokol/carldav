package org.unitedinternet.cosmo.dav.acegisecurity

import org.junit.Test
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator
import org.unitedinternet.cosmo.model.mock.MockUser

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.POST

/**
 * @author Kamill Sokol
 */
public class UserPrincipalEvaluatorTests {

    def UserPrincipalEvaluator uut = new UserPrincipalEvaluator()

    @Test(expected = AclEvaluationException.class)
    void shouldThrowAclEvaluationException() {
        def user = new MockUser(admin: false)
        uut.evaluate(POST.name(), new UserAclEvaluator(user))
    }

    @Test
    void nullCheck() {
        uut.evaluate(POST.name(), new UserAclEvaluator())
    }

    @Test
    void allowPropfind() {
        def user = new MockUser(admin: false)
        uut.evaluate("PROPFIND", new UserAclEvaluator(user))
    }

    @Test
    void allowMethodForAdmin() {
        def user = new MockUser(admin: true)
        uut.evaluate(GET.name(), new UserAclEvaluator(user))
    }
}