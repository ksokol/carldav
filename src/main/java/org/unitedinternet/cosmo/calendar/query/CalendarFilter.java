/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import carldav.CarldavConstants;
import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import carldav.jackrabbit.webdav.xml.CustomElementIterator;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * Filter for querying calendar events. The structure of this filter is based on the structure of the <CALDAV:filter> element.
 * 
 * @author cdobrota See section 9.6 of the CalDAV spec
 */
public class CalendarFilter implements CaldavConstants {

    private ComponentFilter filter;
    private Long parent;

    public CalendarFilter() {
    }

    public CalendarFilter(Element element) throws ParseException {
        this(element, null);
    }

    /**
     * Construct a CalendarFilter object from a DOM Element.
     * 
     * @param element
     *            The element.
     * @param timezone
     *            The timezone.
     * @throws ParseException
     *             - if something is wrong this exception is thrown.
     */
    public CalendarFilter(Element element, VTimeZone timezone) throws ParseException {
        // Can only have a single comp-filter element
        final CustomElementIterator i = CustomDomUtils.getChildren(element, CarldavConstants.c(ELEMENT_CALDAV_COMP_FILTER));
        if (!i.hasNext()) {
            throw new ParseException("CALDAV:filter must contain a comp-filter", -1);
        }

        final Element child = i.nextElement();

        if (i.hasNext()) {
            throw new ParseException("CALDAV:filter can contain only one comp-filter", -1);
        }

        // Create new component filter and have it parse the element
        filter = new ComponentFilter(child, timezone);
    }

    /**
     * A CalendarFilter has exactly one ComponentFilter.
     * 
     * @return The component filter.
     */
    public ComponentFilter getFilter() {
        return filter;
    }

    public void setFilter(ComponentFilter filter) {
        this.filter = filter;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public void validate() {
        if (filter != null) {
            filter.validate();
        }
    }
}
