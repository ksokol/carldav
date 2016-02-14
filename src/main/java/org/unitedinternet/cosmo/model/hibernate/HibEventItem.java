package org.unitedinternet.cosmo.model.hibernate;

import java.nio.charset.Charset;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * @author Kamill Sokol
 */
@Entity
@DiscriminatorValue("event")
public class HibEventItem extends HibICalendarItem {

    private static final long serialVersionUID = 1L;

    @Column(name= "body", columnDefinition="CLOB")
    @Lob
    private String body;

    public HibEventItem() {
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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