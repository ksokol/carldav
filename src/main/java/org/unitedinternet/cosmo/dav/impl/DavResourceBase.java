/*
 * Copyright 2006-2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav.impl;

import carldav.jackrabbit.webdav.CustomReportType;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
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
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;

/**
 * <p>
 * Base class for implementations of <code>WebDavResource</code>
 * which provides behavior common to all resources.
 * </p>
 * <p>
 * This class declares the following live properties:
 * </p>
 * <ul>
 * <li> DAV:supported-report-set </li>
 * <li> DAV:current-user-privilege-set </li>
 * </ul>
 * <p>
 * This class does not declare any reports.
 * </p>
 * 
 * @see WebDavResource
 */
public abstract class DavResourceBase implements ExtendedDavConstants, WebDavResource {

    protected static final EntityConverter converter = new EntityConverter();

    private final HashSet<DavPropertyName> liveProperties = new HashSet<>(10);
    protected final Set<CustomReportType> reportTypes = new HashSet<>(10);

    private DavResourceLocator locator;
    private DavResourceFactory factory;
    private DavPropertySet properties;
    private boolean initialized;

    public DavResourceBase(DavResourceLocator locator,
                           DavResourceFactory factory)
        throws CosmoDavException {
        registerLiveProperty(SUPPORTEDREPORTSET);
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

    public org.apache.jackrabbit.webdav.property.DavProperty<?>
        getProperty(DavPropertyName name) {
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
            sorted.put(propertyName.getName(), (WebDavProperty) properties.get(propertyName));
        }

        return sorted;
    }

    public void setProperty(org.apache.jackrabbit.webdav.property.DavProperty<?> property) throws DavException {
        throw new UnsupportedOperationException();
    }

    public void removeMember2(WebDavResource member) {
        throw new UnsupportedOperationException();
    }

    public Report getReport(ReportInfo reportInfo) throws CosmoDavException {
        if (! exists()) {
            throw new NotFoundException();
        }

        if (! isSupportedReport(reportInfo)) {
            throw new UnprocessableEntityException("Unknown report " + reportInfo.getReportName());
        }

        try {
            final CustomReportType type = CustomReportType.getType(reportInfo);
            return type.createReport(this, reportInfo);
        } catch (DavException exception){
            throw new CosmoDavException(exception.getErrorCode(),exception.getMessage(),exception.getCause());
        }
    }

    public DavResourceFactory getResourceFactory() {
        return factory;
    }

    public DavResourceLocator getResourceLocator() {
        return locator;
    }

    // our methods

    protected CosmoSecurityManager getSecurityManager() {
        return factory.getSecurityManager();
    }

    /**
     * Determines whether or not the report indicated by the given
     * report info is supported by this collection.
     */
    protected boolean isSupportedReport(ReportInfo info) {
        for (Iterator<CustomReportType> i=getReportTypes().iterator(); i.hasNext();) {
            if (i.next().isRequestedReportType(info)) {
                return true;
            }
        }
        return false;
    }

    protected Set<CustomReportType> getReportTypes() {
     return reportTypes;
    }

    /**
     * <p>
     * Registers the name of a live property.
     * </p>
     * <p>
     * Typically used in subclass static initializers to add to the set
     * of live properties for the resource.
     * </p>
     */
    protected void registerLiveProperty(DavPropertyName name) {
        liveProperties.add(name);
    }

    /**
     * Returns the set of resource types for this resource.
     */
    protected Set<QName> getResourceTypes() {
        return new HashSet<>();
    }

    protected void loadProperties() {
        if (initialized) {
            return;
        }

        properties.add(new SupportedReportSet(getReportTypes()));

        loadLiveProperties(properties);

        initialized = true;
    }    

    /**
     * Loads the live DAV properties for the resource.
     */
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
