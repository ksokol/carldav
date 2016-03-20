/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.service.impl;

import org.springframework.util.Assert;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import carldav.entity.CollectionItem;
import carldav.entity.HibItem;
import carldav.entity.User;
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

    /**
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param hibItem item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(HibItem hibItem, CollectionItem collection) {
        itemRepository.delete(hibItem);
        collection.setModifiedDate(new Date());
    }

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent, CollectionItem collection) {
        collection.setParent(parent);
        collectionRepository.save(collection);
        return collection;
    }

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection) {
        // prevent HomeCollection from being removed (should only be removed
        // when user is removed)
        //TODO
        if(HOME_COLLECTION.equals(collection.getDisplayName())) {
            throw new IllegalArgumentException("cannot remove home collection");
        }
        collectionRepository.delete(collection);
    }

    /**
     * Create new content item. A content item represents a piece of content or
     * file.
     * 
     * @param parent
     *            parent collection of content. If null, content is assumed to
     *            live in the top-level user collection
     * @param content
     *            content to create
     * @return newly created content
     */
    public HibItem createContent(CollectionItem parent, HibItem content) {
        content.setCollection(parent);
        content.getCollection().setModifiedDate(new Date());
        itemRepository.save(content);
        return content;
    }

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    public HibItem updateContent(HibItem content) {
        final Date date = new Date();
        content.setModifiedDate(date);
        content.getCollection().setModifiedDate(date);
        itemRepository.save(content);
        return content;
    }

    @Override
    public CollectionItem createRootItem(User user) {
        CollectionItem newItem = new CollectionItem();

        newItem.setOwner(user);
        //TODO
        newItem.setName(user.getEmail());
        newItem.setDisplayName("homeCollection");
        collectionRepository.save(newItem);
        return newItem;
    }
}
