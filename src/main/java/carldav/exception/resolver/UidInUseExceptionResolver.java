package carldav.exception.resolver;

import org.springframework.dao.DataIntegrityViolationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;

/**
 * @author Kamill Sokol
 */
public class UidInUseExceptionResolver implements ExceptionResolver {
    @Override
    public CosmoDavException resolve(final Exception exception) {
        if(exception instanceof DataIntegrityViolationException) {
            final DataIntegrityViolationException cve = (DataIntegrityViolationException) exception;
            if(cve.getMessage().contains("UID_COLLECTION")) {
                return new UidConflictException(cve.getMessage());
            }
        }
        return null;
    }
}
