package org.unitedinternet.cosmo.service.impl;

import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.entity.User;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.service.ContentService;

import java.util.Date;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.HOME_COLLECTION;

public class StandardContentService implements ContentService {

  private final ItemRepository itemRepository;
  private final CollectionRepository collectionRepository;

  public StandardContentService(final ItemRepository itemRepository, CollectionRepository collectionRepository) {
    Assert.notNull(itemRepository, "itemRepository is null");
    Assert.notNull(collectionRepository, "collectionRepository is null");
    this.itemRepository = itemRepository;
    this.collectionRepository = collectionRepository;
  }

  public void removeItemFromCollection(Item item, CollectionItem collection) {
    itemRepository.delete(item);
    collection.setModifiedDate(new Date());
  }

  public CollectionItem createCollection(CollectionItem parent, CollectionItem collection) {
    collection.setParentId(parent.getId());
    collectionRepository.save(collection);
    return collection;
  }

  public void removeCollection(CollectionItem collection) {
    // prevent HomeCollection from being removed (should only be removed when user is removed)
    //TODO
    if (HOME_COLLECTION.equals(collection.getDisplayName())) {
      throw new IllegalArgumentException("cannot remove home collection");
    }
    itemRepository.deleteAllByParentId(collection.getId());
    collectionRepository.delete(collection);
  }

  public Item createContent(CollectionItem parent, Item content) {
    content.setCollectionid(parent.getId());
    var collectionItem = collectionRepository.findById(content.getCollectionid()).orElseThrow();
    collectionItem.setModifiedDate(new Date());
    collectionRepository.save(collectionItem);
    itemRepository.save(content);
    return content;
  }

  public Item updateContent(Item content) {
    final Date date = new Date();
    content.setModifiedDate(date);
    var collectionItem = collectionRepository.findById(content.getCollectionid()).orElseThrow();
    collectionItem.setModifiedDate(new Date());
    collectionRepository.save(collectionItem);
    itemRepository.save(content);
    return content;
  }

  @Override
  public CollectionItem createRootItem(User user) {
    CollectionItem newItem = new CollectionItem();

    newItem.setOwnerId(user.getId());
    //TODO
    newItem.setName(user.getEmail());
    newItem.setDisplayName("homeCollection");
    collectionRepository.save(newItem);
    return newItem;
  }
}
