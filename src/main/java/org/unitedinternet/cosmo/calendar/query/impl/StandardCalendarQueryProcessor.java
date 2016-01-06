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
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import java.util.HashSet;
import java.util.Set;

public class StandardCalendarQueryProcessor implements CalendarQueryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StandardCalendarQueryProcessor.class);

    private CalendarDao calendarDao;
    private EntityConverter entityConverter = new EntityConverter(null);

    /**
     * Filter query.
     * @param collection The collection item.
     * @param filter The calendar filter.
     * @return The calendar items.
     */
    public Set<ICalendarItem> filterQuery(CollectionItem collection, CalendarFilter filter) {
        LOG.debug("finding events in collection {} by filter {}", collection.getUid(),filter);
        return new HashSet<>(calendarDao.findCalendarItems(collection, filter));
    }

    /**
     * Filter query.
     * @param item The ICalendar item.
     * @param filter The calendar filter.
     * @return The calendar filter evaluater.
     */
    public boolean filterQuery(ICalendarItem item, CalendarFilter filter) {
        LOG.debug("matching item {} to filter {}", item.getUid(), filter);
        Calendar calendar = entityConverter.convertContent(item);
        if(calendar != null) {
            return new CalendarFilterEvaluater().evaluate(calendar, filter);
        }
        return false;
    }

    public void setCalendarDao(CalendarDao calendarDao) {
        this.calendarDao = calendarDao;
    }

}
