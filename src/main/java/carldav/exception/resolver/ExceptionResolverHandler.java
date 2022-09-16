package carldav.exception.resolver;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.dav.servlet.ExceptionMapper;

@Component
public class ExceptionResolverHandler {

  public CosmoDavException resolve(final Exception exception) {
    if (exception instanceof DbActionExecutionException && exception.getCause() instanceof DuplicateKeyException) {
      var cve = (DuplicateKeyException) exception.getCause();
      var message = cve.getCause().getMessage();
      if (message.contains("UID_COLLECTION")) {
        return new UidConflictException(message);
      }
      return new ConflictException(message);
    }
    if (exception instanceof DbActionExecutionException && exception.getCause() instanceof DataIntegrityViolationException) {
      var cve = (DataIntegrityViolationException) exception.getCause();
      var message = cve.getCause().getMessage();
      return new BadRequestException(message);
    }

    return ExceptionMapper.map(exception);
  }
}
