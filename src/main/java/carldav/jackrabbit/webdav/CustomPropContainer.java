package carldav.jackrabbit.webdav;

import static carldav.CarldavConstants.caldav;

import carldav.jackrabbit.webdav.property.CustomPropEntry;
import carldav.jackrabbit.webdav.xml.CustomXmlSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;

public abstract class CustomPropContainer implements CustomXmlSerializable, CustomDavConstants {

    private static Logger log = LoggerFactory.getLogger(CustomPropContainer.class);

    /**
     * Tries to add the specified object to the <code>PropContainer</code> and
     * returns a boolean indicating whether the content could be added to the
     * internal set/map.
     *
     * @param contentEntry
     * @return true if the object could be added; false otherwise
     * @deprecated Use {@link #addContent(CustomPropEntry)} instead.
     */
    public boolean addContent(Object contentEntry) {
        if (contentEntry instanceof CustomPropEntry) {
            return addContent(contentEntry);
        } else {
            return false;
        }
    }

    /**
     * Tries to add the specified entry to the <code>PropContainer</code> and
     * returns a boolean indicating whether the content could be added to the
     * internal set/map.
     *
     * @param contentEntry
     * @return true if the object could be added; false otherwise
     */
    public abstract boolean addContent(CustomPropEntry contentEntry);

    /**
     * Returns true if the PropContainer does not yet contain any content elements.
     *
     * @return true if this container is empty.
     */
    public abstract boolean isEmpty();

    /**
     * Returns the number of property related content elements that are present
     * in this <code>PropContainer</code>.
     *
     * @return number of content elements
     */
    public abstract int getContentSize();

    /**
     * Returns the collection that contains all the content elements of this
     * <code>PropContainer</code>.
     *
     * @return collection representing the contents of this <code>PropContainer</code>.
     */
    public abstract Collection<? extends CustomPropEntry> getContent();

    /**
     * Returns true if this <code>PropContainer</code> contains a content element
     * that matches the given <code>DavPropertyName</code>.
     *
     * @param name
     * @return true if any of the content elements (be it a DavProperty or a
     * DavPropertyName only) matches the given name.
     */
    public abstract boolean contains(CustomDavPropertyName name);

    /**
     * Returns the xml representation of a property related set with the
     * following format:
     * <pre>
     * &lt;!ELEMENT prop (ANY) &gt;
     * where ANY consists of a list of elements each reflecting the xml
     * representation of the entries returned by {@link #getContent()}.
     * </pre>
     *
     * @see CustomXmlSerializable#toXml(Document)
     */
    public Element toXml(Document document) {
        Element prop = CustomDomUtils.createElement(document, XML_PROP, caldav(XML_PROP));
        for (Object content : getContent()) {
            if (content instanceof CustomXmlSerializable) {
                prop.appendChild(((CustomXmlSerializable) content).toXml(document));
            } else {
                log.debug("Unexpected content in PropContainer: should be XmlSerializable.");
            }
        }
        return prop;
    }

}
