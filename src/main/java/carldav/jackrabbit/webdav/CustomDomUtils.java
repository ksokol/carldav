package carldav.jackrabbit.webdav;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * @author Kamill Sokol
 */
public class CustomDomUtils {

    private static QName EMPTY = new QName("");

    public static Element createElement(Document factory, String localName, QName namespace) {
        if (namespace != null) {
            return factory.createElementNS(namespace.getNamespaceURI(), getPrefixedName(localName, namespace));
        } else {
            return factory.createElement(localName);
        }
    }

    public static String getPrefixedName(String localName, QName namespace) {
        if (namespace == null || EMPTY.equals(namespace) || EMPTY.getPrefix().equals(namespace.getPrefix())) {
            return localName;
        }
        StringBuffer buf = new StringBuffer(namespace.getPrefix());
        buf.append(":");
        buf.append(localName);
        return buf.toString();
    }

    public static QName getNamespace(Element element) {
        String uri = element.getNamespaceURI();
        String prefix = element.getPrefix();
        if (uri == null) {
            return new QName("");
        } else {
            return new QName(uri, element.getLocalName(), prefix);
        }
    }
}
