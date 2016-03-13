package carldav.exception.resolver;

import org.hibernate.exception.ConstraintViolationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;

/**
 * @author Kamill Sokol
 */
public class UidInUseExceptionResolver implements ExceptionResolver {
    @Override
    public CosmoDavException resolve(final Exception exception) {
        if(exception instanceof ConstraintViolationException) {
            ConstraintViolationException violationException = (ConstraintViolationException) exception;
            if("UID_OWNER_COLLECTION".equals(violationException.getConstraintName())) {
                return new UidConflictException(violationException.getMessage());
            }
        }
        return null;
    }
}
