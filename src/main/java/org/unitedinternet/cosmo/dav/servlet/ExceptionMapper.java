package org.unitedinternet.cosmo.dav.servlet;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ForbiddenException;

@Deprecated
public enum ExceptionMapper {
    DAV_EXCEPTION_MAPPER (CosmoDavException.class){
        @Override
        protected CosmoDavException doMap(Throwable t) {
            return (CosmoDavException)t;
        }
    };

    private Class<? extends Throwable> exceptionRoot;

    <T extends Throwable> ExceptionMapper(Class<T> exceptionRoot){
        this.exceptionRoot = exceptionRoot;
    }

    boolean supports(Throwable t){
        return exceptionRoot.isInstance(t);
    }


    //Default behavior. See http://tools.ietf.org/search/rfc4791#section-1.3
    CosmoDavException doMap(Throwable t){
        return new ForbiddenException(t.getMessage());
    }

    public static CosmoDavException map(Throwable t){

        for(ExceptionMapper mapper : values()){
            if(mapper.supports(t)){
                return mapper.doMap(t);
            }
        }

        return new CosmoDavException(t);
    }
}
