package org.unitedinternet.cosmo.dav.caldav.property;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:calendar-home-set property.
 *
 * The property is protected. The value is a single DAV:href element containing the URI of the home collection.
 */
public class CalendarHomeSet extends StandardDavProperty implements CaldavConstants {

    public CalendarHomeSet(String baseHref, User user) {
        super(CALENDARHOMESET, href(baseHref, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element e = DomUtil.createElement(document, XML_HREF, NAMESPACE);
        DomUtil.setText(e, getHref());
        name.appendChild(e);

        return name;
    }

    private static String href(String baseHref, User user) {
        return TEMPLATE_HOME.bindAbsolute(baseHref, user.getEmail());
    }
}
