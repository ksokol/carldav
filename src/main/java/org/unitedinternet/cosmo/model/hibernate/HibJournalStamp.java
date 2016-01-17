package org.unitedinternet.cosmo.model.hibernate;

import edu.emory.mathcs.backport.java.util.Collections;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.property.DtStart;
import org.unitedinternet.cosmo.hibernate.validator.Journal;
import org.unitedinternet.cosmo.model.EventStamp;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("journal")
public class HibJournalStamp extends HibBaseEventStamp implements EventStamp {

    private static final long serialVersionUID = 2L;

    public HibJournalStamp() {
    }

    public HibJournalStamp(HibItem hibItem) {
        setItem(hibItem);
    }
    
    public String getType() {
        return "journal";
    }

    //TODO
    @Deprecated
    @Override
    public VEvent getEvent() {
        return (VEvent) getMaster();
    }

    /** Used by the hibernate validator **/
    @Journal
    private Calendar getValidationCalendar() {//NOPMD
        return getEventCalendar();
    }

    public List<Component> getExceptions() {
        List<Component> exceptions = new ArrayList<>();
        
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

    @Override
    public Date getStartDate() {
        VJournal journal = (VJournal) getMaster();
        if(journal==null) {
            return null;
        }

        DtStart dtStart = journal.getStartDate();
        if (dtStart == null) {
            return null;
        }
        return dtStart.getDate();
    }

    @Override
    public Date getEndDate() {
        return null;
    }

    @Override
    public List<Recur> getRecurrenceRules() {
        return Collections.emptyList();
    }

    @Override
    public DateList getRecurrenceDates() {
        return null;
    }

    public Component getMaster() {
        if(getEventCalendar()==null) {
            return null;
        }

        ComponentList journal = getEventCalendar().getComponents().getComponents(Component.VJOURNAL);

        if(journal.size()==0) {
            return null;
        }

        return (VJournal) journal.get(0);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
