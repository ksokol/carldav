package org.unitedinternet.cosmo.dav.impl;

import carldav.service.generator.IdGenerator;
import net.fortuna.ical4j.model.Calendar;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.model.hibernate.HibJournalItem;

public class DavJournal extends DavCalendarResource {

    public DavJournal(DavResourceLocator locator, DavResourceFactory factory, IdGenerator idGenerator) throws CosmoDavException {
        this(new HibJournalItem(), locator, factory, idGenerator);
    }

    public DavJournal(HibJournalItem item, DavResourceLocator locator, DavResourceFactory factory, IdGenerator idGenerator) throws CosmoDavException {
        super(item, locator, factory, idGenerator);
    }

    public void setCalendar(Calendar cal) throws CosmoDavException {
        final HibJournalItem item = (HibJournalItem) getItem();
        item.setCalendar(cal.toString());
    }

}
