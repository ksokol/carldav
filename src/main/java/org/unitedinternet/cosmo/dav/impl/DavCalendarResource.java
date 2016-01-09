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

import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.property.ContentLength;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.NoteItem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract calendar resource.
 */
public abstract class DavCalendarResource extends DavContentBase
    implements ICalendarConstants {
    private static final Log LOG = LogFactory.getLog(DavCalendarResource.class);
    private final Set<ReportType> reportTypes = new HashSet<>();

    public DavCalendarResource(ContentItem item,
                               DavResourceLocator locator,
                               DavResourceFactory factory,
                               EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);

        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);

        reportTypes.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        reportTypes.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
    }
       
    // WebDavResource methods

    public String getSupportedMethods() {
        if(exists()) {
            return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PUT, DELETE, REPORT";
        }
        else {
            return "OPTIONS, TRACE, PUT";
        }
    }

    // DavResourceBase methods

    @Override
    protected void populateItem(InputContext inputContext)
        throws CosmoDavException {
        super.populateItem(inputContext);

        DavInputContext dic = (DavInputContext) inputContext;
        Calendar calendar = dic.getCalendar();

        setCalendar(calendar);
    }

    // our methods

    /**
     * @return true if this resource matches the given filter.
     */
    public boolean matches(CalendarFilter filter)
        throws CosmoDavException {
        return getCalendarQueryProcesor().filterQuery((NoteItem)getItem(), filter);
    }

    /**
     * @return The calendar object associated with this resource.
     */
    public abstract Calendar getCalendar();
    
    /**
     * Set the calendar object associated with this resource.
     * @param calendar calendar object parsed from inputcontext
     */
    protected abstract void setCalendar(Calendar calendar)
        throws CosmoDavException;

    public void writeTo(OutputContext outputContext)
        throws CosmoDavException, IOException {
        if (! exists()) {
            throw new IllegalStateException("cannot spool a nonexistent resource");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("spooling file " + getResourcePath());
        }

        String contentType =
            IOUtil.buildContentType(ICALENDAR_MEDIA_TYPE, "UTF-8");
        outputContext.setContentType(contentType);
  
        // Get calendar
        Calendar calendar = getCalendar();
        
        // convert Calendar object to String, then to bytes (UTF-8)    
        byte[] calendarBytes = calendar.toString().getBytes("UTF-8");
        outputContext.setContentLength(calendarBytes.length);
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());
        
        if (! outputContext.hasStream()) {
            return;
        }

        // spool calendar bytes
        ByteArrayInputStream bois = new ByteArrayInputStream(calendarBytes);
        IOUtil.spool(bois, outputContext.getOutputStream());
    }

    public Set<ReportType> getReportTypes() {
        return reportTypes;
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        try {
            byte[] calendarBytes = getCalendar().toString().getBytes("UTF-8");
            properties.add(new ContentLength(Long.valueOf(calendarBytes.length)));
        } catch (Exception e) {
            throw new CosmoException("Can't convert calendar", e);
        }

        properties.add(new ContentType(ICALENDAR_MEDIA_TYPE, "UTF-8"));
    }

    /** */
    protected void setLiveProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        super.setLiveProperty(property, create);

        DavPropertyName name = property.getName();
        if (name.equals(DavPropertyName.GETCONTENTTYPE)) {
            throw new ProtectedPropertyModificationException(name);
        }
    }
}
