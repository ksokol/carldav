package org.unitedinternet.cosmo.dav.caldav.report;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.AddressData;
import org.unitedinternet.cosmo.dav.impl.DavFile;
import org.unitedinternet.cosmo.model.hibernate.HibFileItem;

import java.io.IOException;

/**
 * @author Kamill Sokol
 */
public class AddressbookMultigetReport extends MultigetReport {

    public static final ReportType REPORT_TYPE_CARDDAV_MULTIGET =
            ReportType.register(CaldavConstants.ELEMENT_CARDDAV_ADDRESSBOOK_MULTIGET, NAMESPACE_CARDDAV, AddressbookMultigetReport.class);

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

        final DavFile file = (DavFile) resource;
        if (getPropFindProps().contains(ADDRESSDATA)) {
            msr.add(new AddressData(readCardData(file)));
        }

        return msr;
    }

    private String readCardData(final DavFile resource) throws CosmoDavException {
        if (! resource.exists()) {
            return null;
        }

        final HibFileItem item = (HibFileItem) resource.getItem();
        final StringBuilder builder = new StringBuilder();

        try {
            builder.append(IOUtils.toString(item.getContentInputStream()));
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ReportType getType() {
        return REPORT_TYPE_CARDDAV_MULTIGET;
    }
}
