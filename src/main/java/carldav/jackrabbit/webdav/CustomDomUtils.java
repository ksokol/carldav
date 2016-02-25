package carldav.jackrabbit.webdav;

import static carldav.CarldavConstants.EMPTY;
import static carldav.CarldavConstants.caldav;
import static carldav.jackrabbit.webdav.CustomDavConstants.XML_HREF;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author Kamill Sokol
 */
public class CustomDomUtils {

    private static final Logger LOG = getLogger(CustomDomUtils.class);

    private static DocumentBuilderFactory BUILDER_FACTORY = createFactory();

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
        String prefix = element.getPrefix() == null ? "" : element.getPrefix();
        if (uri == null) {
            return new QName("");
        } else {
            return new QName(uri, element.getLocalName(), prefix);
        }
    }

    public static void setAttribute(Element element, String attrLocalName, QName attrNamespace, String attrValue) {
        if (attrNamespace == null) {
            Attr attr = element.getOwnerDocument().createAttribute(attrLocalName);
            attr.setValue(attrValue);
            element.setAttributeNode(attr);
        } else {
            Attr attr = element.getOwnerDocument().createAttributeNS(attrNamespace.getNamespaceURI(), getPrefixedName(attrLocalName, attrNamespace));
            attr.setValue(attrValue);
            element.setAttributeNodeNS(attr);
        }
    }

    public static boolean matches(Node node, String requiredLocalName, QName requiredNamespace) {
        if (node == null) {
            return false;
        }
        boolean matchingNamespace = matchingNamespace(node, requiredNamespace);
        return matchingNamespace && matchingLocalName(node, requiredLocalName);
    }

    public static Element getChildElement(Node parent, QName childNamespace) {
        if (parent != null) {
            NodeList children = parent.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (isElement(child) && matches(child, childNamespace.getLocalPart(), childNamespace)) {
                    return (Element)child;
                }
            }
        }
        return null;
    }

    public static Element addChildElement(Element parent, QName Name) {
        Element elem = createElement(parent.getOwnerDocument(), Name.getLocalPart(), Name);
        parent.appendChild(elem);
        return elem;
    }

    public static String getAttribute(Element parent, QName name) {
        if (parent == null) {
            return null;
        }
        Attr attribute;
        if (name.getNamespaceURI() == null) {
            attribute = parent.getAttributeNode(name.getLocalPart());
        } else {
            attribute = parent.getAttributeNodeNS(name.getNamespaceURI(), name.getLocalPart());
        }
        if (attribute != null) {
            return attribute.getValue();
        } else {
            return null;
        }
    }

    public static String getAttribute(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        Attr attribute = parent.getAttributeNode(localName);

        if (attribute != null) {
            return attribute.getValue();
        } else {
            return null;
        }
    }

    public static CustomElementIterator getChildren(Element parent, QName name) {
        return new CustomElementIterator(parent, name);
    }

    public static CustomElementIterator getChildren(Element parent) {
        return new CustomElementIterator(parent);
    }

    public static void setText(Element element, String text) {
        if (text == null || "".equals(text)) {
            // ignore null/empty string text
            return;
        }
        Text txt = element.getOwnerDocument().createTextNode(text);
        element.appendChild(txt);
    }

    public static String getText(Element element) {
        StringBuffer content = new StringBuffer();
        if (element != null) {
            NodeList nodes = element.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node child = nodes.item(i);
                if (isText(child)) {
                    // cast to super class that contains Text and CData
                    content.append(((CharacterData) child).getData());
                }
            }
        }
        return (content.length()==0) ? null : content.toString();
    }

    public static String getTextTrim(Element element) {
        String txt = getText(element);
        return (txt == null) ? txt : txt.trim();
    }

    public static Document createDocument() {
        try {
            return BUILDER_FACTORY.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Element hrefToXml(String href, Document factory) {
        final Element element = createElement(factory, XML_HREF, caldav(XML_HREF));
        setText(element, href);
        return element;
    }

    private static boolean isElement(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }

    private static boolean matchingNamespace(Node node, QName requiredNamespace) {
        if (requiredNamespace == null) {
            return true;
        } else {
            return requiredNamespace.getNamespaceURI().equals(node.getNamespaceURI());
        }
    }

    private static boolean matchingLocalName(Node node, String requiredLocalName) {
        if (requiredLocalName == null) {
            return true;
        } else {
            String localName = node.getLocalName();
            return requiredLocalName.equals(localName);
        }
    }

    private static boolean isText(Node node) {
        int ntype = node.getNodeType();
        return ntype == Node.TEXT_NODE || ntype == Node.CDATA_SECTION_NODE;
    }

    private static DocumentBuilderFactory createFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setCoalescing(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (AbstractMethodError|ParserConfigurationException e) {
            LOG.warn("Secure XML processing is not supported", e);
        }
        return factory;
    }
}
