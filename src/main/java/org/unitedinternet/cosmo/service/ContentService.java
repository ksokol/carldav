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

import carldav.entity.CollectionItem;
import carldav.entity.HibItem;
import carldav.entity.User;

/**
 * Interface for services that manage access to user content.
 */
public interface ContentService {

    /**
     * Remove an item from a collection.  The item will be removed if
     * it belongs to no more collections.
     * @param hibItem item to remove from collection
     * @param collection item to remove item from
     */
    void removeItemFromCollection(HibItem hibItem, CollectionItem collection);

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    CollectionItem createCollection(CollectionItem parent,
                                    CollectionItem collection);

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    void removeCollection(CollectionItem collection);

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
    HibItem createContent(CollectionItem parent,
                          HibItem content);

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    HibItem updateContent(HibItem content);

    CollectionItem createRootItem(User user);
}
