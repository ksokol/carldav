package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class CustomDavPropertyName implements CustomDavConstants, XmlSerializable, PropEntry {

    /** internal 'cache' of created property names */
    private static final Map<Namespace, Map<String, CustomDavPropertyName>> cache = new HashMap<>();

    /* some standard webdav property (that have #PCDATA) */
    public static final CustomDavPropertyName CREATIONDATE = CustomDavPropertyName.create(PROPERTY_CREATIONDATE);
    public static final CustomDavPropertyName DISPLAYNAME = CustomDavPropertyName.create(PROPERTY_DISPLAYNAME);
    public static final CustomDavPropertyName GETCONTENTLANGUAGE = CustomDavPropertyName.create(PROPERTY_GETCONTENTLANGUAGE);
    public static final CustomDavPropertyName GETCONTENTLENGTH = CustomDavPropertyName.create(PROPERTY_GETCONTENTLENGTH);
    public static final CustomDavPropertyName GETCONTENTTYPE = CustomDavPropertyName.create(PROPERTY_GETCONTENTTYPE);
    public static final CustomDavPropertyName GETETAG = CustomDavPropertyName.create(PROPERTY_GETETAG);
    public static final CustomDavPropertyName GETLASTMODIFIED = CustomDavPropertyName.create(PROPERTY_GETLASTMODIFIED);

    /* some standard webdav property (that have other elements) */
    public static final CustomDavPropertyName LOCKDISCOVERY = CustomDavPropertyName.create(PROPERTY_LOCKDISCOVERY);
    public static final CustomDavPropertyName RESOURCETYPE = CustomDavPropertyName.create(PROPERTY_RESOURCETYPE);
    public static final CustomDavPropertyName SOURCE = CustomDavPropertyName.create(PROPERTY_SOURCE);
    public static final CustomDavPropertyName SUPPORTEDLOCK = CustomDavPropertyName.create(PROPERTY_SUPPORTEDLOCK);

    /* property use by microsoft that are not specified in the RFC 2518 */
    public static final CustomDavPropertyName ISCOLLECTION = CustomDavPropertyName.create("iscollection");

    /** the name of the property */
    private final String name;

    /** the namespace of the property */
    private final Namespace namespace;

    /**
     * Creates a new <code>CustomDavPropertyName</code> with the given name and
     * Namespace.
     *
     * @param name The local name of the new property name
     * @param namespace The namespace of the new property name
     *
     * @return The WebDAV property name
     */
    public synchronized static CustomDavPropertyName create(String name, Namespace namespace) {

        // get (or create) map for the given namespace
        Map<String, CustomDavPropertyName> map = cache.get(namespace);
        if (map == null) {
            map = new HashMap<String, CustomDavPropertyName>();
            cache.put(namespace, map);
        }
        // get (or create) property name object
        CustomDavPropertyName ret = map.get(name);
        if (ret == null) {
            if (namespace.equals(NAMESPACE)) {
                // ensure prefix for default 'DAV:' namespace
                namespace = NAMESPACE;
            }
            ret = new CustomDavPropertyName(name, namespace);
            map.put(name, ret);
        }
        return ret;
    }

    /**
     * Creates a new <code>CustomDavPropertyName</code> with the given local name
     * and the default WebDAV {@link CustomDavConstants#NAMESPACE namespace}.
     *
     * @param name The local name of the new property name
     *
     * @return The WebDAV property name
     */
    public synchronized static CustomDavPropertyName create(String name) {
        return create(name, NAMESPACE);
    }

    /**
     * Create a new <code>CustomDavPropertyName</code> with the name and namespace
     * of the given Xml element.
     *
     * @param nameElement
     * @return <code>CustomDavPropertyName</code> instance
     */
    public synchronized static CustomDavPropertyName createFromXml(Element nameElement) {
        if (nameElement == null) {
            throw new IllegalArgumentException("Cannot build CustomDavPropertyName from a 'null' element.");
        }
        String ns = nameElement.getNamespaceURI();
        if (ns == null) {
            return create(nameElement.getLocalName(), Namespace.EMPTY_NAMESPACE);
        } else {
            return create(nameElement.getLocalName(), Namespace.getNamespace(nameElement.getPrefix(), ns));
        }
    }

    /**
     * Creates a new <code>CustomDavPropertyName</code> with the given name and
     * Namespace.
     *
     * @param name The local name of the new property name
     * @param namespace The namespace of the new property name
     */
    private CustomDavPropertyName(String name, Namespace namespace) {
        if (name == null || namespace == null) {
            throw new IllegalArgumentException("Name and namespace must not be 'null' for a CustomDavPropertyName.");
        }
        this.name = name;
        this.namespace = namespace;
    }

    /**
     * Return the name of this <code>CustomDavPropertyName</code>.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the namespace of this <code>CustomDavPropertyName</code>.
     *
     * @return namespace
     */
    public Namespace getNamespace() {
        return namespace;
    }

    /**
     * Computes the hash code using this properties name and namespace.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return (name.hashCode() + namespace.hashCode()) % Integer.MAX_VALUE;
    }

    /**
     * Checks if this property has the same name and namespace as the
     * given one.
     *
     * @param obj the object to compare to
     *
     * @return <code>true</code> if the 2 objects are equal;
     *         <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomDavPropertyName) {
            CustomDavPropertyName propName = (CustomDavPropertyName) obj;
            return  name.equals(propName.name) && namespace.equals(propName.namespace);
        }
        return false;
    }

    /**
     * Returns a string representation of this property suitable for debugging
     *
     * @return a human readable string representation
     */
    @Override
    public String toString() {
        return DomUtil.getExpandedName(name, namespace);
    }

    /**
     * Creates a element with the name and namespace of this
     * <code>CustomDavPropertyName</code>.
     *
     * @return A element with the name and namespace of this
     * <code>CustomDavPropertyName</code>.
     * @param document
     */
    public Element toXml(Document document) {
        return DomUtil.createElement(document, name, namespace);
    }
}

