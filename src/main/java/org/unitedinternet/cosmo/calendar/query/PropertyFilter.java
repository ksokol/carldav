package org.unitedinternet.cosmo.calendar.query;

import carldav.jackrabbit.webdav.DavConstants;
import carldav.jackrabbit.webdav.xml.DomUtils;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the CALDAV:prop-filter element. From sec 9.6.2:
 *
 * Name: prop-filter
 *
 * Namespace: urn:ietf:params:xml:ns:caldav
 *
 * Purpose: Specifies search criteria on calendar properties.
 *
 * Description: The CALDAV:prop-filter XML element specifies a search criteria
 * on a specific calendar property (e.g., CATEGORIES) in the scope of a given
 * CALDAV:comp-filter. A calendar component is said to match a
 * CALDAV:prop-filter if:
 *
 * A property of the type specified by the "name" attribute exists, and the
 * CALDAV:prop-filter is empty, or it matches the CALDAV:time-range XML element
 * or CALDAV:text-match conditions if specified, and that any
 * CALDAV:param-filter child elements also match.
 *
 * or:
 * A property of the type specified by the "name" attribute does not exist,
 * and the CALDAV:is-not-defined element is specified.
 *
 * Definition:
 *
 * <!ELEMENT prop-filter ((is-not-defined | ((time-range | text-match)?,
 * param-filter*))>
 *
 * <!ATTLIST prop-filter name CDATA #REQUIRED>
 * name value: a calendar property
 * name (e.g., "ATTENDEE")
 *
 */
public class PropertyFilter implements DavConstants, CaldavConstants {

    private final List<ParamFilter> paramFilters = new ArrayList<>();
    private IsNotDefinedFilter isNotDefinedFilter = null;
    private TimeRangeFilter timeRangeFilter = null;
    private TextMatchFilter textMatchFilter = null;
    private String name;

    public PropertyFilter(String name) {
        this.name = name;
    }

    public PropertyFilter(Element element, VTimeZone timezone) throws ParseException {
        // Name must be present
        name = DomUtils.getAttribute(element, ATTR_CALDAV_NAME);
        if (name == null) {
            throw new ParseException("CALDAV:prop-filter a calendar property name (e.g., \"ATTENDEE\") is required", -1);
        }

        var i = DomUtils.getChildren(element);
        int childCount = 0;

        while (i.hasNext()) {
            var child = i.nextElement();
            childCount++;

            // if is-not-defined is present, then nothing else can be present
            if (childCount > 1 && isNotDefinedFilter!=null) {
                throw new ParseException("CALDAV:is-not-defined cannnot be present with other child elements", -1);
            }
            if (ELEMENT_CALDAV_TIME_RANGE.equals(child.getLocalName())) {

                // Can only have one time-range or text-match
                if (timeRangeFilter != null) {
                    throw new ParseException("CALDAV:prop-filter only one time-range or text-match element permitted", -1);
                }
                timeRangeFilter = new TimeRangeFilter(child, timezone);
            } else if (ELEMENT_CALDAV_TEXT_MATCH.equals(child.getLocalName())) {
                // Can only have one time-range or text-match
                if (textMatchFilter != null) {
                    throw new ParseException("CALDAV:prop-filter only one time-range or text-match element permitted", -1);
                }
                textMatchFilter = new TextMatchFilter(child);
            } else if (ELEMENT_CALDAV_PARAM_FILTER.equals(child.getLocalName())) {
                // Add to list
                paramFilters.add(new ParamFilter(child));
            } else if (ELEMENT_CALDAV_IS_NOT_DEFINED.equals(child.getLocalName())) {
                if (childCount > 1) {
                    throw new ParseException("CALDAV:is-not-defined cannnot be present with other child elements", -1);
                }
                isNotDefinedFilter = new IsNotDefinedFilter();
            } else {
                throw new ParseException("CALDAV:prop-filter an invalid element name found", -1);
            }
        }
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

    public void setName(String name) {
        this.name = name;
    }

    public List<ParamFilter> getParamFilters() {
        return paramFilters;
    }

    public TextMatchFilter getTextMatchFilter() {
        return textMatchFilter;
    }

    public void setTextMatchFilter(TextMatchFilter textMatchFilter) {
        this.textMatchFilter = textMatchFilter;
    }

    public TimeRangeFilter getTimeRangeFilter() {
        return timeRangeFilter;
    }

    public void setTimeRangeFilter(TimeRangeFilter timeRangeFilter) {
        this.timeRangeFilter = timeRangeFilter;
    }

    public void validate() {
        if (textMatchFilter != null) {
            textMatchFilter.validate();
        }
        for (var paramFilter : paramFilters) {
            paramFilter.validate();
        }
    }
}
