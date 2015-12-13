package org.unitedinternet.cosmo.dav.acegisecurity

import org.junit.Test
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator
import org.unitedinternet.cosmo.model.mock.MockUser

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.POST

/**
 * @author Kamill Sokol
 */
public class UserPrincipalEvaluatorCollectionTests {

    def UserPrincipalCollectionEvaluator uut = new UserPrincipalCollectionEvaluator()

    @Test(expected = AclEvaluationException.class)
    void shouldThrowAclEvaluationException() {
        uut.evaluate(POST.name(), new UserAclEvaluator(new MockUser(admin: false)))
    }

    @Test
    void nullCheck() {
        uut.evaluate(POST.name(), new UserAclEvaluator(new MockUser(admin: true)))
    }

    @Test
    void allowPropfind() {
        uut.evaluate("PROPFIND", new UserAclEvaluator(new MockUser(admin: false)))
    }

    @Test
    void allowMethodForAdmin() {
        uut.evaluate(GET.name(), new UserAclEvaluator(new MockUser(admin: true)))
    }
}