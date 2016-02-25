package org.unitedinternet.cosmo.dav.property;

import static carldav.CarldavConstants.PRINCIPAL_URL;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.CustomDomUtils;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PrincipalUrl extends StandardDavProperty {

    public PrincipalUrl(DavResourceLocator locator, User user) {
        super(PRINCIPAL_URL, href(locator, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    private static String href(DavResourceLocator locator, User user) {
        return TEMPLATE_USER.bindAbsolute(locator.getBaseHref(), user.getEmail());
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element href = CustomDomUtils.createElement(document, XML_HREF, CarldavConstants.caldav(XML_HREF));
        DomUtil.setText(href, getHref());
        name.appendChild(href);

        return name;
    }
}
