package carldav.exception.resolver;

import org.hibernate.exception.ConstraintViolationException;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;

/**
 * @author Kamill Sokol
 */
public class DuplicateUserEmailExceptionResolver implements ExceptionResolver {
    @Override
    public CosmoDavException resolve(final Exception exception) {
        if(exception instanceof ConstraintViolationException) {
            ConstraintViolationException violationException = (ConstraintViolationException) exception;
            if("USER_EMAIL".equals(violationException.getConstraintName())) {
                return new ConflictException("duplicate user email");
            }
        }
        return null;
    }
}
