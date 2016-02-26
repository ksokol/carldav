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

import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.CustomDavPropertySet;
import carldav.jackrabbit.webdav.version.report.CustomReportType;
import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.property.ContentLength;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static carldav.CarldavConstants.*;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;

public class DavCalendarResource extends DavItemResourceBase implements ICalendarConstants {

    //private final Set<CustomReportType> reportTypes = new HashSet<>();

    public DavCalendarResource(HibItem item,
                               DavResourceLocator locator,
                               DavResourceFactory factory)
        throws CosmoDavException {
        super(item, locator, factory);

        registerLiveProperty(GET_CONTENT_LENGTH);
        registerLiveProperty(GET_CONTENT_TYPE);

        reportTypes.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        reportTypes.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
    }

    @Override
    protected void populateItem(DavInputContext inputContext) throws CosmoDavException {
        super.populateItem(inputContext);

        DavInputContext dic = inputContext;
        setCalendar(dic.getCalendarString());
    }

    public boolean matches(CalendarFilter filter)
        throws CosmoDavException {
        return getCalendarQueryProcesor().filterQuery((HibICalendarItem)getItem(), filter);
    }

    public String getCalendar() {
        return ((HibICalendarItem)getItem()).getCalendar();
    }

    public void setCalendar(String calendar) throws CosmoDavException {
        final HibICalendarItem item = (HibICalendarItem) getItem();
        item.setCalendar(calendar);
    }

    public void writeHead(final HttpServletResponse response) throws IOException {
        response.setContentType(TEXT_CALENDAR_VALUE);

        // Get calendar
        String calendar = getCalendar();

        // convert Calendar object to String, then to bytes (UTF-8)
        byte[] calendarBytes = calendar.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(calendarBytes.length);
        if (getModificationTime() >= 0) {
            response.addDateHeader(LAST_MODIFIED, getModificationTime());
        }
        if (getETag() != null) {
            response.setHeader(ETAG, getETag());
        }
    }

    public void writeBody(final HttpServletResponse response) throws IOException {
        // Get calendar
        String calendar = getCalendar();
        // convert Calendar object to String, then to bytes (UTF-8)
        byte[] calendarBytes = calendar.getBytes(StandardCharsets.UTF_8);

        // spool calendar bytes
        ByteArrayInputStream bois = new ByteArrayInputStream(calendarBytes);
        IOUtils.copy(bois, response.getOutputStream());
    }

    public Set<CustomReportType> getReportTypes() {
        return reportTypes;
    }

    protected void loadLiveProperties(CustomDavPropertySet properties) {
        super.loadLiveProperties(properties);
        byte[] calendarBytes = getCalendar().getBytes(Charset.forName("UTF-8"));

        properties.add(new ContentLength((long) calendarBytes.length));
        properties.add(new ContentType(ICALENDAR_MEDIA_TYPE, "UTF-8"));
    }
}
