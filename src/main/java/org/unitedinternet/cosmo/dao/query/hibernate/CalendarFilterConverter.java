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
package org.unitedinternet.cosmo.dao.query.hibernate;

import carldav.entity.Item;
import carldav.repository.specification.ItemSpecs;
import net.fortuna.ical4j.model.TimeZone;
import org.springframework.data.jpa.domain.Specification;
import org.unitedinternet.cosmo.calendar.query.*;
import org.unitedinternet.cosmo.model.filter.StampFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Translates <code>CalendarFilter</code> into <code>ItemFilter</code>
 */
public class CalendarFilterConverter {

    private static final String COMP_VCALENDAR = "VCALENDAR";
    private static final String COMP_VTODO = "VTODO";
    private static final String PROP_UID = "UID";
    private static final String PROP_SUMMARY = "SUMMARY";

    /**
     * Tranlsate CalendarFilter to an equivalent ItemFilter.
     * For now, only the basic CalendarFilter is supported, which is
     * essentially a timerange filter.  The majority of CalendarFilters
     * will fall into this case.  More cases will be supported as they
     * are implemented.
     *
     * @param calendarFilter filter to translate
     * @return equivalent List<Specification<Item>>
     */
    public List<Specification<Item>> translateToItemFilter(CalendarFilter calendarFilter) {
        List<Specification<Item>> specifications = new ArrayList<>(5);
        specifications.add(ItemSpecs.parent(calendarFilter.getParent()));
        ComponentFilter rootFilter = calendarFilter.getFilter();
        if (!COMP_VCALENDAR.equalsIgnoreCase(rootFilter.getName())) {
            throw new IllegalArgumentException("unsupported component filter: " + rootFilter.getName());
        }

        for (Iterator it = rootFilter.getComponentFilters().iterator(); it.hasNext(); ) {
            ComponentFilter compFilter = (ComponentFilter) it.next();
            specifications.addAll(handleCompFilter(compFilter));
        }

        return specifications;
    }

    private List<Specification<Item>> handleCompFilter(ComponentFilter compFilter) {
        try {
            return handleEventCompFilter(compFilter, new StampFilter(Item.Type.valueOf(compFilter.getName().toUpperCase(Locale.ENGLISH))));
        } catch(Exception e) {
            throw new IllegalArgumentException("unsupported component filter: " + compFilter.getName());
        }
    }

    private List<Specification<Item>> handleEventCompFilter(ComponentFilter compFilter, StampFilter eventFilter) {
        List<Specification<Item>> specifications = new ArrayList<>(5);
        TimeRangeFilter trf = compFilter.getTimeRangeFilter();

        // handle time-range filter
        if (trf != null) {
            eventFilter.setPeriod(trf.getPeriod());
            if (trf.getTimezone() != null) {
                eventFilter.setTimezone(new TimeZone(trf.getTimezone()));
            }
        }

        specifications.add(ItemSpecs.stamp(eventFilter.getType(), eventFilter.getIsRecurring(), eventFilter.getStart(), eventFilter.getEnd()));

        for (Iterator it = compFilter.getComponentFilters().iterator(); it.hasNext(); ) {
            ComponentFilter subComp = (ComponentFilter) it.next();
            throw new IllegalArgumentException("unsupported sub component filter: " + subComp.getName());
        }

        for (Iterator it = compFilter.getPropFilters().iterator(); it.hasNext(); ) {
            PropertyFilter propFilter = (PropertyFilter) it.next();
            specifications.add(handleEventPropFilter(propFilter));
        }

        return specifications;
    }

    private Specification<Item> handleEventPropFilter(PropertyFilter propFilter) {
        if (PROP_UID.equalsIgnoreCase(propFilter.getName())) {
            return handlePropertyFilter("uid", propFilter);
        } else if (PROP_SUMMARY.equalsIgnoreCase(propFilter.getName())) {
            return handlePropertyFilter("displayName", propFilter);
        } else {
            throw new IllegalArgumentException("unsupported prop filter: " + propFilter.getName());
        }
    }

    private Specification<Item> handlePropertyFilter(String propertyName, PropertyFilter propFilter) {
        for (Iterator it = propFilter.getParamFilters().iterator(); it.hasNext(); ) {
            ParamFilter paramFilter = (ParamFilter) it.next();
            throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
        }

        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        if (textMatch == null) {
            throw new IllegalArgumentException("unsupported filter: must contain text match filter");
        }

        return ItemSpecs.propertyLike(propertyName, textMatch.getValue(), textMatch.isCaseless(), textMatch.isNegateCondition());
    }

}
