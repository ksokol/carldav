package org.unitedinternet.cosmo.dav.property;

import carldav.jackrabbit.webdav.xml.DomUtils;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static carldav.CarldavConstants.CURRENT_USER_PRINCIPAL;
import static carldav.CarldavConstants.caldav;

public class CurrentUserPrincipal extends StandardDavProperty<String> {

    public CurrentUserPrincipal(DavResourceLocator locator, String userId) {
        super(CURRENT_USER_PRINCIPAL, href(locator, userId));
    }

    public String getHref() {
        return getValue();
    }

    private static String href(DavResourceLocator locator, String userId) {
        return TEMPLATE_USER.bindAbsolute(locator.getBaseHref(), userId);
    }

    @Override
    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element href = DomUtils.createElement(document, XML_HREF, caldav("href"));
        DomUtils.setText(href, getHref());
        name.appendChild(href);

        return name;
    }

}
