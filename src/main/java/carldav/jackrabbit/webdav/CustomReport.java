package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.unitedinternet.cosmo.dav.WebDavResource;

/**
 * @author Kamill Sokol
 */
public interface CustomReport extends Report {

    /**
     * Set the <code>WebDavResource</code> for which this report was requested
     * and the <code>ReportInfo</code> as specified by the REPORT request body,
     * that defines the details for this report.<br>
     * Please note that this methods should perform basic validation checks
     * in order to prevent exceptional situations during the xml serialization.
     *
     * @param resource
     * @param info
     * @throws DavException
     */
    void init(WebDavResource resource, ReportInfo info) throws DavException;
}
