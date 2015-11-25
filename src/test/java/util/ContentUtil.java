package util;

import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @author Kamill Sokol
 */
public class ContentUtil {

    public static ResultMatcher reportWithEtag(String content, String etag) {
        //TODO shouldn't happen
        final String s = content.replaceFirst("<D:getetag>(.*)<\\/D:getetag>", "<D:getetag>"+ etag + "</D:getetag>");
        return MockMvcResultMatchers.content().string(s);
    }
}
