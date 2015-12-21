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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.ItemNotFoundException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockItem;
import org.unitedinternet.cosmo.util.PathUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mock implementation of <code>ItemDao</code> useful for testing.
 *
 * @see ItemDao
 * @see Item
 */
public class MockItemDao implements ItemDao {
    private static final Log LOG = LogFactory.getLog(MockItemDao.class);

    private MockDaoStorage storage;

    /**
     * Constructor.
     * @param storage Tge mock dao storage.
     */
    public MockItemDao(MockDaoStorage storage) {
        this.storage = storage;
    }

    // ItemDao methods

    /**
     * Find an item with the specified uid. The return type will be one of
     * Item, CollectionItem, CalendarCollectionItem, CalendarItem.
     * 
     * @param uid
     *            uid of item to find
     * @return item represented by uid
     */
    public Item findItemByUid(String uid) {
        return storage.getItemByUid(uid);
    }
    

    /**
     * Find an item with the specified path. The return type will be one of
     * Item, CollectionItem, CalendarCollectionItem, CalendarItem.
     * 
     * @param path
     *            path of item to find
     * @return item represented by path
     */
    public Item findItemByPath(String path) {
        return storage.getItemByPath(decode(path));
    }
    
    /**
     * Find the parent item of the item with the specified path. 
     * The return type will be one of CollectionItem, CalendarCollectionItem.
     *
     * @param path
     *            path of item
     * @return parent item of item represented by path
     */
    @SuppressWarnings("deprecation")
    public Item findItemParentByPath(String path) {
        return storage.getItemByPath(path).getParent();
    }

    @Override
    public HomeCollectionItem getRootItem(User user, boolean forceReload) {
        return getRootItem(user);
    }

    /**
     * Return the path to an item. The path has the format:
     * /username/parent1/parent2/itemname
     * 
     * @param item
     *            the item to calculate the path for
     * @return hierarchical path to item
     */
    public String getItemPath(Item item) {
        return storage.getItemPath(item);
    }

    /**
     * Get the root item for a user
     * 
     * @param user The user.
     * @return Home collection item.
     */
    public HomeCollectionItem getRootItem(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getting root item for user " + user.getEmail());
        }

        return getStorage().getRootItem(user.getEmail());
    }

    /**
     * Create the root item for a user.
     * @param user The user.
     * @return The home collection item.
     */
    public HomeCollectionItem createRootItem(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating root item for user " + user.getEmail());
        }

        HomeCollectionItem rootCollection = storage.createRootItem(user);

        if (LOG.isDebugEnabled()) {
            LOG.debug("root item uid is " + rootCollection.getUid());
        }

        return rootCollection;
    }

    /**
     * Copy an item to the given path
     * @param item item to copy
     * @param path path to copy item to
     * @param deepCopy true for deep copy, else shallow copy will
     *                 be performed
     */
    public void copyItem(Item item, String path, boolean deepCopy) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        PathUtil.getParentPath(path);
        CollectionItem parent = (CollectionItem)
            storage.getItemByPath(PathUtil.getParentPath(path));
        if (parent == null) {
            throw new ItemNotFoundException("parent collection not found");
        }

        copyItem(item, PathUtil.getBasename(path), parent, deepCopy);
    }

    /**
     * Copy item.
     * @param item The item.
     * @param copyName The copy name.
     * @param parent The parent.
     * @param deepCopy DeepCopy.
     * @return The item.
     */
    private Item copyItem(Item item, String copyName, CollectionItem parent, boolean deepCopy) {
        Item copy = null;
        try {
            copy = item.getClass().newInstance();
        } catch (Exception e) {
            throw new CosmoException("unable to construct new instance of " + item.getClass(), e);
        }

        if (copyName == null) {
            copyName = item.getName();
        }
        copy.setName(copyName);
        copy.getParents().add(parent);
        copy.setOwner(item.getOwner());

        for (Map.Entry<QName, Attribute> entry : item.getAttributes().entrySet()) {
            copy.addAttribute(entry.getValue().copy());
        }

        // XXX: ignoring calendar indexes

        storage.storeItem(copy);

        if (deepCopy && (item instanceof CollectionItem)) {
            CollectionItem collection = (CollectionItem) item;
            for (Item child: collection.getChildren()) {
                copyItem(child, null, (CollectionItem) copy, true);
            }
        }

        return copy;
    }
    
  
    /**
     * Move item to the given path
     * @param fromPath item to move
     * @param toPath path to move item to
     */
    public void moveItem(String fromPath, String toPath) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Remove an item.
     * 
     * @param item
     *            item to remove
     */
    @SuppressWarnings("deprecation")
    public void removeItem(Item item) {
        if (item.getParent()!=null) {
            ((MockCollectionItem) item.getParent()).removeChild(item);
        }
        
        // update modifications
        if(item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if (note.getModifies() != null) {
                note.getModifies().removeModification(note);
            }
        }

        storage.removeItemByUid(item.getUid());
        storage.removeItemByPath(getItemPath(item));
        if (storage.getRootUid(item.getOwner().getEmail()).
            equals(item.getUid())) {
            storage.removeRootUid(item.getOwner().getEmail());
        }
    }

    /**
     * Removes item by uid.
     * {@inheritDoc}
     * @param uid The uid.
     */
    public void removeItemByUid(String uid) {
        removeItem(findItemByUid(uid));
    }
    
    /**
     * Adds item to collection.
     * {@inheritDoc}
     * @param item The item.
     * @param collection The collection.
     */
    public void addItemToCollection(Item item, CollectionItem collection) {
        ((MockCollectionItem) collection).addChild(item);
        ((MockItem) item).addParent(collection);
    }
    
    /**
     * Removes item from collection.
     * {@inheritDoc}
     * @param item The item.
     * @param collection The collection.
     */
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        ((MockItem) item).removeParent(collection);
        ((MockCollectionItem) collection).removeChild(item);
        if (item.getParents().size() == 0) {
            removeItem(item);
        }
    }

    /**
     * Initializes item
     * {@inheritDoc}
     * @param item The item.
     */
    public void initializeItem(Item item) {
    }

    // Dao methods
    /**
     * Initializes the DAO, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
    }

    /**
     * Readies the DAO for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
    }

    // our methods

    /**
     * Gets storage.
     * @return The mock dao storage.
     */
    public MockDaoStorage getStorage() {
        return storage;
    }

    private static String decode(String urlPath){
        try {
            return new URI(urlPath).getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem) {
        Set<CollectionItem> collections = new HashSet<CollectionItem>();
        if (collectionItem instanceof HomeCollectionItem) {
            HomeCollectionItem homeCollection = storage.getRootItem("test");
            for(Item item:homeCollection.getChildren()){
                if (item instanceof CollectionItem) {
                    collections.add((CollectionItem)item);
                }
            }
        }
        return collections;
    }
}
