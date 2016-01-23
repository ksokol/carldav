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
import org.apache.abdera.i18n.text.UrlEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.Uuid;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.util.PathUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Base class for dav resources that are backed by collections or items.
 * </p>
 * <p>
 * This class defines the following live properties:
 * </p>
 * <ul>
 * <li><code>DAV:getcreationdate</code> (protected)</li>
 * <li><code>DAV:displayname</code> (protected)</li>
 * <li><code>DAV:iscollection</code> (protected)</li>
 * <li><code>DAV:resourcetype</code> (protected)</li>
 * <li><code>DAV:owner</code> (protected)</li>
 * <li><code>DAV:principal-collection-set</code> (protected)</li>
 * <li><code>ticket:ticketdiscovery</code> (protected)</li>
 * <li><code>cosmo:uuid</code> (protected)</li>
 * </ul>
 * <p>
 * This class does not define any resource types.
 * </p>
 * @see HibItem
 */
public abstract class DavItemResourceBase extends DavResourceBase implements DavItemResource {

    private static final Log log = LogFactory.getLog(DavItemResourceBase.class);

    private HibItem hibItem;
    private DavCollection parent;
    private IdGenerator idGenerator;

    public DavItemResourceBase(HibItem hibItem, DavResourceLocator locator,
            DavResourceFactory factory, IdGenerator idGenerator)
            throws CosmoDavException {
        super(locator, factory);

        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(UUID);

        this.hibItem = hibItem;
        this.idGenerator = idGenerator;
    }

    // WebDavResource methods

    public boolean exists() {
        return hibItem != null && hibItem.getUid() != null;
    }

    public String getDisplayName() {
        return hibItem.getDisplayName();
    }

    public String getETag() {
        if (getItem() == null)
            return null;
        // an item that is about to be created does not yet have an etag
        if (StringUtils.isBlank(getItem().getEntityTag()))
            return null;
        return "\"" + getItem().getEntityTag() + "\"";
    }

    public long getModificationTime() {
        if (getItem() == null)
            return -1;
        if (getItem().getModifiedDate() == null)
            return new Date().getTime();
        return getItem().getModifiedDate().getTime();
    }

    public void setProperty(
            org.apache.jackrabbit.webdav.property.DavProperty<?> property)
            throws org.apache.jackrabbit.webdav.DavException {
        super.setProperty(property);
        updateItem();
    }

    public void removeProperty(DavPropertyName propertyName)
            throws org.apache.jackrabbit.webdav.DavException {
        super.removeProperty(propertyName);

        updateItem();
    }

    public WebDavResource getCollection() {
        try {
            return getParent();
        } catch (CosmoDavException e) {
            throw new RuntimeException(e);
        }
    }

    // WebDavResource methods

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
                parent = new DavCollectionBase(parentLocator,
                        getResourceFactory(), idGenerator);
        }

        return parent;
    }

    public MultiStatusResponse updateProperties(DavPropertySet setProperties,
            DavPropertyNameSet removePropertyNames) throws CosmoDavException {
        MultiStatusResponse msr = super.updateProperties(setProperties,
                removePropertyNames);
        if (hasNonOK(msr)) {
            return msr;
        }

        updateItem();

        return msr;
    }

    // DavItemResource methods

    public HibItem getItem() {
        return hibItem;
    }

    public void setItem(HibItem hibItem) throws CosmoDavException {
        this.hibItem = hibItem;
        loadProperties();
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    // our methods

    protected ContentService getContentService() {
        return getResourceFactory().getContentService();
    }

    protected CalendarQueryProcessor getCalendarQueryProcesor() {
        return getResourceFactory().getCalendarQueryProcessor();
    }

    /**
     * Sets the properties of the item backing this resource from the given
     * input context.
     */
    protected void populateItem(InputContext inputContext)
            throws CosmoDavException {
        if (log.isDebugEnabled()) {
            log.debug("populating item for " + getResourcePath());
        }

        if (hibItem.getUid() == null) {
            try {
                hibItem.setName(UrlEncoding.decode(PathUtil.getBasename(getResourcePath()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new CosmoDavException(e);
            }
            if (hibItem.getDisplayName() == null){
                hibItem.setDisplayName(hibItem.getName());
            }
        }

        // if we don't know specifically who the user is, then the
        // owner of the resource becomes the person who issued the
        // ticket

        // Only initialize owner once
        if (hibItem.getOwner() == null) {
            User owner = getSecurityManager().getSecurityContext().getUser();
            hibItem.setOwner(owner);
        }

        if (hibItem.getUid() == null) {
            hibItem.setClientCreationDate(Calendar.getInstance().getTime());
            hibItem.setClientModifiedDate(hibItem.getClientCreationDate());
        }
    }

    protected void loadLiveProperties(DavPropertySet properties) {
        if (hibItem == null) {
            return;
        }

        properties.add(new LastModified(hibItem.getModifiedDate()));
        properties.add(new Etag(getETag()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        properties.add(new Uuid(hibItem.getUid()));
    }

    protected void setLiveProperty(WebDavProperty property, boolean create)
            throws CosmoDavException {
        if (hibItem == null) {
            return;
        }

        DavPropertyName name = property.getName();
        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property " + name
                    + " requires a value");
        }

        if (name.equals(DavPropertyName.GETLASTMODIFIED)
                || name.equals(DavPropertyName.GETETAG)
                || name.equals(DavPropertyName.RESOURCETYPE)
                || name.equals(DavPropertyName.ISCOLLECTION)
                || name.equals(UUID)) {
            throw new ProtectedPropertyModificationException(name);
        }

        if (name.equals(DavPropertyName.DISPLAYNAME)) {
            hibItem.setDisplayName(property.getValueText());
        }
    }

    protected void removeLiveProperty(DavPropertyName name)
            throws CosmoDavException {
        if (hibItem == null) {
            return;
        }

        if (name.equals(DavPropertyName.GETLASTMODIFIED)
                || name.equals(DavPropertyName.GETETAG)
                || name.equals(DavPropertyName.DISPLAYNAME)
                || name.equals(DavPropertyName.RESOURCETYPE)
                || name.equals(DavPropertyName.ISCOLLECTION)
                || name.equals(UUID)) {
            throw new ProtectedPropertyModificationException(name);
        }

        getProperties().remove(name);
    }

    /**
     * Returns a list of names of <code>Attribute</code>s that should not be
     * exposed through DAV as dead properties.
     */
    protected abstract Set<String> getDeadPropertyFilter();

    protected void setDeadProperty(WebDavProperty property)
            throws CosmoDavException {
        if (log.isDebugEnabled()) {
            log.debug("setting dead property " + property.getName() + " on "
                    + getResourcePath() + " to " + property.getValue());
        }

        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property "
                    + property.getName() + " requires a value");
        }
    }

    abstract protected void updateItem() throws CosmoDavException;

    public static boolean hasNonOK(MultiStatusResponse msr) {
        if (msr == null || msr.getStatus() == null) {
            return false;
        }

        for (Status status : msr.getStatus()) {

            if (status != null) {
                int statusCode = status.getStatusCode();

                if (statusCode != 200) {
                    return true;
                }
            }
        }
        return false;
    }
}