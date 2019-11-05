package carldav.trace;

import org.apache.commons.io.IOUtils;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Kamill Sokol
 */
final class ResponseToString {

    private ResponseToString() {
        // prevent instantiation
    }

    private static final String lineSeparator = System.getProperty("line.separator");

    static String responseToString(ContentCachingResponseWrapper response) throws IOException {
        StringBuilder toString = new StringBuilder();

        toString.append("< status: ").append(response.getStatus()).append(lineSeparator);

        for (String headerName : response.getHeaderNames()) {
            toString.append("< ").append(headerName).append(": ").append(response.getHeader(headerName)).append(lineSeparator);
        }

        if (response.getContentType() != null) {
            toString.append("content-type: ").append(response.getContentType()).append(lineSeparator);
        }

        StringWriter writer = new StringWriter();
        IOUtils.copy(new ByteArrayInputStream(response.getContentAsByteArray()), writer, response.getCharacterEncoding());
        response.copyBodyToResponse();

        return toString.append(writer.toString()).toString();
    }
}
