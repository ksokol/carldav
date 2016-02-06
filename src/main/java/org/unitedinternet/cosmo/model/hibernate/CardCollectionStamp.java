package org.unitedinternet.cosmo.model.hibernate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Kamill Sokol
 */
@Entity
@DiscriminatorValue("card")
public class CardCollectionStamp extends HibStamp {

    private static final long serialVersionUID = -1L;

    public static CardCollectionStamp getStamp(HibItem hibItem) {
        return (CardCollectionStamp) hibItem.getStamp(CardCollectionStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
