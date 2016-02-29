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
import org.hibernate.Query;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class ItemDaoImpl extends AbstractDaoImpl implements ItemDao {

    private final ItemPathTranslator itemPathTranslator;

    public ItemDaoImpl(final ItemPathTranslator itemPathTranslator) {
        Assert.notNull(itemPathTranslator, "itemPathTranslator is null");
        this.itemPathTranslator = itemPathTranslator;
    }

    public HibItem findItemByPath(String path) {
        return itemPathTranslator.findItemByPath(path);
    }

    public HibItem findItemByUid(String uid) {
        getSession().setFlushMode(FlushMode.MANUAL);
        return (HibItem) getSession().createQuery("select item from HibItem item where item.uid = :uid").setParameter("uid", uid).uniqueResult();
    }

    public void removeItem(HibItem hibItem) {
        if (hibItem == null) {
            throw new IllegalArgumentException("item cannot be null");
        }

        if (hibItem instanceof HibHomeCollectionItem) {
            throw new IllegalArgumentException("cannot remove root item");
        }

        getSession().refresh(hibItem);
        hibItem.setCollection(null);
        getSession().delete(hibItem);
        getSession().flush();
    }

    public HibHomeCollectionItem getRootItem(User user) {
        return findRootItem(user.getId());
    }

    public HibHomeCollectionItem createRootItem(User user) {
        if (user == null) {
            throw new IllegalArgumentException("invalid user");
        }

        if (findRootItem(user.getId()) != null) {
            throw new CosmoException("user already has root item", new CosmoException());
        }

        HibHomeCollectionItem newItem = new HibHomeCollectionItem();

        newItem.setOwner(user);
        newItem.setName(user.getEmail());
        newItem.setDisplayName("homeCollection");

        getSession().save(newItem);
        getSession().refresh(user);
        getSession().flush();
        return newItem;
    }

    public void removeItemFromCollection(HibItem hibItem, HibCollectionItem collection) {
        removeItemFromCollectionInternal(hibItem, collection);
        getSession().flush();
    }

    /**
     * find the set of collection items as children of the given collection item.
     *
     * @param hibCollectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<HibCollectionItem> findCollectionItems(HibCollectionItem hibCollectionItem){
        HashSet<HibCollectionItem> children = new HashSet<HibCollectionItem>();
        Query hibQuery = getSession().getNamedQuery("collections.children.by.parent")
                .setParameter("parent", hibCollectionItem);

        List<?> results = hibQuery.list();
        for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
            HibCollectionItem content = (HibCollectionItem) it.next();
            children.add(content);
        }
        return children;
    }

    public Set<HibItem> findCollectionFileItems(HibCollectionItem hibCollectionItem){
        HashSet<HibItem> children = new HashSet<HibItem>();
        Query hibQuery = getSession().getNamedQuery("collections.files.by.parent")
                .setParameter("parent", hibCollectionItem);

        List<?> results = hibQuery.list();
        for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
            HibItem content = (HibItem) it.next();
            children.add(content);
        }
        return children;
    }

    protected HibHomeCollectionItem findRootItem(Long dbUserId) {
        Query hibQuery = getSession().getNamedQuery(
                "homeCollection.by.ownerId").setParameter("ownerid",
                dbUserId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);

        return (HibHomeCollectionItem) hibQuery.uniqueResult();
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
        getSession().refresh(collection);
    }

    @Override
    public HibItem save(HibItem item) {
        getSession().saveOrUpdate(item);
        getSession().flush();
        return item;
    }
}
