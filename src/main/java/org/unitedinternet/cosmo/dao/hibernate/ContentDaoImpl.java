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

import carldav.service.generator.IdGenerator;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;

public class ContentDaoImpl extends ItemDaoImpl implements ContentDao {

    public ContentDaoImpl(final IdGenerator idGenerator, final ItemPathTranslator itemPathTranslator) {
        super(idGenerator, itemPathTranslator);
    }

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

        if (getBaseModelObject(collection).getId() != null) {
            throw new IllegalArgumentException("invalid collection id (expected -1)");
        }


        try {
            // verify uid not in use
            checkForDuplicateUid(collection);

            setBaseItemProps(collection);
            collection.setCollection(parent);

            getSession().save(collection);
            getSession().refresh(parent);
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
    public HibItem createContent(HibCollectionItem parent, HibItem content) {

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
    public HibItem createContent(Set<HibCollectionItem> parents, HibItem content) {

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
    public HibItem updateContent(HibItem content) {
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
            getSession().delete(collection);
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
    public void removeContent(HibItem content) {

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

            List<HibItem> results = query.list();
            for (HibItem content : results) {
                removeContentRecursive(content);
            }
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    @Override
    public void removeItem(HibItem hibItem) {
        if (hibItem instanceof HibItem) {
            removeContent(hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
        }
    }


    @Override
    public void removeItemByPath(String path) {
        HibItem hibItem = this.findItemByPath(path);
        if (hibItem instanceof HibItem) {
            removeContent(hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
        }
    }

    @Override
    public void removeItemByUid(String uid) {
        HibItem hibItem = this.findItemByUid(uid);
        if (hibItem instanceof HibItem) {
            removeContent(hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
        }
    }


    private void removeContentRecursive(HibItem content) {
        content.setCollection(null);
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
       for (HibItem hibItem : collection.getItems()) {
            if (hibItem instanceof HibCollectionItem) {
                removeCollectionRecursive((HibCollectionItem) hibItem);
            } else if (hibItem instanceof HibItem) {
                hibItem.setCollection(null);
                getSession().delete(hibItem);
            } else {
                hibItem.setCollection(null);
                getSession().delete(hibItem);
            }
        }
    }

    private void removeNoteItemFromCollectionInternal(HibNoteItem note, HibCollectionItem collection) {
        getSession().update(collection);
        getSession().update(note);

        // do nothing if item doesn't belong to collection
        if (note.getCollection().getId() != collection.getId()) {
            return;
        }

        note.setCollection(null);
        getSession().delete(note);
    }


    protected void createContentInternal(HibCollectionItem parent, HibItem content) {

        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        if (getBaseModelObject(content).getId() != null) {
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

        // add parent to new content
        content.setCollection(parent);

        getSession().save(content);
        getSession().refresh(parent);
    }

    protected void createContentInternal(Set<HibCollectionItem> parents, HibItem content) {

        if (parents == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        if (getBaseModelObject(content).getId() != null) {
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
            checkForDuplicateICalUid((HibICalendarItem) content, content.getCollection());
        }

        setBaseItemProps(content);

        for (HibCollectionItem parent : parents) {
            content.setCollection(parent);
        }


        getSession().save(content);
    }

    protected void updateContentInternal(HibItem content) {

        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }

        getSession().update(content);

        if (content.getOwner() == null) {
            throw new IllegalArgumentException("content must have owner");
        }

        content.updateTimestamp();
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

        if (hibItem instanceof HibICalendarItem) {
            // verify icaluid is unique within collection
            checkForDuplicateICalUid((HibICalendarItem) hibItem, collection);
        }

        super.addItemToCollectionInternal(hibItem, collection);
    }

    @Override
    protected void removeItemFromCollectionInternal(HibItem hibItem, HibCollectionItem collection) {
        if (hibItem instanceof HibNoteItem) {
            removeNoteItemFromCollectionInternal((HibNoteItem) hibItem, collection);
        } else {
            super.removeItemFromCollectionInternal(hibItem, collection);
        }
    }

    protected void checkForDuplicateICalUid(HibICalendarItem item, HibCollectionItem parent) {
        if (item.getUid() == null) {
            return;
        }

        // Lookup item by parent/icaluid
        Query hibQuery = null;
        if (item instanceof HibNoteItem) {
            hibQuery = getSession().getNamedQuery(
                    "noteItemId.by.parent.icaluid").setParameter("parentid",
                    getBaseModelObject(parent).getId()).setParameter("icaluid", item.getUid());
        } else {
            hibQuery = getSession().getNamedQuery(
                    "icalendarItem.by.parent.icaluid").setParameter("parentid",
                    getBaseModelObject(parent).getId()).setParameter("icaluid", item.getUid());
        }
        hibQuery.setFlushMode(FlushMode.MANUAL);

        Long itemId = (Long) hibQuery.uniqueResult();

        // if icaluid is in use throw exception
        if (itemId != null) {
            // If the note is new, then its a duplicate icaluid
            if (getBaseModelObject(item).getId() == null) {
                HibItem dup = (HibItem) getSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
            // If the note exists and there is another note with the same
            // icaluid, then its a duplicate icaluid
            if (getBaseModelObject(item).getId().equals(itemId)) {
                HibItem dup = (HibItem) getSession().load(HibItem.class, itemId);
                throw new IcalUidInUseException("iCal uid" + item.getUid()
                        + " already in use for collection " + parent.getUid(),
                        item.getUid(), dup.getUid());
            }
        }
    }
}
