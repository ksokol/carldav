package org.unitedinternet.cosmo.model.hibernate;

import carldav.entity.Item;
import ezvcard.Ezvcard;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.CARD_MEDIA_TYPE;
import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.ICALENDAR_MEDIA_TYPE;

public class EntityConverter {

  public Item convertCard(Item cardItem) {
    var vcard = Ezvcard.parse(cardItem.getCalendar()).first();
    var uidString = vcard.getUid().getValue();
    uidString = "".equals(uidString) ? null : uidString;
    cardItem.setUid(uidString);
    cardItem.setMimetype(CARD_MEDIA_TYPE);

    if (vcard.getFormattedName() != null) {
      cardItem.setDisplayName(vcard.getFormattedName().getValue());
    } else {
      cardItem.setDisplayName(uidString);
    }

    return cardItem;
  }

  public Item convert(Item calendarItem) {
    try {
      var calendar = new CalendarBuilder().build(new StringReader(calendarItem.getCalendar()));
      var component = getFirstComponent(calendar.getComponents(calendarItem.getType()));
      setCalendarAttributes(calendarItem, component);
      calculateEventStampIndexes(calendar, component, calendarItem);
      calendarItem.setMimetype(ICALENDAR_MEDIA_TYPE);
      return calendarItem;
    } catch (Exception exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  public Calendar convertContent(Item item) {
    if (item.getCalendar() != null) {
      try {
        return new CalendarBuilder().build(new StringReader(item.getCalendar()));
      } catch (Exception exception) {
        throw new RuntimeException(exception.getMessage(), exception);
      }
    }
    return null;
  }

  private void setCalendarAttributes(Item note, Component component) {
    var uid = component.getProperty(Property.UID);
    String uidString = null;

    if (uid != null) {
      uidString = "".equals(uid.getValue()) ? null : uid.getValue();
    }

    note.setUid(uidString);

    var summary = component.getProperty(Property.SUMMARY);
    if (summary != null && !"".equals(summary.getValue())) {
      note.setDisplayName(summary.getValue());
    } else {
      note.setDisplayName(uidString);
    }

    var dtStamp = component.getProperty(Property.DTSTAMP);
    if (dtStamp != null) {
      note.setClientModifiedDate((((DtStamp) dtStamp).getDate()));
    }

    var startDate = ICalendarUtils.getStartDate(component);
    if (startDate != null) {
      final Date date = startDate.getDate();
      note.setStartDate(date);
      note.setEndDate(date);
    }
  }

  private Component getFirstComponent(ComponentList components) {
    for (Component c : (Iterable<Component>) components) {
      if (c.getProperty(Property.RECURRENCE_ID) == null) {
        return c;
      }
    }

    if (components.size() == 1) {
      return (Component) components.get(0);
    }
    throw new IllegalArgumentException("no component in calendar found");
  }

  private Date getEndDate(Component event) {
    if (event == null) {
      return null;
    }

    DtEnd dtEnd = null;
    if (event instanceof VEvent) {
      dtEnd = ((VEvent) event).getEndDate(false);
    }

    // if no DTEND, then calculate endDate from DURATION
    if (dtEnd == null) {
      var dtStart = ICalendarUtils.getStartDate(event);
      Date startDate = null;

      if (dtStart != null) {
        startDate = dtStart.getDate();
      }

      var duration = ICalendarUtils.getDuration(event);

      // if no DURATION, then there is no end time
      if (duration == null) {
        return null;
      }

      Date endDate;
      if (startDate instanceof DateTime) {
        endDate = new DateTime(startDate);
      } else {
        endDate = new Date(startDate);
      }

      endDate.setTime(duration.getTime(startDate).getTime());
      return endDate;
    }

    return dtEnd.getDate();
  }

  private boolean isRecurring(Component event) {
    if (getRecurrenceRules(event).size() > 0) {
      return true;
    }

    var rdates = getRecurrenceDates(event);
    return rdates != null && rdates.size() > 0;
  }

  private List<Recur> getRecurrenceRules(Component event) {
    List<Recur> l = new ArrayList<>();
    if (event == null) {
      return l;
    }

    var properties = event.getProperties().getProperties(Property.RRULE);
    for (Object rrule : properties) {
      l.add(((RRule) rrule).getRecur());
    }
    return l;
  }

  private DateList getRecurrenceDates(Component event) {
    DateList l = null;

    if (event == null) {
      return null;
    }

    for (var property : event.getProperties().getProperties(Property.RDATE)) {
      var rdate = (RDate) property;
      if (l == null) {
        if (Value.DATE.equals(rdate.getParameter(Parameter.VALUE))) {
          l = new DateList(Value.DATE);
        } else {
          l = new DateList(Value.DATE_TIME, rdate.getDates().getTimeZone());
        }
      }
      l.addAll(rdate.getDates());
    }

    return l;
  }

  private void calculateEventStampIndexes(Calendar calendar, Component event, Item note) {
    var dtStart = ICalendarUtils.getStartDate(event);
    Date startDate = null;

    if (dtStart != null) {
      startDate = dtStart.getDate();
    }

    var endDate = getEndDate(event);
    var isRecurring = false;

    if (isRecurring(event)) {
      isRecurring = true;
      var expander = new RecurrenceExpander();
      var range = expander.calculateRecurrenceRange(calendar);

      if (range.length > 0) {
        startDate = range[0];
      }

      if (range.length > 1) {
        endDate = range[1];
      }
    } else {
      // If there is no end date, then its a point-in-time event
      if (endDate == null) {
        endDate = startDate;
      }
    }

    var isFloating = false;

    // must have start date
    if (startDate == null) {
      return;
    }

    // A floating date is a DateTime with no timezone, or
    // a Date
    if (startDate instanceof DateTime) {
      var dtStart2 = (DateTime) startDate;
      if (dtStart2.getTimeZone() == null && !dtStart2.isUtc()) {
        isFloating = true;
      }
    } else {
      // Date instances are really floating because you can't pin
      // the a date like 20070101 to an instant without first
      // knowing the timezone
      isFloating = true;
    }

    var startDateTime = new DateTime(startDate);
    note.setStartDate(startDateTime);

    // A null endDate equates to infinity, which is represented by
    // a String that will always come after any date when compared.
    if (endDate != null) {
      var endDateTime = new DateTime(endDate);
      note.setEndDate(endDateTime);
    }

    note.setFloating(isFloating);
    note.setRecurring(isRecurring);
  }
}
