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
package org.unitedinternet.cosmo.dao.hibernate;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibItemTombstone;
import org.springframework.orm.hibernate4.SessionFactoryUtils;

/**
 * Implementation of ContentDao using hibernate persistence objects
 */
public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(ContentDaoImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#createCollection(org.unitedinternet.cosmo.model.CollectionItem,
     *      org.unitedinternet.cosmo.model.CollectionItem)
     */
    public CollectionItem createCollection(CollectionItem parent,
                                           CollectionItem collection) {

        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }

        if (collection.getOwner() == null) {
            throw new IllegalArgumentException("collection must have owner");
        }

        if (getBaseModelObject(collection).getId() != -1) {
            throw new IllegalArgumentException("invalid collection id (expected -1)");
        }


        try {
            // verify uid not in use
            checkForDuplicateUid(collection);

            setBaseItemProps(collection);
            ((HibItem) collection).addParent(parent);

            getSession().save(collection);
            getSession().flush();

            return collection;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ContentDao#updateCollection(org.unitedinternet.cosmo.model.CollectionItem, java.util.Set, java.util.Map)
     */

    /**
     * Updates collection.
     */
    public CollectionItem updateCollection(CollectionItem collection, Set<ContentItem> children) {

        // Keep track of duplicate icalUids because we don't flush
        // the db until the end so we need to handle the case of 
        // duplicate icalUids in the same request.
        HashMap<String, NoteItem> icalUidMap = new HashMap<String, NoteItem>();

        try {
            updateCollectionInternal(collection);

            // Either create, update, or delete each item
            for (ContentItem item : children) {

                // Because we batch all the db operations, we must check
                // for duplicate icalUid within the same request
                if (item instanceof NoteItem
                        && ((NoteItem) item).getIcalUid() != null) {
                    NoteItem note = (NoteItem) item;
                    if (item.getIsActive() == true) {
                        NoteItem dup = icalUidMap.get(note.getIcalUid());
                        if (dup != null && !dup.getUid().equals(item.getUid())) {
                            throw new IcalUidInUseException("iCal uid"
                                    + note.getIcalUid()
                                    + " already in use for collection "
                                    + collection.getUid(), item.getUid(), dup
                                    .getUid());
                        }
                    }

                    icalUidMap.put(note.getIcalUid(), note);
                }

                // create item
                if (getBaseModelObject(item).getId() == -1) {
                    createContentInternal(collection, item);
                }
                // delete item
                else if (item.getIsActive() == false) {
                    // If item is a note modification, only remove the item
                    // if its parent is not also being removed.  This is because
                    // when a master item is removed, all its modifications are
                    // removed.
                    if (item instanceof NoteItem) {
                        NoteItem note = (NoteItem) item;
                        if (note.getModifies() != null && note.getModifies().getIsActive() == false) {
                            continue;
                        }
                    }
                    removeItemFromCollectionInternal(item, collection);
                }
                // update item
                else {
                    if (!item.getParents().contains(collection)) {

                        // If item is being added to another collection,
                        // we need the ticket/perms to add that item.
                        // If ticket exists, then add with ticket and ticket perms.
                        // If ticket doesn't exist, but item uuid is present in
                        // itemPerms map, then add with read-only access.

                        addItemToCollectionInternal(item, collection);
                    }

                    updateContentInternal(item);
                }
            }

            getSession().flush();

            // clear the session to improve subsequent flushes
            getSession().clear();

            // load collection to get it back into the session
            getSession().load(collection, getBaseModelObject(collection).getId());

            return collection;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#createContent(org.unitedinternet.cosmo.model.CollectionItem,
     *      org.unitedinternet.cosmo.model.ContentItem)
     */
    public ContentItem createContent(CollectionItem parent, ContentItem content) {

        try {
            createContentInternal(parent, content);
            getSession().flush();
            return content;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#createBatchContent(org.unitedinternet.cosmo.model.CollectionItem,
     *      java.util.Set)
     */
    public void createBatchContent(CollectionItem parent, Set<ContentItem> contents) {
        
        try {
            for(ContentItem content : contents) {
                createContentInternal(parent, content);
            }
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            getSession().clear();
            logConstraintViolationException(cve);
            throw cve;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#updateBatchContent(java.util.Set)
     */
    public void updateBatchContent(Set<ContentItem> contents) {
        try {
            for(ContentItem content : contents) {
                updateContentInternal(content);
            }
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#removeBatchContent(org.unitedinternet.cosmo.model.CollectionItem,
     *      java.util.Set)
     */
    public void removeBatchContent(CollectionItem parent, Set<ContentItem> contents) {
        try {
            for(ContentItem content : contents) {
                removeItemFromCollectionInternal(content, parent);
            }
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }
    
    

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ContentDao#createContent(java.util.Set, org.unitedinternet.cosmo.model.ContentItem)
     */
    public ContentItem createContent(Set<CollectionItem> parents, ContentItem content) {

        try {
            createContentInternal(parents, content);
            getSession().flush();
            return content;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }
    

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ContentDao#updateCollectionTimestamp(org.unitedinternet.cosmo.model.CollectionItem)
     */

    /**
     * Updates collection timestamp.
     *
     * @param collection The timestamp of this collection needs to be updated.
     * @return The new collection item updated.
     */
    public CollectionItem updateCollectionTimestamp(CollectionItem collection) {
        try {
            if (!getSession().contains(collection)) {
                collection = (CollectionItem) getSession().merge(collection);
            }
            collection.updateTimestamp();
            getSession().flush();
            return collection;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#updateCollection(org.unitedinternet.cosmo.model.CollectionItem)
     */

    /**
     * Updates collection.
     *
     * @param collection The updated collection.
     */
    public CollectionItem updateCollection(CollectionItem collection) {
        try {

            updateCollectionInternal(collection);
            getSession().flush();

            return collection;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#updateContent(org.unitedinternet.cosmo.model.ContentItem)
     */

    /**
     * Updates content item.
     *
     * @param content The content that needs to be updated.
     * @return The updated content.
     */
    public ContentItem updateContent(ContentItem content) {
        try {
            updateContentInternal(content);
            getSession().flush();
            return content;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#removeCollection(org.unitedinternet.cosmo.model.CollectionItem)
     */

    /**
     * Removes the collection given.
     *
     * @param collection The collection that needs to be removed.
     */
    public void removeCollection(CollectionItem collection) {

        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }

        try {
            getSession().refresh(collection);
            removeCollectionRecursive(collection);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#removeContent(org.unitedinternet.cosmo.model.ContentItem)
     */

    /**
     * Removes the content given.
     *
     * @param content The content item that need to be removed.
     */
    public void removeContent(ContentItem content) {

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        try {
            getSession().refresh(content);
            removeContentRecursive(content);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ContentDao#removeUserContent(org.unitedinternet.cosmo.model.User)
     */

    /**
     * Removes user content.
     *
     * @param user The content of the user needs to be removed.
     */
    public void removeUserContent(User user) {
        try {
            Query query = getSession().getNamedQuery("contentItem.by.owner")
                    .setParameter("owner", user);

            List<ContentItem> results = query.list();
            for (ContentItem content : results) {
                removeContentRecursive(content);
            }
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    @Override
    public void initializeItem(Item item) {
        super.initializeItem(item);

        // Initialize master NoteItem if applicable
        try {
            if (item instanceof NoteItem) {
                NoteItem note = (NoteItem) item;
                if (note.getModifies() != null) {
                    Hibernate.initialize(note.getModifies());
                    initializeItem(note.getModifies());
                }
            }
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }

    }

    @Override
    public void removeItem(Item item) {
        if (item instanceof ContentItem) {
            removeContent((ContentItem) item);
        } else if (item instanceof CollectionItem) {
            removeCollection((CollectionItem) item);
        } else {
            super.removeItem(item);
        }
    }


    @Override
    public void removeItemByPath(String path) {
        Item item = this.findItemByPath(path);
        if (item instanceof ContentItem) {
            removeContent((ContentItem) item);
        } else if (item instanceof CollectionItem) {
            removeCollection((CollectionItem) item);
        } else {
            super.removeItem(item);
        }
    }

    @Override
    public void removeItemByUid(String uid) {
        Item item = this.findItemByUid(uid);
        if (item instanceof ContentItem) {
            removeContent((ContentItem) item);
        } else if (item instanceof CollectionItem) {
            removeCollection((CollectionItem) item);
        } else {
            super.removeItem(item);
        }
    }


    private void removeContentRecursive(ContentItem content) {
        removeContentCommon(content);

        // Remove modifications
        if (content instanceof NoteItem) {
            NoteItem note = (NoteItem) content;
            if (note.getModifies() != null) {
                // remove mod from master's collection
                note.getModifies().removeModification(note);
                note.getModifies().updateTimestamp();
            } else {
                // mods will be removed by Hibernate cascading rules, but we
                // need to add tombstones for mods
                for (NoteItem mod : note.getModifications()) {
                    removeContentCommon(mod);
                }
            }
        }

        getSession().delete(content);
    }

    private void removeContentCommon(ContentItem content) {
        // Add a tombstone to each parent collection to track
        // when the removal occurred.
        for (CollectionItem parent : content.getParents()) {
            getHibItem(parent).addTombstone(new HibItemTombstone(parent, content));
            getSession().update(parent);
        }
    }

    private void removeCollectionRecursive(CollectionItem collection) {
        // Removing a collection does not automatically remove
        // its children.  Instead, the association to all the
        // children is removed, and any children who have no
        // parent collection are then removed.
        removeItemsFromCollection(collection);

        getSession().delete(collection);
    }

    @Override
    public void removeItemsFromCollection(CollectionItem collection) {
        // faster way to delete all calendar items but requires cascade delete on FK at DB
/*        Long collectionId = ((HibCollectionItem)collection).getId();
        String deleteAllQuery = "delete from HibContentItem item where item.id in " +
        		" (select collItem.primaryKey.item.id from HibCollectionItemDetails collItem "+
                 " where collItem.primaryKey.collection.id=:collectionId)";
        getSession().createQuery(deleteAllQuery).setLong("collectionId", collectionId).executeUpdate();
*/
       for (Item item : collection.getChildren()) {
            if (item instanceof CollectionItem) {
                removeCollectionRecursive((CollectionItem) item);
            } else if (item instanceof ContentItem) {
                ((HibItem) item).removeParent(collection);
                if (item.getParents().size() == 0) {
                    getSession().delete(item);
                }
            } else {
                getSession().delete(item);
            }
        }
    }

    private void removeNoteItemFromCollectionInternal(NoteItem note, CollectionItem collection) {
        getSession().update(collection);
        getSession().update(note);

        // do nothing if item doesn't belong to collection
        if (!note.getParents().contains(collection)) {
            return;
        }

        getHibItem(collection).addTombstone(new HibItemTombstone(collection, note));
        ((HibItem) note).removeParent(collection);

        for (NoteItem mod : note.getModifications()) {
            removeNoteItemFromCollectionInternal(mod, collection);
        }

        // If the item belongs to no collection, then it should
        // be purged.
        if (note.getParents().size() == 0) {
            removeItemInternal(note);
        }

    }


    protected void createContentInternal(CollectionItem parent, ContentItem content) {

        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        if (getBaseModelObject(content).getId() != -1) {
            throw new IllegalArgumentException("invalid content id (expected -1)");
        }

        if (content.getOwner() == null) {
            throw new IllegalArgumentException("content must have owner");
        }

        // verify uid not in use
        checkForDuplicateUid(content);

        // verify icaluid not in use for collection
        if (content instanceof ICalendarItem) {
            checkForDuplicateICalUid((ICalendarItem) content, parent);
        }

        setBaseItemProps(content);


        // When a note modification is added, it must be added to all
        // collections that the parent note is in, because a note modification's
        // parents are tied to the parent note.
        if (isNoteModification(content)) {
            NoteItem note = (NoteItem) content;

            // ensure master is dirty so that etag gets updated
            note.getModifies().updateTimestamp();
            note.getModifies().addModification(note);

            if (!note.getModifies().getParents().contains(parent)) {
                throw new ModelValidationException(note, "cannot create modification "
                        + note.getUid() + " in collection " + parent.getUid()
                        + ", master must be created or added first");
            }

            // Add modification to all parents of master
            for (CollectionItem col : note.getModifies().getParents()) {
                if (((HibCollectionItem) col).removeTombstone(content) == true) {
                    getSession().update(col);
                }
                ((HibItem) note).addParent(col);
            }
        } else {
            // add parent to new content
            ((HibItem) content).addParent(parent);

            // remove tombstone (if it exists) from parent
            if (((HibCollectionItem) parent).removeTombstone(content) == true) {
                getSession().update(parent);
            }
        }


        getSession().save(content);
    }

    protected void createContentInternal(Set<CollectionItem> parents, ContentItem content) {

        if (parents == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        if (getBaseModelObject(content).getId() != -1) {
            throw new IllegalArgumentException("invalid content id (expected -1)");
        }

        if (content.getOwner() == null) {
            throw new IllegalArgumentException("content must have owner");
        }

        if (parents.size() == 0) {
            throw new IllegalArgumentException("content must have at least one parent");
        }

        // verify uid not in use
        checkForDuplicateUid(content);

        // verify icaluid not in use for collections
        if (content instanceof ICalendarItem) {
            checkForDuplicateICalUid((ICalendarItem) content, content.getParents());
        }

        setBaseItemProps(content);

        // Ensure NoteItem modifications have the same parents as the 
        // master note.
        if (isNoteModification(content)) {
            NoteItem note = (NoteItem) content;

            // ensure master is dirty so that etag gets updated
            note.getModifies().updateTimestamp();
            note.getModifies().addModification(note);

            if (!note.getModifies().getParents().equals(parents)) {
                StringBuffer modParents = new StringBuffer();
                StringBuffer masterParents = new StringBuffer();
                for (CollectionItem p : parents) {
                    modParents.append(p.getUid() + ",");
                }
                for (CollectionItem p : note.getModifies().getParents()) {
                    masterParents.append(p.getUid() + ",");
                }
                throw new ModelValidationException(note,
                        "cannot create modification " + note.getUid()
                                + " in collections " + modParents.toString()
                                + " because master's parents are different: "
                                + masterParents.toString());
            }
        }

        for (CollectionItem parent : parents) {
            ((HibItem) content).addParent(parent);
            if (((HibCollectionItem) parent).removeTombstone(content) == true) {
                getSession().update(parent);
            }
        }


        getSession().save(content);
    }

    protected void updateContentInternal(ContentItem content) {

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        if (Boolean.FALSE.equals(content.getIsActive())) {
            throw new IllegalArgumentException("content must be active");
        }

        getSession().update(content);

        if (content.getOwner() == null) {
            throw new IllegalArgumentException("content must have owner");
        }

        content.updateTimestamp();

        if (isNoteModification(content)) {
            // ensure master is dirty so that etag gets updated
            ((NoteItem) content).getModifies().updateTimestamp();
        }

    }

    protected void updateCollectionInternal(CollectionItem collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection cannot be null");
        }

        getSession().update(collection);

        if (collection.getOwner() == null) {
            throw new IllegalArgumentException("collection must have owner");
        }
        collection.updateTimestamp();
    }

    /**
     * Override so we can handle NoteItems. Adding a note to a collection
     * requires verifying that the icaluid is unique within the collection.
     */
    @Override
    protected void addItemToCollectionInternal(Item item,
                                               CollectionItem collection) {

        // Don't allow note modifications to be added to a collection
        // When a master is added, all the modifications are added
        if (isNoteModification(item)) {
            throw new ModelValidationException(item, "cannot add modification "
                    + item.getUid() + " to collection " + collection.getUid()
                    + ", only master");
        }

        if (item instanceof ICalendarItem) {
            // verify icaluid is unique within collection
            checkForDuplicateICalUid((ICalendarItem) item, collection);
        }

        super.addItemToCollectionInternal(item, collection);

        // Add all modifications
        if (item instanceof NoteItem) {
            for (NoteItem mod : ((NoteItem) item).getModifications()) {
                super.addItemToCollectionInternal(mod, collection);
            }
        }
    }

    @Override
    protected void removeItemFromCollectionInternal(Item item, CollectionItem collection) {
        if (item instanceof NoteItem) {
            // When a note modification is removed, it is really removed from
            // all collections because a modification can't live in one collection
            // and not another.  It is tied to the collections that the master
            // note is in.  Therefore you can't just remove a modification from
            // a single collection when the master note is in multiple collections.
            NoteItem note = (NoteItem) item;
            if (note.getModifies() != null) {
                removeContentRecursive((ContentItem) item);
            } else {
                removeNoteItemFromCollectionInternal((NoteItem) item, collection);
            }
        } else {
            super.removeItemFromCollectionInternal(item, collection);
        }
    }

    protected void checkForDuplicateICalUid(ICalendarItem item, CollectionItem parent) {

        // TODO: should icalUid be required?  Currrently its not and all
        // items created by the webui dont' have it.
        if (item.getIcalUid() == null) {
            return;
        }

        // ignore modifications
        if (item instanceof NoteItem && ((NoteItem) item).getModifies() != null) {
            return;
        }

        // Lookup item by parent/icaluid
        Query hibQuery = null;
        if (item instanceof NoteItem) {
            hibQuery = getSession().getNamedQuery(
                    "noteItemId.by.parent.icaluid").setParameter("parentid",
                    getBaseModelObject(parent).getId()).setParameter("icaluid", item.getIcalUid());
        } else {
            hibQuery = getSession().getNamedQuery(
                    "icalendarItem.by.parent.icaluid").setParameter("parentid",
                    getBaseModelObject(parent).getId()).setParameter("icaluid", item.getIcalUid());
        }
        hibQuery.setFlushMode(FlushMode.MANUAL);

        Long itemId = (Long) hibQuery.uniqueResult();

        // if icaluid is in use throw exception
        if (itemId != null) {
            // If the note is new, then its a duplicate icaluid
            if (getBaseModelObject(item).getId() == -1) {
                Item dup = (Item) getSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getIcalUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
            // If the note exists and there is another note with the same
            // icaluid, then its a duplicate icaluid
            if (getBaseModelObject(item).getId().equals(itemId)) {
                Item dup = (Item) getSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getIcalUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
        }
    }

    protected void checkForDuplicateICalUid(ICalendarItem item,
                                            Set<CollectionItem> parents) {

        if (item.getIcalUid() == null) {
            return;
        }

        // ignore modifications
        if (item instanceof NoteItem && ((NoteItem) item).getModifies() != null) {
            return;
        }

        for (CollectionItem parent : parents) {
            checkForDuplicateICalUid(item, parent);
        }
    }

    private boolean isNoteModification(Item item) {
        if (!(item instanceof NoteItem)) {
            return false;
        }

        return ((NoteItem) item).getModifies() != null;
    }


    @Override
    public void destroy() {

    }

}
