package org.unitedinternet.cosmo.dav.caldav.property;

import static carldav.CarldavConstants.ADDRESS_DATA;

import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;

/**
 * @author Kamill Sokol
 */
public class AddressData extends StandardDavProperty implements CaldavConstants, ICalendarConstants {

    public AddressData(String calendarData) {
        super(ADDRESS_DATA, calendarData, true);
    }

//    public Element toXml(Document document) {
//        Element element = super.toXml(document);
//
//        DomUtil.setAttribute(element, ATTR_CALDAV_CONTENT_TYPE, NAMESPACE_CARDDAV, CARD_MEDIA_TYPE);
//        DomUtil.setAttribute(element, ATTR_CALDAV_VERSION, NAMESPACE_CARDDAV, CARD_VERSION);
//
//        return element;
//    }
}
