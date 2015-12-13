package org.unitedinternet.cosmo.dav.acegisecurity;

import org.unitedinternet.cosmo.dav.CaldavMethodType;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator;

/**
 * @author Kamill Sokol
 */
public class UserPrincipalCollectionEvaluator implements PrincipalEvaluator {

    @Override
    public void evaluate(String method, UserAclEvaluator evaluator) {
        if ("PROPFIND".equals(method)) {
            return;
        }

        DavPrivilege privilege = CaldavMethodType.isReadMethod(method) ? DavPrivilege.READ : DavPrivilege.WRITE;

        if (!evaluator.evaluateUserPrincipalCollection(privilege)) {
            throw new AclEvaluationException(privilege);
        }
    }
}
