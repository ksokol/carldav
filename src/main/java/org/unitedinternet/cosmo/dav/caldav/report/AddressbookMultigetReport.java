package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.jackrabbit.webdav.CustomDavPropertyNameSet;
import carldav.jackrabbit.webdav.CustomMultiStatusResponse;
import carldav.jackrabbit.webdav.CustomReportInfo;
import carldav.jackrabbit.webdav.CustomReportType;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.AddressData;
import org.unitedinternet.cosmo.dav.impl.DavCard;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;

import javax.xml.namespace.QName;

/**
 * @author Kamill Sokol
 */
public class AddressbookMultigetReport extends MultigetReport {

    public static final CustomReportType REPORT_TYPE_CARDDAV_MULTIGET =
            CustomReportType.register(new QName(NS_CARDDAV, ELEMENT_CARDDAV_ADDRESSBOOK_MULTIGET, PRE_CARD), AddressbookMultigetReport.class);

    @Override
    protected OutputFilter findOutputFilter(CustomReportInfo info) throws CosmoDavException {
        return null;
        //TODO
        /*
            Element propdata =
                    DomUtil.getChildElement(getReportElementFrom(info),
                            XML_PROP, NAMESPACE);
            if (propdata == null) {
                return null;
            }

            Element cdata =
                    DomUtil.getChildElement(propdata, ELEMENT_CARDDAV_ADDRESS_DATA,
                            NAMESPACE_CARDDAV);
            if (cdata == null) {
                return null;
            }

            return CarddavOutputFilter.createFromXml(cdata);
        */
    }

    @Override
    protected CustomMultiStatusResponse buildMultiStatusResponse(WebDavResource resource, CustomDavPropertyNameSet props) throws CosmoDavException {
        CustomMultiStatusResponse msr;

        if (props.isEmpty()) {
            final String href = resource.getResourceLocator().getHref(resource.isCollection());
            msr = new CustomMultiStatusResponse(href, 200);
        } else {
            msr = new CustomMultiStatusResponse(resource, props, propfindType);
        }

        final DavCard file = (DavCard) resource;
        if (getPropFindProps().contains(ADDRESSDATA)) {
            msr.add(new AddressData(readCardData(file)));
        }

        return msr;
    }

    private String readCardData(final DavCard resource) throws CosmoDavException {
        if (! resource.exists()) {
            return null;
        }

        final HibICalendarItem item = (HibICalendarItem) resource.getItem();
        final StringBuilder builder = new StringBuilder();

        builder.append(item.getCalendar());
        return builder.toString();
    }

    public CustomReportType getType() {
        return REPORT_TYPE_CARDDAV_MULTIGET;
    }
}
