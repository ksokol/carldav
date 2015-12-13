package org.unitedinternet.cosmo.dav.acegisecurity;

import org.unitedinternet.cosmo.dav.acl.DavPrivilege;

public class AclEvaluationException extends RuntimeException {

    static final long serialVersionUID = -7034897190145766939L;

    private transient DavPrivilege privilege;

    public AclEvaluationException(final DavPrivilege privilege) {
        this.privilege = privilege;
    }

    public DavPrivilege getPrivilege() {
        return privilege;
    }
}