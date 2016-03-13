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

import carldav.repository.CollectionDao;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.service.ContentService;

import java.util.Date;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.HOME_COLLECTION;

public class StandardContentService implements ContentService {

    private final ItemDao itemDao;
    private final CollectionDao collectionDao;

    public StandardContentService(final ItemDao itemDao, CollectionDao collectionDao) {
        Assert.notNull(itemDao, "itemDao is null");
        Assert.notNull(collectionDao, "collectionDao is null");
        this.itemDao = itemDao;
        this.collectionDao = collectionDao;
    }

    /**
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param hibItem item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection) {
        itemDao.remove(hibItem);
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
    public HibCollectionItem createCollection(HibCollectionItem parent, HibCollectionItem collection) {
        collection.setParent(parent);
        collectionDao.save(collection);
        return collection;
    }

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(HibCollectionItem collection) {
        // prevent HomeCollection from being removed (should only be removed
        // when user is removed)
        //TODO
        if(HOME_COLLECTION.equals(collection.getDisplayName())) {
            throw new IllegalArgumentException("cannot remove home collection");
        }
        collectionDao.remove(collection);
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
    public HibItem createContent(HibCollectionItem parent, HibItem content) {
        content.setCollection(parent);
        content.getCollection().setModifiedDate(new Date());
        itemDao.save(content);
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
        itemDao.save(content);
        return content;
    }

    @Override
    public HibCollectionItem createRootItem(User user) {
        HibCollectionItem newItem = new HibCollectionItem();

        newItem.setOwner(user);
        //TODO
        newItem.setName(user.getEmail());
        newItem.setDisplayName("homeCollection");
        collectionDao.save(newItem);
        return newItem;
    }
}
