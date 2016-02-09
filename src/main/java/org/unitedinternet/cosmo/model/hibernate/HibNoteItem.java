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
import net.fortuna.ical4j.model.component.VEvent;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Target;
import org.unitedinternet.cosmo.hibernate.validator.Task;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@DiscriminatorValue("note")
public class HibNoteItem extends HibICalendarItem {

    private static final long serialVersionUID = 2L;

    @OneToMany(targetEntity=HibNoteItem.class, mappedBy = "modifies", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE} )
    private Set<HibNoteItem> modifications = new HashSet<>();
    
    @ManyToOne(targetEntity=HibNoteItem.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiesitemid")
    private HibNoteItem modifies;

    @Column(name= "body", columnDefinition="CLOB")
    @Lob
    private String body;

    @Column(name = "remindertime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date remindertime;

    @Embedded
    @Target(TriageStatus.class)
    private TriageStatus triageStatus = new TriageStatus();

    public HibNoteItem() {}

    public HibNoteItem(HibEventStamp eventStamp) {
        HibEventExceptionStamp exceptionStamp = new HibEventExceptionStamp(this);
        addStamp(exceptionStamp);
        exceptionStamp.createCalendar();
        exceptionStamp.setStartDate(eventStamp.getStartDate());
        exceptionStamp.setRecurrenceId(eventStamp.getStartDate());
    }

    public HibNoteItem(Calendar calendar, VEvent vEvent) {
        HibEventExceptionStamp exceptionStamp =new HibEventExceptionStamp(this);
        exceptionStamp.setEventCalendar(calendar);
        exceptionStamp.setExceptionEvent(vEvent);
        addStamp(exceptionStamp);
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

    public Date getRemindertime() {
        return new Date(remindertime.getTime());
    }

    @Task
    public Calendar getTaskCalendar() {
        return getCalendar();
    }
    
    public void setTaskCalendar(Calendar calendar) {
        setCalendar(calendar);
    }

    public Set<HibNoteItem> getModifications() {
        return Collections.unmodifiableSet(modifications);
    }

    public void addModification(HibNoteItem mod) {
        modifications.add(mod);
    }

    public boolean removeModification(HibNoteItem mod) {
        return modifications.remove(mod);
    }

    public void removeAllModifications() {
        modifications.clear();
    }

    public HibNoteItem getModifies() {
        return modifies;
    }

    public void setModifies(HibNoteItem modifies) {
        this.modifies = modifies;
    }

    public HibEventExceptionStamp getEventException() {
        return (HibEventExceptionStamp) getStamp(HibEventExceptionStamp.class);
    }

    public boolean hasRecurrenceId(net.fortuna.ical4j.model.Date recurrenceId) {
        HibEventExceptionStamp exceptionStamp = getEventException();
        // only interested in mods with event stamp
        if (exceptionStamp == null) {
            return false;
        }
        if (exceptionStamp.getRecurrenceId().equals(recurrenceId)) {
            return true;
        }
        return false;
    }

    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
         
        StringBuffer etag = new StringBuffer(uid + ":" + modTime);
        
        // etag is constructed from self plus modifications
        if(modifies==null) {
            for(HibNoteItem mod: getModifications()) {
                uid = mod.getUid() != null ? mod.getUid() : "-";
                modTime = mod.getModifiedDate() != null ?
                        Long.valueOf(mod.getModifiedDate().getTime()).toString() : "-";
                etag.append("," + uid + ":" + modTime);
            }
        }
      
        return encodeEntityTag(etag.toString().getBytes(Charset.forName("UTF-8")));
    }
}
