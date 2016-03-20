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
package org.unitedinternet.cosmo.model.hibernate;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.calendar.RecurrenceExpander;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.CARD_MEDIA_TYPE;
import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.ICALENDAR_MEDIA_TYPE;

public class EntityConverter {

    public HibItem convertCard(HibItem cardItem) {
        VCard vcard = Ezvcard.parse(cardItem.getCalendar()).first();
        String uidString = vcard.getUid().getValue();
        uidString = "".equals(uidString) ? null : uidString;
        cardItem.setUid(uidString);
        cardItem.setMimetype(CARD_MEDIA_TYPE);

        if(vcard.getFormattedName() != null) {
            cardItem.setDisplayName(vcard.getFormattedName().getValue());
        } else {
            cardItem.setDisplayName(uidString);
        }

        return cardItem;
    }

    public HibItem convert(HibItem calendarItem) {
        try {
            final Calendar calendar = new CalendarBuilder().build(new StringReader(calendarItem.getCalendar()));
            Component component = getFirstComponent(calendar.getComponents(calendarItem.getType().name()));
            setCalendarAttributes(calendarItem, component);
            calculateEventStampIndexes(calendar, component, calendarItem);
            calendarItem.setMimetype(ICALENDAR_MEDIA_TYPE);
            return calendarItem;
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    public Calendar convertContent(HibItem item) {
        if(item.getCalendar() != null) {
            try {
                return new CalendarBuilder().build(new StringReader(item.getCalendar()));
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
        }
        return null;
    }

    private void setCalendarAttributes(HibItem note, Component component) {
        final Property uid = component.getProperty(Property.UID);
        String uidString = null;
        if(uid != null) {
            uidString = "".equals(uid.getValue()) ? null : uid.getValue();
        }

        note.setUid(uidString);

        final Property summary = component.getProperty(Property.SUMMARY);
        if(summary != null && !"".equals(summary.getValue())) {
            note.setDisplayName(summary.getValue());
        } else {
            note.setDisplayName(uidString);
        }

        final Property dtStamp = component.getProperty(Property.DTSTAMP);
        if(dtStamp != null) {
            note.setClientModifiedDate((((DtStamp) dtStamp).getDate()));
        }

        final DtStart startDate = ICalendarUtils.getStartDate(component);
        if(startDate != null) {
            final Date date = startDate.getDate();
            note.setStartDate(date);
            note.setEndDate(date);
        }

        VAlarm va = ICalendarUtils.getDisplayAlarm(component);
        if (va != null && va.getTrigger()!=null) {
            Trigger trigger = va.getTrigger();
            Date reminderTime = trigger.getDateTime();
            if (reminderTime != null) {
                note.setRemindertime(reminderTime);
            }
        }
    }

    private Component getFirstComponent(ComponentList components) {
        Iterator<Component> it = components.iterator();
        while(it.hasNext()) {
            Component c = it.next();
            if(c.getProperty(Property.RECURRENCE_ID)==null) {
                return c;
            }
        }

        if(components.size() == 1) {
            return (Component) components.get(0);
        }
        throw new IllegalArgumentException("no component in calendar found");
    }

    private Date getEndDate(Component event) {
        if(event==null) {
            return null;
        }

        DtEnd dtEnd = null;
        if(event instanceof VEvent) {
            dtEnd = ((VEvent) event).getEndDate(false);
        }

        // if no DTEND, then calculate endDate from DURATION
        if (dtEnd == null) {
            final DtStart dtStart = ICalendarUtils.getStartDate(event);
            Date startDate = null;

            if(dtStart != null) {
                startDate = dtStart.getDate();
            }

            Dur duration = ICalendarUtils.getDuration(event);

            // if no DURATION, then there is no end time
            if(duration==null) {
                return null;
            }

            Date endDate;
            if(startDate instanceof DateTime) {
                endDate = new DateTime(startDate);
            }
            else {
                endDate = new Date(startDate);
            }

            endDate.setTime(duration.getTime(startDate).getTime());
            return endDate;
        }

        return dtEnd.getDate();
    }

    private boolean isRecurring(Component event) {
        if(getRecurrenceRules(event).size()>0) {
            return true;
        }

        final DateList rdates = getRecurrenceDates(event);

        return rdates!=null && rdates.size()>0;
    }

    private List<Recur> getRecurrenceRules(Component event) {
        final List<Recur> l = new ArrayList<>();
        if(event==null) {
            return l;
        }
        final PropertyList properties = event.getProperties().getProperties(Property.RRULE);
        for (Object rrule : properties) {
            l.add(((RRule)rrule).getRecur());

        }
        return l;
    }

    private DateList getRecurrenceDates(Component event) {
        DateList l = null;

        if(event==null) {
            return null;
        }

        for (Object property : event.getProperties().getProperties(Property.RDATE)) {
            RDate rdate = (RDate) property;
            if(l==null) {
                if(Value.DATE.equals(rdate.getParameter(Parameter.VALUE))) {
                    l = new DateList(Value.DATE);
                }
                else {
                    l = new DateList(Value.DATE_TIME, rdate.getDates().getTimeZone());
                }
            }
            l.addAll(rdate.getDates());
        }

        return l;
    }

    private void calculateEventStampIndexes(Calendar calendar, Component event, HibItem note) {
        final DtStart dtStart = ICalendarUtils.getStartDate(event);
        Date startDate = null;

        if(dtStart != null) {
            startDate = dtStart.getDate();
        }

        Date endDate = getEndDate(event);

        boolean isRecurring = false;

        if (isRecurring(event)) {
            isRecurring = true;
            RecurrenceExpander expander = new RecurrenceExpander();
            Date[] range = expander.calculateRecurrenceRange(calendar);

            if(range.length > 0) {
                startDate = range[0];
            }

            if(range.length > 1) {
                endDate = range[1];
            }
        } else {
            // If there is no end date, then its a point-in-time event
            if (endDate == null) {
                endDate = startDate;
            }
        }

        boolean isFloating = false;

        // must have start date
        if(startDate==null) {
            return;
        }

        // A floating date is a DateTime with no timezone, or
        // a Date
        if(startDate instanceof DateTime) {
            DateTime dtStart2 = (DateTime) startDate;
            if(dtStart2.getTimeZone()==null && !dtStart2.isUtc()) {
                isFloating = true;
            }
        } else {
            // Date instances are really floating because you can't pin
            // the a date like 20070101 to an instant without first
            // knowing the timezone
            isFloating = true;
        }

        final DateTime startDateTime = new DateTime(startDate);
        note.setStartDate(startDateTime);

        // A null endDate equates to infinity, which is represented by
        // a String that will always come after any date when compared.
        if(endDate!=null) {
            final DateTime endDateTime = new DateTime(endDate);
            note.setEndDate(endDateTime);
        }

        note.setFloating(isFloating);
        note.setRecurring(isRecurring);
    }
}
