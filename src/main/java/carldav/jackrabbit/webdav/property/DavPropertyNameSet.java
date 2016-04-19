package carldav.jackrabbit.webdav.property;

import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.ElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static carldav.CarldavConstants.caldav;

public class DavPropertyNameSet extends PropContainer {

    private static Logger LOG = LoggerFactory.getLogger(DavPropertyNameSet.class);
    private final Set<DavPropertyName> set = new HashSet<>();

    /**
     * Create a new empty set.
     */
    public DavPropertyNameSet() {
    }

    /**
     * Create a new <code>DavPropertyNameSet</code> with the given initial values.
     *
     * @param initialSet
     */
    public DavPropertyNameSet(DavPropertyNameSet initialSet) {
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
    public DavPropertyNameSet(Element propElement) {
        if (!DomUtils.matches(propElement, XML_PROP, caldav(XML_PROP))) {
            throw new IllegalArgumentException("'DAV:prop' element expected.");
        }

        // fill the set
        ElementIterator it = DomUtils.getChildren(propElement);
        while (it.hasNext()) {
            add(DavPropertyName.createFromXml(it.nextElement()));
        }
    }

    /**
     * Adds the specified {@link DavPropertyName} object to this
     * set if it is not already present.
     *
     * @param propertyName element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     */
    public boolean add(DavPropertyName propertyName) {
        return set.add(propertyName);
    }

    /**
     * Add the property names contained in the specified set to this set.
     *
     * @param propertyNames
     * @return true if the set has been modified by this call.
     */
    public boolean addAll(DavPropertyNameSet propertyNames) {
        return set.addAll(propertyNames.set);
    }

    /**
     * Removes the specified {@link DavPropertyName} object from this set.
     *
     * @param propertyName
     * @return true if the given property name could be removed.
     * @see HashSet#remove(Object)
     */
    public boolean remove(DavPropertyName propertyName) {
        return set.remove(propertyName);
    }

    //------------------------------------------------------< PropContainer >---
    /**
     * @see PropContainer#contains(DavPropertyName)
     */
    @Override
    public boolean contains(DavPropertyName name) {
        return set.contains(name);
    }

    /**
     * @param contentEntry NOTE that an instance of <code>DavPropertyName</code>
     * in order to successfully add the given entry.
     * @return true if contentEntry is an instance of <code>DavPropertyName</code>
     * that could be added to this set. False otherwise.
     */
    @Override
    public boolean addContent(PropEntry contentEntry) {
        if (contentEntry instanceof DavPropertyName) {
            return add((DavPropertyName) contentEntry);
        }
        LOG.debug("DavPropertyName object expected. Found: " + contentEntry.getClass().toString());
        return false;
    }

    /**
     * @see PropContainer#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * @see PropContainer#getContent()
     */
    @Override
    public Collection<DavPropertyName> getContent() {
        return set;
    }
}
