package org.unitedinternet.cosmo.model.filter;

import carldav.entity.HibItem;

public class JournalStampFilter extends StampFilter {

    public JournalStampFilter() {
        setType(HibItem.Type.VJOURNAL);
    }
}
