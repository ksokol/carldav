package org.unitedinternet.cosmo.calendar;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.util.Dates;
import org.unitedinternet.cosmo.CosmoParseException;

import java.text.ParseException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A list of instances . Instances are created by adding a component, either the
 * master recurrence component or an overridden instance of one. Its is the
 * responsibility of the caller to ensure all the components added are for the
 * same event (i.e. UIDs are all the same).
 */
public class InstanceList extends TreeMap<String, Instance> {

    private static final long serialVersionUID = 1838360990532590682L;
    private boolean isUTC = false;
    private TimeZone timezone = null;

    public void addComponent(Component comp, Date rangeStart, Date rangeEnd) {
        if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null) {
            addMaster(comp, rangeStart, rangeEnd);
        } else {
            addOverride(comp, rangeStart, rangeEnd);
        }
    }

    public boolean isUTC() {
        return isUTC;
    }

    public void setUTC(boolean isUTC) {
        this.isUTC = isUTC;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        if(timezone != null ){
            this.timezone = TimeZoneRegistryFactory.getInstance().createRegistry().getTimeZone(timezone.getID());
        }
    }

    /**
     * Add a master component if it falls within the specified time range.
     *
     * @param comp       The component.
     * @param rangeStart The start date in range.
     * @param rangeEnd   The end date in range.
     */
    public void addMaster(Component comp, Date rangeStart, Date rangeEnd) {
        var start = getStartDate(comp);

        if (start == null) {
            return;
        }

        var startValue = start instanceof DateTime ? Value.DATE_TIME : Value.DATE;

        start = convertToUTCIfNecessary(start);

        if (start instanceof DateTime) {
            // adjust floating time if timezone is present
            start = adjustFloatingDateIfNecessary(start);
        }

        Dur duration;
        var end = getEndDate(comp);
        if (end == null) {
            if (startValue.equals(Value.DATE_TIME)) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            end = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
        } else {
            end = convertToUTCIfNecessary(end);
            if (startValue.equals(Value.DATE_TIME)) {
                // Adjust floating end time if timezone present
                end = adjustFloatingDateIfNecessary(end);
                // Handle case where dtend is before dtstart, in which the duration
                // will be 0, since it is a timed event
                if (end.before(start)) {
                    end = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(
                            new Dur(0, 0, 0, 0).getTime(start), start);
                }
            } else {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 1 day since its an all-day event
                if (end.before(start)) {
                    end = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(
                            new Dur(1, 0, 0, 0).getTime(start), start);
                }
            }
            duration = new Dur(start, end);
        }
		   // Always add first instance if included in range..
        if (dateBefore(start, rangeEnd) &&
                (dateAfter(end, rangeStart) ||
                        dateEquals(end, rangeStart)) &&
            comp.getProperties(Property.RRULE).isEmpty()) {
            Instance instance = new Instance(comp, start, end);
            put(instance.getRid().toString(), instance);
        }
        // recurrence dates..
        var rDates = comp.getProperties().getProperties(Property.RDATE);
        for (var rDate : rDates) {
            RDate rdate = (RDate) rDate;
            // Both PERIOD and DATE/DATE-TIME values allowed
            if (Value.PERIOD.equals(rdate.getParameters().getParameter(Parameter.VALUE))) {
                for (var o : rdate.getPeriods()) {
                    var period = (Period) o;
                    var periodStart = adjustFloatingDateIfNecessary(period.getStart());
                    var periodEnd = adjustFloatingDateIfNecessary(period.getEnd());
                    // Add period if it overlaps rage
                    if (periodStart.before(rangeEnd) && periodEnd.after(rangeStart)) {
                        var instance = new Instance(comp, periodStart, periodEnd);
                        put(instance.getRid().toString(), instance);
                    }
                }
            } else {
                for (var o : rdate.getDates()) {
                    var startDate = (Date) o;
                    startDate = convertToUTCIfNecessary(startDate);
                    startDate = adjustFloatingDateIfNecessary(startDate);
                    var endDate = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(startDate), startDate);
                    // Add RDATE if it overlaps range
                    if (inRange(startDate, endDate, rangeStart, rangeEnd)) {
                        var instance = new Instance(comp, startDate, endDate);
                        put(instance.getRid().toString(), instance);
                    }
                }
            }
        }

        // recurrence rules..
        var rRules = comp.getProperties().getProperties(Property.RRULE);

        // Adjust startRange to account for instances that occur before
        // the startRange, and end after it
        Date adjustedRangeStart = null;
        Date ajustedRangeEnd = null;

        if (!rRules.isEmpty()) {
            adjustedRangeStart = adjustStartRangeIfNecessary(rangeStart, start, duration);
            ajustedRangeEnd = adjustEndRangeIfNecessary(rangeEnd, start);
        }

        for (var rRule : rRules) {
            var rrule = (RRule) rRule;

            //if start and adjustedRangeStart must be in the same timezone

            var startDates = rrule.getRecur().getDates(start, adjustedRangeStart,
                    ajustedRangeEnd, start instanceof DateTime ? Value.DATE_TIME : Value.DATE);
            for (var date : startDates) {
                var sd = (Date) date;
                var startDate = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(sd, start);
                var endDate = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(sd), start);
                var instance = new Instance(comp, startDate, endDate);
                put(instance.getRid().toString(), instance);
            }
        }
        // exception dates..
        var exDates = comp.getProperties().getProperties(Property.EXDATE);
        for (var date : exDates) {
            var exDate = (ExDate) date;
            for (var o : exDate.getDates()) {
                var sd = (Date) o;
                sd = convertToUTCIfNecessary(sd);
                sd = adjustFloatingDateIfNecessary(sd);
                var instance = new Instance(comp, sd, sd);
                remove(instance.getRid().toString());
            }
        }
        // exception rules..
        PropertyList exRules = comp.getProperties().getProperties(Property.EXRULE);
        if (!exRules.isEmpty() && adjustedRangeStart == null) {
            adjustedRangeStart = adjustStartRangeIfNecessary(rangeStart, start, duration);
            ajustedRangeEnd = adjustEndRangeIfNecessary(rangeEnd, start);
        }

        for (var exRule : exRules) {
            ExRule exrule = (ExRule) exRule;
            DateList startDates = exrule.getRecur().getDates(start, adjustedRangeStart,
                    ajustedRangeEnd, start instanceof DateTime ? Value.DATE_TIME : Value.DATE);
            for (var startDate : startDates) {
                var sd = (Date) startDate;
                var instance = new Instance(comp, sd, sd);
                remove(instance.getRid().toString());
            }
        }
    }

    /**
     * Add an override component if it falls within the specified time range.
     *
     * @param comp       The component.
     * @param rangeStart The date.
     * @param rangeEnd   The date.
     * @return true if the override component modifies instance list and false
     *         if the override component has no effect on instance list
     */
    public boolean addOverride(Component comp, Date rangeStart, Date rangeEnd) {
        boolean modified = false;

        // Verify if component is an override
        if (comp.getProperties().getProperty(Property.RECURRENCE_ID) == null) {
            return false;
        }

        // First check to see that the appropriate properties are present.

        // We need a DTSTART.
        var dtstart = getStartDate(comp);
        if (dtstart == null) {
            return false;
        }

        var startValue = dtstart instanceof DateTime ? Value.DATE_TIME : Value.DATE;

        dtstart = convertToUTCIfNecessary(dtstart);

        if (dtstart instanceof DateTime) {
            // adjust floating time if timezone is present
            dtstart = adjustFloatingDateIfNecessary(dtstart);
        }

        // We need either DTEND or DURATION.
        var dtend = getEndDate(comp);
        if (dtend == null) {
            Dur duration;
            if (startValue.equals(Value.DATE_TIME)) {
                // Its an timed event with no duration
                duration = new Dur(0, 0, 0, 0);
            } else {
                // Its an all day event so duration is one day
                duration = new Dur(1, 0, 0, 0);
            }
            dtend = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getTime(dtstart), dtstart);
        } else {
            // Convert to UTC if needed
            dtend = convertToUTCIfNecessary(dtend);
            if (startValue.equals(Value.DATE_TIME)) {
                // Adjust floating end time if timezone present
                dtend = adjustFloatingDateIfNecessary(dtend);
                // Handle case where dtend is before dtstart, in which the duration
                // will be 0, since it is a timed event
                if (dtend.before(dtstart)) {
                    dtend = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(
                            new Dur(0, 0, 0, 0).getTime(dtstart), dtstart);
                }
            } else {
                // Handle case where dtend is before dtstart, in which the duration
                // will be 1 day since its an all-day event
                if (dtend.before(dtstart)) {
                    dtend = org.unitedinternet.cosmo.calendar.util.Dates.getInstance(new Dur(1, 0, 0, 0).getTime(dtstart), dtstart);
                }
            }
        }

        // Now create the map entry
        var riddt = getRecurrenceId(comp);
        riddt = convertToUTCIfNecessary(riddt);
        if (riddt instanceof DateTime) {
            riddt = adjustFloatingDateIfNecessary(riddt);
        }

        var instance = new Instance(comp, dtstart, dtend, riddt, true);
        var key = instance.getRid().toString();

        // Replace the master instance if it exists
        if (containsKey(key)) {
            remove(key);
            modified = true;
        }

        // Add modification instance if its in the range
        if (dtstart.before(rangeEnd) && dtend.after(rangeStart)) {
            put(key, instance);
            modified = true;
        }

        // Handle THISANDFUTURE if present
        var range = (Range) comp.getProperties().getProperty(Property.RECURRENCE_ID).getParameters().getParameter(Parameter.RANGE);

        // TODO Ignoring THISANDPRIOR
        if (Range.THISANDFUTURE.equals(range)) {

            // Policy - iterate over all the instances after this one, replacing
            // the original instance withg a version adjusted to match the
            // override component

            // We need to account for a time shift in the overridden component
            // by applying the same shift to the future instances
            var timeShift = dtstart.compareTo(riddt) != 0;
            var offsetTime = timeShift ? new Dur(riddt, dtstart) : null;
            var newDuration = timeShift ? new Dur(dtstart, dtend) : null;

            // Get a sorted list rids so we can identify the starting location
            // for the override.  The starting position will be the rid after
            // the current rid, or in the case of no matching rid, the first
            // rid that is greater than the current rid.
            var containsKey = containsKey(key);
            var sortedKeys = new TreeSet<>(keySet());
            for (Iterator<String> iter = sortedKeys.iterator(); iter.hasNext(); ) {
                var ikey = iter.next();

                if (ikey.equals(key) || !containsKey && ikey.compareTo(key) > 0) {
                    if (containsKey && !iter.hasNext()) {
                        continue;
                    } else if (containsKey) {
                        ikey = iter.next();
                    }

                    var moreKeys = true;
                    var firstMatch = true;
                    while (moreKeys) {

                        // The target key is already set for the first
                        // iteration, so for all other iterations
                        // get the next target key.
                        if (firstMatch) {
                            firstMatch = false;
                        } else {
                            ikey = iter.next();
                        }

                        var oldinstance = get(ikey);

                        // Do not override an already overridden instance
                        if (oldinstance.isOverridden()) {
                            continue;
                        }

                        // Determine start/end for new instance which may need
                        // to be offset by the start/end offset and adjusted for
                        // a new duration from the overridden component
                        var originalstart = oldinstance.getRid();
                        var originalvalue = originalstart instanceof DateTime ? Value.DATE_TIME : Value.DATE;
                        var start = oldinstance.getStart();
                        var end = oldinstance.getEnd();

                        if (timeShift) {
                            // Handling of overlapping overridden THISANDFUTURE
                            // components is not defined in 2445. The policy
                            // here is that a THISANDFUTURE override should
                            // override any previous THISANDFUTURE overrides. So
                            // we need to use the original start time for the
                            // instance being adjusted as the time that is
                            // shifted, and the original start time is geiven by
                            // its recurrence-id.
                            start = Dates.getInstance(offsetTime.getTime(originalstart), originalvalue);
                            end = Dates.getInstance(newDuration.getTime(start), originalvalue);
                        }

                        // Replace with new instance
                        var newinstance = new Instance(comp, start, end, originalstart, false);
                        remove(ikey);
                        put(newinstance.getRid().toString(), newinstance);
                        modified = true;

                        if (!iter.hasNext()) {
                            moreKeys = false;
                        }
                    }
                }
            }
        }

        return modified;
    }

    private Date getStartDate(Component comp) {
        var prop = (DtStart) comp.getProperties().getProperty(Property.DTSTART);
        return (prop != null) ? prop.getDate() : null;
    }

    private Date getEndDate(Component comp) {
        var dtEnd = (DtEnd) comp.getProperties().getProperty(Property.DTEND);
        // No DTEND? No problem, we'll use the DURATION if present.
        if (dtEnd == null) {
            var dtStart = getStartDate(comp);
            var duration = (Duration) comp.getProperties().getProperty(Property.DURATION);
            if (duration != null) {
                dtEnd = new DtEnd(org.unitedinternet.cosmo.calendar.util.Dates.getInstance(duration.getDuration().getTime(dtStart), dtStart));
            }
        }
        return (dtEnd != null) ? dtEnd.getDate() : null;
    }

    private Date getRecurrenceId(Component comp) {
        var rid = (RecurrenceId) comp.getProperties().getProperty(Property.RECURRENCE_ID);
        return (rid != null) ? rid.getDate() : null;
    }

    /**
     * If the InstanceList is configured to convert all date/times to UTC,
     * then convert the given Date instance into a UTC DateTime.
     *
     * @param date The date.
     * @return The date.
     */
    private Date convertToUTCIfNecessary(Date date) {
        if (!isUTC) {
            return date;
        }

        return ICalendarUtils.convertToUTC(date, timezone);
    }

    /**
     * Adjust startRange to account for instances that begin before the given
     * startRange, but end after. For example if you have a daily recurring event
     * at 8am lasting for an hour and your startRange is 8:01am, then you
     * want to adjust the range back an hour to catch the instance that is
     * already occurring.
     *
     * @param startRange The date in range we want to adjust.
     * @param start      The date of the event.
     * @param dur        The duration.
     * @return The adjusted start Range date.
     */
    private Date adjustStartRangeIfNecessary(Date startRange, Date start, Dur dur) {
        // If start is a Date, then we need to convert startRange to
        // a Date using the timezone present
        if (!(start instanceof DateTime) && timezone != null && startRange instanceof DateTime) {
            return ICalendarUtils.normalizeUTCDateTimeToDate((DateTime) startRange, timezone);
        }

        // Otherwise start is a DateTime

        // If startRange is not the event start, no adjustment necessary
        if (!startRange.after(start)) {
            return startRange;
        }

        // Need to adjust startRange back one duration to account for instances
        // that occur before the startRange, but end after the startRange
        var negatedDur = dur.negate();
        var cal = Dates.getCalendarInstance(startRange);
        cal.setTime(negatedDur.getTime(startRange));

        // Return new startRange only if it is before the original startRange
        if (cal.getTime().before(startRange)) {
            return org.unitedinternet.cosmo.calendar.util.Dates.getInstance(cal.getTime(), startRange);
        }

        return startRange;
    }

    /**
     * Adjust endRange for Date instances.  First convert the UTC endRange
     * into a Date instance, then add a second
     *
     * @param endRange The date end range.
     * @param start    The date start.
     * @return The date.
     */
    private Date adjustEndRangeIfNecessary(Date endRange, Date start) {
        // If instance is DateTime or timezone is not present, then
        // do nothing
        if (start instanceof DateTime || timezone == null || !(endRange instanceof DateTime)) {
            return endRange;
        }

        endRange = ICalendarUtils.normalizeUTCDateTimeToDefaultOffset((DateTime) endRange, timezone);


        return endRange;
    }

    /**
     * Adjust a floating time if a timezone is present.  A floating time
     * is initially created with the default system timezone.  If a timezone
     * if present, we need to adjust the floating time to be in specified
     * timezone.  This allows a server in the US to return floating times for
     * a query made by someone whos timezone is in Australia.  If no timezone is
     * set for the InstanceList, then the system default timezone will be
     * used in floating time calculations.
     * <p/>
     * What happens is a floating time will get converted into a
     * date/time with a timezone.  This is ok for comparison and recurrence
     * generation purposes.  Note that Instances will get indexed as a UTC
     * date/time and for floating DateTimes, the the recurrenceId associated
     * with the Instance loses its "floating" property.
     *
     * @param date The date.
     * @return The date.
     */
    private Date adjustFloatingDateIfNecessary(Date date) {
        if (timezone == null || !(date instanceof DateTime)) {
            return date;
        }

        var dtDate = (DateTime) date;
        if (dtDate.isUtc() || dtDate.getTimeZone() != null) {
            return date;
        }

        try {
            return new DateTime(dtDate.toString(), timezone);
        } catch (ParseException e) {
            throw new CosmoParseException("error parsing date", e);
        }
    }

    private boolean dateBefore(Date date1, Date date2) {
        return ICalendarUtils.beforeDate(date1, date2, timezone);
    }

    private boolean dateAfter(Date date1, Date date2) {
        return ICalendarUtils.afterDate(date1, date2, timezone);
    }

    private boolean dateEquals(Date date1, Date date2) {
        return ICalendarUtils.equalsDate(date1, date2, timezone);
    }

    private boolean inRange(Date dateStart, Date dateEnd, Date rangeStart, Date rangeEnd) {
        return dateBefore(dateStart, rangeEnd) && dateAfter(dateEnd, rangeStart);
    }
}
