package carldav.exception.resolver;

import org.unitedinternet.cosmo.dav.CosmoDavException;

/**
 * @author Kamill Sokol
 */
public interface ExceptionResolver {

    CosmoDavException resolve(Exception exception);
}
