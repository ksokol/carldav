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
package org.unitedinternet.cosmo.model.mock;

import net.fortuna.ical4j.model.Calendar;
import org.unitedinternet.cosmo.hibernate.validator.Task;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.hibernate.TextAttribute;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extends {@link ICalendarItem} to represent a Note item.
 */
public class MockNoteItem extends MockICalendarItem implements NoteItem {

    public static final QName ATTR_NOTE_BODY = new MockQName(
            NoteItem.class, "body");

    private static final Set<NoteItem> EMPTY_MODS = Collections
            .unmodifiableSet(new HashSet<NoteItem>(0));

    
    private Set<NoteItem> modifications = new HashSet<NoteItem>(0);
    
    
    private NoteItem modifies = null;
    
    private boolean hasModifications = false;

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#getBody()
     */
    /**
     * Gets body.
     * @return The body.
     */
    public String getBody() {
        return TextAttribute.getValue(this, ATTR_NOTE_BODY);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#setBody(java.lang.String)
     */
    /**
     * Sets body.
     * @param body The body.
     */
    public void setBody(String body) {
        // body stored as TextAttribute on Item
        TextAttribute.setValue(this, ATTR_NOTE_BODY, body);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#setBody(java.io.Reader)
     */
    /**
     * Sets body.
     * @param body The body.
     */
    public void setBody(Reader body) {
        // body stored as TextAttribute on Item
        TextAttribute.setValue(this, ATTR_NOTE_BODY, body);
    }

    /**
     * Gets task calendar.
     * {@inheritDoc}
     * @return The calendar.
     */
    @Task
    public Calendar getTaskCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return MockICalendarAttribute.getValue(this, ATTR_ICALENDAR);
    }
    
   /**
    * Sets task calendar.
    * {@inheritDoc}
    * @param calendar The calendar.
    */
    public void setTaskCalendar(Calendar calendar) {
        // calendar stored as ICalendarAttribute on Item
        MockICalendarAttribute.setValue(this, ATTR_ICALENDAR, calendar);
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The item.
     */
    public Item copy() {
        NoteItem copy = new MockNoteItem();
        copyToItem(copy);
        return copy;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#getModifications()
     */
    /**
     * Gets modification.
     * @return The modification.
     */
    public Set<NoteItem> getModifications() {
        if (hasModifications) {
            return Collections.unmodifiableSet(modifications);
        }
        else {
            return EMPTY_MODS;
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#addModification(org.unitedinternet.cosmo.model.copy.NoteItem)
     */
    /**
     * Adds modification.
     * @param mod The note item.
     */
    public void addModification(NoteItem mod) {
        modifications.add(mod);
        hasModifications = true;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#removeModification(org.unitedinternet.cosmo.model.copy.NoteItem)
     */
    /**
     * Removes modification.
     * @param mod The note item.
     * @return boolean.
     */
    public boolean removeModification(NoteItem mod) {
        boolean removed = modifications.remove(mod);
        hasModifications = modifications.size()!=0;
        return removed;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#removeAllModifications()
     */
    /**
     * Removes all modifications.
     */
    public void removeAllModifications() {
        modifications.clear();
        hasModifications = false;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#getModifies()
     */
    /**
     * Gets modifies.
     * @return The note item.
     */
    public NoteItem getModifies() {
        return modifies;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceNoteItem#setModifies(org.unitedinternet.cosmo.model.copy.NoteItem)
     */
    /**
     * Sets modifies.
     * @param modifies The note item.
     */
    public void setModifies(NoteItem modifies) {
        this.modifies = modifies;
    }
    
    /**
     * Calculates entity tag.
     * {@inheritDoc}
     * @return The entity tag.
     */
    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
         
        StringBuffer etag = new StringBuffer(uid + ":" + modTime);
        
        // etag is constructed from self plus modifications
        if(modifies==null) {
            for(NoteItem mod: getModifications()) {
                uid = mod.getUid() != null ? mod.getUid() : "-";
                modTime = mod.getModifiedDate() != null ?
                        Long.valueOf(mod.getModifiedDate().getTime()).toString() : "-";
                etag.append("," + uid + ":" + modTime);
            }
        }
      
        return encodeEntityTag(etag.toString().getBytes());
    }
}
