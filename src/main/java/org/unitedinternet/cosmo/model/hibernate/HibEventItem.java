package org.unitedinternet.cosmo.model.hibernate;

import java.nio.charset.Charset;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Kamill Sokol
 */
@Entity
@DiscriminatorValue("event")
public class HibEventItem extends HibICalendarItem {

    private static final long serialVersionUID = 1L;

    public HibEventItem() {
    }

    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
                Long.valueOf(getModifiedDate().getTime()).toString() : "-";

        StringBuffer etag = new StringBuffer(uid + ":" + modTime);

        return encodeEntityTag(etag.toString().getBytes(Charset.forName("UTF-8")));
    }
}