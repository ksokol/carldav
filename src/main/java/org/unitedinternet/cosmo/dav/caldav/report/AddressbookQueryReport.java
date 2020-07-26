package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.version.report.ReportType;
import carldav.jackrabbit.webdav.xml.DomUtils;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.calendar.query.UnsupportedCollationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.SupportedCollationException;
import org.unitedinternet.cosmo.dav.impl.DavCardCollection;
import org.unitedinternet.cosmo.dav.report.MultiStatusReport;

import javax.xml.namespace.QName;
import java.text.ParseException;

import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_ALL_PROP;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_BY_PROPERTY;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_PROPERTY_NAMES;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CARDDAV_ADDRESSBOOK_QUERY;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.NS_CARDDAV;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.PRE_CARD;

public class AddressbookQueryReport extends MultiStatusReport {

    public static final ReportType REPORT_TYPE_CARDDAV_QUERY =
        ReportType.register(new QName(NS_CARDDAV, ELEMENT_CARDDAV_ADDRESSBOOK_QUERY, PRE_CARD), AddressbookQueryReport.class);

    private AddressbookFilter queryFilter;

    @Override
    protected void parseReport(final ReportInfo info) {
        if (!getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " + getType());
        }

        setPropFindProps(info.getPropertyNameSet());
        if (info.containsContentElement(DavConstants.ALLPROP)) {
            setPropFindType(PROPFIND_ALL_PROP);
        } else if (info.containsContentElement(DavConstants.PROPNAME)) {
            setPropFindType(PROPFIND_PROPERTY_NAMES);
        } else {
            setPropFindType(PROPFIND_BY_PROPERTY);
        }

        queryFilter = findQueryFilter(info);
    }

    @Override
    protected void doQuerySelf(final WebDavResource resource) {
        //no implementation;
    }

    @Override
    protected void doQueryChildren(final DavCollection collection) {
        if (collection instanceof DavCardCollection) {
            var dcc = (DavCardCollection) collection;
            getResults().addAll(dcc.findMembers(queryFilter));
        }
    }

    public ReportType getType() {
        return REPORT_TYPE_CARDDAV_QUERY;
    }

    private AddressbookFilter findQueryFilter(ReportInfo info) {
        var filterdata = DomUtils.getChildElement(getReportElementFrom(info), CarldavConstants.carddav(CaldavConstants.ELEMENT_CALDAV_FILTER));

        if (filterdata == null) {
            return null;
        }

        try {
            return new AddressbookFilter(filterdata);
        } catch (ParseException e) {
            throw new InvalidFilterException(e);
        } catch (UnsupportedCollationException e) {
            throw new SupportedCollationException();
        }
    }
}
