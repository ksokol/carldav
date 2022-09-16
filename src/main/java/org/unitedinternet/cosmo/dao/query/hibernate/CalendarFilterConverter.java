package org.unitedinternet.cosmo.dao.query.hibernate;

import carldav.entity.Item;
import carldav.repository.ItemQuery;
import net.fortuna.ical4j.model.TimeZone;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.ComponentFilter;
import org.unitedinternet.cosmo.calendar.query.PropertyFilter;
import org.unitedinternet.cosmo.model.filter.StampFilter;

import java.util.Locale;

public class CalendarFilterConverter {

  private static final String COMP_VCALENDAR = "VCALENDAR";
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
  public ItemQuery translateToItemFilter(CalendarFilter calendarFilter) {
    var query = new ItemQuery().parent(calendarFilter.getParent());
    var rootFilter = calendarFilter.getFilter();

    if (!COMP_VCALENDAR.equalsIgnoreCase(rootFilter.getName())) {
      throw new IllegalArgumentException("unsupported component filter: " + rootFilter.getName());
    }

    for (var compFilter : rootFilter.getComponentFilters()) {
      query.add(handleCompFilter(compFilter));
    }

    return query;
  }

  private ItemQuery handleCompFilter(ComponentFilter compFilter) {
    try {
      return handleEventCompFilter(compFilter, new StampFilter(Item.Type.valueOf(compFilter.getName().toUpperCase(Locale.ENGLISH))));
    } catch (Exception e) {
      throw new IllegalArgumentException("unsupported component filter: " + compFilter.getName());
    }
  }

  private ItemQuery handleEventCompFilter(ComponentFilter compFilter, StampFilter eventFilter) {
    var query = new ItemQuery();
    var trf = compFilter.getTimeRangeFilter();

    // handle time-range filter
    if (trf != null) {
      eventFilter.setPeriod(trf.getPeriod());
      if (trf.getTimezone() != null) {
        eventFilter.setTimezone(new TimeZone(trf.getTimezone()));
      }
    }

    query.stamp(eventFilter.getType(), eventFilter.getIsRecurring(), eventFilter.getStart(), eventFilter.getEnd());

    for (var subComp : compFilter.getComponentFilters()) {
      throw new IllegalArgumentException("unsupported sub component filter: " + subComp.getName());
    }

    for (var propFilter : compFilter.getPropFilters()) {
      query.add(handleEventPropFilter(propFilter));
    }

    return query;
  }

  private ItemQuery handleEventPropFilter(PropertyFilter propFilter) {
    if (PROP_UID.equalsIgnoreCase(propFilter.getName())) {
      return handlePropertyFilter("uid", propFilter);
    } else if (PROP_SUMMARY.equalsIgnoreCase(propFilter.getName())) {
      return handlePropertyFilter("displayName", propFilter);
    } else {
      throw new IllegalArgumentException("unsupported prop filter: " + propFilter.getName());
    }
  }

  private ItemQuery handlePropertyFilter(String propertyName, PropertyFilter propFilter) {
    for (var paramFilter : propFilter.getParamFilters()) {
      throw new IllegalArgumentException("unsupported param filter: " + paramFilter.getName());
    }

    var textMatch = propFilter.getTextMatchFilter();
    if (textMatch == null) {
      throw new IllegalArgumentException("unsupported filter: must contain text match filter");
    }

    return new ItemQuery().propertyLike(propertyName, textMatch.getValue(), textMatch.isCaseless(), textMatch.isNegateCondition());
  }

}
