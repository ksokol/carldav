package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.w3c.dom.Element;

/**
 * @author Kamill Sokol
 */
public class CustomReportInfo extends ReportInfo {

    private Element documentElement;

    public CustomReportInfo(final Element documentElement, final int depth) throws DavException {
        super((Element) documentElement.cloneNode(true), depth);
        this.documentElement = documentElement;
    }

    public Element getDocumentElement() {
        return documentElement;
    }
}
