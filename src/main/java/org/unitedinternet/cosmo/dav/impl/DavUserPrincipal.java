package org.unitedinternet.cosmo.dav.impl;

import carldav.jackrabbit.webdav.property.DavPropertySet;
import carldav.jackrabbit.webdav.version.report.ReportType;
import javax.xml.namespace.QName;
import org.apache.commons.lang.StringEscapeUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.AddressbookHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarHomeSet;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.PrincipalUrl;
import org.unitedinternet.cosmo.dav.property.ResourceType;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import static carldav.CarldavConstants.ADDRESSBOOK_HOME_SET;
import static carldav.CarldavConstants.CALENDAR_HOME_SET;
import static carldav.CarldavConstants.DISPLAY_NAME;
import static carldav.CarldavConstants.GET_ETAG;
import static carldav.CarldavConstants.GET_LAST_MODIFIED;
import static carldav.CarldavConstants.IS_COLLECTION;
import static carldav.CarldavConstants.PRINCIPAL_URL;
import static carldav.CarldavConstants.RESOURCE_TYPE;
import static carldav.CarldavConstants.TEXT_HTML_VALUE;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;

public class DavUserPrincipal extends DavResourceBase implements CaldavConstants {

  private final String userId;

  public DavUserPrincipal(String userId, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
    super(locator, factory);

    registerLiveProperty(GET_LAST_MODIFIED);
    registerLiveProperty(DISPLAY_NAME);
    registerLiveProperty(IS_COLLECTION);
    registerLiveProperty(RESOURCE_TYPE);
    registerLiveProperty(GET_ETAG);
    registerLiveProperty(CALENDAR_HOME_SET);
    registerLiveProperty(PRINCIPAL_URL);
    registerLiveProperty(ADDRESSBOOK_HOME_SET);

    this.userId = userId;
  }

  public String getSupportedMethods() {
    return "OPTIONS, GET, PROPFIND";
  }

  public boolean isCollection() {
    return false;
  }

  public long getModificationTime() {
    return -1;
    //TODO user.getModifiedDate().getTime();
  }

  public boolean exists() {
    return true;
  }

  public String getDisplayName() {
    return userId;
  }

  public String getETag() {
    return null;
    //TODO"\"" + user.getETag() + "\"";
  }

  protected Set<QName> getResourceTypes() {
    return Collections.emptySet();
  }

  public Set<ReportType> getReportTypes() {
    return Collections.emptySet();
  }

  protected void loadLiveProperties(DavPropertySet properties) {
    properties.add(new DisplayName(getDisplayName()));
    properties.add(new ResourceType(getResourceTypes()));
    properties.add(new IsCollection(isCollection()));
    //TODO    properties.add(new Etag(user.getETag()));
    //TODO    properties.add(new LastModified(user.getModifiedDate()));
    properties.add(new CalendarHomeSet(getResourceLocator(), userId));
    properties.add(new PrincipalUrl(getResourceLocator(), userId));
    properties.add(new AddressbookHomeSet(getResourceLocator(), userId));
  }

  public void writeHead(final HttpServletResponse response) {
    response.setContentType(TEXT_HTML_VALUE);
    if (getModificationTime() >= 0) {
      response.addDateHeader(LAST_MODIFIED, getModificationTime());
    }
    if (getETag() != null) {
      response.setHeader(ETAG, getETag());
    }
  }

  public void writeBody(final HttpServletResponse response) throws IOException {
    try (var writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
      writer.write("<html>\n<head><title>");
      writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
      writer.write("</title></head>\n");
      writer.write("<body>\n");
      writer.write("<h1>");
      writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
      writer.write("</h1>\n");

      writer.write("<h2>Properties</h2>\n");
      writer.write("<dl>\n");
      for (var i : getWebDavProperties().entrySet()) {
        var prop = i.getValue();
        var text = prop.getValueText();
        writer.write("<dt>");
        writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
        writer.write("</dt><dd>");

        generateHrefIfNecessary(writer, prop, text);

        writer.write("</dd>\n");
      }
      writer.write("</dl>\n");
      writer.write("<p>\n");

      var homeLocator = getResourceLocator().getFactory().createHomeLocator(getResourceLocator().getContext(), userId);
      writer.write("<a href=\"");
      writer.write(homeLocator.getHref(true));
      writer.write("\">");
      writer.write("Home collection");
      writer.write("</a><br>\n");

      writer.write("</body>");
      writer.write("</html>\n");
    }
  }

  @Override
  public String getName() {
    return userId;
  }
}
