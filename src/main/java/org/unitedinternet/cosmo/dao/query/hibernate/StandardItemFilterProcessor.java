/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.query.hibernate;

import carldav.entity.Item;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.ItemFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Standard Implementation of <code>ItemFilterProcessor</code>.
 * Translates filter into HQL Query, executes
 * query and processes the results.
 */
public class StandardItemFilterProcessor implements ItemFilterProcessor {

    private static final CalendarFilterConverter filterConverter = new CalendarFilterConverter();

    @PersistenceContext
    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Item> processFilter(CalendarFilter filter) {
        final ItemFilter itemFilter = filterConverter.translateToItemFilter(filter);
        Query hibQuery = buildQuery(entityManager, itemFilter);
        return hibQuery.getResultList();
    }

    /**
     * Build Hibernate Query from ItemFilter using HQL.
     * The query returned is essentially the first pass at
     * retrieving the matched items.  A second pass is required in
     * order determine if any recurring events match a timeRange
     * in the filter.  This is due to the fact that recurring events
     * may have complicated recurrence rules that are extremely
     * hard to match using HQL.
     *
     * @param session session
     * @param filter  item filter
     * @return hibernate query built using HQL
     */
    public Query buildQuery(EntityManager session, ItemFilter filter) {
        StringBuffer selectBuf = new StringBuffer();
        StringBuffer whereBuf = new StringBuffer();
        Map<String, Object> params = new TreeMap<>();

        filter.bind(selectBuf, whereBuf, params);

        selectBuf.append(whereBuf);

        Query hqlQuery = session.createQuery(selectBuf.toString());

        for (Entry<String, Object> param : params.entrySet()) {
            hqlQuery.setParameter(param.getKey(), param.getValue());
        }

        if (filter.getMaxResults() != null) {
            hqlQuery.setMaxResults(filter.getMaxResults());
        }

        return hqlQuery;
    }
}
