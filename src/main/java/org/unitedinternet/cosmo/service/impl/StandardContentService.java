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

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.lock.LockManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Standard implementation of <code>ContentService</code>.
 *
 * @see ContentService
 * @see ContentDao
 */
public class StandardContentService implements ContentService {

    private final ContentDao contentDao;
    private final LockManager lockManager;
  
    private long lockTimeout = 100;

    public StandardContentService(final ContentDao contentDao, final LockManager lockManager) {
        Assert.notNull(contentDao, "contentDao is null");
        Assert.notNull(lockManager, "lockManager is null");
        this.contentDao = contentDao;
        this.lockManager = lockManager;
    }

    public HomeCollectionItem getRootItem(User user, boolean forceReload) {
        return contentDao.getRootItem(user, forceReload);
    }

    public HomeCollectionItem getRootItem(User user) {
        return contentDao.getRootItem(user);
    }

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     */
    public Item findItemByPath(String path) {
        return contentDao.findItemByPath(path);
    }

    /**
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param item item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(Item item, CollectionItem collection) {
        contentDao.removeItemFromCollection(item, collection);
        contentDao.updateCollectionTimestamp(collection);
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
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection) {
        return contentDao.createCollection(parent, collection);
    }

    /**
     * Create a new collection.
     * 
     * @param parent
     *            parent of collection.
     * @param collection
     *            collection to create
     * @param children
     *            collection children
     * @return newly created collection
     */
    public CollectionItem createCollection(CollectionItem parent,
            CollectionItem collection, Set<Item> children) {
        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of the children.  If all children are new
        // items, then no locks are obtained.
        Set<CollectionItem> locks = acquireLocks(children);
        
        try {
            // Create the new collection
            collection = contentDao.createCollection(parent, collection);
            
            Set<ContentItem> childrenToUpdate = new LinkedHashSet<ContentItem>();
            
            // Keep track of NoteItem modifications that need to be processed
            // after the master NoteItem.
            ArrayList<NoteItem> modifications = new ArrayList<NoteItem>(); 
            
            // Either create or update each item
            for (Item item : children) {
                if (item instanceof NoteItem) {
                    
                    NoteItem note = (NoteItem) item;
                    
                    // If item is a modification and the master note
                    // hasn't been created, then we need to process
                    // the master first.
                    if(note.getModifies()!=null) {
                        modifications.add(note);
                    }
                    else {
                        childrenToUpdate.add(note);
                    }
                }
            }
            
            // add modifications to end of set
            for(NoteItem mod: modifications) {
                childrenToUpdate.add(mod);
            }
            
            // update all children and collection
            collection = contentDao.updateCollection(collection, childrenToUpdate);
            
            // update timestamps on all collections involved
            for(CollectionItem lockedCollection : locks) {
               contentDao.updateCollectionTimestamp(lockedCollection);
            }
            
            // update timestamp on new collection
            collection = contentDao.updateCollectionTimestamp(collection);
            
            // get latest timestamp
            return collection;
            
        } finally {
           releaseLocks(locks);
        }
    }
    
    /**
     * Update collection item
     * 
     * @param collection
     *            collection item to update
     * @return updated collection
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if CollectionItem is locked
     */
    public CollectionItem updateCollection(CollectionItem collection) {

       /* if(collection instanceof HomeCollectionItem) {
            throw new IllegalArgumentException("cannot update home collection");
        }*///ical adds default alarms in home collection, so we must allow the update
        
        if (! lockManager.lockCollection(collection, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        
        try {
            return contentDao.updateCollection(collection);
        } finally {
            lockManager.unlockCollection(collection);
        }
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
        if(collection instanceof HomeCollectionItem) {
            throw new IllegalArgumentException("cannot remove home collection");
        }
        contentDao.removeCollection(collection);
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
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public ContentItem createContent(CollectionItem parent,
                                     ContentItem content) {
        checkDatesForEvent(content);
        // Obtain locks to all collections involved.
        Set<CollectionItem> locks = acquireLocks(parent, content);
        
        try {
            content = contentDao.createContent(parent, content);
            
            // update collections
            for(CollectionItem col : locks) {
                contentDao.updateCollectionTimestamp(col);
            }
            
            return content;
        } finally {
            releaseLocks(locks);
        }   
    }
    private void checkDatesForEvents(Collection<ContentItem> items){
        if(items == null){
            return;
        }
        for(ContentItem item : items){
            checkDatesForEvent(item);
        }
    }
    private void checkDatesForEvent(ContentItem item){
        if(!(item instanceof NoteItem)){
            return;
        }
        
        NoteItem noteItem = (NoteItem)item;
        Stamp stamp = noteItem.getStamp("event");
        
        if(!(stamp instanceof EventStamp)){
            return;
        }
        
        EventStamp eventStamp = (EventStamp) stamp;
        
        VEvent masterEvent = eventStamp.getMasterEvent();
        
        checkDatesForComponent(masterEvent);
    }
    
    private void checkDatesForComponent(Component component){
        if(component == null){
            return;
        }
        
        Property dtStart = component.getProperty(Property.DTSTART);
        Property dtEnd = component.getProperty(Property.DTEND);
        
        if( dtStart instanceof DtStart && dtStart.getValue()!= null 
            && dtEnd instanceof DtEnd && dtEnd.getValue() != null 
           && ((DtStart)dtStart).getDate().compareTo(((DtEnd)dtEnd).getDate()) > 0 ){
            throw new IllegalArgumentException("End date [" + dtEnd + " is lower than start date [" + dtStart + "]");
        }
        
    }
    
    /**
     * Create new content items in a parent collection.
     * 
     * @param parent
     *            parent collection of content items.
     * @param contentItems
     *            content items to create
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void createContentItems(CollectionItem parent,
                                     Set<ContentItem> contentItems) {
        checkDatesForEvents(contentItems);
        if (! lockManager.lockCollection(parent, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        try {
            for(ContentItem content : contentItems) {
                contentDao.createContent(parent, content);
            }
            
            contentDao.updateCollectionTimestamp(parent);
        } finally {
            lockManager.unlockCollection(parent);
        }   
    }

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
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void updateContentItems(Set<CollectionItem> parents, Set<ContentItem> contentItems) {
        checkDatesForEvents(contentItems);
        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of updated items.
        Set<CollectionItem> locks = acquireLocks(contentItems);
        
        try {
            
           for(ContentItem content: contentItems) {
               if(content.getCreationDate()==null) {
                   contentDao.createContent(parents, content);
               }
               else if(Boolean.FALSE.equals(content.getIsActive())) {
                   contentDao.removeContent(content);
               }
               else {
                   contentDao.updateContent(content);
               }
           }
           
           // update collections
           for(CollectionItem parent : locks) {
               contentDao.updateCollectionTimestamp(parent);
           }
        } finally {
            releaseLocks(locks);
        }
    }
    

    /**
     * Update an existing content item.
     * 
     * @param content
     *            content item to update
     * @return updated content item
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public ContentItem updateContent(ContentItem content) {
        checkDatesForEvent(content);
        Set<CollectionItem> locks = acquireLocks(content);
        
        try {
            content = contentDao.updateContent(content);
            
            // update collections
            for(CollectionItem parent : locks) {
                contentDao.updateCollectionTimestamp(parent);
            }
            
            return content;
        } finally {
            releaseLocks(locks);
        }
    }

    /**
     * Remove content item
     * 
     * @param content
     *            content item to remove
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked           
     */
    public void removeContent(ContentItem content) {
        Set<CollectionItem> locks = acquireLocks(content);
        
        try {
            contentDao.removeContent(content);
            // update collections
            for(CollectionItem parent : locks) {
                contentDao.updateCollectionTimestamp(parent);
            }
        } finally {
            releaseLocks(locks);
        }
    }

    
    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param collectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem) {
        return contentDao.findCollectionItems(collectionItem);
    }

    /**
     * Given a set of items, aquire a lock on all parents
     */
    private Set<CollectionItem> acquireLocks(Set<? extends Item> children) {
        
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            for(Item child : children) {
                acquireLocks(locks, child);
            }
           
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    /**
     * Given a collection and a set of items, aquire a lock on the collection and
     * all 
     */
    private Set<CollectionItem> acquireLocks(CollectionItem collection, Set<Item> children) {
        
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            if (! lockManager.lockCollection(collection, lockTimeout)) {
                throw new CollectionLockedException("unable to obtain collection lock");
            }
            
            locks.add(collection);
            
            for(Item child : children) {
                acquireLocks(locks, child);
            }
           
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private Set<CollectionItem> acquireLocks(CollectionItem collection, Item item) {
        HashSet<Item> items = new HashSet<Item>();
        items.add(item);
        
        return acquireLocks(collection, items);
    }
    
    private Set<CollectionItem> acquireLocks(Item item) {
        HashSet<CollectionItem> locks = new HashSet<CollectionItem>();
        try {
            acquireLocks(locks,item);
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private void acquireLocks(Set<CollectionItem> locks, Item item) {
        for(CollectionItem parent: item.getParents()) {
            if(locks.contains(parent)) {
                continue;
            }
            if (! lockManager.lockCollection(parent, lockTimeout)) {
                throw new CollectionLockedException("unable to obtain collection lock");
            }
            locks.add(parent);
        }
        
        // Acquire locks on master item's parents, as an addition/deletion
        // of a modifications item affects all the parents of the master item.
        if(item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if(note.getModifies()!=null) {
                acquireLocks(locks, note.getModifies());
            }
        }
    }
    
    private void releaseLocks(Set<CollectionItem> locks) {
        for(CollectionItem lock : locks) {
            lockManager.unlockCollection(lock);
        }
    }
}
