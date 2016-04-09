package org.unitedinternet.cosmo.dav.caldav.report;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CARDDAV_ADDRESSBOOK_QUERY;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.NS_CARDDAV;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.PRE_CARD;

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.calendar.query.UnsupportedCollationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.SupportedCollationException;
import org.unitedinternet.cosmo.dav.impl.DavCardCollection;
import org.unitedinternet.cosmo.dav.report.MultiStatusReport;
import org.w3c.dom.Element;

import java.text.ParseException;

import javax.xml.namespace.QName;

/**
 * @author Kamill Sokol
 */
public class AddressbookQueryReport extends MultiStatusReport {

    public static final ReportType REPORT_TYPE_CARDDAV_QUERY =
        ReportType.register(new QName(NS_CARDDAV, ELEMENT_CARDDAV_ADDRESSBOOK_QUERY, PRE_CARD), AddressbookQueryReport.class);

    private AddressbookFilter queryFilter;
    private OutputFilter outputFilter;

    @Override
    protected void parseReport(final ReportInfo info) throws CosmoDavException {
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
            outputFilter = findOutputFilter(info);
        }

        queryFilter = findQueryFilter(info);
    }

    @Override
    protected void doQuerySelf(final WebDavResource resource) throws CosmoDavException {
    }

    @Override
    protected void doQueryChildren(final DavCollection collection) throws CosmoDavException {
        if (collection instanceof DavCardCollection) {
            DavCardCollection dcc = (DavCardCollection) collection;
            getResults().addAll(dcc.findMembers(queryFilter));
            return;
        }
    }

    public ReportType getType() {
        return REPORT_TYPE_CARDDAV_QUERY;
    }

    private AddressbookFilter findQueryFilter(ReportInfo info) throws CosmoDavException {
        Element filterdata = DomUtils.getChildElement(getReportElementFrom(info), CarldavConstants.carddav(CaldavConstants.ELEMENT_CALDAV_FILTER));

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

    private OutputFilter findOutputFilter(ReportInfo info) throws CosmoDavException {
        return null;
        //TODO not implemented yet
        /*
        Element propdata = DomUtil.getChildElement(getReportElementFrom(info), XML_PROP, NAMESPACE);
        if (propdata == null) {
            return null;
        }

        Element cdata = DomUtil.getChildElement(propdata, CaldavConstants.ELEMENT_CALDAV_CALENDAR_DATA, CaldavConstants.NAMESPACE_CALDAV);
        if (cdata == null) {
            return null;
        }

        return CaldavOutputFilter.createFromXml(cdata);
        */
    }
}
