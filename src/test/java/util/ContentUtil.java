package util;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.springframework.test.web.servlet.ResultMatcher;
import util.xmlunit.XmlMatcher;

/**
 * @author Kamill Sokol
 */
public class ContentUtil {

    public static ResultMatcher xml(String content) {
        return content().source(XmlMatcher.equalXml(content));
    }

    public static ResultMatcher html(String content) {
        return content().string(content);
    }

}
