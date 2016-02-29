package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.hibernate.HibJournalItem;

public class JournalStampFilter extends StampFilter {

    public JournalStampFilter() {
        super(HibJournalItem.class);
        setType("journal");
    }
}
