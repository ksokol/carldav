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
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.List;

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
    HibItem findItemByPath(String path);

    /**
     * Remove an item from a collection.  The item will be removed if
     * it belongs to no more collections.
     * @param hibItem item to remove from collection
     * @param collection item to remove item from
     */
    void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection);

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
    HibItem createContent(HibCollectionItem parent,
                          HibItem content);

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    HibItem updateContent(HibItem content);

    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    List<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem);

    HibHomeCollectionItem createRootItem(User user);
}
