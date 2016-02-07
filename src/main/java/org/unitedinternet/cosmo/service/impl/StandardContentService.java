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
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibContentItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.lock.LockManager;

import java.util.Collection;
import java.util.HashSet;
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

    /**
     * Find content item by path. Path is of the format:
     * /username/parent1/parent2/itemname.
     */
    public HibItem findItemByPath(String path) {
        return contentDao.findItemByPath(path);
    }

    /**
     * Remove an item from a collection.  The item will be deleted if
     * it belongs to no more collections.
     * @param hibItem item to remove from collection
     * @param collection item to remove item from
     */
    public void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection) {
        contentDao.removeItemFromCollection(hibItem, collection);
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
    public HibCollectionItem createCollection(HibCollectionItem parent,
                                           HibCollectionItem collection) {
        return contentDao.createCollection(parent, collection);
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
    public HibCollectionItem updateCollection(HibCollectionItem collection) {

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
    public void removeCollection(HibCollectionItem collection) {
        // prevent HomeCollection from being removed (should only be removed
        // when user is removed)
        if(collection instanceof HibHomeCollectionItem) {
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
    public HibContentItem createContent(HibCollectionItem parent,
                                     HibContentItem content) {
        checkDatesForEvent(content);
        // Obtain locks to all collections involved.
        Set<HibCollectionItem> locks = acquireLocks(parent, content);
        
        try {
            content = contentDao.createContent(parent, content);
            
            // update collections
            for(HibCollectionItem col : locks) {
                contentDao.updateCollectionTimestamp(col);
            }
            
            return content;
        } finally {
            releaseLocks(locks);
        }   
    }
    private void checkDatesForEvents(Collection<HibContentItem> items){
        if(items == null){
            return;
        }
        for(HibContentItem item : items){
            checkDatesForEvent(item);
        }
    }
    private void checkDatesForEvent(HibContentItem item){
        if(!(item instanceof HibNoteItem)){
            return;
        }

        HibNoteItem noteItem = (HibNoteItem)item;
        HibEventStamp eventStamp = (HibEventStamp) noteItem.getStamp(HibEventStamp.class);
        
        if(eventStamp == null){
            return;
        }

        Component masterEvent = eventStamp.getMaster();
        
        checkDatesForComponent(masterEvent);
        
        for(Component component : eventStamp.getExceptions()){
            checkDatesForComponent(component);
        }
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
     * @param hibContentItems
     *            content items to create
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void createContentItems(HibCollectionItem parent,
                                     Set<HibContentItem> hibContentItems) {
        checkDatesForEvents(hibContentItems);
        if (! lockManager.lockCollection(parent, lockTimeout)) {
            throw new CollectionLockedException("unable to obtain collection lock");
        }
        try {
            for(HibContentItem content : hibContentItems) {
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
     * @param hibContentItems to update
     * @throws org.unitedinternet.cosmo.model.CollectionLockedException
     *         if parent CollectionItem is locked
     */
    public void updateContentItems(Set<HibCollectionItem> parents, Set<HibContentItem> hibContentItems) {
        checkDatesForEvents(hibContentItems);
        // Obtain locks to all collections involved.  A collection is involved
        // if it is the parent of one of updated items.
        Set<HibCollectionItem> locks = acquireLocks(hibContentItems);
        
        try {
            
           for(HibContentItem content: hibContentItems) {
               if(content.getId()== null) {
                   contentDao.createContent(parents, content);
               }
               else {
                   contentDao.updateContent(content);
               }
           }
           
           // update collections
           for(HibCollectionItem parent : locks) {
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
    public HibContentItem updateContent(HibContentItem content) {
        checkDatesForEvent(content);
        Set<HibCollectionItem> locks = acquireLocks(content);
        
        try {
            content = contentDao.updateContent(content);
            
            // update collections
            for(HibCollectionItem parent : locks) {
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
    public void removeContent(HibContentItem content) {
        Set<HibCollectionItem> locks = acquireLocks(content);
        
        try {
            contentDao.removeContent(content);
            // update collections
            for(HibCollectionItem parent : locks) {
                contentDao.updateCollectionTimestamp(parent);
            }
        } finally {
            releaseLocks(locks);
        }
    }

    
    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem) {
        return contentDao.findCollectionItems(hibCollectionItem);
    }

    /**
     * Given a set of items, aquire a lock on all parents
     */
    private Set<HibCollectionItem> acquireLocks(Set<? extends HibItem> children) {
        
        HashSet<HibCollectionItem> locks = new HashSet<HibCollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            for(HibItem child : children) {
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
    private Set<HibCollectionItem> acquireLocks(HibCollectionItem collection, Set<HibItem> children) {
        
        HashSet<HibCollectionItem> locks = new HashSet<HibCollectionItem>();
        
        // Get locks for all collections involved
        try {
            
            if (! lockManager.lockCollection(collection, lockTimeout)) {
                throw new CollectionLockedException("unable to obtain collection lock");
            }
            
            locks.add(collection);
            
            for(HibItem child : children) {
                acquireLocks(locks, child);
            }
           
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private Set<HibCollectionItem> acquireLocks(HibCollectionItem collection, HibItem hibItem) {
        HashSet<HibItem> hibItems = new HashSet<HibItem>();
        hibItems.add(hibItem);
        
        return acquireLocks(collection, hibItems);
    }
    
    private Set<HibCollectionItem> acquireLocks(HibItem hibItem) {
        HashSet<HibCollectionItem> locks = new HashSet<HibCollectionItem>();
        try {
            acquireLocks(locks, hibItem);
            return locks;
        } catch (RuntimeException e) {
            releaseLocks(locks);
            throw e;
        }
    }
    
    private void acquireLocks(Set<HibCollectionItem> locks, HibItem hibItem) {
        final HibCollectionItem parent = hibItem.getCollection();

        if(parent == null) {
            return;
        }

        if(!locks.contains(parent)) {
            if (! lockManager.lockCollection(parent, lockTimeout)) {
                throw new CollectionLockedException("unable to obtain collection lock");
            }
            locks.add(parent);
        }
        
        // Acquire locks on master item's parents, as an addition/deletion
        // of a modifications item affects all the parents of the master item.
        if(hibItem instanceof HibNoteItem) {
            HibNoteItem note = (HibNoteItem) hibItem;
            if(note.getModifies()!=null) {
                acquireLocks(locks, note.getModifies());
            }
        }
    }
    
    private void releaseLocks(Set<HibCollectionItem> locks) {
        for(HibCollectionItem lock : locks) {
            lockManager.unlockCollection(lock);
        }
    }
}
