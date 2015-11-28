package util;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import org.springframework.test.web.servlet.ResultMatcher;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import javax.xml.transform.Source;

/**
 * @author Kamill Sokol
 */
public class ContentUtil {

    public static ResultMatcher reportWithEtag(String content, String etag) {
        final String contentWithEtag = content.replace("${etag}", etag);
        final Source build = Input.fromString(contentWithEtag).build();

        return content().source(isSimilarTo(build).withNodeMatcher(ignoreNodeOrder()));
    }

    private static DefaultNodeMatcher ignoreNodeOrder() {
        return new DefaultNodeMatcher(ElementSelectors.byName);
    }
}
