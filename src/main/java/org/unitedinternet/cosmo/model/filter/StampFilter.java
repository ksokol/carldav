/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.filter;


import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import carldav.entity.Item;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a filter that matches an Item with a specified Stamp.
 */
public class StampFilter {

    private static final Log LOG = LogFactory.getLog(StampFilter.class);

    private Class stampClass = null;
    private Boolean isRecurring = null;
    private Period period = null;
    private DateTime dstart;
    private DateTime dend;
    private Date fstart;
    private Date fend;
    private TimeZone timezone = null;
    private Item.Type type;

    public StampFilter() {}
    
    public StampFilter(Class stampClass) {
        this.stampClass = stampClass;
    }

    public Class getStampClass() {
        return stampClass;
    }
    
    /**
     * Match Items that contain the specified Stamp
     * type.
     * @param stampClass
     */
    public void setStampClass(Class stampClass) {
        this.stampClass = stampClass;
    }

    /**
     * Match only recurring events.
     * @param isRecurring if set, return recurring events only, or only
     *                    non recurring events
     */
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public Period getPeriod() {
        return period;
    }

    /**
     * Matches events that occur in a given time-range.
     * @param period time-range
     */
    public void setPeriod(Period period) {
        this.period = period;
        // Get fixed start/end time
        dstart = period.getStart();
        dend = period.getEnd();

        // set timezone on floating times
        updateFloatingTimes();
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public Date getEnd() {
        return new Date(fend.getTime());
    }

    public Date getStart() {
        return new Date(fstart.getTime());
    }

    public Item.Type getType() {
        return type;
    }

    public void setType(final Item.Type type) {
        this.type = type;
    }

    /**
     * Used in time-range filtering.  If set, and there is a time-range
     * to filter on, the timezone will be used in comparing floating
     * times.  If null, the server time-zone will be used in determining
     * if floating time values match.
     * @param timezone timezone to use in comparing floating times
     */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
        updateFloatingTimes();
    }

    private void updateFloatingTimes() {
        if(dstart!=null) {
            Value v = Value.DATE_TIME;
            fstart = fstart == null ? (DateTime) Dates.getInstance(dstart, v) : fstart;
            if(fstart instanceof DateTime){
                ((DateTime)fstart).setUtc(false);
                // if the timezone is null then default system timezone is used
                ((DateTime)fstart).setTimeZone((timezone != null) ? timezone : null);
            }
        }
        if(dend!=null) {
            Value v = Value.DATE_TIME;
            fend = fend == null ? (DateTime) Dates.getInstance(dend, v) : fend;
            if(fend instanceof DateTime){
                ((DateTime)fend).setUtc(false);
                // if the timezone is null then default system timezone is used
                ((DateTime)fend).setTimeZone((timezone != null) ? timezone : null);
            }
        }

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        TimeZone tz = getTimezone();
        if(tz != null){
            //for some reason ical4j's TimeZone for a valid ID (i.e. Europe/Bucharest) has raw offset 0
            df.setTimeZone(java.util.TimeZone.getTimeZone(tz.getID()));
        }

        if(fstart != null && fstart.getClass() == net.fortuna.ical4j.model.Date.class){
            try {
                dstart = new DateTime(df.parse(fstart.toString()).getTime());
                dstart.setUtc(true);
            } catch (ParseException e) {
                LOG.error("Error occured while parsing fstart [" + fstart.toString() + "]", e);
            }
        }

        if(fend != null && fend.getClass() == net.fortuna.ical4j.model.Date.class){
            try {
                dend = new DateTime(df.parse(fend.toString()).getTime());
                dend.setUtc(true);
            } catch (ParseException e) {
                LOG.error("Error occured while parsing fend [" + fend.toString() + "]", e);
            }
        }
    }
}
