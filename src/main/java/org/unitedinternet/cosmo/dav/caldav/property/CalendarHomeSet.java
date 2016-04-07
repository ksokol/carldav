package org.unitedinternet.cosmo.dav.caldav.property;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static carldav.CarldavConstants.CALENDAR_HOME_SET;

public class CalendarHomeSet extends StandardDavProperty implements CaldavConstants {

    public CalendarHomeSet(DavResourceLocator locator, String userId) {
        super(CALENDAR_HOME_SET, href("/" + locator.contextPath(), userId));
    }

    public String getHref() {
        return (String) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element e = CustomDomUtils.createElement(document, XML_HREF, CarldavConstants.caldav(XML_HREF));
        CustomDomUtils.setText(e, getHref());
        name.appendChild(e);

        return name;
    }

    private static String href(String baseHref, String userId) {
        return TEMPLATE_HOME.bindAbsolute(baseHref, userId);
    }
}
