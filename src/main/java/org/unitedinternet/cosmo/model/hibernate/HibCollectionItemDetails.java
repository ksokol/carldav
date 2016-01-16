/*
 * Copyright 2008 Open Source Applications Foundation
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

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Hibernate persistent CollectionItemDetails, which is
 * used to store extra attributes in the many-to-many
 * association of collection<-->item.  Extra information
 * that is stored include the date the item was added
 * to the collection.
 */
@Entity
@Table(name="collection_item")
public class HibCollectionItemDetails implements CollectionItemDetails,Serializable {

    private static final long serialVersionUID = -1L;

    @Id
    @ManyToOne(targetEntity = HibCollectionItem.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "collectionid", nullable = false)
    public CollectionItem collection;

    @Id
    @ManyToOne(targetEntity = HibItem.class)
    @JoinColumn(name = "itemid", nullable = false)
    public Item item;

    public HibCollectionItemDetails() {}
    
    public HibCollectionItemDetails(CollectionItem collection,
            Item item) {
        this.collection = collection;
        this.item = item;
    }
    
    public void setCollection(CollectionItem collection) {
        this.collection = collection;
    }
    
    public CollectionItem getCollection() {
        return collection;
    }

    public void  setItem(Item item) {
        this.item = item;
    }
    
    public Item getItem() {
        return item;
    }
}
