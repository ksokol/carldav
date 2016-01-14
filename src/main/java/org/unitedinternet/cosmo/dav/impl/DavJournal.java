package org.unitedinternet.cosmo.dav.impl;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

public class DavJournal extends DavCalendarResource {

    public DavJournal(DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        this(entityFactory.createNote(), locator, factory, entityFactory);
        getItem().addStamp(entityFactory.createJournalStamp((NoteItem) getItem()));
    }

    public DavJournal(NoteItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }

    public Calendar getCalendar() {
        NoteItem note = (NoteItem) getItem();
        return new EntityConverter(null).convertNote(note);
    }

    public EventStamp getEventStamp() {
        return StampUtils.getEventStamp(getItem());
    }

    public void setCalendar(Calendar cal) throws CosmoDavException {
        ComponentList vjournals = cal.getComponents(Component.VJOURNAL);
        if (vjournals.isEmpty()) {
            throw new UnprocessableEntityException("VCALENDAR does not contain VJOURNAL");
        }

        getEventStamp().setEventCalendar(cal);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
