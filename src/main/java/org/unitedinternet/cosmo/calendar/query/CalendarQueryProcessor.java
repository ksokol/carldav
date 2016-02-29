/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.calendar.query;

import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;

import java.util.Set;

/**
 * <p>
 * A component that accepts queries formulated against the iCalendar data
 * model and processes them against the Cosmo data model. A query processor
 * supports the following types of queries:
 * </p>
 * <dl>
 * <dt>General calendar query</dt>
 * <dd>Finds items that match one or more iCalendar components, properties
 * and/or parameters. The items may be required to occur within a specified
 * time period. Query criteria are expressed using a
 * {@link CalendarFilter}.</dd>
 * </dl>
 * <p>
 * Calendar queries will only ever match instances of {@link HibICalendarItem}.
 * </p>
 */
public interface CalendarQueryProcessor { 
 
    /**
     * <p>
     * Executes a general calendar query against a collection. Returns all
     * members that match the provided filter.
     * </p>
     * @param collection The collection.
     * @param filter The calendar filter.
     * @return All members that match the provided filter.
     */
    Set<HibICalendarItem> filterQuery(CalendarFilter filter);

    /**
     * <p>
     * Executes a general calendar query against an item. Returns true if the
     * item matches the provided filter.
     * </p>
     * @param item The ICalendar item.
     * @param filter The calendar filter.
     * @return True if the item matches the provided filter.
     */
    boolean filterQuery(HibICalendarItem item, CalendarFilter filter);
}
