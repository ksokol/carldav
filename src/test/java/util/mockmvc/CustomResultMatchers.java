package util.mockmvc;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.hamcrest.Matcher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * @author Kamill Sokol
 */
public class CustomResultMatchers {
    private CustomResultMatchers() {
        //private
    }

    public static ResultMatcher etag(Matcher<? super String> m) {
        return header().string(ETAG, m);
    }

    public static ResultMatcher lastModified(Matcher<? super String> m) {
        return header().string(LAST_MODIFIED, m);
    }

    public static ResultMatcher contentType(Matcher<? super String> m) {
        return header().string(CONTENT_TYPE, m);
    }

    public static ResultMatcher textXmlContentType() {
        return contentType(is(TEXT_XML_VALUE + "; charset=UTF-8"));
    }

    public static ResultMatcher textHtmlContentType() {
        return contentType(is(TEXT_HTML_VALUE + "; charset=UTF-8"));
    }
}
