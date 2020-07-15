package testutil.mockmvc

import org.hamcrest.Matcher
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import testutil.xmlunit.XmlMatcher

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace
import static org.springframework.http.HttpHeaders.*
import static org.springframework.http.MediaType.TEXT_HTML_VALUE
import static org.springframework.http.MediaType.TEXT_XML_VALUE
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header

/**
 * @author Kamill Sokol
 */
class CustomResultMatchers {

    private CustomResultMatchers() {
        //private
    }

    static ResultMatcher xml(String content) {
        return MockMvcResultMatchers.content().source(XmlMatcher.equalXml(content))
    }

    static ResultMatcher html(String content) {
        return MockMvcResultMatchers.content().string(content)
    }

    static ResultMatcher text(String content) {
        return MockMvcResultMatchers.content().string(equalToCompressingWhiteSpace(content))
    }

    static ResultMatcher etag(Matcher<? super String> m) {
        return header().string(ETAG, m)
    }

    static ResultMatcher contentType(Matcher<? super String> m) {
        return header().string(CONTENT_TYPE, m)
    }

    static ResultMatcher textXmlContentType() {
        return contentType(is(TEXT_XML_VALUE + "; charset=UTF-8"))
    }

    static ResultMatcher textHtmlContentType() {
        return contentType(is(TEXT_HTML_VALUE + ";charset=UTF-8"))
    }

    static ResultMatcher textCalendarContentType() {
        return contentType(is("text/calendar;charset=UTF-8"))
    }

    static ResultMatcher textCardContentType() {
        return contentType(is("text/vcard"))
    }
}
