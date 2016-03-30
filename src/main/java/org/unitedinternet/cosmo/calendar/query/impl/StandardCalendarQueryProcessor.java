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

import carldav.entity.Item;
import carldav.repository.ItemRepository;
import net.fortuna.ical4j.model.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dao.query.hibernate.CalendarFilterConverter;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import java.util.List;

import static carldav.repository.specification.ItemSpecs.combine;

public class StandardCalendarQueryProcessor implements CalendarQueryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StandardCalendarQueryProcessor.class);

    private static final CalendarFilterConverter filterConverter = new CalendarFilterConverter();
    private static final EntityConverter entityConverter = new EntityConverter();

    private final ItemRepository itemRepository;

    public StandardCalendarQueryProcessor(ItemRepository itemRepository) {
        Assert.notNull(itemRepository, "itemRepository is null");
        this.itemRepository = itemRepository;
    }

    public List<Item> filterQuery(CalendarFilter filter) {
        return itemRepository.findAll(combine(filterConverter.translateToItemFilter(filter)));
    }

    /**
     * Filter query.
     * @param item The ICalendar item.
     * @param filter The calendar filter.
     * @return The calendar filter evaluater.
     */
    public boolean filterQuery(Item item, CalendarFilter filter) {
        LOG.debug("matching item {} to filter {}", item.getUid(), filter);
        Calendar calendar = entityConverter.convertContent(item);
        if(calendar != null) {
            return new CalendarFilterEvaluater().evaluate(calendar, filter);
        }
        return false;
    }
}
