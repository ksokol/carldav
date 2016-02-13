package org.unitedinternet.cosmo.model.hibernate;

import java.nio.charset.Charset;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Kamill Sokol
 */
@Entity
@DiscriminatorValue("event")
/*
@Table(name = "stamp",
        indexes = {
                @Index(name = "idx_startdt",columnList = "startdate"),
                @Index(name = "idx_enddt",columnList = "enddate"),
                @Index(name = "idx_floating",columnList = "floating"),
                @Index(name = "idx_recurring",columnList = "recurring")
        }
)
*/
public class HibEventItem extends HibICalendarItem {

    private static final long serialVersionUID = 1L;

    @Column(name= "body", columnDefinition="CLOB")
    @Lob
    private String body;

    @Column(name = "remindertime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date remindertime;

    @Column(name = "startdate")
    private Date startDate;

    @Column(name = "enddate")
    private Date endDate;

    @Column(name = "floating")
    private boolean floating;

    @Column(name = "recurring")
    private boolean recurring;

    public HibEventItem() {
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRemindertime(Date remindertime) {
        this.remindertime = new Date(remindertime.getTime());
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public void setFloating(boolean floating) {
        this.floating = floating;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
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