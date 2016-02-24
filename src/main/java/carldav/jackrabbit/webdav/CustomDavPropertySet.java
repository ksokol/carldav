package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

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
     *
     * @param pset Properties to add
     */
    public void addAll(CustomDavPropertySet pset) {
        map.putAll(pset.map);
    }

    /**
     * Retrieves the property with the specified <code>name</code> and the
     * default WebDAV {@link org.apache.jackrabbit.webdav.CustomDavConstants#NAMESPACE namespace}.
     *
     * @param name The name of the property to retrieve
     *
     * @return The desired property or <code>null</code>
     */
    public WebDavProperty<?> get(String name) {
        return get(CustomDavPropertyName.create(name));
    }

    /**
     * Retrieves the property with the specified <code>name</code> and
     * <code>namespace</code>.
     *
     * @param name The name of the property to retrieve
     * @param namespace The namespace of the property to retrieve
     *
     * @return The desired property or <code>null</code>
     */
    public WebDavProperty<?> get(String name, Namespace namespace) {
        return get(CustomDavPropertyName.create(name, namespace));
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
     * Removes the property with the specified <code>name</code> and the
     * default WebDAV {@link org.apache.jackrabbit.webdav.CustomDavConstants#NAMESPACE namespace}.
     *
     * @param name The name of the property to remove
     *
     * @return The removed property or <code>null</code>
     */
    public WebDavProperty<?> remove(String name) {
        return remove(CustomDavPropertyName.create(name));
    }

    /**
     * Removes the property with the specified <code>name</code> and
     * <code>namespace</code> from this set.
     *
     * @param name The name of the property to remove
     * @param namespace The namespace of the property to remove
     *
     * @return The removed property or <code>null</code>
     */
    public WebDavProperty<?> remove(String name, Namespace namespace) {
        return remove(CustomDavPropertyName.create(name, namespace));
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
     * Returns an iterator over all those property in this set, that have the
     * indicated <code>namespace</code>.
     *
     * @param namespace The namespace of the property in the iteration.
     *
     * @return An iterator over {@link WebDavProperty}.
     */
    public PropIter iterator(Namespace namespace) {
        return new PropIter(namespace);
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
     * @see PropContainer#contains(CustomDavPropertyName)
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
     * @see PropContainer#addContent(PropEntry)
     */
    @Override
    public boolean addContent(PropEntry contentEntry) {
        if (contentEntry instanceof WebDavProperty) {
            add((WebDavProperty<?>) contentEntry);
            return true;
        }
        LOG.debug("DavProperty object expected. Found: " + contentEntry.getClass().toString());
        return false;
    }

    /**
     * @see PropContainer#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * @see PropContainer#getContentSize()
     */
    @Override
    public int getContentSize() {
        return map.size();
    }

    /**
     * @see PropContainer#getContent()
     */
    @Override
    public Collection<? extends PropEntry> getContent() {
        return map.values();
    }

    //---------------------------------------------------------- Inner class ---
    /**
     * Implementation of a DavPropertyIterator that returns webdav property.
     * Additionally, it can only return property with the given namespace.
     */
    public class PropIter implements Iterator<WebDavProperty<?>> {

        /** the namespace to match against */
        private final Namespace namespace;

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
        private PropIter(Namespace namespace) {
            this.namespace = namespace;
            iterator = map.values().iterator();
            seek();
        }

        /**
         * @see DavPropertyIterator#nextProperty();
         */
        public WebDavProperty<?> nextProperty() throws NoSuchElementException {
            if (next==null) {
                throw new NoSuchElementException();
            }
            WebDavProperty<?> ret = next;
            seek();
            return ret;
        }

        /**
         * @see DavPropertyIterator#hasNext();
         */
        public boolean hasNext() {
            return next!=null;
        }

        /**
         * @see DavPropertyIterator#next();
         */
        public WebDavProperty<?> next() {
            return nextProperty();
        }

        /**
         * @see DavPropertyIterator#remove();
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Seeks for the next valid property
         */
        private void seek() {
            while (iterator.hasNext()) {
                next = iterator.next();
                if (namespace == null || namespace.equals(next.getName().getNamespace())) {
                    return;
                }
            }
            next = null;
        }
    }
}

