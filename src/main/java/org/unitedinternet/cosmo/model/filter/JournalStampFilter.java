package org.unitedinternet.cosmo.model.filter;

import carldav.entity.Item;

public class JournalStampFilter extends StampFilter {

    public JournalStampFilter() {
        setType(Item.Type.VJOURNAL);
    }
}
