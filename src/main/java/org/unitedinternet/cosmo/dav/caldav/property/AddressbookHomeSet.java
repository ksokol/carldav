package org.unitedinternet.cosmo.dav.caldav.property;

import carldav.jackrabbit.webdav.xml.DomUtils;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.server.ServerConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static carldav.CarldavConstants.ADDRESSBOOK_HOME_SET;
import static carldav.CarldavConstants.caldav;

/**
 * @author Kamill Sokol
 */
public class AddressbookHomeSet extends StandardDavProperty implements CaldavConstants {

    public AddressbookHomeSet(DavResourceLocator locator, String userId) {
        super(ADDRESSBOOK_HOME_SET, href(locator.getBaseHref() + "/" + ServerConstants.SVC_DAV, userId));
    }

    public String getHref() {
        return (String) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element e = DomUtils.createElement(document, XML_HREF, caldav(XML_HREF));
        DomUtils.setText(e, getHref());
        name.appendChild(e);

        return name;
    }

    private static String href(String baseHref, String userId) {
        return CARD_HOME.bindAbsolute(baseHref, userId);
    }
}
