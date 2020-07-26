package carldav.jackrabbit.webdav;

import carldav.jackrabbit.webdav.property.DavPropertyName;
import carldav.jackrabbit.webdav.property.DavPropertyNameSet;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import carldav.jackrabbit.webdav.property.PropContainer;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static carldav.CarldavConstants.caldav;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_ALL_PROP;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_ALL_PROP_INCLUDE;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_BY_PROPERTY;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_PROPERTY_NAMES;
import static carldav.jackrabbit.webdav.DavConstants.XML_PROPSTAT;
import static carldav.jackrabbit.webdav.DavConstants.XML_RESPONSE;

public class MultiStatusResponse implements XmlSerializable {

    private static final int TYPE_PROPSTAT = 0;
    private static final int TYPE_HREFSTATUS = 1;

    private final Map<Integer, PropContainer> statusMap = new HashMap<>();
    private int type;
    private String href;
    private Status status;

    public MultiStatusResponse(WebDavResource resource, DavPropertyNameSet propNameSet, int propFindType) {
        customMultiStatusResponse(resource, propNameSet, propFindType);
    }

    public MultiStatusResponse(String href, int i) {
        if (!isValidHref(href)) {
            throw new IllegalArgumentException("Invalid href ('" + href + "')");
        }
        this.href = href;
        this.status = new Status(i);
        this.type = TYPE_HREFSTATUS;
    }

    private void customMultiStatusResponse(String href, int type) {
        if (!isValidHref(href)) {
            throw new IllegalArgumentException("Invalid href ('" + href + "')");
        }
        this.href = href;
        this.type = type;
    }

    public void customMultiStatusResponse(WebDavResource resource, DavPropertyNameSet propNameSet, int propFindType) {
        customMultiStatusResponse(resource.getHref(), TYPE_PROPSTAT);

        if (propFindType == PROPFIND_PROPERTY_NAMES) {
            // only property names requested
            var status200 = getPropContainer(200, true);
            for (var propName : resource.getPropertyNames()) {
                status200.addContent(propName);
            }
        } else {
            // all or a specified set of property and their values requested.
            var status200 = getPropContainer(200, false);

            // Collection of missing property names for 404 responses
            var missing = new HashSet<>(propNameSet.getContent());

            // Add requested properties or all non-protected properties,
            // or non-protected properties plus requested properties (allprop/include)
            if (propFindType == PROPFIND_BY_PROPERTY) {
                // add explicitly requested properties (proptected or non-protected)
                for (var propName : propNameSet.getContent()) {
                    var prop = resource.getProperty(propName);
                    if (prop != null) {
                        status200.addContent(prop);
                        missing.remove(propName);
                    }
                }
            } else {
                // add all non-protected properties
                for (var property : resource.getProperties()) {
                    var allDeadPlusRfc4918LiveProperties = (propFindType == PROPFIND_ALL_PROP) || (propFindType == PROPFIND_ALL_PROP_INCLUDE);
                    var wasRequested = missing.remove(property.getName());

                    if ((allDeadPlusRfc4918LiveProperties && !property.isInvisibleInAllprop()) || wasRequested) {
                        status200.addContent(property);
                    }
                }

                // try if missing properties specified in the include section
                // can be obtained using resource.getProperty
                if (propFindType == PROPFIND_ALL_PROP_INCLUDE && !missing.isEmpty()) {
                    for (DavPropertyName propName : new HashSet<>(missing)) {
                        var prop = resource.getProperty(propName);
                        if (prop != null) {
                            status200.addContent(prop);
                            missing.remove(propName);
                        }
                    }
                }
            }

            if (!missing.isEmpty() && propFindType != PROPFIND_ALL_PROP) {
                var status404 = getPropContainer(404, true);
                for (var propName : missing) {
                    status404.addContent(propName);
                }
            }
        }
    }

    private PropContainer getPropContainer(int status, boolean forNames) {
        var propContainer = statusMap.get(status);
        if (propContainer == null) {
            if (forNames) {
                propContainer = new DavPropertyNameSet();
            } else {
                propContainer = new DavPropertySet();
            }
            statusMap.put(status, propContainer);
        }
        return propContainer;
    }

    private static boolean isValidHref(String href) {
        return href != null && !"".equals(href);
    }

    public void add(WebDavProperty<?> property) {
        checkType(TYPE_PROPSTAT);
        PropContainer status200 = getPropContainer(200, false);
        status200.addContent(property);
    }

    public String getHref() {
        return href;
    }

    public void add(DavPropertyName propertyName) {
        checkType(TYPE_PROPSTAT);
        var status200 = getPropContainer(200, true);
        status200.addContent(propertyName);
    }

    public void add(WebDavProperty<?> property, int status) {
        checkType(TYPE_PROPSTAT);
        var propCont = getPropContainer(status, false);
        propCont.addContent(property);
    }

    public void add(DavPropertyName propertyName, int status) {
        checkType(TYPE_PROPSTAT);
        var propCont = getPropContainer(status, true);
        propCont.addContent(propertyName);
    }

    private void checkType(int type) {
        if (this.type != type) {
            throw new IllegalStateException("The given MultiStatusResponse is not of the required type.");
        }
    }

    @Override
    public Element toXml(final Document document) {
        var response = DomUtils.createElement(document, XML_RESPONSE, caldav(XML_RESPONSE));
        // add '<href>'
        response.appendChild(DomUtils.hrefToXml(href, document));
        if (type == TYPE_PROPSTAT) {
            // add '<propstat>' elements
            for (var keyValue : statusMap.entrySet()) {
                var st = new Status(keyValue.getKey());
                var propCont = statusMap.get(keyValue.getKey());
                if (!propCont.isEmpty()) {
                    var propstat = DomUtils.createElement(document, XML_PROPSTAT, caldav(XML_PROPSTAT));
                    propstat.appendChild(propCont.toXml(document));
                    propstat.appendChild(st.toXml(document));
                    response.appendChild(propstat);
                }
            }
        } else {
            // add a single '<status>' element
            // NOTE: a href+status response cannot be created with 'null' status
            response.appendChild(status.toXml(document));
        }
        return response;
    }
}
