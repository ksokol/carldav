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
public class AddressData extends StandardDavProperty implements CaldavConstants, ICalendarConstants {

    public AddressData(String calendarData) {
        super(ADDRESSDATA, calendarData, true);
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
