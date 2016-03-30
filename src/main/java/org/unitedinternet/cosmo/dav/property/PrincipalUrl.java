package org.unitedinternet.cosmo.dav.property;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static carldav.CarldavConstants.PRINCIPAL_URL;

public class PrincipalUrl extends StandardDavProperty {

    public PrincipalUrl(DavResourceLocator locator, String userId) {
        super(PRINCIPAL_URL, href(locator, userId));
    }

    public String getHref() {
        return (String) getValue();
    }

    private static String href(DavResourceLocator locator, String userId) {
        return TEMPLATE_USER.bindAbsolute(locator.getBaseHref(), userId);
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element href = CustomDomUtils.createElement(document, XML_HREF, CarldavConstants.caldav(XML_HREF));
        CustomDomUtils.setText(href, getHref());
        name.appendChild(href);

        return name;
    }
}
