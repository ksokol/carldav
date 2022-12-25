package org.unitedinternet.cosmo.dav;

import carldav.card.CardQueryProcessor;
import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.impl.DavCard;
import org.unitedinternet.cosmo.dav.impl.DavCardCollection;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.DavHomeCollection;
import org.unitedinternet.cosmo.dav.impl.DavUserPrincipal;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.util.UriTemplate;

import jakarta.servlet.http.HttpServletRequest;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.CALENDAR;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.CONTACTS;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.HOME_COLLECTION;

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
                                 CardQueryProcessor cardQueryProcessor
  ) {
    this.contentService = contentService;
    this.itemRepository = itemRepository;
    this.collectionRepository = collectionRepository;
    this.securityManager = securityManager;
    this.calendarQueryProcessor = calendarQueryProcessor;
    this.cardQueryProcessor = cardQueryProcessor;
  }

  public WebDavResource resolve(DavResourceLocator locator, HttpServletRequest request) {
    var resource = resolve(locator);
    if (resource != null) {
      return resource;
    }

    if (request.getMethod().equals("PUT")) {
      // will be replaced by the provider if a different resource
      // type is required
      var parent = resolve(locator.getParentLocator());
      //TODO
      if (parent != null && CALENDAR.equals(parent.getName())) {
        var item = new Item();
        item.setType(Item.Type.VEVENT.toString());
        return new DavCalendarResource(item, locator, this);
      }
      //TODO
      if (parent != null && CONTACTS.equals(parent.getName())) {
        return new DavCard(locator, this);
      }
    }

    if (request.getMethod().equals("DELETE")) {
      return new DavCollectionBase(locator, this);
    }

    throw new NotFoundException();
  }

  public WebDavResource resolve(DavResourceLocator locator) throws CosmoDavException {
    var uri = locator.getPath();

    var match = TEMPLATE_USER.match(uri);
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

  public WebDavResource createResource(DavResourceLocator locator, Item item) throws CosmoDavException {
    Assert.notNull(item, "item cannot be null");

    //TODO
    if (item.getName().endsWith(".vcf")) {
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
    var itemUid = locator.itemUid();
    var collectionName = locator.collection();

    if (collectionName != null && itemUid != null) {
      var userItem = itemRepository.findByOwnerEmailAndCollectionNameAndName(securityManager.getUsername(), collectionName, locator.itemUid());
      if (userItem == null) {
        return null;
      }
      return createResource(locator, userItem);
    }

    if (collectionName != null) {
      var userCollection = collectionRepository.findByOwnerEmailAndName(securityManager.getUsername(), collectionName);
      if (userCollection == null) {
        return null;
      }
      return createCollectionResource(locator, userCollection);
    }

    var homeCollection = collectionRepository.findHomeCollectionByOwnerEmail(securityManager.getUsername());
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
