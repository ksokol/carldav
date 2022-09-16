package carldav.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("COLLECTION")
public class CollectionItem {

  private Long id;
  private Date modifiedDate;
  private String displayName;
  private String name;
  private Long ownerId;
  private Long parentId;
  private String color;

  @Id
  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
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

  @Column("OWNERID")
  public Long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
  }

  @Column("COLLECTIONID")
  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  @Column("CALENDAR_COLOR")
  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }
}
