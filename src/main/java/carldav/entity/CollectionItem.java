package carldav.entity;

import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "collection")
public class CollectionItem {

    private Long id;
    private Date modifiedDate;
    private String displayName;
    private String name;
    private Set<Item> items;
    private User owner;
    private CollectionItem parent;
    private Set<CollectionItem> collections;
    private String color;

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

    @OneToMany(targetEntity=Item.class, mappedBy="collection", fetch=FetchType.LAZY, orphanRemoval=true)
    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    @ManyToOne(targetEntity=User.class, fetch= FetchType.LAZY)
    @JoinColumn(name="ownerid", nullable = false)
    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @ManyToOne(targetEntity=CollectionItem.class, fetch=FetchType.LAZY)
    @JoinColumn(name = "collectionid")
    public CollectionItem getParent() {
        return parent;
    }

    public void setParent(final CollectionItem parent) {
        this.parent = parent;
    }

    @OneToMany(targetEntity=CollectionItem.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "collectionid")
    public Set<CollectionItem> getCollections() {
        return collections;
    }

    public void setCollections(Set<CollectionItem> collections) {
        this.collections = collections;
    }

    @Column(name = "calendar_color", nullable = true, columnDefinition = "default '#000000'")
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
