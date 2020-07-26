package org.unitedinternet.cosmo.calendar.query;

import carldav.jackrabbit.webdav.xml.DomUtils;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Element;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a component filter as defined in the CalDAV spec:
 *
 * from Section 9.6.1:
 *
 * CALDAV:comp-filter <!ELEMENT comp-filter (is-not-defined | (time-range?,
 * prop-filter*, comp-filter*))>
 *
 * <!ATTLIST comp-filter name CDATA #REQUIRED>
 *
 * name value: a calendar component name (e.g., "VEVENT")
 *
 * Note: This object model does not validate. For example you can create a
 * ComponentFilter with a IsNotDefinedFilter and a TimeRangeFilter. It is the
 * responsibility of the business logic to enforce this. This may be changed
 * later.
 */
public class ComponentFilter implements CaldavConstants, ICalendarConstants {

    private static final Log LOG = LogFactory.getLog(ComponentFilter.class);

    private final List<ComponentFilter> componentFilters = new ArrayList<>();
    private final List<PropertyFilter> propFilters = new ArrayList<>();
    private IsNotDefinedFilter isNotDefinedFilter;
    private TimeRangeFilter timeRangeFilter;
    private String name;

    private interface InitializationOperation {
        void initialize(Element element, VTimeZone timezone, ComponentFilter componentFilter, int childCount) throws ParseException;
    }

    private enum Initializers implements InitializationOperation {
        TIME_RANGE(ELEMENT_CALDAV_TIME_RANGE) {
            @Override
            public void initialize(Element element, VTimeZone timezone, ComponentFilter componentFilter, int childCount) throws ParseException {
             // Can only have one time-range element in a comp-filter
                if (componentFilter.timeRangeFilter != null) {
                    throw new ParseException("CALDAV:comp-filter only one time-range element permitted", -1);
                }

                componentFilter.timeRangeFilter = new TimeRangeFilter(element, timezone);
            }
        },

        COMP_FILTER(ELEMENT_CALDAV_COMP_FILTER) {
            @Override
            public void initialize(Element element, VTimeZone timezone, ComponentFilter componentFilter, int childCount) throws ParseException {
                componentFilter.componentFilters.add(new ComponentFilter(element, timezone));
            }
        },
        PROP_FILTER(ELEMENT_CALDAV_PROP_FILTER) {
            @Override
            public void initialize(Element element, VTimeZone timezone, ComponentFilter componentFilter, int childCount) throws ParseException {
                componentFilter.propFilters.add(new PropertyFilter(element, timezone));
            }
        },
        NOT_DEFINED_FILTER(ELEMENT_CALDAV_IS_NOT_DEFINED) {
            @Override
            public void initialize(Element element, VTimeZone timezone, ComponentFilter componentFilter, int childCount) throws ParseException {
                if (childCount > 1) {
                    throw new ParseException("CALDAV:is-not-defined cannnot be present with other child elements", -1);
                }
                componentFilter.isNotDefinedFilter = new IsNotDefinedFilter();
            }
        },
        DEFINED_FILTER(ELEMENT_CALDAV_IS_DEFINED) {
            @Override
            public void initialize(Element element, VTimeZone timezone, ComponentFilter componentFilter, int childCount) throws ParseException {
                // XXX provided for backwards compatibility with Evolution 2.6, which does not implement is-not-defined
                if (childCount > 1) {
                    throw new ParseException("CALDAV:is-defined cannnot be present with other child elements", -1);
                }
                LOG.warn("old style 'is-defined' ignored from (outdated) client!");
            }

        };

        private final String name;

        String getName() {
            return name;
        }

        Initializers(String name) {
            this.name = name;
        }

        static InitializationOperation getInitializer(String elementLocalName) throws ParseException {
            for (var initializer : values()) {
                if (initializer.getName().equals(elementLocalName)) {
                    return initializer;
                }
            }
            throw new ParseException("CALDAV:comp-filter an invalid element name found", -1);
        }
    }

    public ComponentFilter(Element element, VTimeZone timezone) throws ParseException {
        // Name must be present
        validateName(element);

        var i = DomUtils.getChildren(element);
        int childCount = 0;

        while (i.hasNext()) {
            var child = i.nextElement();
            childCount++;

            // if is-not-defined is present, then nothing else can be present
            validateNotDefinedState(childCount);

            Initializers.getInitializer(child.getLocalName()).initialize(child, timezone, this, childCount);
        }
    }

    private void validateName(Element element) throws ParseException {
        name = DomUtils.getAttribute(element, ATTR_CALDAV_NAME);

        if (name == null) {
            throw new ParseException("CALDAV:comp-filter a calendar component name  (e.g., \"VEVENT\") is required", -1);
        }

        if (!(name.equals(Calendar.VCALENDAR) || CalendarUtils.isSupportedComponent(name) || name.equals(Component.VALARM) || name.equals(Component.VTIMEZONE))) {
            throw new ParseException(name + " is not a supported iCalendar component", -1);
        }
    }

    private void validateNotDefinedState(int childCount) throws ParseException {
        if (childCount > 1 && isNotDefinedFilter != null) {
            throw new ParseException("CALDAV:is-not-defined cannnot be present with other child elements", -1);
        }
    }

    public ComponentFilter(String name) {
        this.name = name;
    }

    public IsNotDefinedFilter getIsNotDefinedFilter() {
        return isNotDefinedFilter;
    }

    public void setIsNotDefinedFilter(IsNotDefinedFilter isNotDefinedFilter) {
        this.isNotDefinedFilter = isNotDefinedFilter;
    }

    public String getName() {
        return name;
    }

    public List<ComponentFilter> getComponentFilters() {
        return componentFilters;
    }

    public TimeRangeFilter getTimeRangeFilter() {
        return timeRangeFilter;
    }

    public void setTimeRangeFilter(TimeRangeFilter timeRangeFilter) {
        this.timeRangeFilter = timeRangeFilter;
    }

    public List<PropertyFilter> getPropFilters() {
        return propFilters;
    }

    public void validate() {
        for (var componentFilter : componentFilters) {
            componentFilter.validate();
        }
        for (var propFilter : propFilters) {
            propFilter.validate();
        }
    }
}
