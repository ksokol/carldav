package org.unitedinternet.cosmo.dav.impl;

import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import carldav.jackrabbit.webdav.version.report.ReportType;
import javax.xml.namespace.QName;
import org.apache.commons.lang.StringEscapeUtils;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ETagUtil;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.service.ContentService;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static carldav.CarldavConstants.TEXT_HTML_VALUE;
import static carldav.CarldavConstants.caldav;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;

public class DavCollectionBase extends DavResourceBase implements WebDavResource, DavCollection {

  protected final Set<ReportType> reportTypes = new HashSet<>();
  private final List<WebDavResource> members;
  private final CollectionItem item;
  private DavCollection parent;

  public DavCollectionBase(CollectionItem collection, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
    super(locator, factory);
    this.item = collection;
    members = new ArrayList<>();
  }

  public DavCollectionBase(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
    this(new CollectionItem(), locator, factory);
  }

  public CollectionItem getItem() {
    return item;
  }

  @Override
  public boolean exists() {
    return item.getId() != null;
  }

  public boolean isCollection() {
    return true;
  }

  @Override
  public String getDisplayName() {
    return item.getDisplayName();
  }

  public long getModificationTime() {
    return item.getModifiedDate() == null ? 0 : item.getModifiedDate().getTime();
  }

  @Override
  public List<WebDavResource> getMembers() {
    var collections = getResourceFactory().getCollectionRepository().findByParentId(item.getId());
    var items = getResourceFactory().getItemRepository().findByCollectionId(item.getId());

    members.addAll(collections.stream().map(this::collectionToResource).toList());
    members.addAll(items.stream().map(this::memberToResource).toList());

    return Collections.unmodifiableList(members);
  }

  @Override
  public String getName() {
    return item.getName();
  }

  public List<WebDavResource> getCollectionMembers() {
    var collections = getResourceFactory().getCollectionRepository().findByParentId(item.getId());
    for (CollectionItem memberItem : collections) {
      var resource = collectionToResource(memberItem);
      members.add(resource);
    }
    return Collections.unmodifiableList(members);
  }

  public void removeItem(WebDavResource member) {
    var item = ((DavItemResourceBase) member).getItem();
    getContentService().removeItemFromCollection(item, this.item);
    members.remove(member);
  }

  public void removeCollection(DavCollectionBase member) {
    var hibItem = member.getItem();
    getContentService().removeCollection(hibItem);
    members.remove(member);
  }

  @Override
  public DavCollection getParent() throws CosmoDavException {
    if (parent == null) {
      var parentLocator = getResourceLocator().getParentLocator();
      try {
        parent = (DavCollection) getResourceFactory().resolve(parentLocator);
      } catch (ClassCastException e) {
        throw new ForbiddenException("Resource " + parentLocator.getPath() + " is not a collection");
      }
      if (parent == null)
        parent = new DavCollectionBase(parentLocator, getResourceFactory());
    }

    return parent;
  }

  @Override
  public String getETag() {
    return ETagUtil.createETagEscaped(getItem().getId(), getItem().getModifiedDate());
  }

  public void addContent(WebDavResource content, DavInputContext context) throws CosmoDavException {
    var base = (DavItemResourceBase) content;
    base.populateItem(context);
    saveContent(base);
    members.add(base);
  }

  public WebDavResource findMember(String href) throws CosmoDavException {
    return memberToResource(href);
  }

  public boolean isHomeCollection() {
    return false;
  }

  protected Set<QName> getResourceTypes() {
    Set<QName> rt = new LinkedHashSet<>();
    rt.add(caldav(XML_COLLECTION));
    return rt;
  }

  public Set<ReportType> getReportTypes() {
    return reportTypes;
  }

  protected void loadLiveProperties(DavPropertySet properties) {
    properties.add(new LastModified(item.getModifiedDate()));
    properties.add(new Etag(getETag()));
    properties.add(new DisplayName(getDisplayName()));
    properties.add(new ResourceType(getResourceTypes()));
    properties.add(new IsCollection(isCollection()));
  }

  protected void saveContent(DavItemResourceBase member) throws CosmoDavException {
    var content = member.getItem();

    if (content.getId() != null) {
      content = getContentService().updateContent(content);
    } else {
      content = getContentService().createContent(item, content);
    }

    member.setItem(content);
  }

  protected WebDavResource memberToResource(Item item) throws CosmoDavException {
    var path = getResourcePath() + "/" + URLEncoder.encode(item.getName(), StandardCharsets.UTF_8);
    var locator = getResourceLocator().getFactory().createResourceLocatorByPath(getResourceLocator().getContext(), path);

    return getResourceFactory().createResource(locator, item);
  }

  protected WebDavResource collectionToResource(CollectionItem hibItem) {
    var path = getResourcePath() + "/" + URLEncoder.encode(hibItem.getName(), StandardCharsets.UTF_8);
    DavResourceLocator locator = getResourceLocator().getFactory().createResourceLocatorByPath(getResourceLocator().getContext(), path);
    return getResourceFactory().createCollectionResource(locator, hibItem);
  }

  protected WebDavResource memberToResource(String uri) throws CosmoDavException {
    var locator = getResourceLocator().getFactory().createResourceLocatorByUri(getResourceLocator().getContext(), uri);
    return getResourceFactory().resolve(locator);
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
      var colName = StringEscapeUtils.escapeHtml(getDisplayName());
      writer.write(colName);

      writer.write("</title></head>\n");
      writer.write("<body>\n");
      writer.write("<h1>");
      writer.write(colName);
      writer.write("</h1>\n");

      var parent = getParent();

      writer.write("Parent: <a href=\"");
      writer.write(parent.getResourceLocator().getHref(true));
      writer.write("\">");
      writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
      writer.write("</a></li>\n");

      writer.write("<h2>Members</h2>\n");
      writer.write("<ul>\n");

      for (var child : getMembers()) {
        writer.write("<li><a href=\"");
        writer.write(child.getResourceLocator().getHref(child.isCollection()));
        writer.write("\">");
        writer.write(StringEscapeUtils.escapeHtml(child.getDisplayName()));
        writer.write("</a></li>\n");
      }
      writer.write("</ul>\n");

      writer.write("<h2>Properties</h2>\n");
      writer.write("<dl>\n");

      for (final var i : getWebDavProperties().entrySet()) {
        var prop = i.getValue();
        var text = prop.getValueText();
        if (text == null) {
          text = "-- no value --";
        }
        writer.write("<dt>");
        writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
        writer.write("</dt><dd>");
        writer.write(StringEscapeUtils.escapeHtml(text));
        writer.write("</dd>\n");
      }
      writer.write("</dl>\n");

      writer.write("<p>\n");

      var principalLocator = getResourceLocator().getFactory().createPrincipalLocator(getResourceLocator().getContext(), getUsername());
      writer.write("<a href=\"");
      writer.write(principalLocator.getHref(false));
      writer.write("\">");
      writer.write("Principal resource");
      writer.write("</a><br>\n");
      writer.write("<p>\n");
      if (!isHomeCollection()) {
        var homeLocator = getResourceLocator().getFactory().createHomeLocator(getResourceLocator().getContext(), getUsername());
        writer.write("<a href=\"");
        writer.write(homeLocator.getHref(true));
        writer.write("\">");
        writer.write("Home collection");
        writer.write("</a><br>\n");
      }

      writer.write("</body>");
      writer.write("</html>\n");
    }
  }

  protected ContentService getContentService() {
    return getResourceFactory().getContentService();
  }

  protected CalendarQueryProcessor getCalendarQueryProcesor() {
    return getResourceFactory().getCalendarQueryProcessor();
  }
}
