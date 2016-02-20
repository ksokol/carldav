package org.unitedinternet.cosmo.dav.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.springframework.web.util.UriComponentsBuilder;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.AddressbookHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarHomeSet;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.PrincipalUrl;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.server.ServerConstants;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * <p>
 * Models a WebDAV principal resource (as described in RFC 3744) that represents a user account.
 * </p>
 *
 * @see DavContent
 * @see DavResourceBase
 * @see User
 */
public class DavUserPrincipal extends DavResourceBase implements CaldavConstants, DavContent {

    private final User user;

    public DavUserPrincipal(User user, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);

        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(CALENDARHOMESET);
        registerLiveProperty(PRINCIPALURL);
        registerLiveProperty(ADDRESSBOOKHOMESET);

        this.user = user;
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
        return user.getEmail();
    }

    public String getETag() {
        return null;
        //TODO"\"" + user.getEntityTag() + "\"";
    }

    public void writeTo(OutputContext context) throws CosmoDavException, IOException {
        writeHtmlRepresentation(context);
    }


    public DavCollection getParent() throws CosmoDavException {
        //TODO
        return null;
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
    //TODO    properties.add(new Etag(user.getEntityTag()));
    //TODO    properties.add(new LastModified(user.getModifiedDate()));
        properties.add(new CalendarHomeSet("/" + ServerConstants.SVC_DAV, user));
        properties.add(new PrincipalUrl(getResourceLocator(), user));
        properties.add(new AddressbookHomeSet("/" + ServerConstants.SVC_DAV, user));
    }

    private void writeHtmlRepresentation(OutputContext context) throws CosmoDavException, IOException {
        context.setContentType(IOUtil.buildContentType("text/html", "UTF-8"));
        context.setModificationTime(getModificationTime());
        context.setETag(getETag());

        if (!context.hasStream()) {
            return;
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(context.getOutputStream(), "utf8"));
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

            WebDavResource parent = getParent();
            if(parent != null) {
                writer.write("<a href=\"");
                writer.write(parent.getResourceLocator().getHref(true));
                writer.write("\">");
                writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
                writer.write("</a>\n");
            }
            writer.write("<p>\n");

            final DavResourceLocator homeLocator;

            try {
                final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(getResourceLocator().getContext().toURI());
                uriComponentsBuilder.replacePath(ServerConstants.SVC_DAV);
                final URI uri = uriComponentsBuilder.build().toUri();
                homeLocator = getResourceLocator().getFactory().createHomeLocator(uri.toURL(), user);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

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
}
