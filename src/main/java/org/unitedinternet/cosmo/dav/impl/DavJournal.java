package org.unitedinternet.cosmo.dav.impl;

import carldav.service.generator.IdGenerator;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibJournalStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

public class DavJournal extends DavCalendarResource {

    public DavJournal(DavResourceLocator locator,
                      DavResourceFactory factory,
                      IdGenerator idGenerator)
        throws CosmoDavException {
        this(new HibNoteItem(), locator, factory, idGenerator);
        getItem().addStamp(new HibJournalStamp(getItem()));
    }

    public DavJournal(HibNoteItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      IdGenerator idGenerator)
        throws CosmoDavException {
        super(item, locator, factory, idGenerator);
    }

    public Calendar getCalendar() {
        HibNoteItem note = (HibNoteItem) getItem();
        return new EntityConverter(getIdGenerator()).convertNote(note);
    }

    public void setCalendar(Calendar cal) throws CosmoDavException {
        ComponentList vjournals = cal.getComponents(Component.VJOURNAL);
        if (vjournals.isEmpty()) {
            throw new UnprocessableEntityException("VCALENDAR does not contain VJOURNAL");
        }

        final HibJournalStamp stamp = (HibJournalStamp) getItem().getStamp(HibJournalStamp.class);
        stamp.setEventCalendar(cal);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
