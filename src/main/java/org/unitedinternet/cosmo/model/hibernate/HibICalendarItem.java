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
public class HibICalendarItem extends HibItem {

    public enum Type {
        VEVENT, VJOURNAL, VTODO, VCARD
    }

    private String calendar;
    private Date remindertime;
    private Date startDate;
    private Date endDate;
    private Boolean floating;
    private Boolean recurring;
    private Date clientCreationDate;
    private Date clientModifiedDate;
    private Type type;

    public HibICalendarItem() {}

    public HibICalendarItem(Type type) {
        this.type = type;
    }

    @Column(name = "calendar", columnDefinition = "CLOB")
    @Lob
    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    @Column(name = "remindertime")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getRemindertime() {
        return remindertime;
    }

    public void setRemindertime(final Date remindertime) {
        this.remindertime = remindertime;
    }

    @Column(name = "startdate")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    @Column(name = "enddate")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "floating")
    public Boolean getFloating() {
        return floating;
    }

    public void setFloating(final Boolean floating) {
        this.floating = floating;
    }

    @Column(name = "recurring")
    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(final Boolean recurring) {
        this.recurring = recurring;
    }

    @Column(name = "clientcreatedate")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }

    @Column(name = "clientmodifieddate")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }

    @Enumerated(EnumType.STRING)
    @Column(name ="type")
    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }
}
