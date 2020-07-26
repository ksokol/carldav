package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.jackrabbit.webdav.MultiStatusResponse;
import carldav.jackrabbit.webdav.property.DavPropertyNameSet;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.AddressData;
import org.unitedinternet.cosmo.dav.impl.DavCard;

import javax.xml.namespace.QName;

import static carldav.CarldavConstants.ADDRESS_DATA;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CARDDAV_ADDRESSBOOK_MULTIGET;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.NS_CARDDAV;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.PRE_CARD;

public class AddressbookMultigetReport extends MultigetReport {

    public static final ReportType REPORT_TYPE_CARDDAV_MULTIGET =
            ReportType.register(new QName(NS_CARDDAV, ELEMENT_CARDDAV_ADDRESSBOOK_MULTIGET, PRE_CARD), AddressbookMultigetReport.class);

    @Override
    protected OutputFilter findOutputFilter(ReportInfo info) {
        return null;
    }

    @Override
    protected MultiStatusResponse buildMultiStatusResponse(WebDavResource resource, DavPropertyNameSet props) {
        MultiStatusResponse msr;

        if (props.isEmpty()) {
            var href = resource.getResourceLocator().getHref(resource.isCollection());
            msr = new MultiStatusResponse(href, 200);
        } else {
            msr = new MultiStatusResponse(resource, props, propfindType);
        }

        var file = (DavCard) resource;
        if (getPropFindProps().contains(ADDRESS_DATA)) {
            msr.add(new AddressData(readCardData(file)));
        }

        return msr;
    }

    private String readCardData(DavCard resource) {
        if (!resource.exists()) {
            return null;
        }

        var item = resource.getItem();
        var builder = new StringBuilder();

        builder.append(item.getCalendar());
        return builder.toString();
    }

    public ReportType getType() {
        return REPORT_TYPE_CARDDAV_MULTIGET;
    }
}
