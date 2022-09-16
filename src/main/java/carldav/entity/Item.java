package carldav.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("ITEM")
public class Item {

  public enum Type {
    VEVENT, VJOURNAL, VTODO, VCARD
  }

  private Long id;
  private Date modifiedDate;
  private String displayName;
  private String name;
  private String uid;
  private Long collectionid;
  private String mimetype;
  private String calendar;
  private Date startDate;
  private Date endDate;
  private Boolean floating;
  private Boolean recurring;
  private Date clientCreationDate;
  private Date clientModifiedDate;
  private String type;

  public Item() {
  }

  public Item(String type) {
    this.type = type;
  }

  @Id
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column("MODIFYDATE")
  public Date getModifiedDate() {
    return modifiedDate != null ? new Date(modifiedDate.getTime()) : null;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate != null ? new Date(modifiedDate.getTime()) : null;
  }

  @Column("DISPLAYNAME")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Column("ITEMNAME")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column("UID")
  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public void setCollectionid(Long collectionId) {
    this.collectionid = collectionId;
  }

  @Column("COLLECTIONID")
  public Long getCollectionid() {
    return collectionid;
  }

  @Column("MIMETYPE")
  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  @Column("CALENDAR")
  public String getCalendar() {
    return calendar;
  }

  public void setCalendar(String calendar) {
    this.calendar = calendar;
  }

  @Column("STARTDATE")
  public Date getStartDate() {
    return startDate != null ? new Date(startDate.getTime()) : null;
  }

  public void setStartDate(final Date startDate) {
    this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
  }

  @Column("ENDDATE")
  public Date getEndDate() {
    return endDate != null ? new Date(endDate.getTime()) : null;
  }

  public void setEndDate(final Date endDate) {
    this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
  }

  @Column("FLOATING")
  public Boolean getFloating() {
    return floating;
  }

  public void setFloating(final Boolean floating) {
    this.floating = floating;
  }

  @Column("RECURRING")
  public Boolean getRecurring() {
    return recurring;
  }

  public void setRecurring(final Boolean recurring) {
    this.recurring = recurring;
  }

  @Column("CLIENTCREATEDATE")
  public Date getClientCreationDate() {
    return clientCreationDate != null ? new Date(clientCreationDate.getTime()) : null;
  }

  public void setClientCreationDate(Date clientCreationDate) {
    this.clientCreationDate = clientCreationDate != null ? new Date(clientCreationDate.getTime()) : null;
  }

  @Column("CLIENTMODIFIEDDATE")
  public Date getClientModifiedDate() {
    return clientModifiedDate != null ? new Date(clientModifiedDate.getTime()) : null;
  }

  public void setClientModifiedDate(Date clientModifiedDate) {
    this.clientModifiedDate = clientModifiedDate != null ? new Date(clientModifiedDate.getTime()) : null;
  }

  @Column("TYPE")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
