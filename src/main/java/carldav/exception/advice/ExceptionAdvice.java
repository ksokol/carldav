package carldav.exception.advice;

import carldav.exception.resolver.ExceptionResolverHandler;
import carldav.exception.resolver.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.unitedinternet.cosmo.dav.CosmoDavException;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Kamill Sokol
 */
@ControllerAdvice
public class ExceptionAdvice {

    private final ExceptionResolverHandler resolver;

    @Autowired
    public ExceptionAdvice(final ExceptionResolverHandler resolver) {
        Assert.notNull(resolver, "resolver is null");
        this.resolver = resolver;
    }

    @ExceptionHandler
    public void resolve(Exception exception, HttpServletResponse response) {
        final CosmoDavException resolved = resolver.resolve(exception);
        ResponseUtils.sendDavError(resolved, response);
    }
}
