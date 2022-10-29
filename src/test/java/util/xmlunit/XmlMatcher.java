package util.xmlunit;

import java.io.StringWriter;
import java.util.Map;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.stream.StreamResult;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.matchers.CompareMatcher;
import org.xmlunit.xpath.JAXPXPathEngine;

import javax.xml.transform.Source;

import static org.xmlunit.diff.ElementSelectors.byNameAndAllAttributes;
import static org.xmlunit.diff.ElementSelectors.byXPath;
import static org.xmlunit.diff.ElementSelectors.selectorForElementNamed;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class XmlMatcher {

    private XmlMatcher() {
        //private
    }

    public static CompareMatcher equalXml(String content) {
        var build = Input.fromString(content).build();
        return isSimilarTo(build).ignoreWhitespace().normalizeWhitespace().withNodeMatcher(nodeMatcher());
    }

  public static Matcher<Source> toContainText(String xpath, Map<String, String> namespaces, String expected) {
    return new CustomMatcher<>("to contain '" + expected + "'") {
      @Override
      public void describeMismatch(Object item, Description description) {
        try {
          var writer = new StringWriter();
          var result = new StreamResult(writer);
          var factory = TransformerFactory.newInstance();
          var transformer = factory.newTransformer();
          transformer.transform((Source) item, result);
          super.describeMismatch(writer.toString(), description);
        } catch (TransformerException exception) {
          throw new RuntimeException(exception);
        }
      }

      @Override
      public boolean matches(Object source) {
        var engine = new JAXPXPathEngine();
        engine.setNamespaceContext(namespaces);
        var result = engine.evaluate(xpath, (Source) source);
        return result.contains(expected);
      }
    };
  }

    private static DefaultNodeMatcher nodeMatcher() {
        return new DefaultNodeMatcher(unorderedSupportedReportNodes(), unorderedPrivilegeNodes(), unorderedSupportedCalendarComponentSet(), unorderedPropstatNodes(), byNameAndAllAttributes);
    }

    private static ElementSelector unorderedSupportedReportNodes() {
        return selectorForElementNamed("supported-report", new FirstChildElementNameSelector());
    }

    private static ElementSelector unorderedPropstatNodes() {
        return selectorForElementNamed("propstat", new FirstChildElementNameSelector());
    }

    private static ElementSelector unorderedPrivilegeNodes() {
        return unorderedNodes("privilege");
    }

    private static ElementSelector unorderedSupportedCalendarComponentSet() {
        return unorderedNodes("supported-calendar-component-set");
    }

    private static ElementSelector unorderedNodes(final String localName) {
        return selectorForElementNamed(localName, byXPath("./*[1]", byNameAndAllAttributes));
    }
}
