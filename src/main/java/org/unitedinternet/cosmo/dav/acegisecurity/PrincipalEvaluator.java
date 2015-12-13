package org.unitedinternet.cosmo.dav.acegisecurity;

import org.unitedinternet.cosmo.dav.acl.UserAclEvaluator;

/**
 * @author Kamill Sokol
 */
public interface PrincipalEvaluator {

    void evaluate(String method, UserAclEvaluator userAclEvaluator);
}
