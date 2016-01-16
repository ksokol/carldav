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
package org.unitedinternet.cosmo.dav;

import carldav.card.CardQueryProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCardCollection;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.unitedinternet.cosmo.dav.impl.DavFile;
import org.unitedinternet.cosmo.dav.impl.DavHomeCollection;
import org.unitedinternet.cosmo.dav.impl.DavJournal;
import org.unitedinternet.cosmo.dav.impl.DavTask;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.CardCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibFileItem;
import org.unitedinternet.cosmo.model.hibernate.HibJournalStamp;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;

public class StandardResourceFactory implements DavResourceFactory, ExtendedDavConstants{
    private static final Log LOG =  LogFactory.getLog(StandardResourceFactory.class);

    private ContentService contentService;
    private CosmoSecurityManager securityManager;
    private EntityFactory entityFactory;
    private CalendarQueryProcessor calendarQueryProcessor;
    private CardQueryProcessor cardQueryProcessor;

    public StandardResourceFactory(ContentService contentService,
                                   CosmoSecurityManager securityManager,
                                   EntityFactory entityFactory,
                                   CalendarQueryProcessor calendarQueryProcessor,
                                   CardQueryProcessor cardQueryProcessor) {
        this.contentService = contentService;
        this.securityManager = securityManager;
        this.entityFactory = entityFactory;
        this.calendarQueryProcessor = calendarQueryProcessor;
        this.cardQueryProcessor = cardQueryProcessor;
    }

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exist and the request method
     * indicates that one is to be created, returns a resource backed by a 
     * newly-instantiated item that has not been persisted or assigned a UID.
     * Otherwise, if the resource does not exists, then a
     * {@link NotFoundException} is thrown.
     * </p>
     * <p>
     * The type of resource to create is chosen as such:
     * <ul>
     * <li><code>PUT</code>, <code>COPY</code>, <code>MOVE</code></li>:
     * {@link DavFile}</li>
     * </ul>
     */
    public WebDavResource resolve(DavResourceLocator locator,
                               DavRequest request)
        throws CosmoDavException {
        WebDavResource resource = resolve(locator);
        if (resource != null) {
            return resource;
        }

        if (request.getMethod().equals("PUT")) {
            // will be replaced by the provider if a different resource
            // type is required
            WebDavResource parent = resolve(locator.getParentLocator());
            if (parent instanceof DavCalendarCollection) {
                return new DavEvent(locator, this, entityFactory);
            }
            return new DavFile(locator, this, entityFactory);
        }
        
        // handle OPTIONS for non-existent resource
        if(request.getMethod().equals("OPTIONS")) { 
            // ensure parent exists first
            WebDavResource parent = resolve(locator.getParentLocator());
            if(parent!=null && parent.exists()) {
                if(parent instanceof DavCalendarCollection) {
                    return new DavEvent(locator, this, entityFactory);
                }
                else {
                    return new DavCollectionBase(locator, this, entityFactory);
                }
            }
        }
    
        throw new NotFoundException();
    }

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exists, returns <code>null</code>.
     * </p>
     */
    public WebDavResource resolve(DavResourceLocator locator)
        throws CosmoDavException {
        String uri = locator.getPath();
        if (LOG.isDebugEnabled()) {
            LOG.debug("resolving URI " + uri);
        }

        return createUnknownResource(locator, uri);
    }

    /**
     * <p>
     * Instantiates a <code>WebDavResource</code> representing the
     * <code>Item</code> located by the given <code>DavResourceLocator</code>.
     * </p>
     */
    public WebDavResource createResource(DavResourceLocator locator, Item item)  throws CosmoDavException {
        Assert.notNull(item, "item cannot be null");

        if (item instanceof HomeCollectionItem) {
            return new DavHomeCollection((HomeCollectionItem) item, locator,
                                         this, entityFactory);
        }

        if (item instanceof CollectionItem) {
            if (item.getStamp(HibCalendarCollectionStamp.class) != null) {
                return new DavCalendarCollection((CollectionItem) item,
                                                 locator, this,entityFactory);
            } else if(item.getStamp(CardCollectionStamp.class) != null) {
                return new DavCardCollection((CollectionItem) item, locator, this, entityFactory, getCardQueryProcessor());
            } else {
                return new DavCollectionBase((CollectionItem) item, locator, this, entityFactory);
            }
        }

        if (item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            // don't expose modifications
            if(note.getModifies()!=null) {
                return null;
            }
            else if (item.getStamp(EventStamp.class) instanceof HibEventStamp) {
                return new DavEvent(note, locator, this, entityFactory);
            }
            else if (item.getStamp(EventStamp.class) instanceof HibJournalStamp) {
                return new DavJournal(note, locator, this, entityFactory);
            }
            else {
                return new DavTask(note, locator, this, entityFactory);
            }
        }

        return new DavFile((HibFileItem) item, locator, this, entityFactory);
    }

    protected WebDavResource createUnknownResource(DavResourceLocator locator,
                                                String uri)
        throws CosmoDavException {
        Item item = contentService.findItemByPath(uri);
        return item != null ? createResource(locator, item) : null;
    }

    public ContentService getContentService() {
        return contentService;
    }
    
    public CalendarQueryProcessor getCalendarQueryProcessor() {
        return calendarQueryProcessor;
    }

    public CardQueryProcessor getCardQueryProcessor() {
        return cardQueryProcessor;
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }
}
