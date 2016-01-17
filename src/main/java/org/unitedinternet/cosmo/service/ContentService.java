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
package org.unitedinternet.cosmo.service;

import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;

import java.util.Set;

/**
 * Interface for services that manage access to user content.
 */
public interface ContentService {

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     * @param path The given path.
     * @return The content item.
     *
     */
    Item findItemByPath(String path);

    /**
     * Remove an item from a collection.  The item will be removed if
     * it belongs to no more collections.
     * @param item item to remove from collection
     * @param collection item to remove item from
     */
    void removeItemFromCollection(Item item, HibCollectionItem collection);

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    HibCollectionItem createCollection(HibCollectionItem parent,
                                    HibCollectionItem collection);

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    void removeCollection(HibCollectionItem collection);

    /**
     * Update collection item
     * 
     * @param collection
     *            collection item to update
     * @return updated collection
     */
    HibCollectionItem updateCollection(HibCollectionItem collection);
    
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
    ContentItem createContent(HibCollectionItem parent,
                              ContentItem content);

    /**
     * Create new content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to create
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    void createContentItems(HibCollectionItem parent,
                            Set<ContentItem> contentItems);

    /**
     * Update content items.  This includes creating new items, removing
     * existing items, and updating existing items.  ContentItem deletion is
     * represented by setting ContentItem.isActive to false.  ContentItem deletion
     * removes item from system, not just from the parent collections.
     * ContentItem creation adds the item to the specified parent collections.
     * 
     * @param parents
     *            parents that new content items will be added to.
     * @param contentItems to update
     * @throws org.osaf.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    void updateContentItems(Set<HibCollectionItem> parents, Set<ContentItem> contentItems);
    
    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    ContentItem updateContent(ContentItem content);


    /**
     * Remove content item
     *
     * @param content
     *            content item to remove
     */
    void removeContent(ContentItem content);


    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    Set<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem);
}
