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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    public enum Type {
        VEVENT, VJOURNAL, VTODO, VCARD
    }

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
    private Boolean floating;

    @Column(name = "recurring")
    private Boolean recurring;

    @Column(name = "clientcreatedate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date clientCreationDate;

    @Column(name = "clientmodifieddate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date clientModifiedDate;

    @Enumerated(EnumType.STRING)
    @Column(name ="type")
    private Type type;

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

    public Boolean getFloating() {
        return floating;
    }

    public void setFloating(final Boolean floating) {
        this.floating = floating;
    }

    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(final Boolean recurring) {
        this.recurring = recurring;
    }

    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }

    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }
}
