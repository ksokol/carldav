package util;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.xmlunit.diff.ElementSelectors.byName;
import static org.xmlunit.diff.ElementSelectors.selectorForElementNamed;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import org.springframework.test.web.servlet.ResultMatcher;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelector;
import util.xmlunit.FirstChildElementNameSelector;

import javax.xml.transform.Source;

/**
 * @author Kamill Sokol
 */
public class ContentUtil {

    public static ResultMatcher reportWithEtag(String content, String etag) {
        final String contentWithEtag = content.replace("${etag}", etag);
        final Source build = Input.fromString(contentWithEtag).build();
        return content().source(isSimilarTo(build).withNodeMatcher(nodeMatcher()));
    }

    private static DefaultNodeMatcher nodeMatcher() {
        return new DefaultNodeMatcher(unorderedSupportedReportNodes(), unorderedPrivilegeNodes(), byName);
    }

    private static ElementSelector unorderedSupportedReportNodes() {
        return unorderedNodes("supported-report");
    }

    private static ElementSelector unorderedPrivilegeNodes() {
        return unorderedNodes("privilege");
    }

    private static ElementSelector unorderedNodes(final String localName) {
        return selectorForElementNamed(localName, new FirstChildElementNameSelector());
    }
}
