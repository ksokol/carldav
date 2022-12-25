package org.unitedinternet.cosmo.dav.impl;

import carldav.entity.Item;
import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import carldav.jackrabbit.webdav.version.report.ReportType;
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

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static carldav.CarldavConstants.GET_CONTENT_LENGTH;
import static carldav.CarldavConstants.GET_CONTENT_TYPE;
import static carldav.CarldavConstants.TEXT_CALENDAR_VALUE;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;

public class DavCalendarResource extends DavItemResourceBase implements ICalendarConstants {

  public DavCalendarResource(Item item, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
    super(item, locator, factory);

    registerLiveProperty(GET_CONTENT_LENGTH);
    registerLiveProperty(GET_CONTENT_TYPE);

    reportTypes.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
    reportTypes.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
  }

  @Override
  protected void populateItem(DavInputContext inputContext) throws CosmoDavException {
    super.populateItem(inputContext);

    setCalendar(inputContext.getCalendarString());
  }

  public boolean matches(CalendarFilter filter) throws CosmoDavException {
    return getCalendarQueryProcesor().filterQuery(getItem(), filter);
  }

  public String getCalendar() {
    return getItem().getCalendar();
  }

  public void setCalendar(String calendar) throws CosmoDavException {
    var item = getItem();
    item.setCalendar(calendar);
  }

  public void writeHead(final HttpServletResponse response) {
    response.setContentType(TEXT_CALENDAR_VALUE);

    var calendar = getCalendar();
    var calendarBytes = calendar.getBytes(StandardCharsets.UTF_8);

    response.setContentLength(calendarBytes.length);
    if (getModificationTime() >= 0) {
      response.addDateHeader(LAST_MODIFIED, getModificationTime());
    }
    if (getETag() != null) {
      response.setHeader(ETAG, getETag());
    }
  }

  public void writeBody(final HttpServletResponse response) throws IOException {
    var calendar = getCalendar();
    var calendarBytes = calendar.getBytes(StandardCharsets.UTF_8);

    var bois = new ByteArrayInputStream(calendarBytes);
    IOUtils.copy(bois, response.getOutputStream());
  }

  public Set<ReportType> getReportTypes() {
    return reportTypes;
  }

  protected void loadLiveProperties(DavPropertySet properties) {
    super.loadLiveProperties(properties);
    var calendarBytes = getCalendar().getBytes(StandardCharsets.UTF_8);

    properties.add(new ContentLength((long) calendarBytes.length));
    properties.add(new ContentType(ICALENDAR_MEDIA_TYPE, "UTF-8"));
  }
}
