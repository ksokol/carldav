package org.unitedinternet.cosmo.dav.property;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.PRINCIPALURL;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the DAV:principal-URL property.
 *
 * The property is protected. The value is a single DAV:href element containing the URL of the home collection.
 */
public class PrincipalUrl extends StandardDavProperty {

    public PrincipalUrl(DavResourceLocator locator, User user) {
        super(PRINCIPALURL, href(locator, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    private static String href(DavResourceLocator locator, User user) {
        return TEMPLATE_USER.bindAbsolute(locator.getBaseHref(), user.getEmail());
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element href = DomUtil.createElement(document, XML_HREF, NAMESPACE);
        DomUtil.setText(href, getHref());
        name.appendChild(href);

        return name;
    }
}
