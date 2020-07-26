package carldav.exception.resolver;

import org.springframework.dao.DataIntegrityViolationException;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;

class DataIntegrityViolationExceptionResolver implements ExceptionResolver {

    @Override
    public CosmoDavException resolve(Exception exception) {
        if (exception instanceof DataIntegrityViolationException) {
            var cve = (DataIntegrityViolationException) exception;
            var message = cve.getMessage();
            if (message != null && message.contains("constraint [USER_EMAIL]")) {
                return new ConflictException("duplicate user email");
            }
        }
        return null;
    }
}
