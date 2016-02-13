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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
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

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
