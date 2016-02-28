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

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name = "item",
        indexes={@Index(name = "idx_itemtype",columnList = "itemtype" ),
                 @Index(name = "idx_itemuid",columnList = "uid" ),
                 @Index(name = "idx_itemname",columnList = "itemname" ),
        },
        uniqueConstraints = @UniqueConstraint(name = "uid", columnNames = "uid")
)
@DiscriminatorColumn(
        name="itemtype",
        discriminatorType=DiscriminatorType.STRING,
        length=32)
public abstract class HibItem extends HibAuditableObject {

    private String uid;
    private HibCollectionItem collection;

    @Column(name = "uid", nullable = false)
    @NotEmpty
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setCollection(HibCollectionItem parent) {
        collection = parent;
    }

    @ManyToOne(targetEntity=HibCollectionItem.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "collectionid")
    public HibCollectionItem getCollection() {
        return collection;
    }
}
