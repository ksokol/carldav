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

import carldav.entity.Item;
import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import org.apache.abdera.i18n.text.UrlEncoding;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ETagUtil;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.util.PathUtil;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import static carldav.CarldavConstants.DISPLAY_NAME;
import static carldav.CarldavConstants.GET_CONTENT_LENGTH;
import static carldav.CarldavConstants.GET_CONTENT_TYPE;
import static carldav.CarldavConstants.GET_ETAG;
import static carldav.CarldavConstants.GET_LAST_MODIFIED;
import static carldav.CarldavConstants.IS_COLLECTION;
import static carldav.CarldavConstants.RESOURCE_TYPE;

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
 * @see Item
 */
public abstract class DavItemResourceBase extends DavResourceBase implements WebDavResource {

    private Item item;
    private DavCollection parent;

    public DavItemResourceBase(Item item, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);

        registerLiveProperty(GET_LAST_MODIFIED);
        registerLiveProperty(GET_ETAG);
        registerLiveProperty(DISPLAY_NAME);
        registerLiveProperty(IS_COLLECTION);
        registerLiveProperty(RESOURCE_TYPE);
        registerLiveProperty(GET_CONTENT_LENGTH);
        registerLiveProperty(GET_CONTENT_TYPE);

        this.item = item;
    }

    public boolean exists() {
        return item != null && item.getId() != null;
    }

    public String getDisplayName() {
        return item.getDisplayName();
    }

    public String getETag() {
        return ETagUtil.createETagEscaped(getItem().getId(), getItem().getModifiedDate());
    }

    @Override
    public String getName() {
        return item.getName();
    }

    public long getModificationTime() {
        if (getItem().getModifiedDate() == null)
            return new Date().getTime();
        return getItem().getModifiedDate().getTime();
    }

    public DavCollection getParent() throws CosmoDavException {
        if (parent == null) {
            DavResourceLocator parentLocator = getResourceLocator().getParentLocator();
            parent = (DavCollection) getResourceFactory().resolve(parentLocator);
        }
        return parent;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) throws CosmoDavException {
        this.item = item;
        loadProperties();
    }

    protected CalendarQueryProcessor getCalendarQueryProcesor() {
        return getResourceFactory().getCalendarQueryProcessor();
    }

    protected void populateItem(DavInputContext inputContext) throws CosmoDavException {
        if (item.getId() == null) {
            try {
                item.setName(UrlEncoding.decode(PathUtil.getBasename(getResourcePath()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new CosmoDavException(e);
            }
        }

        Item hibICalendarItem = item;
        hibICalendarItem.setClientCreationDate(Calendar.getInstance().getTime());
        hibICalendarItem.setClientModifiedDate(hibICalendarItem.getClientCreationDate());
    }

    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new LastModified(item.getModifiedDate()));
        properties.add(new Etag(getETag()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
    }
}