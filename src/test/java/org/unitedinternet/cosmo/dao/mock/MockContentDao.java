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
package org.unitedinternet.cosmo.dao.mock;

import org.springframework.dao.ConcurrencyFailureException;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.UidInUseException;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;

import java.util.Date;
import java.util.Set;

/**
 * Mock implementation of <code>ContentDao</code> useful for testing.
 *
 * @see ContentDao
 * @see ContentItem
 * @see CollectionItem
 */
@SuppressWarnings("unchecked")
public class MockContentDao extends MockItemDao implements ContentDao {

    public static final boolean THROW_CONCURRENT_EXCEPTION = false;
    
    /**
     * Constructor.
     * @param storage Mock dao storage.
     */
    public MockContentDao(MockDaoStorage storage) {
        super(storage);
    }

    // ContentDao methods

    /**
     * Create a new collection.
     *
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }
        if (getStorage().getItemByUid(collection.getUid()) != null) {
            throw new UidInUseException(collection.getUid(), collection.getUid());
        }

        ((HibItem) collection).addParent(parent);

        getStorage().storeItem(collection);

        return collection;
    }

    /**
     * Update an existing collection.
     *
     * @param collection
     *            collection to update
     * @return updated collection
     */
    public CollectionItem updateCollection(CollectionItem collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }

        getStorage().updateItem(collection);

        return collection;
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
    public ContentItem createContent(CollectionItem parent,
                                     ContentItem content) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }

        if(THROW_CONCURRENT_EXCEPTION) {
            throw new ConcurrencyFailureException("fail!");
        }

        if (getStorage().getItemByUid(content.getUid()) != null) {
            throw new UidInUseException(content.getUid(), "Uid " + content.getUid() + " already in use");
        }

        ((HibItem) content).addParent(parent);
        getStorage().storeItem(content);

        return content;
    } 
   

    /**
     * Update an existing content item.
     *
     * @param content
     *            content item to update
     * @return updated content item
     */
    public ContentItem updateContent(ContentItem content) {
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        if(THROW_CONCURRENT_EXCEPTION) {
            throw new ConcurrencyFailureException("fail!");
        }

        Item stored = getStorage().getItemByUid(content.getUid());
        if (stored != null && stored != content) {
            throw new UidInUseException(content.getUid(), "Uid " + content.getUid() + " already in use");
        }
        
        getStorage().updateItem((Item) content);

        return content;
    }

    /**
     * Remove content item
     *
     * @param content
     *            content item to remove
     */
    public void removeContent(ContentItem content) {
        removeItem(content);
    }

    /**
     * Removed user content.
     * {@inheritDoc}
     * @param user The user.
     */
    public void removeUserContent(User user) {
        // do nothing for now
    }

    /**
     * Remove collection item
     *
     * @param collection
     *            collection item to remove
     */
    public void removeCollection(CollectionItem collection) {
        removeItem(collection);
    }

    /**
     * Creates content.
     * {@inheritDoc}
     * @param parents Set<CollectionItem>.
     * @param content ContentItem.
     * @return ContentItem.
     */
    public ContentItem createContent(Set<CollectionItem> parents, ContentItem content) {
        if (parents == null || parents.size()==0) {
            throw new IllegalArgumentException("parents cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }

        if(THROW_CONCURRENT_EXCEPTION) {
            throw new ConcurrencyFailureException("fail!");
        }
        
        if (getStorage().getItemByUid(content.getUid()) != null) {
            throw new UidInUseException(content.getUid(), "Uid " + content.getUid() + " already in use");
        }
        
        for (CollectionItem parent: parents) {
            ((HibItem) content).addParent(parent);
        }
          
        getStorage().storeItem(content);

        return content;
    }

    /**
     * Updates collection.
     * {@inheritDoc}
     * @param collection The collection item.
     * @param children Set<ContentItem>.
     * @return Collection item.
     */
    public CollectionItem updateCollection(CollectionItem collection,
            Set<ContentItem> children) {
        for (ContentItem child : children) {
            if (child.getCreationDate() == null) {
                createContent(collection, child);
            } else if (child.getIsActive() == false) {
                removeItemFromCollection(child, collection);
            } else {
                if (!child.getParents().contains(collection)) { 
                   addItemToCollection(child, collection);    
                }
                
                updateContent(child);
            }
            
        }
        return collection;
    }

    /**
     * Updates collection timestamp.
     * {@inheritDoc}
     * @param collection The collection item.
     * @return CollectionItem.
     */
    public CollectionItem updateCollectionTimestamp(CollectionItem collection) {
        ((MockCollectionItem) collection).setModifiedDate(new Date());
        getStorage().updateItem(collection);
        return collection;
    }

    @Override
    public void removeItemsFromCollection(CollectionItem collection) {
        
    }
}
