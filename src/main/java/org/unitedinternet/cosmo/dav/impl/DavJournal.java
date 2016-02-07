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
import org.unitedinternet.cosmo.model.hibernate.HibJournalItem;

public class DavJournal extends DavCalendarResource {

    public DavJournal(DavResourceLocator locator,
                      DavResourceFactory factory,
                      IdGenerator idGenerator)
        throws CosmoDavException {
        this(new HibJournalItem(), locator, factory, idGenerator);
    }

    public DavJournal(HibJournalItem item,
                      DavResourceLocator locator,
                      DavResourceFactory factory,
                      IdGenerator idGenerator)
        throws CosmoDavException {
        super(item, locator, factory, idGenerator);
    }

    public Calendar getCalendar() {
        HibJournalItem note = (HibJournalItem) getItem();
        return new EntityConverter(getIdGenerator()).convertJournal(note);
    }

    public void setCalendar(Calendar cal) throws CosmoDavException {
        ComponentList vjournals = cal.getComponents(Component.VJOURNAL);
        if (vjournals.isEmpty()) {
            throw new UnprocessableEntityException("VCALENDAR does not contain VJOURNAL");
        }

        final HibJournalItem item = (HibJournalItem) getItem();
        item.setCalendar(cal);
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
