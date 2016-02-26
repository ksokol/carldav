package carldav.jackrabbit.webdav.property;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.CustomDavConstants;
import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import carldav.jackrabbit.webdav.xml.CustomXmlSerializable;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public class CustomDavPropertyName implements CustomDavConstants, CustomXmlSerializable, CustomPropEntry {

    private QName name;

    public static CustomDavPropertyName create(String name, QName namespace) {
        return new CustomDavPropertyName(new QName(namespace.getNamespaceURI(), name, namespace.getPrefix()));
    }

    public static CustomDavPropertyName create(final QName name) {
        return new CustomDavPropertyName(name);
    }

    public static CustomDavPropertyName createFromXml(Element nameElement) {
        if (nameElement == null) {
            throw new IllegalArgumentException("Cannot build CustomDavPropertyName from a 'null' element.");
        }
        String ns = nameElement.getNamespaceURI();
        if (ns == null) {
            return create(nameElement.getLocalName(), CarldavConstants.EMPTY);
        } else {
            return create(nameElement.getLocalName(), CustomDomUtils.getNamespace(nameElement));
        }
    }

    public CustomDavPropertyName(final QName name) {
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
        if (obj instanceof CustomDavPropertyName) {
            CustomDavPropertyName propName = (CustomDavPropertyName) obj;
            return  name.equals(propName.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public Element toXml(Document document) {
        return CustomDomUtils.createElement(document, name.getLocalPart(), name);
    }
}

