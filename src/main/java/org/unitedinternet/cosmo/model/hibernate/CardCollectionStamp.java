package org.unitedinternet.cosmo.model.hibernate;

import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Stamp;

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

    public Stamp copy() {
        final CardCollectionStamp copy = new CardCollectionStamp();
        copy.setItem(this.getItem());
        copy.setCreationDate(this.getCreationDate());
        copy.setModifiedDate(this.getModifiedDate());
        copy.setEntityTag(this.getEntityTag());
        return copy;
    }

    public static CardCollectionStamp getStamp(Item item) {
        return (CardCollectionStamp) item.getStamp(CardCollectionStamp.class);
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
