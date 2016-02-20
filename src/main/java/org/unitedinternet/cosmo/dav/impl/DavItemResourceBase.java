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

import org.apache.abdera.i18n.text.UrlEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.util.PathUtil;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
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

    public DavItemResourceBase(HibItem hibItem, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);

        registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
        registerLiveProperty(DavPropertyName.GETETAG);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);

        this.hibItem = hibItem;
    }

    // WebDavResource methods

    public boolean exists() {
        return hibItem != null && hibItem.getId() != null;
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
                parent = new DavCollectionBase(parentLocator, getResourceFactory());
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

        if (hibItem.getId() == null) {
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

        if (hibItem.getId() == null && hibItem instanceof HibICalendarItem) {
            HibICalendarItem hibICalendarItem = (HibICalendarItem) hibItem;
            hibICalendarItem.setClientCreationDate(Calendar.getInstance().getTime());
            hibICalendarItem.setClientModifiedDate(hibICalendarItem.getClientCreationDate());
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
    }

    /**
     * Returns a list of names of <code>Attribute</code>s that should not be
     * exposed through DAV as dead properties.
     */
    protected abstract Set<String> getDeadPropertyFilter();

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