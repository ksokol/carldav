package carldav.jackrabbit.webdav;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.DeltaVResource;
import org.apache.jackrabbit.webdav.version.report.ExpandPropertyReport;
import org.apache.jackrabbit.webdav.version.report.LocateByHistoryReport;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.VersionTreeReport;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;

public class CustomReportType implements DeltaVConstants, XmlSerializable {

    private static final HashMap<String, CustomReportType> types = new HashMap<>();

    public static final CustomReportType VERSION_TREE = register(XML_VERSION_TREE, NAMESPACE, VersionTreeReport.class);
    public static final CustomReportType EXPAND_PROPERTY = register(XML_EXPAND_PROPERTY, NAMESPACE, ExpandPropertyReport.class);
    public static final CustomReportType LOCATE_BY_HISTORY = register(XML_LOCATE_BY_HISTORY, NAMESPACE, LocateByHistoryReport.class);

    private final String key;
    private final String localName;
    private final Namespace namespace;
    private final Class<? extends Report> reportClass;

    /**
     * Private constructor
     *
     * @see CustomReportType#register(String, org.apache.jackrabbit.webdav.xml.Namespace, Class)
     */
    private CustomReportType(String localName, Namespace namespace, String key, Class<? extends Report> reportClass) {
        this.localName = localName;
        this.namespace = namespace;
        this.key = key;
        this.reportClass = reportClass;
    }

    /**
     * Creates a new {@link Report} with this type.
     *
     * @return
     * @throws DavException
     */
    public Report createReport(DeltaVResource resource, ReportInfo info) throws DavException {
        try {
            Report report = reportClass.newInstance();
            report.init(resource, info);
            return report;
        } catch (IllegalAccessException e) {
            // should never occur
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create new report (" + reportClass.getName() + ") from class: " + e.getMessage());
        } catch (InstantiationException e) {
            // should never occur
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create new report (" + reportClass.getName() + ") from class: " + e.getMessage());
        }
    }

    /**
     * Returns an Xml element representing this report type. It may be used to
     * build the body for a REPORT request.
     *
     * @param document
     * @return Xml representation
     * @see XmlSerializable#toXml(org.w3c.dom.Document)
     */
    public Element toXml(Document document) {
        return DomUtil.createElement(document, localName, namespace);
    }

    /**
     * Returns true if this <code>ReportType</code> is requested by the given
     * <code>ReportInfo</code>
     *
     * @param reqInfo
     * @return
     */
    public boolean isRequestedReportType(ReportInfo reqInfo) {
        if (reqInfo != null) {
            return getReportName().equals(reqInfo.getReportName());
        }
        return false;
    }

    /**
     * Return the qualified name of this <code>ReportType</code>.
     *
     * @return qualified name
     */
    public String getReportName() {
        return key;
    }

    /**
     * @return
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * @return
     */
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
    public static CustomReportType register(String localName, Namespace namespace, Class<? extends Report> reportClass) {
        if (localName == null || namespace == null || reportClass == null) {
            throw new IllegalArgumentException("A ReportType cannot be registered with a null name, namespace or report class");
        }
        String key = DomUtil.getExpandedName(localName, namespace);
        if (types.containsKey(key)) {
            return types.get(key);
        } else {
            try {
                Object report = reportClass.newInstance();
                if (!(report instanceof Report)) {
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
    public static CustomReportType getType(ReportInfo reportInfo) {
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