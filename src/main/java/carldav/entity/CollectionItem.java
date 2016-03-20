/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package carldav.entity;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "collection", uniqueConstraints = {@UniqueConstraint(name = "displayname_owner", columnNames = {"displayname", "ownerid"})})
public class CollectionItem {

    private Long id;
    private Date modifiedDate;
    private String displayName;
    private String name;
    private Set<HibItem> items;
    private User owner;
    private CollectionItem parent;
    private Set<CollectionItem> collections;

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
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
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

    @OneToMany(targetEntity=HibItem.class, mappedBy="collection", fetch=FetchType.LAZY, orphanRemoval=true)
    public Set<HibItem> getItems() {
        return items;
    }

    public void setItems(Set<HibItem> items) {
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
}
