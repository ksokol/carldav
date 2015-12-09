package org.unitedinternet.cosmo.dav.servlet;

import static org.unitedinternet.cosmo.server.ServerConstants.ATTR_SERVICE_EXCEPTION;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.NeedsPrivilegesException;
import org.unitedinternet.cosmo.dav.caldav.CaldavExceptionForbidden;
import org.unitedinternet.cosmo.security.CosmoSecurityException;
import org.unitedinternet.cosmo.security.ItemSecurityException;
import org.unitedinternet.cosmo.security.Permission;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

public enum ExceptionMapper {
    SECURITY_EXCEPTION_MAPPER (CosmoSecurityException.class) {
        @Override
        protected CosmoDavException doMap(Throwable t, HttpServletRequest request) {
            // handle security errors
            NeedsPrivilegesException npe = null;
            // Determine required privilege if we can and include
            // in response
            if(t instanceof ItemSecurityException) {
                ItemSecurityException ise = (ItemSecurityException) t;
                DavPrivilege priv = ise.getPermission()== Permission.READ ? DavPrivilege.READ : DavPrivilege.WRITE;
                npe = new NeedsPrivilegesException(request.getRequestURI(), priv);
            } else {
                // otherwise send generic response
                npe = new NeedsPrivilegesException(t.getMessage());
            }

            return npe;
        }
    },
    FORBIDDEN_EXCEPTION_MAPPER(CaldavExceptionForbidden.class),
    DAV_EXCEPTION_MAPPER (CosmoDavException.class){
        @Override
        protected CosmoDavException doMap(Throwable t, HttpServletRequest request) {
            CosmoDavException de = (CosmoDavException)t;
            return de;
        }
    },
    VALIDATION_EXCEPTION_MAPPER (ValidationException.class);

    private Class<? extends Throwable> exceptionRoot;

    <T extends Throwable> ExceptionMapper(Class<T> exceptionRoot){
        this.exceptionRoot = exceptionRoot;
    }

    boolean supports(Throwable t){
        return exceptionRoot.isInstance(t);
    }


    //Default behavior. See http://tools.ietf.org/search/rfc4791#section-1.3
    CosmoDavException doMap(Throwable t, HttpServletRequest request){
        return new ForbiddenException(t.getMessage());
    }

    public static CosmoDavException map(Throwable t, HttpServletRequest request){

        for(ExceptionMapper mapper : values()){
            if(mapper.supports(t)){
                return mapper.doMap(t, request);
            }
        }

        request.setAttribute(ATTR_SERVICE_EXCEPTION, t);
        return new CosmoDavException(t);
    }
}
