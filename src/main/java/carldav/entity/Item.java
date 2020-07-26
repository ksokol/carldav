package carldav.entity;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "item")
public class Item {

    public enum Type {
        VEVENT, VJOURNAL, VTODO, VCARD
    }

    private Long id;
    private Date modifiedDate;
    private String displayName;
    private String name;
    private String uid;
    private CollectionItem collection;
    private String mimetype;
    private String calendar;
    private Date startDate;
    private Date endDate;
    private Boolean floating;
    private Boolean recurring;
    private Date clientCreationDate;
    private Date clientModifiedDate;
    private Type type;

    public Item() {}

    public Item(Type type) {
        this.type = type;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Column(name = "modifydate")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getModifiedDate() {
        return modifiedDate != null ? new Date(modifiedDate.getTime()) : null;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate != null ? new Date(modifiedDate.getTime()) : null;
    }

    @NotNull
    @Column(name = "displayname")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Column(name = "itemname", nullable = false, length=255)
    @NotNull
    @Length(min = 1, max = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Column(name = "uid", nullable = false)
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setCollection(CollectionItem parent) {
        collection = parent;
    }

    @ManyToOne(targetEntity=CollectionItem.class, fetch=FetchType.LAZY)
    @JoinColumn(name = "collectionid")
    public CollectionItem getCollection() {
        return collection;
    }

    @Column(name = "mimetype", nullable = false)
    @NotEmpty
    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    @Column(name = "calendar", columnDefinition = "CLOB")
    @Lob
    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    @Column(name = "startdate")
    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
    }

    @Column(name = "enddate")
    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
    }

    @Column(name = "floating")
    public Boolean getFloating() {
        return floating;
    }

    public void setFloating(final Boolean floating) {
        this.floating = floating;
    }

    @Column(name = "recurring")
    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(final Boolean recurring) {
        this.recurring = recurring;
    }

    @Column(name = "clientcreatedate")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getClientCreationDate() {
        return clientCreationDate != null ? new Date(clientCreationDate.getTime()) : null;
    }

    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate != null ? new Date(clientCreationDate.getTime()) : null;
    }

    @Column(name = "clientmodifieddate")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getClientModifiedDate() {
        return clientModifiedDate != null ? new Date(clientModifiedDate.getTime()) : null;
    }

    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate != null ? new Date(clientModifiedDate.getTime()) : null;
    }

    @Enumerated(EnumType.STRING)
    @Column(name ="type")
    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }
}
