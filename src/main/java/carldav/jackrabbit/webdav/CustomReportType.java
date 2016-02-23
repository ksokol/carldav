package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;

public class CustomReportType implements XmlSerializable {

    private static final HashMap<String, CustomReportType> types = new HashMap<>();

    private final String key;
    private final String localName;
    private final Namespace namespace;
    private final Class<? extends CustomReport> reportClass;

    private CustomReportType(String localName, Namespace namespace, String key, Class<? extends CustomReport> reportClass) {
        this.localName = localName;
        this.namespace = namespace;
        this.key = key;
        this.reportClass = reportClass;
    }

    public CustomReport createReport(WebDavResource resource, CustomReportInfo info) {
        try {
            CustomReport report = reportClass.newInstance();
            report.init(resource, info);
            return report;
        } catch (IllegalAccessException e) {
            // should never occur
            throw new CosmoDavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create new report (" + reportClass.getName() + ") from class: " + e.getMessage());
        } catch (InstantiationException e) {
            // should never occur
            throw new CosmoDavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create new report (" + reportClass.getName() + ") from class: " + e.getMessage());
        }
    }

    public Element toXml(Document document) {
        return DomUtil.createElement(document, localName, namespace);
    }

    public boolean isRequestedReportType(CustomReportInfo reqInfo) {
        if (reqInfo != null) {
            return getReportName().equals(reqInfo.getReportName());
        }
        return false;
    }

    public String getReportName() {
        return key;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    /**
     * Register the report type with the given name, namespace and class, that can
     * run that report.
     *
     * @param localName
     * @param namespace
     * @param reportClass
     * @return
     * @throws IllegalArgumentException if either parameter is <code>null</code> or
     *                                  if the given class does not implement the {@link Report} interface or if
     *                                  it does not provide an empty constructor.
     */
    public static CustomReportType register(String localName, Namespace namespace, Class<? extends CustomReport> reportClass) {
        if (localName == null || namespace == null || reportClass == null) {
            throw new IllegalArgumentException("A ReportType cannot be registered with a null name, namespace or report class");
        }
        String key = DomUtil.getExpandedName(localName, namespace);
        if (types.containsKey(key)) {
            return types.get(key);
        } else {
            try {
                Object report = reportClass.newInstance();
                if (!(report instanceof CustomReport)) {
                    throw new IllegalArgumentException("Unable to register Report class: " + reportClass + " does not implement the Report interface.");
                }
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Error while validating Report class: " + e.getMessage());
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Error while validating Report class.: " + e.getMessage());
            }

            CustomReportType type = new CustomReportType(localName, namespace, key, reportClass);
            types.put(key, type);
            return type;
        }
    }

    /**
     * Return the <code>ReportType</code> requested by the given report info object.
     *
     * @param reportInfo
     * @return the requested <code>ReportType</code>
     * @throws IllegalArgumentException if the reportInfo is <code>null</code> or
     *                                  if the requested report type has not been registered yet.
     */
    public static CustomReportType getType(CustomReportInfo reportInfo) {
        if (reportInfo == null) {
            throw new IllegalArgumentException("ReportInfo must not be null.");
        }
        String key = reportInfo.getReportName();
        if (types.containsKey(key)) {
            return types.get(key);
        } else {
            throw new IllegalArgumentException("The request report '" + key + "' has not been registered yet.");
        }
    }
}