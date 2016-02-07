package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.hibernate.HibJournalStamp;

public class JournalStampFilter extends StampFilter {

    public JournalStampFilter() {
        super(HibJournalStamp.class);
    }
}
