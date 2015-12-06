package testutil.xmlunit;

import static org.xmlunit.diff.ElementSelectors.byName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlunit.diff.ElementSelector;

/**
 * @author Stefan Bodewig
 * @author Kamill Sokol
 */
class FirstChildElementNameSelector implements ElementSelector {

    @Override
    public boolean canBeCompared(Element controlElement, Element testElement) {
        return byName.canBeCompared(firstChildElement(controlElement), firstChildElement(testElement));
    }

    private Element firstChildElement(Element e) {
        final NodeList nl = e.getChildNodes();
        int len = nl.getLength();
        for (int i = 0; i < len; i++) {
            if (nl.item(i) instanceof Element) {
                final Element e1 = (Element) nl.item(i);
                final Element element = firstChildElement(e1);

                if (element != null) {
                    return element;
                }

                return e1;
            }
        }
        return null;
    }
}