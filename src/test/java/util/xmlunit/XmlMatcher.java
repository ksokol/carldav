package util.xmlunit;

import static org.xmlunit.diff.ElementSelectors.byNameAndAllAttributes;
import static org.xmlunit.diff.ElementSelectors.selectorForElementNamed;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import org.hamcrest.Matcher;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelector;

import javax.xml.transform.Source;

/**
 * @author Kamill Sokol
 */
public class XmlMatcher {
    private XmlMatcher() {
        //private
    }

    public static Matcher equalXml(String content) {
        final Source build = Input.fromString(content).build();
        return isSimilarTo(build).ignoreWhitespace().normalizeWhitespace().withNodeMatcher(nodeMatcher());
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
