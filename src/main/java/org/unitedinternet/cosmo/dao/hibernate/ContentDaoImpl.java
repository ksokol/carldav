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

import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;

/**
 * Implementation of ContentDao using hibernate persistence objects
 */
public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    public HibCollectionItem createCollection(HibCollectionItem parent,
                                           HibCollectionItem collection) {

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

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.dao.ContentDao#createContent(org.unitedinternet.cosmo.model.CollectionItem,
     *      org.unitedinternet.cosmo.model.ContentItem)
     */
    public ContentItem createContent(HibCollectionItem parent, ContentItem content) {

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
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ContentDao#createContent(java.util.Set, org.unitedinternet.cosmo.model.ContentItem)
     */
    public ContentItem createContent(Set<HibCollectionItem> parents, ContentItem content) {

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
    public HibCollectionItem updateCollectionTimestamp(HibCollectionItem collection) {
        try {
            if (!getSession().contains(collection)) {
                collection = (HibCollectionItem) getSession().merge(collection);
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
    public HibCollectionItem updateCollection(HibCollectionItem collection) {
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
    public void removeCollection(HibCollectionItem collection) {

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
            if (item instanceof HibNoteItem) {
                HibNoteItem note = (HibNoteItem) item;
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
        } else if (item instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) item);
        } else {
            super.removeItem(item);
        }
    }


    @Override
    public void removeItemByPath(String path) {
        Item item = this.findItemByPath(path);
        if (item instanceof ContentItem) {
            removeContent((ContentItem) item);
        } else if (item instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) item);
        } else {
            super.removeItem(item);
        }
    }

    @Override
    public void removeItemByUid(String uid) {
        Item item = this.findItemByUid(uid);
        if (item instanceof ContentItem) {
            removeContent((ContentItem) item);
        } else if (item instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) item);
        } else {
            super.removeItem(item);
        }
    }


    private void removeContentRecursive(ContentItem content) {
        // Remove modifications
        if (content instanceof HibNoteItem) {
            HibNoteItem note = (HibNoteItem) content;
            if (note.getModifies() != null) {
                // remove mod from master's collection
                note.getModifies().removeModification(note);
                note.getModifies().updateTimestamp();
            }
        }

        getSession().delete(content);
    }

    private void removeCollectionRecursive(HibCollectionItem collection) {
        // Removing a collection does not automatically remove
        // its children.  Instead, the association to all the
        // children is removed, and any children who have no
        // parent collection are then removed.
        removeItemsFromCollection(collection);

        getSession().delete(collection);
    }

    @Override
    public void removeItemsFromCollection(HibCollectionItem collection) {
        // faster way to delete all calendar items but requires cascade delete on FK at DB
/*        Long collectionId = ((HibCollectionItem)collection).getId();
        String deleteAllQuery = "delete from HibContentItem item where item.id in " +
        		" (select collItem.item.id from HibCollectionItemDetails collItem "+
                 " where collItem.collection.id=:collectionId)";
        getSession().createQuery(deleteAllQuery).setLong("collectionId", collectionId).executeUpdate();
*/
       for (Item item : collection.getChildren()) {
            if (item instanceof HibCollectionItem) {
                removeCollectionRecursive((HibCollectionItem) item);
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

    private void removeNoteItemFromCollectionInternal(HibNoteItem note, HibCollectionItem collection) {
        getSession().update(collection);
        getSession().update(note);

        // do nothing if item doesn't belong to collection
        if (!note.getParents().contains(collection)) {
            return;
        }

        ((HibItem) note).removeParent(collection);

        for (HibNoteItem mod : note.getModifications()) {
            removeNoteItemFromCollectionInternal(mod, collection);
        }

        // If the item belongs to no collection, then it should
        // be purged.
        if (note.getParents().size() == 0) {
            removeItemInternal(note);
        }

    }


    protected void createContentInternal(HibCollectionItem parent, ContentItem content) {

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
        if (content instanceof HibICalendarItem) {
            checkForDuplicateICalUid((HibICalendarItem) content, parent);
        }

        setBaseItemProps(content);


        // When a note modification is added, it must be added to all
        // collections that the parent note is in, because a note modification's
        // parents are tied to the parent note.
        if (isNoteModification(content)) {
            HibNoteItem note = (HibNoteItem) content;

            // ensure master is dirty so that etag gets updated
            note.getModifies().updateTimestamp();
            note.getModifies().addModification(note);

            if (!note.getModifies().getParents().contains(parent)) {
                throw new ModelValidationException(note, "cannot create modification "
                        + note.getUid() + " in collection " + parent.getUid()
                        + ", master must be created or added first");
            }

            // Add modification to all parents of master
            for (HibCollectionItem col : note.getModifies().getParents()) {
                ((HibItem) note).addParent(col);
            }
        } else {
            // add parent to new content
            ((HibItem) content).addParent(parent);
        }


        getSession().save(content);
    }

    protected void createContentInternal(Set<HibCollectionItem> parents, ContentItem content) {

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
        if (content instanceof HibICalendarItem) {
            checkForDuplicateICalUid((HibICalendarItem) content, content.getParents());
        }

        setBaseItemProps(content);

        // Ensure NoteItem modifications have the same parents as the 
        // master note.
        if (isNoteModification(content)) {
            HibNoteItem note = (HibNoteItem) content;

            // ensure master is dirty so that etag gets updated
            note.getModifies().updateTimestamp();
            note.getModifies().addModification(note);

            if (!note.getModifies().getParents().equals(parents)) {
                StringBuffer modParents = new StringBuffer();
                StringBuffer masterParents = new StringBuffer();
                for (HibCollectionItem p : parents) {
                    modParents.append(p.getUid() + ",");
                }
                for (HibCollectionItem p : note.getModifies().getParents()) {
                    masterParents.append(p.getUid() + ",");
                }
                throw new ModelValidationException(note,
                        "cannot create modification " + note.getUid()
                                + " in collections " + modParents.toString()
                                + " because master's parents are different: "
                                + masterParents.toString());
            }
        }

        for (HibCollectionItem parent : parents) {
            ((HibItem) content).addParent(parent);
        }


        getSession().save(content);
    }

    protected void updateContentInternal(ContentItem content) {

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        getSession().update(content);

        if (content.getOwner() == null) {
            throw new IllegalArgumentException("content must have owner");
        }

        content.updateTimestamp();

        if (isNoteModification(content)) {
            // ensure master is dirty so that etag gets updated
            ((HibNoteItem) content).getModifies().updateTimestamp();
        }

    }

    protected void updateCollectionInternal(HibCollectionItem collection) {
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
                                               HibCollectionItem collection) {

        // Don't allow note modifications to be added to a collection
        // When a master is added, all the modifications are added
        if (isNoteModification(item)) {
            throw new ModelValidationException(item, "cannot add modification "
                    + item.getUid() + " to collection " + collection.getUid()
                    + ", only master");
        }

        if (item instanceof HibICalendarItem) {
            // verify icaluid is unique within collection
            checkForDuplicateICalUid((HibICalendarItem) item, collection);
        }

        super.addItemToCollectionInternal(item, collection);

        // Add all modifications
        if (item instanceof HibNoteItem) {
            for (HibNoteItem mod : ((HibNoteItem) item).getModifications()) {
                super.addItemToCollectionInternal(mod, collection);
            }
        }
    }

    @Override
    protected void removeItemFromCollectionInternal(Item item, HibCollectionItem collection) {
        if (item instanceof HibNoteItem) {
            // When a note modification is removed, it is really removed from
            // all collections because a modification can't live in one collection
            // and not another.  It is tied to the collections that the master
            // note is in.  Therefore you can't just remove a modification from
            // a single collection when the master note is in multiple collections.
            HibNoteItem note = (HibNoteItem) item;
            if (note.getModifies() != null) {
                removeContentRecursive((ContentItem) item);
            } else {
                removeNoteItemFromCollectionInternal((HibNoteItem) item, collection);
            }
        } else {
            super.removeItemFromCollectionInternal(item, collection);
        }
    }

    protected void checkForDuplicateICalUid(HibICalendarItem item, HibCollectionItem parent) {

        // TODO: should icalUid be required?  Currrently its not and all
        // items created by the webui dont' have it.
        if (item.getIcalUid() == null) {
            return;
        }

        // ignore modifications
        if (item instanceof HibNoteItem && ((HibNoteItem) item).getModifies() != null) {
            return;
        }

        // Lookup item by parent/icaluid
        Query hibQuery = null;
        if (item instanceof HibNoteItem) {
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

    protected void checkForDuplicateICalUid(HibICalendarItem item,
                                            Set<HibCollectionItem> parents) {

        if (item.getIcalUid() == null) {
            return;
        }

        // ignore modifications
        if (item instanceof HibNoteItem && ((HibNoteItem) item).getModifies() != null) {
            return;
        }

        for (HibCollectionItem parent : parents) {
            checkForDuplicateICalUid(item, parent);
        }
    }

    private boolean isNoteModification(Item item) {
        if (!(item instanceof HibNoteItem)) {
            return false;
        }

        return ((HibNoteItem) item).getModifies() != null;
    }


    @Override
    public void destroy() {

    }

}
