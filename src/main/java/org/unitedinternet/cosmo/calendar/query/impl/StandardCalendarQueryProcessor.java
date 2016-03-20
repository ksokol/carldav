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
package org.unitedinternet.cosmo.calendar.query.impl;

import net.fortuna.ical4j.model.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import carldav.repository.ItemDao;
import org.unitedinternet.cosmo.dao.query.hibernate.CalendarFilterConverter;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import carldav.entity.HibItem;

import java.util.HashSet;
import java.util.Set;

public class StandardCalendarQueryProcessor implements CalendarQueryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StandardCalendarQueryProcessor.class);

    private static final CalendarFilterConverter filterConverter = new CalendarFilterConverter();

    private final ItemDao itemDao;
    private final EntityConverter entityConverter;

    public StandardCalendarQueryProcessor(ItemDao itemDao, final EntityConverter entityConverter) {
        Assert.notNull(entityConverter, "entityConverter is null");
        Assert.notNull(itemDao, "itemDao is null");
        this.entityConverter = entityConverter;
        this.itemDao = itemDao;
    }

    public Set<HibItem> filterQuery(CalendarFilter filter) {
        return new HashSet<>(itemDao.findCalendarItems(filterConverter.translateToItemFilter(filter)));
    }

    /**
     * Filter query.
     * @param item The ICalendar item.
     * @param filter The calendar filter.
     * @return The calendar filter evaluater.
     */
    public boolean filterQuery(HibItem item, CalendarFilter filter) {
        LOG.debug("matching item {} to filter {}", item.getUid(), filter);
        Calendar calendar = entityConverter.convertContent(item);
        if(calendar != null) {
            return new CalendarFilterEvaluater().evaluate(calendar, filter);
        }
        return false;
    }
}
