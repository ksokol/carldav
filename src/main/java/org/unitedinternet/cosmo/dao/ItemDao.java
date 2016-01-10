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
package org.unitedinternet.cosmo.dao;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;

import java.util.Set;

/**
 * Interface for DAO that provides base functionality for items stored in the
 * server.
 *
 */
public interface ItemDao extends Dao {

    /**
     * Find an item with the specified uid. The return type will be one of
     * ContentItem, CollectionItem, NoteItem.
     *
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    Item findItemByUid(String uid);

    /**
     * Find an item with the specified path. The return type will be one of
     * ContentItem, NoteItem, CollectionItem.
     *
     * @param path
     *            path of item to find
     * @return item represented by path
     */
    Item findItemByPath(String path);
    
    /**
     * Find an item with the specified path, relative to a parent collection.
     * The return type will be one of
     * ContentItem, NoteItem, CollectionItem.
     *
     * @param path
     *            path of item to find
     * @param parentUid
     *            uid of parent that path is relative to
     * @return item represented by path
     */
    Item findItemByPath(String path, String parentUid);
    
    /**
     * Find the parent item of the item with the specified path. 
     * The return type will be of type CollectionItem.
     *
     * @param path
     *            path of item
     * @return parent item of item represented by path
     */
    Item findItemParentByPath(String path);

    /**
     * Get the root item for a user
     *
     * @param user The user for get the root item.
     * @param forceReload cleans the session before loading the item
     * @return home collection item.
     */
    HomeCollectionItem getRootItem(User user, boolean forceReload);

    /**
     * Get the root item for a user
     *
     * @param user The user for get the root item.
     * @return home collection item.
     */
    HomeCollectionItem getRootItem(User user);

    /**
     * Create the root item for a user.
     * @param user The user for create the root item.
     * @return Home collection item.
     */
    HomeCollectionItem createRootItem(User user);

    /**
     * Remove an item.
     *
     * @param item
     *            item to remove
     */
    void removeItem(Item item);

    /**
     * Remove an item give the item's path
     * @param path path of item to remove
     */
    void removeItemByPath(String path);

    /**
     * Remove an item given the item's uid
     * @param uid the uid of the item to remove
     */
    void removeItemByUid(String uid);

    /**
     * Adds item to a collection.
     *
     * @param item the item
     * @param collection the collection to add to
     */
    void addItemToCollection(Item item, CollectionItem collection);

    /**
     * Remove item from a collection.
     *
     * @param item the item
     * @param collection the collection to remove from
     */
    void removeItemFromCollection(Item item, CollectionItem collection);

    /**
     * Initialize item, ensuring any proxied associations will be loaded.
     * @param item The item initialized.
     */
    void initializeItem(Item item);

    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param collectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    Set<CollectionItem> findCollectionItems(CollectionItem collectionItem);
}
