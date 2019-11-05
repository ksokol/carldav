package carldav.trace;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static carldav.trace.RequestToString.requestToString;
import static carldav.trace.ResponseToString.responseToString;

/**
 * @author Kamill Sokol
 */
@Component
final class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!logger.isDebugEnabled() || isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestToUse = request instanceof ContentCachingRequestWrapper ? (ContentCachingRequestWrapper) request : new ContentCachingRequestWrapper(request, 50000);
        ContentCachingResponseWrapper responseToUse = response instanceof ContentCachingResponseWrapper ? (ContentCachingResponseWrapper) response : new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestToUse, responseToUse);
        logger.debug(String.format("%n%s%n%n%s", requestToString(requestToUse), responseToString(responseToUse)));
    }
}
