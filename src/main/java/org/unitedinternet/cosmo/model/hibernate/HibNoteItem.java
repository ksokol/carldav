/*
 * Copyright 2006 Open Source Applications Foundation
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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import org.hibernate.annotations.Target;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;

import java.nio.charset.Charset;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@DiscriminatorValue("note")
public class HibNoteItem extends HibICalendarItem {

    private static final long serialVersionUID = 4L;

    @Column(name= "body", columnDefinition="CLOB")
    @Lob
    private String body;

    @Column(name = "remindertime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date remindertime;

    @Column(name = "startdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Embedded
    @Target(TriageStatus.class)
    private TriageStatus triageStatus = new TriageStatus();

    public HibNoteItem() {
    }

    public TriageStatus getTriageStatus() {
        return triageStatus;
    }

    public void setTriageStatus(TriageStatus ts) {
        triageStatus = ts;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRemindertime(Date remindertime) {
        this.remindertime = new Date(remindertime.getTime());
    }

    public Calendar getTaskCalendar() {
        return getCalendar();
    }
    
    public void setTaskCalendar(Calendar calendar) {
        setCalendar(calendar);
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public net.fortuna.ical4j.model.Date getStartDate() {
        //return getEventException().getStartDate();
        VEvent event = (VEvent) getCalendar().getComponents("VEVENT").get(0);
        if(event==null) {
            return null;
        }

        DtStart dtStart = event.getStartDate();
        if (dtStart == null) {
            return null;
        }
        return dtStart.getDate();
    }

    private Dur getDuration() {
      //  return ICalendarUtils.getDuration(getExceptionEvent());
        return ICalendarUtils.getDuration((VEvent) getCalendar().getComponents("VEVENT").get(0));
    }

    public net.fortuna.ical4j.model.Date getEndDate() {
       // VEvent event = getExceptionEvent();
        VEvent event = (VEvent) getCalendar().getComponents("VEVENT").get(0);
        if(event==null) {
            return null;
        }
        DtEnd dtEnd = event.getEndDate(false);
        // if no DTEND, then calculate endDate from DURATION
        if (dtEnd == null) {
            net.fortuna.ical4j.model.Date startDate = getStartDate();
            Dur duration = this.getDuration();

            // if no DURATION, then there is no end time
            if(duration==null) {
                return null;
            }

            net.fortuna.ical4j.model.Date endDate = null;
            if(startDate instanceof DateTime) {
                endDate = new DateTime(startDate);
            }
            else {
                endDate = new net.fortuna.ical4j.model.Date(startDate);
            }

            endDate.setTime(duration.getTime(startDate).getTime());
            return endDate;
        }

        return dtEnd.getDate();
    }

    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
         
        StringBuffer etag = new StringBuffer(uid + ":" + modTime);

        return encodeEntityTag(etag.toString().getBytes(Charset.forName("UTF-8")));
    }
}
