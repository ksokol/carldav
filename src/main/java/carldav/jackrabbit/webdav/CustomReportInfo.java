package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class CustomReportInfo implements XmlSerializable {

    private static Logger LOG = LoggerFactory.getLogger(ReportInfo.class);

    private final String typeLocalName;
    private final Namespace typeNamespace;
    private final int depth;
    private final DavPropertyNameSet propertyNames;
    private final List<Element> content = new ArrayList<>();
    private Element documentElement;

    /**
     * Create a new <code>ReportInfo</code>
     *
     * @param type
     */
    public CustomReportInfo(ReportType type) {
        this(type, DavConstants.DEPTH_0, null);
    }

    /**
     * Create a new <code>ReportInfo</code>
     *
     * @param type
     * @param depth
     */
    public CustomReportInfo(ReportType type, int depth) {
        this(type, depth, null);
    }

    /**
     * Create a new <code>ReportInfo</code>
     *
     * @param type
     * @param depth
     * @param propertyNames
     */
    public CustomReportInfo(ReportType type, int depth, DavPropertyNameSet propertyNames) {
        this(type.getLocalName(), type.getNamespace(), depth, propertyNames);
    }

    /**
     * Create a new <code>ReportInfo</code>
     *
     * @param typeLocalName
     * @param typeNamespace
     */
    public CustomReportInfo(String typeLocalName, Namespace typeNamespace) {
        this(typeLocalName, typeNamespace, DavConstants.DEPTH_0, null);
    }

    /**
     * Create a new <code>ReportInfo</code>
     *
     * @param typelocalName
     * @param typeNamespace
     * @param depth
     * @param propertyNames
     */
    public CustomReportInfo(String typelocalName, Namespace typeNamespace, int depth, DavPropertyNameSet propertyNames) {
        this.typeLocalName = typelocalName;
        this.typeNamespace = typeNamespace;
        this.depth = depth;
        if (propertyNames != null) {
            this.propertyNames = new DavPropertyNameSet(propertyNames);
        } else {
            this.propertyNames = new DavPropertyNameSet();
        }
    }

    /**
     * Create a new <code>ReportInfo</code> object from the given Xml element.
     *
     * @param reportElement
     * @param depth Depth value as retrieved from the {@link DavConstants#HEADER_DEPTH}.
     * @throws DavException if the report element is <code>null</code>.
     */
    public CustomReportInfo(Element reportElement, int depth) throws DavException {
        if (reportElement == null) {
            LOG.warn("Report request body must not be null.");
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }

        this.documentElement = (Element) reportElement.cloneNode(true);

        this.typeLocalName = reportElement.getLocalName();
        this.typeNamespace = DomUtil.getNamespace(reportElement);
        this.depth = depth;
        Element propElement = DomUtil.getChildElement(reportElement, DavConstants.XML_PROP, DavConstants.NAMESPACE);
        if (propElement != null) {
            propertyNames = new DavPropertyNameSet(propElement);
            reportElement.removeChild(propElement);
        } else {
            propertyNames = new DavPropertyNameSet();
        }

        ElementIterator it = DomUtil.getChildren(reportElement);
        while (it.hasNext()) {
            Element el = it.nextElement();
            if (!DavConstants.XML_PROP.equals(el.getLocalName())) {
                content.add(el);
            }
        }
    }

    /**
     * Returns the depth field. The request must be applied separately to the
     * collection itself and to all members of the collection that satisfy the
     * depth value.
     *
     * @return depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Name of the report type that will be / has been requested.
     *
     * @return Name of the report type
     */
    public String getReportName() {
        return DomUtil.getExpandedName(typeLocalName, typeNamespace);
    }

    /**
     * Indicates whether this info contains an element with the given name/namespace.
     *
     * @param localName
     * @param namespace
     * @return true if an element with the given name/namespace is present in the
     * body of the request info.
     */
    public boolean containsContentElement(String localName, Namespace namespace) {
        if (content.isEmpty()) {
            return false;
        }
        for (Element elem : content) {
            boolean sameNamespace = (namespace == null) ? elem.getNamespaceURI() == null : namespace.isSame(elem.getNamespaceURI());
            if (sameNamespace && elem.getLocalName().equals(localName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the Xml element with the given name/namespace that is a child
     * of this info. If no such child exists <code>null</code> is returned. If
     * multiple elements with the same name exist, the first one is returned.
     *
     * @param localName
     * @param namespace
     * @return Xml element with the given name/namespace or <code>null</code>
     */
    public Element getContentElement(String localName, Namespace namespace) {
        List<Element> values = getContentElements(localName, namespace);
        if (values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }

    /**
     * Returns a list containing all child Xml elements of this info that have
     * the specified name/namespace. If this info contains no such element,
     * an empty list is returned.
     *
     * @param localName
     * @param namespace
     * @return List contain all child elements with the given name/namespace
     * or an empty list.
     */
    public List<Element> getContentElements(String localName, Namespace namespace) {
        List<Element> l = new ArrayList<Element>();
        for (Element elem : content) {
            if (DomUtil.matches(elem, localName, namespace)) {
                l.add(elem);
            }
        }
        return l;
    }

    /**
     * Add the specified Xml element as child of this info.
     *
     * @param contentElement
     */
    public void setContentElement(Element contentElement) {
        content.add(contentElement);
    }

    /**
     * Returns a <code>DavPropertyNameSet</code> providing the property names present
     * in an eventual {@link DavConstants#XML_PROP} child element. If no such
     * child element is present an empty set is returned.
     *
     * @return {@link DavPropertyNameSet} providing the property names present
     * in an eventual {@link DavConstants#XML_PROP DAV:prop} child element or an empty set.
     */
    public DavPropertyNameSet getPropertyNameSet() {
        return propertyNames;
    }


    /**
     * @see org.apache.jackrabbit.webdav.xml.XmlSerializable#toXml(Document)
     * @param document
     */
    public Element toXml(Document document) {
        Element reportElement = DomUtil.createElement(document, typeLocalName, typeNamespace);
        if (!content.isEmpty()) {
            for (Element contentEntry : content) {
                Node n = document.importNode(contentEntry, true);
                reportElement.appendChild(n);
            }
        }
        if (!propertyNames.isEmpty()) {
            reportElement.appendChild(propertyNames.toXml(document));
        }
        return reportElement;
    }

    public Element getDocumentElement() {
        return documentElement;
    }
}
