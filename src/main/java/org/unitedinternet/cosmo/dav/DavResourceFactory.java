package org.unitedinternet.cosmo.dav;

import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;

import jakarta.servlet.http.HttpServletRequest;

public interface DavResourceFactory {

  WebDavResource resolve(DavResourceLocator locator, HttpServletRequest request) throws CosmoDavException;

  WebDavResource resolve(DavResourceLocator locator) throws CosmoDavException;

  WebDavResource createResource(DavResourceLocator locator, Item item) throws CosmoDavException;

  WebDavResource createCollectionResource(DavResourceLocator locator, CollectionItem hibItem);

  ContentService getContentService();

  CalendarQueryProcessor getCalendarQueryProcessor();

  CosmoSecurityManager getSecurityManager();

  ItemRepository getItemRepository();

  CollectionRepository getCollectionRepository();
}
