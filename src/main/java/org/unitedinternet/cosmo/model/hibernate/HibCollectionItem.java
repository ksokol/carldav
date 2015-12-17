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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Hibernate persistent CollectionItem.
 */
@Entity
@DiscriminatorValue("collection")
public class HibCollectionItem extends HibItem implements CollectionItem {

    private static final long serialVersionUID = 2873258323314048223L;

    public static final QName ATTR_EXCLUDE_FREE_BUSY_ROLLUP =
            new HibQName(CollectionItem.class, "excludeFreeBusyRollup");

    @OneToMany(targetEntity=HibCollectionItemDetails.class, mappedBy="primaryKey.collection", fetch=FetchType.LAZY)
    @Cascade( {CascadeType.DELETE }) 
    private Set<CollectionItemDetails> childDetails = new HashSet<CollectionItemDetails>(0);

    private transient Set<Item> children = null;

    public Set<Item> getChildren() {
        if(children!=null) {
            return children;
        }

        children = new HashSet<Item>();
        for(CollectionItemDetails cid: childDetails) {
            children.add(cid.getItem());
        }

        children = Collections.unmodifiableSet(children);

        return children;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildDetails(org.unitedinternet.cosmo.model.Item)
     */
    public CollectionItemDetails getChildDetails(Item item) {
        for(CollectionItemDetails cid: childDetails) {
            if(cid.getItem().getUid().equals(item.getUid()) && 
                    cid.getItem().getName().equals(item.getName())&& 
                    cid.getItem().getOwner().getUid().equals(item.getOwner().getUid()) ) {
                return cid;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildByName(java.lang.String)
     */
    public Item getChildByName(String name) {
        for (Item child : getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#generateHash()
     */
    public int generateHash() {
        return getVersion();
    }

    public Item copy() {
        CollectionItem copy = new HibCollectionItem();
        copyToItem(copy);
        return copy;
    }
}
