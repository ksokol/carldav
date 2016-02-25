package carldav.jackrabbit.webdav.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

public class CustomDavPropertySet extends CustomPropContainer implements Iterable<WebDavProperty<?>> {

    private static Logger LOG = LoggerFactory.getLogger(CustomDavPropertySet.class);

    private final Map<CustomDavPropertyName, WebDavProperty<?>> map = new HashMap<>();

    /**
     * Adds a new property to this set.
     *
     * @param property The property to add
     *
     * @return The previously assigned property or <code>null</code>.
     */
    public WebDavProperty<?> add(WebDavProperty<?> property) {
        return map.put(property.getName(), property);
    }

    /**
     * Retrieves the property with the specified <code>name</code>
     *
     * @param name The webdav property name of the property to retrieve
     *
     * @return The desired property or <code>null</code>
     */
    public WebDavProperty<?> get(CustomDavPropertyName name) {
        return map.get(name);
    }


    /**
     * Removes the indicated property from this set.
     *
     * @param name The webdav property name to remove
     *
     * @return The removed property or <code>null</code>
     */
    public WebDavProperty<?> remove(CustomDavPropertyName name) {
        return map.remove(name);
    }

    /**
     * Returns an iterator over all property in this set.
     *
     * @return An iterator over {@link WebDavProperty}.
     */
    public PropIter iterator() {
        return new PropIter();
    }

    /**
     * Return the names of all properties present in this set.
     *
     * @return array of {@link CustomDavPropertyName property names} present in this set.
     */
    public CustomDavPropertyName[] getPropertyNames() {
        return map.keySet().toArray(new CustomDavPropertyName[map.keySet().size()]);
    }

    //------------------------------------------------------< PropContainer >---
    /**
     * Checks if this set contains the property with the specified name.
     *
     * @param name The name of the property
     * @return <code>true</code> if this set contains the property;
     *         <code>false</code> otherwise.
     * @see CustomPropContainer#contains(CustomDavPropertyName)
     */
    @Override
    public boolean contains(CustomDavPropertyName name) {
        return map.containsKey(name);
    }

    /**
     * @param contentEntry NOTE, that the given object must be an instance of
     * <code>DavProperty</code> in order to be successfully added to this set.
     * @return true if the specified object is an instance of <code>DavProperty</code>
     * and false otherwise.
     * @see CustomPropContainer#addContent(CustomPropEntry)
     */
    @Override
    public boolean addContent(CustomPropEntry contentEntry) {
        if (contentEntry instanceof WebDavProperty) {
            add((WebDavProperty<?>) contentEntry);
            return true;
        }
        LOG.debug("DavProperty object expected. Found: " + contentEntry.getClass().toString());
        return false;
    }

    /**
     * @see CustomPropContainer#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @see CustomPropContainer#getContentSize()
     */
    @Override
    public int getContentSize() {
        return map.size();
    }

    /**
     * @see CustomPropContainer#getContent()
     */
    @Override
    public Collection<? extends CustomPropEntry> getContent() {
        return map.values();
    }

    //---------------------------------------------------------- Inner class ---
    /**
     * Implementation of a DavPropertyIterator that returns webdav property.
     * Additionally, it can only return property with the given namespace.
     */
    public class PropIter implements Iterator<WebDavProperty<?>> {

        /** the namespace to match against */
        private final QName namespace;

        /** the internal iterator */
        private final Iterator<WebDavProperty<?>> iterator;

        /** the next property to return */
        private WebDavProperty<?> next;

        /**
         * Creates a new property iterator.
         */
        private PropIter() {
            this(null);
        }

        /**
         * Creates a new iterator with the given namespace
         * @param namespace The namespace to match against
         */
        private PropIter(QName namespace) {
            this.namespace = namespace;
            iterator = map.values().iterator();
            seek();
        }

        public WebDavProperty<?> nextProperty() throws NoSuchElementException {
            if (next==null) {
                throw new NoSuchElementException();
            }
            WebDavProperty<?> ret = next;
            seek();
            return ret;
        }

        public boolean hasNext() {
            return next!=null;
        }

        public WebDavProperty<?> next() {
            return nextProperty();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Seeks for the next valid property
         */
        private void seek() {
            while (iterator.hasNext()) {
                next = iterator.next();
                if (namespace == null || namespace.getNamespaceURI().equals(next.getName().getNamespace())) {
                    return;
                }
            }
            next = null;
        }
    }
}

