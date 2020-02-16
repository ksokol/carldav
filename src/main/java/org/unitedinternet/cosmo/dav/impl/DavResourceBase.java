package org.unitedinternet.cosmo.dav.impl;

import carldav.calendar.property.CurrentUserPrivilegeSet;
import carldav.jackrabbit.webdav.property.DavPropertyName;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import carldav.jackrabbit.webdav.version.report.Report;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.version.report.ReportType;
import org.apache.commons.lang.StringEscapeUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.AddressbookHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarHomeSet;
import org.unitedinternet.cosmo.dav.property.PrincipalUrl;
import org.unitedinternet.cosmo.dav.property.SupportedReportSet;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static carldav.CarldavConstants.PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET;
import static carldav.CarldavConstants.SUPPORTED_REPORT_SET;

public abstract class DavResourceBase implements ExtendedDavConstants, WebDavResource {

    protected static final EntityConverter converter = new EntityConverter();

    private final HashSet<DavPropertyName> liveProperties = new HashSet<>(10);
    protected final Set<ReportType> reportTypes = new HashSet<>(10);

    private DavResourceLocator locator;
    private DavResourceFactory factory;
    private DavPropertySet properties;
    private boolean initialized;

    public DavResourceBase(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        registerLiveProperty(SUPPORTED_REPORT_SET);
        registerLiveProperty(PROPERTY_ACL_CURRENT_USER_PRIVILEGE_SET);
        this.locator = locator;
        this.factory = factory;
        this.properties = new DavPropertySet();
        this.initialized = false;
    }

    public String getSupportedMethods() {
        throw new UnsupportedOperationException();
    }

    public boolean isCollection() {
        return false;
    }

    public List<WebDavResource> getMembers() {
        return new ArrayList<>();
    }

    public String getComplianceClass() {
        return WebDavResource.COMPLIANCE_CLASS;
    }

    public String getResourcePath() {
        return locator.getPath();
    }

    public String getHref() {
        return locator.getHref(isCollection());
    }

    public DavPropertyName[] getPropertyNames() {
        loadProperties();
        return properties.getPropertyNames();
    }

    public WebDavProperty<?> getProperty(DavPropertyName name) {
        loadProperties();
        return properties.get(name);
    }

    @Deprecated
    public DavPropertySet getProperties() {
        loadProperties();
        return properties;
    }

    public Map<String, WebDavProperty> getWebDavProperties() {
        final DavPropertySet properties = getProperties();
        final DavPropertyName[] propertyNames = properties.getPropertyNames();
        final Map<String, WebDavProperty> sorted = new TreeMap<>();

        for (final DavPropertyName propertyName : propertyNames) {
            sorted.put(propertyName.getName(), properties.get(propertyName));
        }

        return sorted;
    }

    public void removeItem(WebDavResource member) {
        throw new UnsupportedOperationException();
    }

    public Report getReport(ReportInfo reportInfo) throws CosmoDavException {
        if (! exists()) {
            throw new NotFoundException();
        }

        if (! isSupportedReport(reportInfo)) {
            throw new UnprocessableEntityException("Unknown report " + reportInfo.getReportName());
        }

        final ReportType type = ReportType.getType(reportInfo);
        return type.createReport(this, reportInfo);
    }

    public DavResourceFactory getResourceFactory() {
        return factory;
    }

    public DavResourceLocator getResourceLocator() {
        return locator;
    }

    protected String getUsername() {
        return factory.getSecurityManager().getUsername();
    }

    protected boolean isSupportedReport(ReportInfo info) {
        for (Iterator<ReportType> i = getReportTypes().iterator(); i.hasNext();) {
            if (i.next().isRequestedReportType(info)) {
                return true;
            }
        }
        return false;
    }

    protected Set<ReportType> getReportTypes() {
     return reportTypes;
    }

    protected void registerLiveProperty(DavPropertyName name) {
        liveProperties.add(name);
    }

    protected Set<QName> getResourceTypes() {
        return new HashSet<>();
    }

    protected void loadProperties() {
        if (initialized) {
            return;
        }

        properties.add(new SupportedReportSet(getReportTypes()));
        properties.add(new CurrentUserPrivilegeSet());

        loadLiveProperties(properties);

        initialized = true;
    }

    protected abstract void loadLiveProperties(DavPropertySet properties);

    public DavCollection getParent() throws CosmoDavException {
        return null;
    }

    protected void generateHrefIfNecessary(final PrintWriter writer, final WebDavProperty prop, final String text) {
        if(instanceOf(prop)) {
            writer.write("<a href=\"");
        }

        writer.write(StringEscapeUtils.escapeHtml(text));

        if(instanceOf(prop)) {
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(text));
            writer.write("</a>\n");
        }
    }

    protected boolean instanceOf(final WebDavProperty prop) {
        return prop instanceof AddressbookHomeSet || prop instanceof CalendarHomeSet || prop instanceof PrincipalUrl;
    }
}
