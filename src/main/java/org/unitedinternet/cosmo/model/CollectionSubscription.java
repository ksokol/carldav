/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model;

/**
 * Represents a subscription to a shared collection.
 * A subscription belongs to a user and consists of 
 * a collection uid.
 */
public interface CollectionSubscription extends AuditableObject{

    /**
     * Return the uid of the shared collection.  
     * Note, it is possible that the Collection with this uid is not
     * present in the system.  This will happen if a collection is 
     * shared and then the owner deletes the collection.
     * @return Collection uid
     */
    String getCollectionUid();

    void setCollectionUid(String collectionUid);

    void setCollection(CollectionItem collection);

    String getDisplayName();

    void setDisplayName(String displayName);

    User getOwner();

    void setOwner(User owner);

}