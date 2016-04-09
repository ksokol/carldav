package carldav.jackrabbit.webdav.property;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public class DavPropertyName implements DavConstants, XmlSerializable, PropEntry {

    private QName name;

    public static DavPropertyName create(String name, QName namespace) {
        return new DavPropertyName(new QName(namespace.getNamespaceURI(), name, namespace.getPrefix()));
    }

    public static DavPropertyName create(final QName name) {
        return new DavPropertyName(name);
    }

    public static DavPropertyName createFromXml(Element nameElement) {
        if (nameElement == null) {
            throw new IllegalArgumentException("Cannot build CustomDavPropertyName from a 'null' element.");
        }
        String ns = nameElement.getNamespaceURI();
        if (ns == null) {
            return create(nameElement.getLocalName(), CarldavConstants.EMPTY);
        } else {
            return create(nameElement.getLocalName(), DomUtils.getNamespace(nameElement));
        }
    }

    public DavPropertyName(final QName name) {
        Assert.notNull(name, "name is null");
        this.name = name;
    }

    public String getName() {
        return name.getLocalPart();
    }

    public String getNamespace() {
        return name.getNamespaceURI();
    }

    @Override
    public int hashCode() {
        return (name.hashCode()) % Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DavPropertyName) {
            DavPropertyName propName = (DavPropertyName) obj;
            return  name.equals(propName.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public Element toXml(Document document) {
        return DomUtils.createElement(document, name.getLocalPart(), name);
    }
}

