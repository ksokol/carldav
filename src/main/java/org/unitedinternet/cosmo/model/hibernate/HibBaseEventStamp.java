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

import net.fortuna.ical4j.model.Calendar;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "stamp",
        indexes = {
                @Index(name = "idx_startdt",columnList = "startdate"),
                @Index(name = "idx_enddt",columnList = "enddate"),
                @Index(name = "idx_floating",columnList = "isfloating"),
                @Index(name = "idx_recurring",columnList = "isrecurring")
        }
)
public class HibBaseEventStamp extends HibAuditableObject {

    @Column(name = "icaldata", length=102400000, nullable = false)
    @Type(type="calendar_clob")
    @NotNull
    private Calendar eventCalendar = null;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(targetEntity=HibItem.class, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "itemid", nullable = false)
    private HibItem item;

    @Column(name = "startdate")
    private Date startDate;

    @Column(name = "enddate")
    private Date endDate;

    @Column(name = "isfloating")
    private Boolean isFloating;

    @Column(name = "isrecurring")
    private Boolean isRecurring;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public HibItem getItem() {
        return item;
    }

    public void setItem(HibItem hibItem) {
        this.item = hibItem;
    }

    public HibBaseEventStamp() {}

    public HibBaseEventStamp(HibItem hibItem) {
        setItem(hibItem);
    }

    public Calendar getEventCalendar() {
        return eventCalendar;
    }

    public void setEventCalendar(Calendar calendar) {
        this.eventCalendar = calendar;
    }

    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    public void setStartDate(final Date startDate) {
        this.startDate = new Date(startDate.getTime());
    }

    public Date getEndDate() {
        return new Date(endDate.getTime());
    }

    public void setEndDate(final Date endDate) {
        this.endDate = new Date(endDate.getTime());
    }

    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(final Boolean isFloating) {
        this.isFloating = isFloating;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(final Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
