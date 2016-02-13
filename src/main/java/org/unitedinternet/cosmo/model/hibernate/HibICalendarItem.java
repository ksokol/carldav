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
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("icalendar")
public abstract class HibICalendarItem extends HibItem {

    @Column(name="icaluid", length=255)
    private String icalUid = null;

    @Column(name = "calendar", length= 2147483647)
    @Type(type="calendar_clob")
    private Calendar calendar;

    @OneToMany(targetEntity=HibBaseEventStamp.class, mappedBy = "item", fetch= FetchType.LAZY, cascade= CascadeType.ALL, orphanRemoval=true)
    private Set<HibBaseEventStamp> stamps = new HashSet<>();

    public String getIcalUid() {
        return icalUid;
    }

    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
        super.setIcalUid(icalUid);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    @Deprecated
    public boolean isEvent() {
        return getStamp() != null;
    }

    public void addStamp(HibBaseEventStamp stamp) {
        if (stamp == null) {
            throw new IllegalArgumentException("stamp cannot be null");
        }

        stamp.setItem(this);
        stamps.add(stamp);
    }

    @Deprecated
    public HibBaseEventStamp getStamp() {
        for(HibBaseEventStamp stamp : stamps) {
            // only return stamp if it is an instance of the specified class
            if(HibBaseEventStamp.class.isInstance(stamp)) {
                return stamp;
            }
        }

        return null;
    }

}
