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

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.List;
import java.util.Set;

public class ItemDaoImpl implements ItemDao {

    private final ItemFilterProcessor itemFilterProcessor;
    private final SessionFactory sessionFactory;

    public ItemDaoImpl(ItemFilterProcessor itemFilterProcessor, SessionFactory sessionFactory) {
        Assert.notNull(itemFilterProcessor, "itemFilterProcessor is null");
        Assert.notNull(sessionFactory, "sessionFactory is null");
        this.itemFilterProcessor = itemFilterProcessor;
        this.sessionFactory = sessionFactory;
    }

    public void remove(HibItem hibItem) {
        final HibCollectionItem collection = hibItem.getCollection();
        sessionFactory.getCurrentSession().delete(hibItem);
        sessionFactory.getCurrentSession().refresh(collection);
        sessionFactory.getCurrentSession().flush();
    }

    public List<HibItem> findByCollectionIdAndType(Long id, HibItem.Type type) {
        Query hibQuery = sessionFactory.getCurrentSession().getNamedQuery("item.findByCollectionIdAndType")
                .setParameter("parent", id)
                .setParameter("type", type);
        return hibQuery.list();
    }

    @Override
    public HibItem save(HibItem item) {
        sessionFactory.getCurrentSession().saveOrUpdate(item);
        sessionFactory.getCurrentSession().flush();
        return item;
    }

    public Set<HibItem> findCalendarItems(ItemFilter itemFilter) {
        return itemFilterProcessor.processFilter(itemFilter);
    }

    @Override
    public HibItem findByOwnerAndName(String owner, String name) {
        return (HibItem) sessionFactory.getCurrentSession().getNamedQuery("item.findByOwnerAndName")
                .setParameter("owner",owner)
                .setParameter("name", name)
                .uniqueResult();
    }
}
