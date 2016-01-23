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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.UnresolvableObjectException;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.security.core.token.TokenService;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.ItemNotFoundException;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.UidInUseException;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;


/**
 * Implementation of ItemDao using Hibernate persistent objects.
 */
public abstract class ItemDaoImpl extends AbstractDaoImpl implements ItemDao {

    private static final Log LOG = LogFactory.getLog(ItemDaoImpl.class);

    private IdGenerator idGenerator = null;
    private TokenService ticketKeyGenerator = null;
    private ItemPathTranslator itemPathTranslator = null;
    private ItemFilterProcessor itemFilterProcessor = null;

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByPath(java.lang.String)
     */
    public HibItem findItemByPath(String path) {
        try {
            HibItem dbHibItem = itemPathTranslator.findItemByPath(path);
            return dbHibItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemParentByPath(java.lang.String)
     */
    public HibItem findItemParentByPath(String path) {
        try {
            HibItem dbHibItem = itemPathTranslator.findItemParent(path);
            return dbHibItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByUid(java.lang.String)
     */
    public HibItem findItemByUid(String uid) {
        try {
            // prevent auto flushing when looking up item by uid
            getSession().setFlushMode(FlushMode.MANUAL);
            return (HibItem) getSession().byNaturalId(HibItem.class).using("uid", uid).load();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItem(org.unitedinternet.cosmo.model.Item)
     */
    public void removeItem(HibItem hibItem) {
        try {

            if (hibItem == null) {
                throw new IllegalArgumentException("item cannot be null");
            }

            if (hibItem instanceof HibHomeCollectionItem) {
                throw new IllegalArgumentException("cannot remove root item");
            }

            getSession().delete(hibItem);
            getSession().flush();

        } catch (ObjectNotFoundException onfe) {
            throw new ItemNotFoundException("item not found");
        } catch (ObjectDeletedException ode) {
            throw new ItemNotFoundException("item not found");
        } catch (UnresolvableObjectException uoe) {
            throw new ItemNotFoundException("item not found");
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#getRootItem(org.unitedinternet.cosmo.model.User)
     */
    public HibHomeCollectionItem getRootItem(User user) {
        try {
            return findRootItem(user.getId());
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#createRootItem(org.unitedinternet.cosmo.model.User)
     */
    public HibHomeCollectionItem createRootItem(User user) {
        try {

            if (user == null) {
                throw new IllegalArgumentException("invalid user");
            }

            if (findRootItem(user.getId()) != null) {
                throw new CosmoException("user already has root item", new CosmoException());
            }

            HibHomeCollectionItem newItem = new HibHomeCollectionItem();

            newItem.setOwner(user);
            newItem.setName(user.getEmail());
            //do not set this, it might be sensitive or different than name
            //newItem.setDisplayName(newItem.getName());
            setBaseItemProps(newItem);
            getSession().save(newItem);
            getSession().flush();
            return newItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    public void addItemToCollection(HibItem hibItem, HibCollectionItem collection) {
        try {
            addItemToCollectionInternal(hibItem, collection);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection) {
        try {
            removeItemFromCollectionInternal(hibItem, collection);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItemByPath(java.lang.String)
     */
    public void removeItemByPath(String path) {
        try {
            HibItem hibItem = itemPathTranslator.findItemByPath(path);
            if (hibItem == null) {
                throw new ItemNotFoundException("item at " + path
                        + " not found");
            }
            removeItem(hibItem);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItemByUid(java.lang.String)
     */
    public void removeItemByUid(String uid) {
        try {
            HibItem hibItem = findItemByUid(uid);
            if (hibItem == null) {
                throw new ItemNotFoundException("item with uid " + uid
                        + " not found");
            }
            removeItem(hibItem);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#initializeItem(org.unitedinternet.cosmo.model.Item)
     */
    public void initializeItem(HibItem hibItem) {
        try {
            LOG.info("initialize Item : "+ hibItem.getUid());
            // initialize all the proxied-associations, to prevent
            // lazy-loading of this data
            Hibernate.initialize(hibItem.getStamps());
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /**
     * find the set of collection items as children of the given collection item.
     *
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem){
        try {
            HashSet<HibCollectionItem> children = new HashSet<HibCollectionItem>();
            Query hibQuery = getSession().getNamedQuery("collections.children.by.parent")
                    .setParameter("parent", hibCollectionItem);

            List<?> results = hibQuery.list();
            for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
                HibCollectionItem content = (HibCollectionItem) it.next();
                children.add(content);
            }
            return children;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<HibItem> findCollectionFileItems(HibCollectionItem hibCollectionItem){
        try {
            HashSet<HibItem> children = new HashSet<HibItem>();
            Query hibQuery = getSession().getNamedQuery("collections.files.by.parent")
                    .setParameter("parent", hibCollectionItem);

            List<?> results = hibQuery.list();
            for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
                HibItem content = (HibItem) it.next();
                children.add(content);
            }
            return children;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /**
     * Set the unique ID generator for new items
     *
     * @param idGenerator
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Set the unique key generator for new tickets
     *
     * @param ticketKeyGenerator
     */
    public void setTicketKeyGenerator(TokenService ticketKeyGenerator) {
        this.ticketKeyGenerator = ticketKeyGenerator;
    }

    /**
     * Set the path translator. The path translator is responsible for
     * translating a path to an item in the database.
     *
     * @param itemPathTranslator
     */
    public void setItemPathTranslator(ItemPathTranslator itemPathTranslator) {
        this.itemPathTranslator = itemPathTranslator;
    }


    public void setItemFilterProcessor(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.Dao#destroy()
     */
    public abstract void destroy();

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.Dao#init()
     */
    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }

        if (ticketKeyGenerator == null) {
            throw new IllegalStateException("ticketKeyGenerator is required");
        }

        if (itemPathTranslator == null) {
            throw new IllegalStateException("itemPathTranslator is required");
        }

        if (itemFilterProcessor == null) {
            throw new IllegalStateException("itemFilterProcessor is required");
        }

    }

    /**
     * Verifies that name is unique in collection, meaning no item exists
     * in collection with the same item name.
     *
     * @param hibItem       item name to check
     * @param collection collection to check against
     * @throws org.unitedinternet.cosmo.dao.DuplicateItemNameException if item with same name exists
     *                                    in collection
     */
    protected void verifyItemNameUnique(HibItem hibItem, HibCollectionItem collection) {
        Query hibQuery = getSession().getNamedQuery("itemId.by.parentId.name");
        hibQuery.setParameter("name", hibItem.getName()).setParameter("parentid",
                ((HibItem) collection).getId());
        List<Long> results = hibQuery.list();
        if (results.size() > 0) {
            throw new DuplicateItemNameException(hibItem, "item name " + hibItem.getName() +
                    " already exists in collection " + collection.getUid());
        }
    }

    // Set server generated item properties
    protected void setBaseItemProps(HibItem hibItem) {
        if (hibItem.getUid() == null) {
            hibItem.setUid(idGenerator.nextStringIdentifier());
        }
        if (hibItem.getName() == null) {
            hibItem.setName(hibItem.getUid());
        }
        if (hibItem instanceof HibICalendarItem) {
            HibICalendarItem ical = (HibICalendarItem) hibItem;
            if (ical.getIcalUid() == null) {
                ical.setIcalUid(hibItem.getUid());
                HibEventStamp es = HibEventStamp.getStamp(ical);
                if (es != null) {
                    es.setIcalUid(ical.getIcalUid());
                }
            }
        }
    }

    protected HibHomeCollectionItem findRootItem(Long dbUserId) {
        Query hibQuery = getSession().getNamedQuery(
                "homeCollection.by.ownerId").setParameter("ownerid",
                dbUserId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);

        return (HibHomeCollectionItem) hibQuery.uniqueResult();
    }

    protected void checkForDuplicateUid(HibItem hibItem) {
        // verify uid not in use
        if (hibItem.getUid() != null) {

            // Lookup item by uid
            Query hibQuery = getSession().getNamedQuery("itemid.by.uid")
                    .setParameter("uid", hibItem.getUid());
            hibQuery.setFlushMode(FlushMode.MANUAL);

            Long itemId = (Long) hibQuery.uniqueResult();

            // if uid is in use throw exception
            if (itemId != null) {
                throw new UidInUseException(hibItem.getUid(), "uid " + hibItem.getUid()
                        + " already in use");
            }
        }
    }

    protected void removeItemFromCollectionInternal(HibItem hibItem, HibCollectionItem collection) {

        getSession().update(collection);
        getSession().update(hibItem);

        // do nothing if item doesn't belong to collection
        if (hibItem.getCollection().getId() != collection.getId()) {
            return;
        }

        hibItem.setCollection(null);
        getSession().delete(hibItem);
    }

    protected void addItemToCollectionInternal(HibItem hibItem,
                                               HibCollectionItem collection) {
        verifyItemNameUnique(hibItem, collection);
        getSession().update(hibItem);
        getSession().update(collection);
        ((HibItem) hibItem).setCollection(collection);
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }
}
