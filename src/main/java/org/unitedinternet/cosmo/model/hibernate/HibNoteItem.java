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
import org.hibernate.annotations.Target;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("note")
public class HibNoteItem extends HibICalendarItem {

    private static final long serialVersionUID = 4L;

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

    public void setTaskCalendar(Calendar calendar) {
        setCalendar(calendar.toString());
    }
}
