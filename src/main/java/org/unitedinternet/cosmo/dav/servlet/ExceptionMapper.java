package org.unitedinternet.cosmo.dav.servlet;

import static org.unitedinternet.cosmo.server.ServerConstants.ATTR_SERVICE_EXCEPTION;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ForbiddenException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

public enum ExceptionMapper {
    DAV_EXCEPTION_MAPPER (CosmoDavException.class){
        @Override
        protected CosmoDavException doMap(Throwable t, HttpServletRequest request) {
            return (CosmoDavException)t;
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
