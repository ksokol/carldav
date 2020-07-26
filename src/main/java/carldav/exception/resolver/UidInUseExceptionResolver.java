package carldav.exception.resolver;

import org.springframework.dao.DataIntegrityViolationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;

public class UidInUseExceptionResolver implements ExceptionResolver {

    @Override
    public CosmoDavException resolve(Exception exception) {
        if (exception instanceof DataIntegrityViolationException) {
            var cve = (DataIntegrityViolationException) exception;
            var message = cve.getMessage();
            if (message != null && message.contains("UID_COLLECTION")) {
                return new UidConflictException(cve.getMessage());
            }
        }
        return null;
    }
}
