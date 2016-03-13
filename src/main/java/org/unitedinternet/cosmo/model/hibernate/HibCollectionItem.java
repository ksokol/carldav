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
package org.unitedinternet.cosmo.model.hibernate;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name = "collection",
        indexes={@Index(name = "idx_itemtype",columnList = "itemtype" ),
                @Index(name = "idx_itemname",columnList = "itemname" ),
        },
        uniqueConstraints = {@UniqueConstraint(name = "displayname_owner", columnNames = {"displayname", "ownerid"})}
)
@DiscriminatorColumn(
        name="itemtype",
        discriminatorType=DiscriminatorType.STRING,
        length=32)
public class HibCollectionItem extends HibAuditableObject {

    private Set<HibItem> items = new HashSet<>();
    private User owner;
    private HibCollectionItem collection;

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

    @ManyToOne(targetEntity=HibCollectionItem.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "collectionid")
    public HibCollectionItem getParent() {
        return collection;
    }

    public void setParent(final HibCollectionItem collection) {
        this.collection = collection;
    }
}
