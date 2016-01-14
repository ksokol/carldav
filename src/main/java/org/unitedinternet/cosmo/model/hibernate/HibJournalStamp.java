package org.unitedinternet.cosmo.model.hibernate;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.hibernate.validator.Journal;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("journal")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibJournalStamp extends HibBaseEventStamp implements EventStamp {

    public HibJournalStamp() {
    }

    public HibJournalStamp(Item item) {
        this();
        setItem(item);
    }
    
    public String getType() {
        return "journal";
    }

    @Override
    public VEvent getEvent() {
        return getMasterEvent();
    }

    /** Used by the hibernate validator **/
    @Journal
    private Calendar getValidationCalendar() {
        return getEventCalendar();
    }

    public List<Component> getExceptions() {
        ArrayList<Component> exceptions = new ArrayList<>();
        
        // add all exception journals
        NoteItem note = (NoteItem) getItem();
        for(NoteItem exception : note.getModifications()) {
            EventExceptionStamp exceptionStamp = HibEventExceptionStamp.getStamp(exception);
            if(exceptionStamp!=null) {
                exceptions.add(exceptionStamp.getEvent());
            }
        }
        
        return exceptions;
    }

    public VEvent getMasterEvent() {
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

    public static EventStamp getStamp(Item item) {
        return (EventStamp) item.getStamp(EventStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
