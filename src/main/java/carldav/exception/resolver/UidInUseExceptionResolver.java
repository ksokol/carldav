package carldav.exception.resolver;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.model.UidInUseException;

/**
 * @author Kamill Sokol
 */
public class UidInUseExceptionResolver implements ExceptionResolver {
    @Override
    public CosmoDavException resolve(final Exception exception) {
        if(exception instanceof UidInUseException) {
            final UidInUseException e = (UidInUseException) exception;
            return new UidConflictException(e);
        }
        return null;
    }
}
