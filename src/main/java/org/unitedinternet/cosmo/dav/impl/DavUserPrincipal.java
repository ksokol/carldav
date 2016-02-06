package org.unitedinternet.cosmo.dav.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.springframework.web.util.UriComponentsBuilder;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
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
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

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

    private static final Log LOG = LogFactory.getLog(DavUserPrincipal.class);

    private final User user;

    private DavUserPrincipalCollection parent;

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

    public void addMember(org.apache.jackrabbit.webdav.DavResource member, InputContext inputContext) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        // while it would be ideal to throw an UnsupportedOperationException,
        // MultiStatus tries to add a MultiStatusResponse for every member
        // of a WebDavResource regardless of whether or not it's a collection,
        // so we need to return an empty iterator.
        return new DavResourceIteratorImpl(Collections.emptyList());
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public WebDavResource getCollection() {
        try {
            return getParent();
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
    }

    public void move(org.apache.jackrabbit.webdav.DavResource destination) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public void copy(org.apache.jackrabbit.webdav.DavResource destination, boolean shallow) throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavCollection getParent() throws CosmoDavException {
        if (parent == null) {
            DavResourceLocator parentLocator = getResourceLocator().getParentLocator();
            parent = (DavUserPrincipalCollection) getResourceFactory().resolve(parentLocator);
        }
        return parent;
    }

    public User getUser() {
        return user;
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

    protected void setLiveProperty(WebDavProperty property, boolean create) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }

    protected void removeLiveProperty(DavPropertyName name) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(name);
    }

    protected void setDeadProperty(WebDavProperty property) throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this resource");
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
                Object value = prop.getValue();
                String text = null;
                if (value instanceof Element) {
                    try {
                        text = DomWriter.write((Element) value);
                    } catch (XMLStreamException e) {
                        LOG.warn("Error serializing value for property " + prop.getName());
                    }
                }
                if (text == null) {
                    text = prop.getValueText();
                }
                writer.write("<dt>");
                writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
                writer.write("</dt><dd>");
                writer.write(StringEscapeUtils.escapeHtml(text));
                writer.write("</dd>\n");
            }
            writer.write("</dl>\n");

            WebDavResource parent = getParent();
            writer.write("<a href=\"");
            writer.write(parent.getResourceLocator().getHref(true));
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
            writer.write("</a></li>\n");
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