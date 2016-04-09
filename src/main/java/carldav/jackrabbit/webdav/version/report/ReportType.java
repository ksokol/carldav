package carldav.jackrabbit.webdav.version.report;

import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;

import javax.xml.namespace.QName;

public class ReportType implements XmlSerializable {

    private static final HashMap<String, ReportType> types = new HashMap<>();

    private final String key;
    private final QName namespace;
    private final Class<? extends Report> reportClass;

    private ReportType(QName namespace, String key, Class<? extends Report> reportClass) {
        this.namespace = namespace;
        this.key = key;
        this.reportClass = reportClass;
    }

    public Report createReport(WebDavResource resource, ReportInfo info) {
        try {
            Report report = reportClass.newInstance();
            report.init(resource, info);
            return report;
        } catch (IllegalAccessException e) {
            // should never occur
            throw new CosmoDavException("Failed to create new report (" + reportClass.getName() + ") from class: " + e.getMessage());
        } catch (InstantiationException e) {
            // should never occur
            throw new CosmoDavException("Failed to create new report (" + reportClass.getName() + ") from class: " + e.getMessage());
        }
    }

    public Element toXml(Document document) {
        return DomUtils.createElement(document, namespace.getLocalPart(), namespace);
        //return DomUtil.createElement(document, localName, namespace);
    }

    public boolean isRequestedReportType(ReportInfo reqInfo) {
        if (reqInfo != null) {
            return getReportName().equals(reqInfo.getReportName());
        }
        return false;
    }

    public String getReportName() {
        return key;
    }

    /**
     * Register the report type with the given name, namespace and class, that can
     * run that report.
     *
     * @param namespace
     * @param reportClass
     * @return
     * @throws IllegalArgumentException if either parameter is <code>null</code> or
     *                                  if the given class does not implement the {@link Report} interface or if
     *                                  it does not provide an empty constructor.
     */
    public static ReportType register(QName namespace, Class<? extends Report> reportClass) {
        if (namespace == null || reportClass == null) {
            throw new IllegalArgumentException("A ReportType cannot be registered with a null name, namespace or report class");
        }
        String key = namespace.toString();
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

            ReportType type = new ReportType(namespace, key, reportClass);
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
    public static ReportType getType(ReportInfo reportInfo) {
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