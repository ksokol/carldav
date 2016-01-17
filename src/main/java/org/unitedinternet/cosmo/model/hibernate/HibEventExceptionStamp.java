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
import org.unitedinternet.cosmo.hibernate.validator.EventException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("eventexception")
public class HibEventExceptionStamp extends HibBaseEventStamp {

    private static final long serialVersionUID = 1L;

    public HibEventExceptionStamp() {
    }
    
    public HibEventExceptionStamp(HibItem hibItem) {
        setItem(hibItem);
    }

    public String getType() {
        return "eventexception";
    }

    @EventException
    private Calendar getValidationCalendar() {//NOPMD
        return getEventCalendar();
    }

    @Override
    public VEvent getEvent() {
        return getExceptionEvent();
    }

    public VEvent getExceptionEvent() {
        return (VEvent) getEventCalendar().getComponents().getComponents(
                Component.VEVENT).get(0);
    }

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

    public HibEventStamp getMasterStamp() {
        HibNoteItem note = (HibNoteItem) getItem();
        return HibEventStamp.getStamp(note.getModifies());
    }
    
    /**
     * Return EventExceptionStamp from Item
     * @param hibItem
     * @return EventExceptionStamp from Item
     */
    public static HibEventExceptionStamp getStamp(HibItem hibItem) {
        return (HibEventExceptionStamp) hibItem.getStamp(HibEventExceptionStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
