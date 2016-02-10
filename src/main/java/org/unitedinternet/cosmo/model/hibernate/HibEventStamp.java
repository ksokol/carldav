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
import org.unitedinternet.cosmo.hibernate.validator.Event;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("event")
public class HibEventStamp extends HibBaseEventStamp {

    private static final long serialVersionUID = 1L;

    public HibEventStamp() {
    }
    
    public HibEventStamp(HibItem hibItem) {
        setItem(hibItem);
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

    public List<Component> getExceptions() {
        ArrayList<Component> exceptions = new ArrayList<>();
        
        // add all exception events
        HibNoteItem note = (HibNoteItem) getItem();
        for(HibNoteItem exception : note.getModifications()) {
            final VEvent exceptionEvent = exception.getExceptionEvent();
            if(exceptionEvent!=null) {
                exceptions.add(exceptionEvent);
            }
        }
        
        return exceptions;
    }

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

    public static HibEventStamp getStamp(HibItem hibItem) {
        return (HibEventStamp) hibItem.getStamp(HibEventStamp.class);
    }
}
