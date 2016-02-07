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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@DiscriminatorValue("journal")
public class HibJournalItem extends HibICalendarItem {

    private static final long serialVersionUID = 3L;

    @Column(name= "body", columnDefinition="CLOB")
    @Lob
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Calendar getCalendar() {
        final HibJournalStamp journalStamp = getJournalStamp();
        if(journalStamp != null) {
            return journalStamp.getEventCalendar();
        }
        return null;
    }

    public void setCalendar(final Calendar calendar) {
        HibJournalStamp journalStamp = getJournalStamp();
        if(journalStamp == null) {
            journalStamp = new HibJournalStamp();
            addStamp(journalStamp);
        }
        journalStamp.setEventCalendar(calendar);
    }

    private HibJournalStamp getJournalStamp() {
        return (HibJournalStamp) super.getStamp(HibJournalStamp.class);
    }
}
