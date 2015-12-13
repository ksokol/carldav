package org.unitedinternet.cosmo.dav.acegisecurity;

import org.unitedinternet.cosmo.dav.CaldavMethodType;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator;
import org.unitedinternet.cosmo.model.User;

/**
 * @author Kamill Sokol
 */
public class UserPrincipalEvaluator implements PrincipalEvaluator {

    @Override
    public void evaluate(final String method, final UserAclEvaluator evaluator) {
        if (evaluator.getPrincipal() == null) {
            return;
        }

        if ("PROPFIND".equals(method)) {
            return;
        }

        final DavPrivilege privilege = CaldavMethodType.isReadMethod(method) ? DavPrivilege.READ : DavPrivilege.WRITE;

        if (!evaluator.evaluateUserPrincipal((User) evaluator.getPrincipal(), privilege)) {
            throw new AclEvaluationException(privilege);
        }
    }
}
