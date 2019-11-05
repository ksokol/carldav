package carldav.trace;

import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * @author Kamill Sokol
 */
final class RequestToString {

    private RequestToString() {
        // prevent instantiation
    }

    private static final String lineSeparator = System.getProperty("line.separator");

    static String requestToString(ContentCachingRequestWrapper request) {
        StringBuilder toString = new StringBuilder();
        toString.append("> ").append(request.getMethod()).append(" ").append(request.getRequestURI());

        if (request.getQueryString() != null) {
            toString.append("?").append(request.getQueryString());
        }

        toString.append(lineSeparator);

        for (Map.Entry<String, List<String>> headerEntries : new ServletServerHttpRequest(request).getHeaders().entrySet()) {
            for (String headerValue : headerEntries.getValue()) {
                toString.append("> ").append(headerEntries.getKey()).append(": ").append(headerValue).append(lineSeparator);
            }
        }

        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            String payload;
            try {
                payload = new String(buf, 0, buf.length, request.getCharacterEncoding());
            }
            catch (UnsupportedEncodingException ex) {
                payload = "[unknown]";
            }
            toString.append(payload);
        }

        return toString.toString();
    }
}
