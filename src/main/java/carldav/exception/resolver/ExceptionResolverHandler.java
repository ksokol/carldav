package carldav.exception.resolver;

import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.servlet.ExceptionMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kamill Sokol
 */
@Component
public class ExceptionResolverHandler {

    private final List<ExceptionResolver> exceptionResolvers;

    public ExceptionResolverHandler() {
        exceptionResolvers = new ArrayList<>();
        exceptionResolvers.add(new ConstrainViolationExceptionResolver());
    }

    public CosmoDavException resolve(final Exception exception) {
        for (final ExceptionResolver exceptionResolver : exceptionResolvers) {
            final CosmoDavException resolved = exceptionResolver.resolve(exception);
            if(resolved != null) {
                return resolved;
            }
        }
        return resolveDefault(exception);
    }

    private CosmoDavException resolveDefault(final Exception exception) {
        return ExceptionMapper.map(exception);
    }
}
