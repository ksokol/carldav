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
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.impl.DavCard;
import org.unitedinternet.cosmo.dav.impl.DavCardCollection;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.DavHomeCollection;
import org.unitedinternet.cosmo.dav.impl.DavUserPrincipal;
import org.unitedinternet.cosmo.dav.impl.DavUserPrincipalCollection;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCardCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCardItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventItem;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;
import org.unitedinternet.cosmo.util.UriTemplate;

public class StandardResourceFactory implements DavResourceFactory, ExtendedDavConstants{
    private static final Log LOG =  LogFactory.getLog(StandardResourceFactory.class);

    private ContentService contentService;
    private CosmoSecurityManager securityManager;
    private CalendarQueryProcessor calendarQueryProcessor;
    private CardQueryProcessor cardQueryProcessor;
    private UserService userService;

    public StandardResourceFactory(ContentService contentService,
                                   CosmoSecurityManager securityManager,
                                   CalendarQueryProcessor calendarQueryProcessor,
                                   CardQueryProcessor cardQueryProcessor,
                                   UserService userService) {
        this.contentService = contentService;
        this.securityManager = securityManager;
        this.calendarQueryProcessor = calendarQueryProcessor;
        this.cardQueryProcessor = cardQueryProcessor;
        this.userService = userService;
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
     * {@link DavCard}</li>
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
                return new DavCalendarResource(new HibEventItem(), locator, this);
            }
            if (parent instanceof DavCardCollection) {
                return new DavCard(locator, this);
            }
        }
        
        // handle OPTIONS for non-existent resource
        if(request.getMethod().equals("OPTIONS")) { 
            // ensure parent exists first
            WebDavResource parent = resolve(locator.getParentLocator());
            if(parent!=null && parent.exists()) {
                if(parent instanceof DavCalendarCollection) {
                    return new DavCalendarResource(new HibEventItem(), locator, this);
                }
                else {
                    return new DavCollectionBase(locator, this);
                }
            }
        }

        if(request.getMethod().equals("DELETE")) {
            return new DavCollectionBase(locator, this);
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

        UriTemplate.Match match = TEMPLATE_USER.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator, match);
        }

        match = TEMPLATE_USERS.match(uri);
        if (match != null) {
            return new DavUserPrincipalCollection(locator, this);
        }

        match = TEMPLATE_PRINCIPALS.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator);
        }

        return createUnknownResource(locator, uri);
    }

    /**
     * <p>
     * Instantiates a <code>WebDavResource</code> representing the
     * <code>Item</code> located by the given <code>DavResourceLocator</code>.
     * </p>
     */
    public WebDavResource createResource(DavResourceLocator locator, HibItem hibItem)  throws CosmoDavException {
        Assert.notNull(hibItem, "item cannot be null");

        if (hibItem instanceof HibHomeCollectionItem) {
            return new DavHomeCollection((HibHomeCollectionItem) hibItem, locator, this);
        }

        if (hibItem instanceof HibCollectionItem) {
            if (hibItem instanceof HibCalendarCollectionItem) {
                return new DavCalendarCollection((HibCollectionItem) hibItem, locator, this);
            }
            else if(hibItem instanceof HibCardCollectionItem) {
                return new DavCardCollection((HibCollectionItem) hibItem, locator, this, getCardQueryProcessor());
            }
            else {
                return new DavCollectionBase((HibCollectionItem) hibItem, locator, this);
            }
        }

        if(hibItem instanceof HibCardItem) {
            return new DavCard((HibCardItem) hibItem, locator, this);
        }

        return new DavCalendarResource(hibItem, locator, this);
    }


    protected WebDavResource createUserPrincipalResource(DavResourceLocator locator, UriTemplate.Match match) throws CosmoDavException {
        User user = userService.getUser(match.get("username"));
        return user != null ? new DavUserPrincipal(user, locator, this) : null;
    }

    protected WebDavResource createUserPrincipalResource(DavResourceLocator locator) throws CosmoDavException {
        User user = securityManager.getSecurityContext().getUser();
        return user != null ? new DavUserPrincipal(user, locator, this) : null;
    }

    protected WebDavResource createUnknownResource(DavResourceLocator locator,
                                                String uri)
        throws CosmoDavException {
        HibItem hibItem = contentService.findItemByPath(uri);
        return hibItem != null ? createResource(locator, hibItem) : null;
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
