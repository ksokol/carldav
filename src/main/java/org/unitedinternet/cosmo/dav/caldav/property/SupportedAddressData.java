package org.unitedinternet.cosmo.dav.caldav.property;

import static carldav.CarldavConstants.SUPPORTED_ADDRESS_DATA;
import static carldav.CarldavConstants.carddav;

import carldav.jackrabbit.webdav.xml.DomUtils;
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
        super(SUPPORTED_ADDRESS_DATA, null);
    }

    public Element toXml(Document document) {
        final Element name = getName().toXml(document);
        final Element element = DomUtils.createElement(document, ELEMENT_CARDDAV_ADDRESS_DATA_TYPE, carddav(ELEMENT_CARDDAV_ADDRESS_DATA_TYPE));

        DomUtils.setAttribute(element, ATTR_CALDAV_CONTENT_TYPE, carddav(ATTR_CALDAV_CONTENT_TYPE), CARD_MEDIA_TYPE);
        DomUtils.setAttribute(element, ATTR_CALDAV_VERSION, carddav(ATTR_CALDAV_VERSION), CARD_VERSION);
        name.appendChild(element);

        return name;
    }

}
