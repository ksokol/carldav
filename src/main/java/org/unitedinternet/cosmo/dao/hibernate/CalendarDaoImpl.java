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

import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dao.query.hibernate.CalendarFilterConverter;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;

import java.util.Set;

public class CalendarDaoImpl implements CalendarDao {

    private final ItemFilterProcessor itemFilterProcessor;

    public CalendarDaoImpl(final ItemFilterProcessor itemFilterProcessor) {
        Assert.notNull(itemFilterProcessor, "itemFilterProcessor is null");
        this.itemFilterProcessor = itemFilterProcessor;
    }

    public Set<HibICalendarItem> findCalendarItems(HibCollectionItem collection, CalendarFilter filter) {
        CalendarFilterConverter filterConverter = new CalendarFilterConverter();

        // translate CalendarFilter to ItemFilter and execute filter
        ItemFilter itemFilter = filterConverter.translateToItemFilter(collection, filter);
        Set results = itemFilterProcessor.processFilter(itemFilter);
        return (Set<HibICalendarItem>) results;
    }
}
