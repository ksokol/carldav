package carldav.jackrabbit.webdav.xml;


import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

public class CustomElementIterator implements Iterator<Element> {

    private final String localName;
    private final QName qName;

    private Element next;

    /**
     * Create a new instance of <code>ElementIterator</code> with the given
     * parent element. Only child elements that match the given {@link QName}
     * will be respected by {@link #hasNext()} and {@link #nextElement()}.
     *
     * @param parent
     * @param qname name to match (exactly)
     */
    public CustomElementIterator(Element parent, QName qname) {
        this.localName = null;
        this.qName = qname;
        seek(parent);
    }

    public CustomElementIterator(Element parent) {
        this(parent, null);
    }

    /**
     * Not implemented
     *
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException("Remove not implemented.");
    }

    /**
     * Returns true if there is a next <code>Element</code>
     *
     * @return true if a next <code>Element</code> is available.
     */
    public boolean hasNext() {
        return next != null;
    }

    /**
     * @see java.util.Iterator#next()
     * @see #nextElement()
     */
    public Element next() {
        return nextElement();
    }

    /**
     * Returns the next <code>Element</code> in the iterator.
     *
     * @return the next element
     * @throws NoSuchElementException if there is no next element.
     */
    public Element nextElement() {
        if (next==null) {
            throw new NoSuchElementException();
        }
        Element ret = next;
        seek();
        return ret;
    }

    /**
     * Seeks for the first matching child element
     */
    private void seek(Element parent) {
        NodeList nodeList = parent.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (matchesName(n)) {
                next = (Element)n;
                return;
            }
        }
    }

    /**
     * Seeks for the next valid element (i.e. the next valid sibling)
     */
    private void seek() {
        Node n = next.getNextSibling();
        while (n != null) {
            if (matchesName(n)) {
                next = (Element)n;
                return;
            } else {
                n = n.getNextSibling();
            }
        }
        // no next element found -> set to null in order to leave the loop.
        next = null;
    }

    /**
     * Matches the node name according to either {@link #qName} or the pair
     * of {@link #localName) and {@link #namespace}.
     */
    private boolean matchesName(Node n) {
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }
        return CustomDomUtils.matches(n, localName, qName);
    }
}

