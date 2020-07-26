package util.mockmvc;

import org.hamcrest.Matcher;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import util.xmlunit.XmlMatcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

public class CustomResultMatchers {

    private CustomResultMatchers() {
        //private
    }

    public static ResultMatcher xml(String content) {
        return MockMvcResultMatchers.content().source(XmlMatcher.equalXml(content));
    }

    public static ResultMatcher html(String content) {
        return MockMvcResultMatchers.content().string(content);
    }

    public static ResultMatcher text(String content) {
        return MockMvcResultMatchers.content().string(equalToCompressingWhiteSpace(content));
    }

    public static ResultMatcher etag(Matcher<? super String> m) {
        return header().string(ETAG, m);
    }

    public static ResultMatcher contentType(Matcher<? super String> m) {
        return header().string(CONTENT_TYPE, m);
    }

    public static ResultMatcher textXmlContentType() {
        return contentType(is(TEXT_XML_VALUE + "; charset=UTF-8"));
    }

    public static ResultMatcher textHtmlContentType() {
        return contentType(is(TEXT_HTML_VALUE + ";charset=UTF-8"));
    }

    public static ResultMatcher textCalendarContentType() {
        return contentType(is("text/calendar;charset=UTF-8"));
    }

    public static ResultMatcher textCardContentType() {
        return contentType(is("text/vcard"));
    }
}
