package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.WebDavResource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>MultiStatusResponse</code> represents the DAV:multistatus element defined
 * by RFC 2518:
 * <pre>
 * &lt;!ELEMENT response (href, ((href*, status)|(propstat+)), responsedescription?) &gt;
 * &lt;!ELEMENT status (#PCDATA) &gt;
 * &lt;!ELEMENT propstat (prop, status, responsedescription?) &gt;
 * &lt;!ELEMENT responsedescription (#PCDATA) &gt;
 * &lt;!ELEMENT prop ANY &gt;
 * </pre>
 */
public class CustomMultiStatusResponse extends MultiStatusResponse implements XmlSerializable, DavConstants {

    private static final int TYPE_PROPSTAT = 0;
    private static final int TYPE_HREFSTATUS = 1;

    /**
     * The type of MultiStatusResponse
     */
    private int type;

    /**
     * The content the 'href' element for this response
     */
    private String href;

    /**
     * An optional response description.
     */
    private String responseDescription;

    /**
     * Type of MultiStatus response: Href + Status
     */
    private Status status;

    /**
     * Type of MultiStatus response: PropStat Hashmap containing all status
     */
    private HashMap<Integer, PropContainer> statusMap = new HashMap<Integer, PropContainer>();

    public CustomMultiStatusResponse(final WebDavResource resource, final DavPropertyNameSet propNameSet, final int propFindType) {
        super(new DummyDavResource(resource), propNameSet, propFindType);
        customMultiStatusResponse(resource, propNameSet, propFindType);
    }

    public CustomMultiStatusResponse(final String href, final int i) {
        super(href, i);
    }

    private void customMultiStatusResponse(String href, String responseDescription, int type) {
        if (!isValidHref(href)) {
            throw new IllegalArgumentException("Invalid href ('" + href + "')");
        }
        this.href = href;
        this.responseDescription = responseDescription;
        this.type = type;
    }

    /**
     * Constructs a WebDAV multistatus response and retrieves the resource
     * properties according to the given <code>DavPropertyNameSet</code>. It
     * adds all known property to the '200' set, while unknown properties are
     * added to the '404' set.
     * <p>
     * Note, that the set of property names is ignored in case of a {@link
     * #PROPFIND_ALL_PROP} and {@link #PROPFIND_PROPERTY_NAMES} propFindType.
     *
     * @param resource The resource to retrieve the property from
     * @param propNameSet The property name set as obtained from the request
     * body.
     * @param propFindType any of the following values: {@link
     * #PROPFIND_ALL_PROP}, {@link #PROPFIND_BY_PROPERTY}, {@link
     * #PROPFIND_PROPERTY_NAMES}, {@link #PROPFIND_ALL_PROP_INCLUDE}
     */
    public void customMultiStatusResponse(
            WebDavResource resource, DavPropertyNameSet propNameSet,
            int propFindType) {
        customMultiStatusResponse(resource.getHref(), null, TYPE_PROPSTAT);

        if (propFindType == PROPFIND_PROPERTY_NAMES) {
            // only property names requested
            PropContainer status200 = getPropContainer(DavServletResponse.SC_OK, true);
            for (DavPropertyName propName : resource.getPropertyNames()) {
                status200.addContent(propName);
            }
        } else {
            // all or a specified set of property and their values requested.
            PropContainer status200 = getPropContainer(DavServletResponse.SC_OK, false);

            // Collection of missing property names for 404 responses
            Set<DavPropertyName> missing = new HashSet<DavPropertyName>(propNameSet.getContent());

            // Add requested properties or all non-protected properties,
            // or non-protected properties plus requested properties (allprop/include)
            if (propFindType == PROPFIND_BY_PROPERTY) {
                // add explicitly requested properties (proptected or non-protected)
                for (DavPropertyName propName : propNameSet) {
                    DavProperty<?> prop = resource.getProperty(propName);
                    if (prop != null) {
                        status200.addContent(prop);
                        missing.remove(propName);
                    }
                }
            } else {
                // add all non-protected properties
                for (DavProperty<?> property : resource.getProperties()) {
                    boolean allDeadPlusRfc4918LiveProperties =
                            propFindType == PROPFIND_ALL_PROP
                                    || propFindType == PROPFIND_ALL_PROP_INCLUDE;
                    boolean wasRequested = missing.remove(property.getName());

                    if ((allDeadPlusRfc4918LiveProperties
                            && !property.isInvisibleInAllprop())
                            || wasRequested) {
                        status200.addContent(property);
                    }
                }

                // try if missing properties specified in the include section
                // can be obtained using resource.getProperty
                if (propFindType == PROPFIND_ALL_PROP_INCLUDE && !missing.isEmpty()) {
                    for (DavPropertyName propName : new HashSet<DavPropertyName>(missing)) {
                        DavProperty<?> prop = resource.getProperty(propName);
                        if (prop != null) {
                            status200.addContent(prop);
                            missing.remove(propName);
                        }
                    }
                }
            }

            if (!missing.isEmpty() && propFindType != PROPFIND_ALL_PROP) {
                PropContainer status404 = getPropContainer(DavServletResponse.SC_NOT_FOUND, true);
                for (DavPropertyName propName : missing) {
                    status404.addContent(propName);
                }
            }
        }
    }

    private PropContainer getPropContainer(int status, boolean forNames) {
        PropContainer propContainer = statusMap.get(status);
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

    /**
     * @param href
     * @return false if the given href is <code>null</code> or empty string.
     */
    private static boolean isValidHref(String href) {
        return href != null && !"".equals(href);
    }
}
