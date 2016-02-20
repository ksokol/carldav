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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.service.ContentService;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

/**
 * Extends <code>DavResourceBase</code> to adapt the Cosmo
 * <code>CollectionItem</code> to the DAV resource model.
 * 
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:supported-report-set</code> (protected)</li>
 * </ul>
 *
 * @see DavResourceBase
 * @see HibCollectionItem
 */
public class DavCollectionBase extends DavResourceBase implements WebDavResource, DavCollection {

    private static final Log LOG = LogFactory.getLog(DavCollectionBase.class);

    protected final Set<ReportType> reportTypes = new HashSet<>();

    private List<org.apache.jackrabbit.webdav.DavResource> members;

    private HibCollectionItem item;
    private DavCollection parent;

    public DavCollectionBase(HibCollectionItem collection, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);
        this.item = collection;
        members = new ArrayList<>();
    }

    public DavCollectionBase(DavResourceLocator locator, DavResourceFactory factory)
            throws CosmoDavException {
        this(new HibCollectionItem(), locator, factory);
    }

    public HibCollectionItem getItem() {
        return item;
    }

    // Jackrabbit WebDavResource

    public String getSupportedMethods() {
        // If resource doesn't exist, then options are limited
        if (!exists()) {
            return "OPTIONS, TRACE, PUT";
        } else {
            return "OPTIONS, GET, HEAD, PROPFIND, TRACE, DELETE, REPORT";
        }
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

    @Override
    public long getModificationTime() {
        return item.getModifiedDate() == null ? 0 : item.getModifiedDate().getTime();
    }

    public void spool(OutputContext outputContext) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DavResource getCollection() {
        return null;
    }

    public DavResourceIterator getMembers() {
        try {
            for (HibItem memberHibItem : item.getItems()) {
                WebDavResource resource = memberToResource(memberHibItem);
                if (resource != null) {
                    members.add(resource);
                }
            }

            return new DavResourceIteratorImpl(members);
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
    }

    public DavResourceIterator getCollectionMembers() {
        try {
            Set<HibCollectionItem> hibCollectionItems = getContentService().findCollectionItems(item);
            for (HibItem memberHibItem : hibCollectionItems) {
                WebDavResource resource = memberToResource(memberHibItem);
                if (resource != null) {
                    members.add(resource);
                }
            }
            return new DavResourceIteratorImpl(members);
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
    }
    
    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
            throws org.apache.jackrabbit.webdav.DavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing resource '" + member.getDisplayName()
                    + "' from '" + getDisplayName() + "'");
        }

        HibItem hibItem;

        if(member instanceof DavCollectionBase) {
            hibItem = ((DavCollectionBase) member).getItem();
        } else if(member instanceof DavItemResource) {
            hibItem = ((DavItemResource) member).getItem();
        } else {
            throw new IllegalArgumentException("Expected 'member' as instance of: [" + DavItemResource.class.getName() +"]");
        }

        HibCollectionItem collection = item;

        if (hibItem instanceof HibCollectionItem) {
            getContentService().removeCollection((HibCollectionItem) hibItem);
        } else {
            getContentService().removeItemFromCollection(hibItem, collection);
        }

        members.remove(member);
    }

    // WebDavResource

    @Override
    public DavCollection getParent() throws CosmoDavException {
        if (parent == null) {
            DavResourceLocator parentLocator = getResourceLocator()
                    .getParentLocator();
            try {
                parent = (DavCollection) getResourceFactory().resolve(
                        parentLocator);
            } catch (ClassCastException e) {
                throw new ForbiddenException("Resource "
                        + parentLocator.getPath() + " is not a collection");
            }
            if (parent == null)
                parent = new DavCollectionBase(parentLocator, getResourceFactory());
        }

        return parent;
    }

    public void writeTo(OutputContext out) throws CosmoDavException,
            IOException {
        writeHtmlDirectoryIndex(out);
    }

    @Override
    public String getETag() {
        if (getItem() == null)
            return null;
        // an item that is about to be created does not yet have an etag
        if (StringUtils.isBlank(getItem().getEntityTag()))
            return null;
        return "\"" + getItem().getEntityTag() + "\"";
    }

    // DavCollection

    public void addContent(DavContent content, InputContext context) throws CosmoDavException {
        if(!(content instanceof DavItemResourceBase)) {
            throw new IllegalArgumentException("Expected instance of : [" + DavItemResourceBase.class.getName() + "]");
        }

        DavItemResourceBase base = (DavItemResourceBase) content;
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

    // our methods

    protected Set<QName> getResourceTypes() {
        Set<QName> rt = new TreeSet<>();
        rt.add(RESOURCE_TYPE_COLLECTION);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return reportTypes;
    }

    protected void loadLiveProperties(DavPropertySet properties) {
        if (item == null) {
            return;
        }

        properties.add(new LastModified(item.getModifiedDate()));
        properties.add(new Etag(getETag()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
    }

    /**
     * Saves the given content resource to storage.
     */
    protected void saveContent(DavItemResource member) throws CosmoDavException {
        HibCollectionItem collection = item;
        HibItem content = member.getItem();

        if (content.getId() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("updating member " + member.getResourcePath());
            }

            content = getContentService().updateContent(content);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating member " + member.getResourcePath());
            }

            content = getContentService()
                    .createContent(collection, content);
        }

        member.setItem(content);
    }

    protected WebDavResource memberToResource(HibItem hibItem) throws CosmoDavException {
        String path;
        try {
            path = getResourcePath() + "/" + URLEncoder.encode(hibItem.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CosmoDavException(e);
        }
        DavResourceLocator locator = getResourceLocator().getFactory()
                .createResourceLocatorByPath(getResourceLocator().getContext(),
                        path);
        return getResourceFactory().createResource(locator, hibItem);
    }

    protected WebDavResource memberToResource(String uri) throws CosmoDavException {
        DavResourceLocator locator = getResourceLocator().getFactory()
                .createResourceLocatorByUri(getResourceLocator().getContext(),
                        uri);
        return getResourceFactory().resolve(locator);
    }

    private void writeHtmlDirectoryIndex(OutputContext context) throws CosmoDavException, IOException {
        context.setContentType(IOUtil.buildContentType("text/html", "UTF-8"));
        context.setModificationTime(getModificationTime());
        context.setETag(getETag());

        if(!context.hasStream()) {
            return;
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(context.getOutputStream(), "utf8"));
        try{
            writer.write("<html>\n<head><title>");
            String colName = StringEscapeUtils.escapeHtml(getDisplayName());
            writer.write(colName);
    
            writer.write("</title></head>\n");
            writer.write("<body>\n");
            writer.write("<h1>");
            writer.write(colName);
            writer.write("</h1>\n");
    
            WebDavResource parent = getParent();

            writer.write("Parent: <a href=\"");
            writer.write(parent.getResourceLocator().getHref(true));
            writer.write("\">");
            writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
            writer.write("</a></li>\n");

            writer.write("<h2>Members</h2>\n");
            writer.write("<ul>\n");

            for (DavResourceIterator i = getMembers(); i.hasNext();) {
                WebDavResource child = (WebDavResource) i.nextResource();
                writer.write("<li><a href=\"");
                writer.write(child.getResourceLocator().getHref(child.isCollection()));
                writer.write("\">");
                writer.write(StringEscapeUtils.escapeHtml(child.getDisplayName()));
                writer.write("</a></li>\n");
            }
            writer.write("</ul>\n");
    
            writer.write("<h2>Properties</h2>\n");
            writer.write("<dl>\n");

            for (final Map.Entry<String, WebDavProperty> i : getWebDavProperties().entrySet()) {
                WebDavProperty prop = i.getValue();
                String text = prop.getValueText();
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
    
            User user = getSecurityManager().getSecurityContext().getUser();

            writer.write("<p>\n");
            if (!isHomeCollection()) {
                DavResourceLocator homeLocator = getResourceLocator()
                        .getFactory().createHomeLocator(
                                getResourceLocator().getContext(), user);
                writer.write("<a href=\"");
                writer.write(homeLocator.getHref(true));
                writer.write("\">");
                writer.write("Home collection");
                writer.write("</a><br>\n");
            }
    
            writer.write("</body>");
            writer.write("</html>\n");
        }finally{
            writer.close();
        }
    }

    protected ContentService getContentService() {
        return getResourceFactory().getContentService();
    }

    protected CalendarQueryProcessor getCalendarQueryProcesor() {
        return getResourceFactory().getCalendarQueryProcessor();
    }
}