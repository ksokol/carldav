package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;

public class JournalStampFilter extends StampFilter {

    public JournalStampFilter() {
        setType(HibICalendarItem.Type.VJOURNAL);
    }
}
