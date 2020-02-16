package org.unitedinternet.cosmo.dav.property;

import carldav.jackrabbit.webdav.property.DavPropertyName;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedHashSet;
import java.util.Set;

public class StandardDavProperty<T> implements WebDavProperty<T>, XmlSerializable {

    private DavPropertyName name;
    private T value;

    public StandardDavProperty(DavPropertyName name, T value) {
        this.name = name;
        this.value = value;
    }

    public DavPropertyName getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public String getValueText() {
        if (value == null) {
            return null;
        }
        if (value instanceof Element) {
            String text = DomUtils.getText((Element) value);
            if (text != null) {
                return text;
            }
        }
        if (value instanceof Set) {
            Set<T> sorted = new LinkedHashSet<>((Set<T>) value);
            return StringUtils.join(sorted, ", ");
        }
        return value.toString();
    }

    public Element toXml(Document document) {
        Element element;

        if (value instanceof Element) {
            element = (Element) document.importNode((Element) value, true);
        } else {
            element = getName().toXml(document);
            Object v = getValue();
            if (v != null) {
                if (v instanceof XmlSerializable) {
                    element.appendChild(((XmlSerializable)v).toXml(document));
                }
                else {
                    DomUtils.setText(element, v.toString());
                }
            }
        }
        return element;
    }

	@Override
	public boolean isInvisibleInAllprop() {
        return false;
    }
}
