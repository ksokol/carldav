package org.unitedinternet.cosmo.dav.caldav.property;

import static carldav.CarldavConstants.ADDRESSBOOK_HOME_SET;
import static carldav.CarldavConstants.caldav;

import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kamill Sokol
 */
public class AddressbookHomeSet extends StandardDavProperty implements CaldavConstants {

    public AddressbookHomeSet(DavResourceLocator locator, User user) {
        super(ADDRESSBOOK_HOME_SET, href(locator.getBaseHref(), user), true);
    }

    public AddressbookHomeSet(String baseHref, User user) {
        super(ADDRESSBOOK_HOME_SET, href(baseHref, user), true);
    }

    public String getHref() {
        return (String) getValue();
    }

    public Element toXml(Document document) {
        Element name = getName().toXml(document);

        Element e = CustomDomUtils.createElement(document, XML_HREF, caldav(XML_HREF));
        CustomDomUtils.setText(e, getHref());
        name.appendChild(e);

        return name;
    }

    private static String href(String baseHref, User user) {
        return CARD_HOME.bindAbsolute(baseHref, user.getEmail());
    }
}
