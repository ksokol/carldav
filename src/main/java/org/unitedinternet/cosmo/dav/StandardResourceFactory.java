package org.unitedinternet.cosmo.dav;

import carldav.card.CardQueryProcessor;
import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.impl.*;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.*;

public class StandardResourceFactory implements DavResourceFactory, ExtendedDavConstants {

    private final ContentService contentService;
    private final ItemRepository itemRepository;
    private final CollectionRepository collectionRepository;
    private final CosmoSecurityManager securityManager;
    private final CalendarQueryProcessor calendarQueryProcessor;
    private final CardQueryProcessor cardQueryProcessor;

    public StandardResourceFactory(ContentService contentService,
                                   ItemRepository itemRepository,
                                   CollectionRepository collectionRepository,
                                   CosmoSecurityManager securityManager,
                                   CalendarQueryProcessor calendarQueryProcessor,
                                   CardQueryProcessor cardQueryProcessor) {
        this.contentService = contentService;
        this.itemRepository = itemRepository;
        this.collectionRepository = collectionRepository;
        this.securityManager = securityManager;
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
     * {@link DavCard}</li>
     * </ul>
     */
    public WebDavResource resolve(DavResourceLocator locator, HttpServletRequest request) {
        WebDavResource resource = resolve(locator);
        if (resource != null) {
            return resource;
        }

        if (request.getMethod().equals("PUT")) {
            // will be replaced by the provider if a different resource
            // type is required
            WebDavResource parent = resolve(locator.getParentLocator());
            //TODO
            if(parent != null && CALENDAR.equals(parent.getName())) {
                final Item item = new Item();
                item.setType(Item.Type.VEVENT);
                return new DavCalendarResource(item, locator, this);
            }
            //TODO
            if(parent != null && CONTACTS.equals(parent.getName())) {
                return new DavCard(locator, this);
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

        UriTemplate.Match match = TEMPLATE_USER.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator, match);
        }

        match = TEMPLATE_PRINCIPALS.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator);
        }

        match = TEMPLATE_USER.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator, match);
        }

        return createUnknownResource(locator);
    }

    public WebDavResource createResource(DavResourceLocator locator, Item item)  throws CosmoDavException {
        Assert.notNull(item, "item cannot be null");

        //TODO
        if(item.getName().endsWith(".vcf")) {
            return new DavCard(item, locator, this);
        }

        return new DavCalendarResource(item, locator, this);
    }

    public WebDavResource createCollectionResource(DavResourceLocator locator, CollectionItem hibItem) {
        Assert.notNull(hibItem, "item cannot be null");

        //TODO
        if (HOME_COLLECTION.equals(hibItem.getDisplayName())) {
            return new DavHomeCollection(hibItem, locator, this);
        }
        //TODO
        if (CALENDAR.equals(hibItem.getName())) {
            return new DavCalendarCollection(hibItem, locator, this);
        }
        //TODO
        if (CONTACTS.equals(hibItem.getName())) {
            return new DavCardCollection(hibItem, locator, this, getCardQueryProcessor());
        }

        return new DavCollectionBase(hibItem, locator, this);
    }

    protected WebDavResource createUserPrincipalResource(DavResourceLocator locator, UriTemplate.Match match) throws CosmoDavException {
        return new DavUserPrincipal(match.get("username"), locator, this);
    }

    protected WebDavResource createUserPrincipalResource(DavResourceLocator locator) throws CosmoDavException {
        return new DavUserPrincipal(securityManager.getUsername(), locator, this);
    }

    private WebDavResource createUnknownResource(DavResourceLocator locator) {
        final String itemUid = locator.itemUid();
        final String collectionName = locator.collection();

        if(collectionName != null && itemUid != null) {
            final Item userItem = itemRepository.findByOwnerEmailAndCollectionNameAndName(securityManager.getUsername(), collectionName, locator.itemUid());
            if(userItem == null) {
                return null;
            }
            return createResource(locator, userItem);
        }

        if(collectionName != null) {
            final CollectionItem userCollection = collectionRepository.findByOwnerEmailAndName(securityManager.getUsername(), collectionName);
            if(userCollection == null) {
                return null;
            }
            return createCollectionResource(locator, userCollection);
        }

        final CollectionItem homeCollection = collectionRepository.findHomeCollectionByOwnerEmail(securityManager.getUsername());
        return createCollectionResource(locator, homeCollection);
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

    @Override
    public ItemRepository getItemRepository() {
        return itemRepository;
    }

    public CollectionRepository getCollectionRepository() {
        return collectionRepository;
    }
}
