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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("icalendar")
public abstract class HibICalendarItem extends HibItem {

    @Column(name="icaluid", length=255)
    private String icalUid = null;

    @Column(name = "calendar", length= 2147483647)
    @Type(type="calendar_clob")
    private Calendar calendar;

    public String getIcalUid() {
        return icalUid;
    }

    public void setIcalUid(String icalUid) {
        this.icalUid = icalUid;
        super.setIcalUid(icalUid);
    }

    public Calendar getCalendar() {
        final HibBaseEventStamp stamp = getStamp();
        if(stamp != null) {
            return stamp.getEventCalendar();
        }
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
        if(getStamp() != null) {
            getStamp().setEventCalendar(calendar);
        }
    }

}
