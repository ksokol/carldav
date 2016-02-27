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
import org.hibernate.Query;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import java.util.Date;

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

        // verify uid not in use
        checkForDuplicateUid(collection);

        setBaseItemProps(collection);
        collection.setCollection(parent);

        getSession().save(collection);
        getSession().refresh(parent);
        getSession().flush();

        return collection;
    }

    public HibItem createContent(HibCollectionItem parent, HibItem content) {
        createContentInternal(parent, content);
        getSession().flush();
        return content;
    }

    public HibCollectionItem updateCollectionTimestamp(HibCollectionItem collection) {
        if (!getSession().contains(collection)) {
            collection = (HibCollectionItem) getSession().merge(collection);
        }
        collection.setModifiedDate(new Date());
        getSession().flush();
        return collection;
    }

    public HibItem updateContent(HibItem content) {
        updateContentInternal(content);
        getSession().flush();
        return content;
    }

    public void removeCollection(HibCollectionItem collection) {
        removeContent(collection);
    }

    public void removeContent(HibItem content) {
        Assert.notNull(content, "content is null");
        getSession().refresh(content);
        content.setCollection(null);
        getSession().delete(content);
        getSession().flush();
    }

    @Override
    public void removeItem(HibItem hibItem) {
        remove(hibItem);
    }

    @Override
    public void removeItemByPath(String path) {
        remove(this.findItemByPath(path));
    }

    @Override
    public void removeItemByUid(String uid) {
        remove(this.findItemByUid(uid));
    }

    private void remove(HibItem hibItem) {
        if (hibItem instanceof HibItem) {
            removeContent(hibItem);
        } else if (hibItem instanceof HibCollectionItem) {
            removeCollection((HibCollectionItem) hibItem);
        } else {
            super.removeItem(hibItem);
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

    protected void updateContentInternal(HibItem content) {
        getSession().update(content);
        content.setModifiedDate(new Date());
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
