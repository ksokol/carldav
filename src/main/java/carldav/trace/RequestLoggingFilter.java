package carldav.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static carldav.trace.RequestToString.requestToString;
import static carldav.trace.ResponseToString.responseToString;

@Component
final class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    if (!logger.isDebugEnabled() || isAsyncDispatch(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    var requestToUse = request instanceof ContentCachingRequestWrapper ? (ContentCachingRequestWrapper) request : new ContentCachingRequestWrapper(request, 50000);
    var responseToUse = response instanceof ContentCachingResponseWrapper ? (ContentCachingResponseWrapper) response : new ContentCachingResponseWrapper(response);

    filterChain.doFilter(requestToUse, responseToUse);
    logger.debug(String.format("%n%s%n%n%s", requestToString(requestToUse), responseToString(responseToUse)));
  }
}
