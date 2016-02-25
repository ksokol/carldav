package carldav.jackrabbit.webdav.property;

import static carldav.CarldavConstants.caldav;

import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import carldav.jackrabbit.webdav.xml.CustomElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomDavPropertyNameSet extends CustomPropContainer {

    private static Logger LOG = LoggerFactory.getLogger(CustomDavPropertyNameSet.class);
    private final Set<CustomDavPropertyName> set = new HashSet<>();

    /**
     * Create a new empty set.
     */
    public CustomDavPropertyNameSet() {
    }

    /**
     * Create a new <code>DavPropertyNameSet</code> with the given initial values.
     *
     * @param initialSet
     */
    public CustomDavPropertyNameSet(CustomDavPropertyNameSet initialSet) {
        addAll(initialSet);
    }

    /**
     * Create a new <code>DavPropertyNameSet</code> from the given DAV:prop
     * element.
     *
     * @param propElement
     * @throws IllegalArgumentException if the specified element is <code>null</code>
     * or is not a DAV:prop element.
     */
    public CustomDavPropertyNameSet(Element propElement) {
        if (!CustomDomUtils.matches(propElement, XML_PROP, caldav(XML_PROP))) {
            throw new IllegalArgumentException("'DAV:prop' element expected.");
        }

        // fill the set
        CustomElementIterator it = CustomDomUtils.getChildren(propElement);
        while (it.hasNext()) {
            add(CustomDavPropertyName.createFromXml(it.nextElement()));
        }
    }

    /**
     * Adds the specified {@link CustomDavPropertyName} object to this
     * set if it is not already present.
     *
     * @param propertyName element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     */
    public boolean add(CustomDavPropertyName propertyName) {
        return set.add(propertyName);
    }

    /**
     * Add the property names contained in the specified set to this set.
     *
     * @param propertyNames
     * @return true if the set has been modified by this call.
     */
    public boolean addAll(CustomDavPropertyNameSet propertyNames) {
        return set.addAll(propertyNames.set);
    }

    /**
     * Removes the specified {@link CustomDavPropertyName} object from this set.
     *
     * @param propertyName
     * @return true if the given property name could be removed.
     * @see HashSet#remove(Object)
     */
    public boolean remove(CustomDavPropertyName propertyName) {
        return set.remove(propertyName);
    }

    //------------------------------------------------------< PropContainer >---
    /**
     * @see CustomPropContainer#contains(CustomDavPropertyName)
     */
    @Override
    public boolean contains(CustomDavPropertyName name) {
        return set.contains(name);
    }

    /**
     * @param contentEntry NOTE that an instance of <code>DavPropertyName</code>
     * in order to successfully add the given entry.
     * @return true if contentEntry is an instance of <code>DavPropertyName</code>
     * that could be added to this set. False otherwise.
     * @see CustomPropContainer#addContent(Object)
     */
    @Override
    public boolean addContent(CustomPropEntry contentEntry) {
        if (contentEntry instanceof CustomDavPropertyName) {
            return add((CustomDavPropertyName) contentEntry);
        }
        LOG.debug("DavPropertyName object expected. Found: " + contentEntry.getClass().toString());
        return false;
    }

    /**
     * @see CustomPropContainer#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * @see CustomPropContainer#getContentSize()
     */
    @Override
    public int getContentSize() {
        return set.size();
    }

    /**
     * @see CustomPropContainer#getContent()
     */
    @Override
    public Collection<CustomDavPropertyName> getContent() {
        return set;
    }
}
