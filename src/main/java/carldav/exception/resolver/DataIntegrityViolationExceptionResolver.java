package carldav.exception.resolver;

import org.springframework.dao.DataIntegrityViolationException;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Locale;

/**
 * @author Kamill Sokol
 */
class DataIntegrityViolationExceptionResolver implements ExceptionResolver {

    @Override
    public CosmoDavException resolve(final Exception exception) {
        if(exception instanceof DataIntegrityViolationException) {
            final DataIntegrityViolationException cve = (DataIntegrityViolationException) exception;
            if (cve.getMessage().contains("constraint [USER_EMAIL]")) {
                return new ConflictException("duplicate user email");
            }
        }
        return null;
    }
}
