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

import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dao.query.hibernate.CalendarFilterConverter;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.HashSet;
import java.util.Set;

public class CalendarDaoImpl extends AbstractDaoImpl implements CalendarDao {

    private static final Log LOG = LogFactory.getLog(CalendarDaoImpl.class);

    private final ItemFilterProcessor itemFilterProcessor;
    private final EntityConverter entityConverter;

    public CalendarDaoImpl(final EntityConverter entityConverter, final ItemFilterProcessor itemFilterProcessor) {
        Assert.notNull(entityConverter, "entityConverter is null");
        Assert.notNull(itemFilterProcessor, "itemFilterProcessor is null");
        this.entityConverter = entityConverter;
        this.itemFilterProcessor = itemFilterProcessor;
    }

    public Set<HibICalendarItem> findCalendarItems(HibCollectionItem collection,
                                                CalendarFilter filter) {

        try {
            CalendarFilterConverter filterConverter = new CalendarFilterConverter();
            try {
                // translate CalendarFilter to ItemFilter and execute filter
                ItemFilter itemFilter = filterConverter.translateToItemFilter(collection, filter);
                Set results = itemFilterProcessor.processFilter(itemFilter);
                return (Set<HibICalendarItem>) results;
            } catch (IllegalArgumentException e) {
                LOG.warn(e.getMessage(), e);
            }

            // Use brute-force method if CalendarFilter can't be translated
            // to an ItemFilter (slower but at least gets the job done).
            HashSet<HibICalendarItem> results = new HashSet<HibICalendarItem>();
            Set<HibItem> itemsToProces = null;

            // Optimization:
            // Do a first pass query if possible to reduce the number
            // of items we have to examine.  Otherwise we have to examine
            // all items.
            ItemFilter firstPassItemFilter = filterConverter.getFirstPassFilter(collection, filter);
            if (firstPassItemFilter != null) {
                itemsToProces = itemFilterProcessor.processFilter(firstPassItemFilter);
            } else {
                itemsToProces = collection.getItems();
            }

            CalendarFilterEvaluater evaluater = new CalendarFilterEvaluater();

            // Evaluate filter against all calendar items
            for (HibItem child : itemsToProces) {

                // only care about calendar items
                if (child instanceof HibICalendarItem) {

                    HibICalendarItem content = (HibICalendarItem) child;
                    Calendar calendar = entityConverter.convertContent(content);

                    if (calendar != null && evaluater.evaluate(calendar, filter)) {
                        results.add(content);
                    }
                }
            }

            return results;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }
}
