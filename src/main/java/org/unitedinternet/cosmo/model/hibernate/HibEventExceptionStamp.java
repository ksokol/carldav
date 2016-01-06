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
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.hibernate.validator.EventException;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Hibernate persistent EventExceptionStamp.
 */
@Entity
@DiscriminatorValue("eventexception")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibEventExceptionStamp extends HibBaseEventStamp implements EventExceptionStamp {

    private static final long serialVersionUID = 3992468809776186156L;

    public HibEventExceptionStamp() {
    }
    
    public HibEventExceptionStamp(Item item) {
        setItem(item);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#getType()
     */
    public String getType() {
        return "eventexception";
    }

    /** Used by the hibernate validator **/
    @EventException
    private Calendar getValidationCalendar() {//NOPMD
        return getEventCalendar();
    }

    @Override
    public VEvent getEvent() {
        return getExceptionEvent();
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventExceptionStamp#getExceptionEvent()
     */
    public VEvent getExceptionEvent() {
        return (VEvent) getEventCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventExceptionStamp#setExceptionEvent(net.fortuna.ical4j.model.component.VEvent)
     */
    public void setExceptionEvent(VEvent event) {
        if(getEventCalendar()==null) {
            createCalendar();
        }
        
        // remove all events
        getEventCalendar().getComponents().removeAll(
                getEventCalendar().getComponents().getComponents(Component.VEVENT));
        
        // add event exception
        getEventCalendar().getComponents().add(event);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.EventExceptionStamp#getMasterStamp()
     */
    public EventStamp getMasterStamp() {
        NoteItem note = (NoteItem) getItem();
        return HibEventStamp.getStamp(note.getModifies());
    }
    
    /**
     * Return EventExceptionStamp from Item
     * @param item
     * @return EventExceptionStamp from Item
     */
    public static EventExceptionStamp getStamp(Item item) {
        return (EventExceptionStamp) item.getStamp(EventExceptionStamp.class);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#copy()
     */
    public Stamp copy() {
        EventExceptionStamp stamp = new HibEventExceptionStamp();
        
        // Need to copy Calendar
        try {
            stamp.setEventCalendar(new Calendar(getEventCalendar()));
        } catch (Exception e) {
            throw new CosmoException("Cannot copy calendar", e);
        }
        
        return stamp;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
