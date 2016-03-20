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

import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.CustomDavPropertySet;
import org.apache.abdera.i18n.text.UrlEncoding;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.*;
import org.unitedinternet.cosmo.dav.property.*;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.util.PathUtil;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import static carldav.CarldavConstants.*;

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

    private HibItem hibItem;
    private DavCollection parent;

    public DavItemResourceBase(HibItem hibItem, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(locator, factory);

        registerLiveProperty(GET_LAST_MODIFIED);
        registerLiveProperty(GET_ETAG);
        registerLiveProperty(DISPLAY_NAME);
        registerLiveProperty(IS_COLLECTION);
        registerLiveProperty(RESOURCE_TYPE);
        registerLiveProperty(GET_CONTENT_LENGTH);
        registerLiveProperty(GET_CONTENT_TYPE);

        this.hibItem = hibItem;
    }

    public boolean exists() {
        return hibItem != null && hibItem.getId() != null;
    }

    public String getDisplayName() {
        return hibItem.getDisplayName();
    }

    public String getETag() {
        return ETagUtil.createETagEscaped(getItem());
    }

    @Override
    public String getName() {
        return hibItem.getName();
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

    public HibItem getItem() {
        return hibItem;
    }

    public void setItem(HibItem hibItem) throws CosmoDavException {
        this.hibItem = hibItem;
        loadProperties();
    }

    protected CalendarQueryProcessor getCalendarQueryProcesor() {
        return getResourceFactory().getCalendarQueryProcessor();
    }

    protected void populateItem(DavInputContext inputContext) throws CosmoDavException {
        if (hibItem.getId() == null) {
            try {
                hibItem.setName(UrlEncoding.decode(PathUtil.getBasename(getResourcePath()), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new CosmoDavException(e);
            }
        }

        // Only initialize owner once
        if (hibItem.getOwner() == null) {
            User owner = getSecurityManager().getSecurityContext().getUser();
            hibItem.setOwner(owner);
        }

        HibItem hibICalendarItem = hibItem;
        hibICalendarItem.setClientCreationDate(Calendar.getInstance().getTime());
        hibICalendarItem.setClientModifiedDate(hibICalendarItem.getClientCreationDate());
    }

    protected void loadLiveProperties(CustomDavPropertySet properties) {
        properties.add(new LastModified(hibItem.getModifiedDate()));
        properties.add(new Etag(getETag()));
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
    }
}