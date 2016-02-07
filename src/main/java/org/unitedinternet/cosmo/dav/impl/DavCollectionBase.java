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

import carldav.service.generator.IdGenerator;
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
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibContentItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

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
import javax.xml.stream.XMLStreamException;

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
public class DavCollectionBase extends DavItemResourceBase implements
        DavItemCollection {
    private static final Log LOG = LogFactory.getLog(DavCollectionBase.class);
    private static final Set<String> DEAD_PROPERTY_FILTER = new HashSet<String>();
    protected final Set<ReportType> reportTypes = new HashSet<>();

    private List<org.apache.jackrabbit.webdav.DavResource> members;

    static {
        DEAD_PROPERTY_FILTER.add(HibCollectionItem.class.getName());
    }

    public DavCollectionBase(HibCollectionItem collection, DavResourceLocator locator, DavResourceFactory factory, IdGenerator idGenerator) throws CosmoDavException {
        super(collection, locator, factory, idGenerator);

        members = new ArrayList<>();
    }

    public DavCollectionBase(DavResourceLocator locator,
            DavResourceFactory factory, IdGenerator idGenerator)
            throws CosmoDavException {
        this(new HibCollectionItem(), locator, factory, idGenerator);
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

    public boolean isCollection() {
        return true;
    }

    public void spool(OutputContext outputContext) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
            InputContext inputContext)
            throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        try {
            for (HibItem memberHibItem : ((HibCollectionItem) getItem()).getItems()) {
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
            Set<HibCollectionItem> hibCollectionItems = getContentService().findCollectionItems((HibCollectionItem) getItem());
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
        if(!(member instanceof DavItemResource)){
            throw new IllegalArgumentException("Expected 'member' as instance of: [" + DavItemResource.class.getName() +"]");
        }
        HibCollectionItem collection = (HibCollectionItem) getItem();
        HibItem hibItem = ((DavItemResource) member).getItem();

        try {
            if (hibItem instanceof HibCollectionItem) {
                getContentService().removeCollection((HibCollectionItem) hibItem);
            } else {
                getContentService().removeItemFromCollection(hibItem, collection);
            }
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }

        members.remove(member);
    }

    // WebDavResource

    public void writeTo(OutputContext out) throws CosmoDavException,
            IOException {
        writeHtmlDirectoryIndex(out);
    }

    // DavCollection

    public void addContent(DavContent content, InputContext context) throws CosmoDavException {
        if(!(content instanceof DavContentBase)) {
            throw new IllegalArgumentException("Expected instance of : [" + DavContentBase.class.getName() + "]");
        }
        
        DavContentBase base = (DavContentBase) content;
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

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);
    }

    /** */
    protected void setLiveProperty(WebDavProperty property, boolean create)
            throws CosmoDavException {
        super.setLiveProperty(property, create);

        HibCollectionItem cc = (HibCollectionItem) getItem();
        if (cc == null) {
            return;
        }

        DavPropertyName name = property.getName();
        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property " + name
                    + " requires a value");
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
            throws CosmoDavException {
        super.removeLiveProperty(name);
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        return DEAD_PROPERTY_FILTER;
    }

    /**
     * Saves the given content resource to storage.
     */
    protected void saveContent(DavItemContent member) throws CosmoDavException {
        HibCollectionItem collection = (HibCollectionItem) getItem();
        HibContentItem content = (HibContentItem) member.getItem();

        try {
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
        } catch (CollectionLockedException e) {
            throw new LockedException();
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

    private void writeHtmlDirectoryIndex(OutputContext context)
            throws CosmoDavException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("writing html directory index for  " + getDisplayName());
        }

        context.setContentType(IOUtil.buildContentType("text/html", "UTF-8"));
        context.setModificationTime(getModificationTime());
        context.setETag(getETag());

        if (!context.hasStream()) {
            return;
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                context.getOutputStream(), "utf8"));
        try{
            writer.write("<html>\n<head><title>");
            String colName = getDisplayName() != null ? getDisplayName()
                    : "no name";
            writer.write(StringEscapeUtils.escapeHtml(colName));
    
            writer.write("</title></head>\n");
            writer.write("<body>\n");
            writer.write("<h1>");
    
            writer.write(StringEscapeUtils.escapeHtml(colName));
    
            writer.write("</h1>\n");
    
            WebDavResource parent = getParent();
            if (parent.exists()) {
                writer.write("Parent: <a href=\"");
                writer.write(parent.getResourceLocator().getHref(true));
                writer.write("\">");
                if (parent.getDisplayName() != null) {
                    writer.write(StringEscapeUtils.escapeHtml(parent
                            .getDisplayName()));
                } else {
                    writer.write("no name");
                }
                writer.write("</a></li>\n");
            }
    
            writer.write("<h2>Members</h2>\n");
            writer.write("<ul>\n");
            for (DavResourceIterator i = getMembers(); i.hasNext();) {
                WebDavResource child = (WebDavResource) i.nextResource();
                writer.write("<li><a href=\"");
                writer.write(child.getResourceLocator().getHref(
                        child.isCollection()));
                writer.write("\">");
                if (child.getDisplayName() != null) {
                    writer.write(StringEscapeUtils.escapeHtml(child
                            .getDisplayName()));
                } else {
                    writer.write("no name");
                }
                writer.write("</a></li>\n");
            }
            writer.write("</ul>\n");
    
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
                        LOG.warn("Error serializing value for property "
                                + prop.getName());
                    }
                }
                if (text == null) {
                    text = prop.getValueText();
                }
                if (text == null) {
                    text = "-- no value --";
                }
                writer.write("<dt>");
                writer.write(StringEscapeUtils
                        .escapeHtml(prop.getName().toString()));
                writer.write("</dt><dd>");
                writer.write(StringEscapeUtils.escapeHtml(text));
                writer.write("</dd>\n");
            }
            writer.write("</dl>\n");
    
            User user = getSecurityManager().getSecurityContext().getUser();
            if (user != null) {
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
            }
    
            writer.write("</body>");
            writer.write("</html>\n");
        }finally{
            writer.close();
        }
    }

    @Override
    protected void updateItem() throws CosmoDavException {
        try {
            getContentService().updateCollection((HibCollectionItem) getItem());
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

}