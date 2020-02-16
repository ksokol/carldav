package org.unitedinternet.cosmo.dav.impl;

import carldav.jackrabbit.webdav.property.DavPropertySet;
import carldav.jackrabbit.webdav.version.report.ReportType;
import org.apache.commons.lang.StringEscapeUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.AddressbookHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarHomeSet;
import org.unitedinternet.cosmo.dav.property.*;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static carldav.CarldavConstants.*;
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

    public void writeHead(final HttpServletResponse response) throws IOException {
        response.setContentType(TEXT_HTML_VALUE);
        if (getModificationTime() >= 0) {
            response.addDateHeader(LAST_MODIFIED, getModificationTime());
        }
        if (getETag() != null) {
            response.setHeader(ETAG, getETag());
        }
    }

    public void writeBody(final HttpServletResponse response) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
        try {
            writer.write("<html>\n<head><title>");
            writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
            writer.write("</title></head>\n");
            writer.write("<body>\n");
            writer.write("<h1>");
            writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
            writer.write("</h1>\n");

            writer.write("<h2>Properties</h2>\n");
            writer.write("<dl>\n");
            for (final Map.Entry<String, WebDavProperty> i : getWebDavProperties().entrySet()) {
                WebDavProperty prop = i.getValue();
                String text = prop.getValueText();
                writer.write("<dt>");
                writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
                writer.write("</dt><dd>");

                generateHrefIfNecessary(writer, prop, text);

                writer.write("</dd>\n");
            }
            writer.write("</dl>\n");
            writer.write("<p>\n");


            DavResourceLocator homeLocator = getResourceLocator().getFactory().createHomeLocator(getResourceLocator().getContext(), userId);
            writer.write("<a href=\"");
            writer.write(homeLocator.getHref(true));
            writer.write("\">");
            writer.write("Home collection");
            writer.write("</a><br>\n");

            writer.write("</body>");
            writer.write("</html>\n");
        } finally {
            writer.close();
        }
    }

    @Override
    public String getName() {
        return userId;
    }
}
