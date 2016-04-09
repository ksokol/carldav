package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.jackrabbit.webdav.MultiStatusResponse;
import carldav.jackrabbit.webdav.property.DavPropertyNameSet;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.AddressData;
import org.unitedinternet.cosmo.dav.impl.DavCard;
import carldav.entity.Item;

import javax.xml.namespace.QName;

import static carldav.CarldavConstants.ADDRESS_DATA;

/**
 * @author Kamill Sokol
 */
public class AddressbookMultigetReport extends MultigetReport {

    public static final ReportType REPORT_TYPE_CARDDAV_MULTIGET =
            ReportType.register(new QName(NS_CARDDAV, ELEMENT_CARDDAV_ADDRESSBOOK_MULTIGET, PRE_CARD), AddressbookMultigetReport.class);

    @Override
    protected OutputFilter findOutputFilter(ReportInfo info) throws CosmoDavException {
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
    protected MultiStatusResponse buildMultiStatusResponse(WebDavResource resource, DavPropertyNameSet props) throws CosmoDavException {
        MultiStatusResponse msr;

        if (props.isEmpty()) {
            final String href = resource.getResourceLocator().getHref(resource.isCollection());
            msr = new MultiStatusResponse(href, 200);
        } else {
            msr = new MultiStatusResponse(resource, props, propfindType);
        }

        final DavCard file = (DavCard) resource;
        if (getPropFindProps().contains(ADDRESS_DATA)) {
            msr.add(new AddressData(readCardData(file)));
        }

        return msr;
    }

    private String readCardData(final DavCard resource) throws CosmoDavException {
        if (! resource.exists()) {
            return null;
        }

        final Item item = resource.getItem();
        final StringBuilder builder = new StringBuilder();

        builder.append(item.getCalendar());
        return builder.toString();
    }

    public ReportType getType() {
        return REPORT_TYPE_CARDDAV_MULTIGET;
    }
}
