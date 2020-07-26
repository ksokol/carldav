package util.xmlunit;

import org.w3c.dom.Element;
import org.xmlunit.diff.ElementSelector;

import java.util.Objects;

import static org.xmlunit.diff.ElementSelectors.byName;

class FirstChildElementNameSelector implements ElementSelector {

    @Override
    public boolean canBeCompared(Element controlElement, Element testElement) {
        return byName.canBeCompared(firstChildElement(controlElement), firstChildElement(testElement));
    }

    private Element firstChildElement(Element e) {
        var nl = e.getChildNodes();
        var len = nl.getLength();
        for (var i = 0; i < len; i++) {
            if (nl.item(i) instanceof Element) {
                var e1 = (Element) nl.item(i);
                var element = firstChildElement(e1);
                return Objects.requireNonNullElse(element, e1);
            }
        }
        return null;
    }
}
