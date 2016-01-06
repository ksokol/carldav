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
package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItemDetails;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extends {@link Item} to represent a collection of items
 */

public class MockCollectionItem extends MockItem implements CollectionItem {

    private Set<CollectionItemDetails> childDetails = new HashSet<CollectionItemDetails>(0);

    /**
     * Adds child.
     * @param item The item.
     */
    public void addChild(Item item) {
        HibCollectionItemDetails cid = new HibCollectionItemDetails(this, item);
        childDetails.add(cid);
    }
    
    /**
     * Removes.
     * @param item The item.
     */
    public void removeChild(Item item) {
        CollectionItemDetails cid = getChildDetails(item);
        if (cid != null) {
            childDetails.remove(cid);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildren()
     */
    /**
     * Gets children.
     * @return set item.
     */
    public Set<Item> getChildren() {
        Set<Item> children = new HashSet<Item>();
        for (CollectionItemDetails cid: childDetails) {
            children.add(cid.getItem());
        }
        
        return Collections.unmodifiableSet(children);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildDetails(org.unitedinternet.cosmo.model.Item)
     */
    /**
     * Gets child details.
     * @param item The item.
     * @return collection item details.
     */
    public CollectionItemDetails getChildDetails(Item item) {
        for (CollectionItemDetails cid: childDetails) {
            if (cid.getItem().equals(item)) {
                return cid;
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChild(java.lang.String)
     */
    /**
     * Gets child.
     * @param uid The id.
     * @return The item.
     */
    public Item getChild(String uid) {
        for (Item child : getChildren()) {
            if (child.getUid().equals(uid)) {
                return child;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CollectionItem#getChildByName(java.lang.String)
     */
    /**
     * Gets child by name.
     * @param name 
     * @return item
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionItem#generateHash()
     */
    /**
     * Generates hash.
     * @return The hash. 
     */
    public int generateHash() {
        return getVersion();
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The item
     */
    public Item copy() {
        CollectionItem copy = new MockCollectionItem();
        copyToItem(copy);
        return copy;
    }
}
