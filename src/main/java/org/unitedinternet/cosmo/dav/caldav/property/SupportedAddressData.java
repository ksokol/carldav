package org.unitedinternet.cosmo.dav.caldav.property;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kamill Sokol
 */
public class SupportedAddressData extends StandardDavProperty implements ICalendarConstants, CaldavConstants {

    public SupportedAddressData() {
        super(SUPPORTEDADDRESSDATA, null, true);
    }

    public Element toXml(Document document) {
        final Element name = getName().toXml(document);
        final Element element = DomUtil.createElement(document, ELEMENT_CARDDAV_ADDRESS_DATA_TYPE, NAMESPACE_CARDDAV);

        DomUtil.setAttribute(element, ATTR_CALDAV_CONTENT_TYPE, NAMESPACE_CARDDAV, CARD_MEDIA_TYPE);
        DomUtil.setAttribute(element, ATTR_CALDAV_VERSION, NAMESPACE_CARDDAV, CARD_VERSION);
        name.appendChild(element);

        return name;
    }

}
