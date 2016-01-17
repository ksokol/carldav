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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.hibernate.validator.Task;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("note")
public class HibNoteItem extends HibICalendarItem {

    public static final HibQName ATTR_NOTE_BODY = new HibQName(HibNoteItem.class, "body");
    
    public static final HibQName ATTR_REMINDER_TIME = new HibQName(HibNoteItem.class, "reminderTime");
    
    private static final long serialVersionUID = -1L;
    
    private static final Set<HibNoteItem> EMPTY_MODS = Collections.unmodifiableSet(new HashSet<>());

    @OneToMany(targetEntity=HibNoteItem.class, mappedBy = "modifies", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE} )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<HibNoteItem> modifications = new HashSet<>();
    
    @ManyToOne(targetEntity=HibNoteItem.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiesitemid")
    private HibNoteItem modifies = null;
    
    @Column(name= "hasmodifications")
    private boolean hasModifications = false;

    @Type(type="materialized_clob")
    @Column(name= "body", length= 214748364)
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setReminderTime(Date reminderTime) {
        // reminderDate stored as TimestampAttribute on Item
        HibTimestampAttribute.setValue(this, ATTR_REMINDER_TIME, reminderTime);
    }
   
    @Task
    public Calendar getTaskCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return getCalendar();
    }
    
    public void setTaskCalendar(Calendar calendar) {
        setCalendar(calendar);
    }

    public Set<HibNoteItem> getModifications() {
        if(hasModifications) {
            return Collections.unmodifiableSet(modifications);
        }
        else {
            return EMPTY_MODS;
        }
    }

    public void addModification(HibNoteItem mod) {
        modifications.add(mod);
        hasModifications = true;
    }

    public boolean removeModification(HibNoteItem mod) {
        boolean removed = modifications.remove(mod);
        hasModifications = modifications.size()!=0;
        return removed;
    }

    public void removeAllModifications() {
        modifications.clear();
        hasModifications = false;
    }

    public HibNoteItem getModifies() {
        return modifies;
    }

    public void setModifies(HibNoteItem modifies) {
        this.modifies = modifies;
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
