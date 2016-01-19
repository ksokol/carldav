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
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibContentItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.model.hibernate.User;

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
    public HibContentItem createContent(HibCollectionItem parent, HibContentItem content) {

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
    public HibContentItem createContent(Set<HibCollectionItem> parents, HibContentItem content) {

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
    public HibContentItem updateContent(HibContentItem content) {
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
    public void removeContent(HibContentItem content) {

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

            List<HibContentItem> results = query.list();
            for (HibContentItem content : results) {
                removeContentRecursive(content);
            }
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    @Override
    public void initializeItem(HibItem hibItem) {
        super.initializeItem(hibItem);

        // Initialize master NoteItem if applicable
        try {
            if (hibItem instanceof HibNoteItem) {
                HibNoteItem note = (HibNoteItem) hibItem;
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
    public void removeItem(HibItem hibItem) {
        if (hibItem instanceof HibContentItem) {
            removeContent((HibContentItem) hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
        }
    }


    @Override
    public void removeItemByPath(String path) {
        HibItem hibItem = this.findItemByPath(path);
        if (hibItem instanceof HibContentItem) {
            removeContent((HibContentItem) hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
        }
    }

    @Override
    public void removeItemByUid(String uid) {
        HibItem hibItem = this.findItemByUid(uid);
        if (hibItem instanceof HibContentItem) {
            removeContent((HibContentItem) hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
        }
    }


    private void removeContentRecursive(HibContentItem content) {
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
       for (HibItem hibItem : collection.getChildren()) {
            if (hibItem instanceof HibCollectionItem) {
                removeCollectionRecursive((HibCollectionItem) hibItem);
            } else if (hibItem instanceof HibContentItem) {
                getSession().delete(hibItem);
            } else {
                getSession().delete(hibItem);
            }
        }
    }

    private void removeNoteItemFromCollectionInternal(HibNoteItem note, HibCollectionItem collection) {
        getSession().update(collection);
        getSession().update(note);

        // do nothing if item doesn't belong to collection
        if (note.getParent().getId() != collection.getId()) {
            return;
        }

        for (HibNoteItem mod : note.getModifications()) {
            removeNoteItemFromCollectionInternal(mod, collection);
        }

        getSession().delete(note);
    }


    protected void createContentInternal(HibCollectionItem parent, HibContentItem content) {

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

            if (note.getModifies().getParent().getId() != parent.getId()) {
                throw new ModelValidationException(note, "cannot create modification "
                        + note.getUid() + " in collection " + parent.getUid()
                        + ", master must be created or added first");
            }

            note.addParent(note.getModifies().getParent());
        } else {
            // add parent to new content
            content.addParent(parent);
        }


        getSession().save(content);
    }

    protected void createContentInternal(Set<HibCollectionItem> parents, HibContentItem content) {

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
            checkForDuplicateICalUid((HibICalendarItem) content, content.getParent());
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

    protected void updateContentInternal(HibContentItem content) {

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
    protected void addItemToCollectionInternal(HibItem hibItem,
                                               HibCollectionItem collection) {

        // Don't allow note modifications to be added to a collection
        // When a master is added, all the modifications are added
        if (isNoteModification(hibItem)) {
            throw new ModelValidationException(hibItem, "cannot add modification "
                    + hibItem.getUid() + " to collection " + collection.getUid()
                    + ", only master");
        }

        if (hibItem instanceof HibICalendarItem) {
            // verify icaluid is unique within collection
            checkForDuplicateICalUid((HibICalendarItem) hibItem, collection);
        }

        super.addItemToCollectionInternal(hibItem, collection);

        // Add all modifications
        if (hibItem instanceof HibNoteItem) {
            for (HibNoteItem mod : ((HibNoteItem) hibItem).getModifications()) {
                super.addItemToCollectionInternal(mod, collection);
            }
        }
    }

    @Override
    protected void removeItemFromCollectionInternal(HibItem hibItem, HibCollectionItem collection) {
        if (hibItem instanceof HibNoteItem) {
            // When a note modification is removed, it is really removed from
            // all collections because a modification can't live in one collection
            // and not another.  It is tied to the collections that the master
            // note is in.  Therefore you can't just remove a modification from
            // a single collection when the master note is in multiple collections.
            HibNoteItem note = (HibNoteItem) hibItem;
            if (note.getModifies() != null) {
                removeContentRecursive((HibContentItem) hibItem);
            } else {
                removeNoteItemFromCollectionInternal((HibNoteItem) hibItem, collection);
            }
        } else {
            super.removeItemFromCollectionInternal(hibItem, collection);
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
                HibItem dup = (HibItem) getSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getIcalUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
            // If the note exists and there is another note with the same
            // icaluid, then its a duplicate icaluid
            if (getBaseModelObject(item).getId().equals(itemId)) {
                HibItem dup = (HibItem) getSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getIcalUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
        }
    }

    private boolean isNoteModification(HibItem hibItem) {
        if (!(hibItem instanceof HibNoteItem)) {
            return false;
        }

        return ((HibNoteItem) hibItem).getModifies() != null;
    }


    @Override
    public void destroy() {

    }

}
