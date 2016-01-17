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
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.hibernate.validator.Event;
import org.unitedinternet.cosmo.model.EventStamp;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * Hibernate persistent EventStamp.
 */
@Entity
@DiscriminatorValue("event")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibEventStamp extends HibBaseEventStamp implements EventStamp {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3992468809776886156L;
    
    
    /** default constructor */
    public HibEventStamp() {
    }
    
    public HibEventStamp(HibItem hibItem) {
        this();
        setItem(hibItem);
    }
    
    public String getType() {
        return "event";
    }

    @Override
    public VEvent getEvent() {
        return (VEvent) getMaster();
    }

    /** Used by the hibernate validator **/
    @Event
    private Calendar getValidationCalendar() {//NOPMD
        return getEventCalendar();
    }
    
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventStamp#getExceptions()
     */
    public List<Component> getExceptions() {
        ArrayList<Component> exceptions = new ArrayList<Component>();
        
        // add all exception events
        HibNoteItem note = (HibNoteItem) getItem();
        for(HibNoteItem exception : note.getModifications()) {
            HibEventExceptionStamp exceptionStamp = HibEventExceptionStamp.getStamp(exception);
            if(exceptionStamp!=null) {
                exceptions.add(exceptionStamp.getEvent());
            }
        }
        
        return exceptions;
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventStamp#getMaster()
     */
    public Component getMaster() {
        if(getEventCalendar()==null) {
            return null;
        }
        
        ComponentList events = getEventCalendar().getComponents().getComponents(
                Component.VEVENT);
        
        if(events.size()==0) {
            return null;
        }
        
        return (VEvent) events.get(0);
    }

    /**
     * Return EventStamp from Item
     * @param hibItem
     * @return EventStamp from Item
     */
    public static EventStamp getStamp(HibItem hibItem) {
        return (EventStamp) hibItem.getStamp(EventStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
