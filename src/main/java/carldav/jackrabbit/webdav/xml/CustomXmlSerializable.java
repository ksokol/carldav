package carldav.jackrabbit.webdav.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface CustomXmlSerializable {

    /**
     * Returns the xml representation of the implementing object as
     * {@link org.w3c.dom.Element}. The given <code>Document</code> is used
     * as factory and represents the {@link org.w3c.dom.Element#getOwnerDocument()
     * owner document} of the returned DOM element.
     *
     * @return a w3c element representing this object
     * @param document to be used as factory.
     */
    Element toXml(Document document);
}
