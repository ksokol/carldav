package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * <code>DavConstants</code> provide constants for request and response
 * headers, XML elements and property names defined by
 * <a href="http://www.webdav.org/specs/rfc2518.html">RFC 2518</a>. In addition,
 * common date formats (creation date and modification time) are included.
 */
public interface CustomDavConstants {

    Namespace NAMESPACE = Namespace.getNamespace("D", "DAV:");

    //-------------------------------------------------------< Depth Header >---
    String HEADER_DEPTH = "Depth";
    String DEPTH_INFINITY_S = "infinity";
    int DEPTH_INFINITY = Integer.MAX_VALUE;
    int DEPTH_0 = 0;
    int DEPTH_1 = 1;

    //---< XML Element, Attribute Names >---------------------------------------
    String XML_ALLPROP = "allprop";
    String XML_COLLECTION = "collection";
    String XML_HREF = "href";
    String XML_MULTISTATUS = "multistatus";
    String XML_PROP = "prop";
    String XML_PROPFIND = "propfind";
    String XML_PROPNAME = "propname";
    String XML_PROPSTAT = "propstat";
    String XML_RESPONSE = "response";

    //-----------------------------------------------------< Property Names >---
    /*
     * Webdav property names as defined by RFC 2518<br>
     * Note: Microsoft webdav clients as well as Webdrive request additional
     * property (e.g. href, name, owner, isRootLocation, isCollection)  within the
     * default namespace, which are are ignored by this implementation, except
     * for the 'isCollection' property, needed for XP built-in clients.
     */
    String PROPERTY_CREATIONDATE = "creationdate";
    String PROPERTY_DISPLAYNAME = "displayname";
    String PROPERTY_GETCONTENTLANGUAGE = "getcontentlanguage";
    String PROPERTY_GETCONTENTLENGTH = "getcontentlength";
    String PROPERTY_GETCONTENTTYPE = "getcontenttype";
    String PROPERTY_GETETAG = "getetag";
    String PROPERTY_GETLASTMODIFIED = "getlastmodified";
    String PROPERTY_LOCKDISCOVERY = "lockdiscovery";
    String PROPERTY_RESOURCETYPE = "resourcetype";
    String PROPERTY_SOURCE = "source";
    String PROPERTY_SUPPORTEDLOCK = "supportedlock";

    //-------------------------------------------------< PropFind Constants >---
    int PROPFIND_BY_PROPERTY = 0;
    int PROPFIND_ALL_PROP = 1;
    int PROPFIND_PROPERTY_NAMES = 2;
    int PROPFIND_ALL_PROP_INCLUDE = 3; // RFC 4918, Section 9.1
}

