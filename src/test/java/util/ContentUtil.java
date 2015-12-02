package util;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.xmlunit.diff.ElementSelectors.byNameAndAllAttributes;
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

    public static ResultMatcher xml(String content) {
        final Source build = Input.fromString(content).build();
        return content().source(isSimilarTo(build).withNodeMatcher(nodeMatcher()));
    }

    public static ResultMatcher html(String content) {
        return content().string(content);
    }

    public static WithStep with() {
        return new WithStep();
    }

    public static class WithStep {

        public InStep etag(String etag) {
            return new InStep("${etag}", etag);
        }

        public InStep ticket(String ticket) {
            return new InStep("${ticket}", ticket);
        }

        public static class InStep {

            private final String template;
            private final String value;

            public InStep(final String template, final String value) {
                this.template = template;
                this.value = value;
            }

            public String in(String content) {
                return content.replace(template, value);
            }
        }
    }

    private static DefaultNodeMatcher nodeMatcher() {
        return new DefaultNodeMatcher(unorderedSupportedReportNodes(), unorderedPrivilegeNodes(), unorderedSupportedCalendarComponentSet(), byNameAndAllAttributes);
    }

    private static ElementSelector unorderedSupportedReportNodes() {
        return unorderedNodes("supported-report");
    }

    private static ElementSelector unorderedPrivilegeNodes() {
        return unorderedNodes("privilege");
    }

    private static ElementSelector unorderedSupportedCalendarComponentSet() {
        return unorderedNodes("supported-calendar-component-set");
    }

    private static ElementSelector unorderedNodes(final String localName) {
        return selectorForElementNamed(localName, new FirstChildElementNameSelector());
    }
}
