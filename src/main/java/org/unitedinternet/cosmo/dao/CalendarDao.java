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
package org.unitedinternet.cosmo.dao;

import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;

import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.User;

/**
 * Interface for DAO that provides query apis for finding 
 * ContentItems with EventStamps matching certain criteria.
 * 
 */
public interface CalendarDao {

    /**
     * Find calendar items by calendar filter.  Calendar filter is
     * based on the CalDAV filter element.
     *
     * @param collection
     *            collection to search
     * @param filter
     *            filter to use in search
     * @return set ICalendar objects that match specified
     *         filter.
     */
    public Set<ICalendarItem> findCalendarItems(CollectionItem collection,
                                             CalendarFilter filter);
}
