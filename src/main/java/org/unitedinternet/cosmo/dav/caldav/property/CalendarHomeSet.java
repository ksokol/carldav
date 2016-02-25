package org.unitedinternet.cosmo.dav.caldav.property;

import static carldav.CarldavConstants.CALENDAR_HOME_SET;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.CustomDomUtils;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CalendarHomeSet extends StandardDavProperty implements CaldavConstants {

    public CalendarHomeSet(String baseHref, User user) {
        super(CALENDAR_HOME_SET, href(baseHref, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element e = CustomDomUtils.createElement(document, XML_HREF, CarldavConstants.caldav(XML_HREF));
        DomUtil.setText(e, getHref());
        name.appendChild(e);

        return name;
    }

    private static String href(String baseHref, User user) {
        return TEMPLATE_HOME.bindAbsolute(baseHref, user.getEmail());
    }
}
