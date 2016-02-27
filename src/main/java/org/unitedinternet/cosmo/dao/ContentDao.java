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

import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;

/**
 * Interface for DAO that provides base operations for content items.
 * 
 * A content item is either a piece of content (or file) or a collection
 * containing content items or other collection items.
 * 
 */
public interface ContentDao extends ItemDao {

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    public HibCollectionItem createCollection(HibCollectionItem parent,
            HibCollectionItem collection);

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
    public HibItem createContent(HibCollectionItem parent, HibItem content);

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     */
    public HibItem updateContent(HibItem content);

   
    /**
     * Remove content item
     * 
     * @param content
     *            content item to remove
     */
    public void removeContent(HibItem content);
    
    /**
     * Remove all content owned by a user
     * 
     * @param user
     *            user to remove content for
     */
    public void removeUserContent(User user);

    /**
     * Remove collection item
     * 
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(HibCollectionItem collection);
    
    /**
     * Update timestamp on collection.
     * @param collection collection to update
     * @return updated collection
     */
    public HibCollectionItem updateCollectionTimestamp(HibCollectionItem collection);
}
