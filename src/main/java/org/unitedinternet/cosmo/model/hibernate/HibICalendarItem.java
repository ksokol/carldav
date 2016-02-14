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
package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.Target;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name = "calendaritem",
        indexes = {
                @Index(name = "idx_startdt",columnList = "startdate"),
                @Index(name = "idx_enddt",columnList = "enddate"),
                @Index(name = "idx_floating",columnList = "floating"),
                @Index(name = "idx_recurring",columnList = "recurring")
        }
)
@Entity
@DiscriminatorValue("icalendar")
public abstract class HibICalendarItem extends HibItem {

    @Column(name="icaluid", length=255)
    private String icalUid = null;

    @Column(name = "calendar", columnDefinition = "CLOB")
    @Lob
    private String calendar;

    @Column(name = "remindertime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date remindertime;

    @Column(name = "startdate")
    private Date startDate;

    @Column(name = "enddate")
    private Date endDate;

    @Column(name = "floating")
    private boolean floating;

    @Column(name = "recurring")
    private boolean recurring;

    @Embedded
    @Target(TriageStatus.class)
    private TriageStatus triageStatus;

    public String getIcalUid() {
        return icalUid;
    }

    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
    }

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    public Date getRemindertime() {
        return remindertime;
    }

    public void setRemindertime(final Date remindertime) {
        this.remindertime = remindertime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public boolean isFloating() {
        return floating;
    }

    public void setFloating(final boolean floating) {
        this.floating = floating;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(final boolean recurring) {
        this.recurring = recurring;
    }

    public TriageStatus getTriageStatus() {
        return triageStatus;
    }

    public void setTriageStatus(final TriageStatus triageStatus) {
        this.triageStatus = triageStatus;
    }
}
