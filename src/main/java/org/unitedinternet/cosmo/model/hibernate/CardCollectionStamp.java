package org.unitedinternet.cosmo.model.hibernate;

import org.unitedinternet.cosmo.model.Item;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Kamill Sokol
 */
@Entity
@DiscriminatorValue("card")
public class CardCollectionStamp extends HibStamp {

    private static final long serialVersionUID = -1L;

    public String getType() {
        return "card";
    }

    public static CardCollectionStamp getStamp(Item item) {
        return (CardCollectionStamp) item.getStamp(CardCollectionStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
