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
/*
@Table(name = "stamp",
        indexes = {
                @Index(name = "idx_startdt",columnList = "startdate"),
                @Index(name = "idx_enddt",columnList = "enddate"),
                @Index(name = "idx_floating",columnList = "floating"),
                @Index(name = "idx_recurring",columnList = "recurring")
        }
)
*/
public class HibNoteItem extends HibICalendarItem {

    private static final long serialVersionUID = 4L;

    @Column(name= "body", columnDefinition="CLOB")
    @Lob
    private String body;

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

    public void setTaskCalendar(Calendar calendar) {
        setCalendar(calendar);
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public void setFloating(boolean floating) {
        this.floating = floating;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
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
