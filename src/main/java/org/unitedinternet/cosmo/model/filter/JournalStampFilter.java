package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.hibernate.HibItem;

public class JournalStampFilter extends StampFilter {

    public JournalStampFilter() {
        setType(HibItem.Type.VJOURNAL);
    }
}
