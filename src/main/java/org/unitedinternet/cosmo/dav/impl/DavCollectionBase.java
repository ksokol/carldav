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

import static carldav.CarldavConstants.TEXT_HTML;

import carldav.jackrabbit.webdav.CustomReportType;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

public class DavCollectionBase extends DavResourceBase implements WebDavResource, DavCollection {

    protected final Set<CustomReportType> reportTypes = new HashSet<>();

    private List<WebDavResource> members;

    private HibCollectionItem item;
    private DavCollection parent;

    public DavCollectionBase(HibCollectionItem collection, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);
        this.item = collection;
        members = new ArrayList<>();
    }

    public DavCollectionBase(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        this(new HibCollectionItem(), locator, factory);
    }

    public HibCollectionItem getItem() {
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
    public List<WebDavResource> getMembers2() {
        for (HibItem memberHibItem : item.getItems()) {
            WebDavResource resource = memberToResource(memberHibItem);
            members.add(resource);
        }
        return Collections.unmodifiableList(members);
    }

    public List<WebDavResource> getCollectionMembers() {
        Set<HibCollectionItem> hibCollectionItems = getContentService().findCollectionItems(item);
        for (HibItem memberHibItem : hibCollectionItems) {
            WebDavResource resource = memberToResource(memberHibItem);
            members.add(resource);
        }
        return Collections.unmodifiableList(members);
    }
    
    public void removeMember2(WebDavResource member) {
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
        return "\"" + getItem().getEntityTag() + "\"";
    }

    public void addContent(DavContent content, InputContext context) throws CosmoDavException {
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

    public Set<CustomReportType> getReportTypes() {
        return reportTypes;
    }

    protected void loadLiveProperties(DavPropertySet properties) {
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
            content = getContentService().updateContent(content);
        } else {
            content = getContentService().createContent(collection, content);
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
        context.setContentType(TEXT_HTML);
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

            for (final WebDavResource child : getMembers2()) {
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