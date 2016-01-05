package org.unitedinternet.cosmo.dav.caldav.report;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.NAMESPACE_CARDDAV;

import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.report.MultiStatusReport;

/**
 * @author Kamill Sokol
 */
public class AddressbookMultigetReport extends MultiStatusReport {

    public static final ReportType REPORT_TYPE_CARDDAV_MULTIGET =
            ReportType.register(CaldavConstants.ELEMENT_CARDDAV_ADDRESSBOOK_MULTIGET, NAMESPACE_CARDDAV, AddressbookMultigetReport.class);

    @Override
    protected void parseReport(final ReportInfo info) throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doQuerySelf(final WebDavResource resource) throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doQueryChildren(final DavCollection collection) throws CosmoDavException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReportType getType() {
        return REPORT_TYPE_CARDDAV_MULTIGET;
    }
}
