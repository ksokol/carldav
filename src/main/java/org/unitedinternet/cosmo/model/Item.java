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

import org.unitedinternet.cosmo.model.hibernate.HibQName;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Represents an item on server.  All
 * content in cosmo extends from Item.
 */
public interface Item extends AuditableObject{

    /**
     * Return all stamps associated with Item.  Use
     * addStamp() and removeStamp() to manipulate set.
     * @return set of stamps associated with Item
     */
    Set<Stamp> getStamps();

    /**
     * Add stamp to Item
     * @param stamp stamp to add
     */
    void addStamp(Stamp stamp);

    /**
     * Remove stamp from Item.
     * @param stamp stamp to remove
     */
    void removeStamp(Stamp stamp);

    /**
     * Get the stamp that corresponds to the specified type
     * @param type stamp type to return
     * @return stamp
     */
    Stamp getStamp(String type);

    /**
     * Get the stamp that corresponds to the specified class
     * @param clazz class of stamp to return
     * @return stamp
     */
    Stamp getStamp(Class<?> clazz);

    /**
     * Get all Attributes of Item.  Use addAttribute() and 
     * removeAttribute() to manipulate map.
     * @return
     */
    Map<HibQName, Attribute> getAttributes();

    void addAttribute(Attribute attribute);

    /**
     * Remove attribute in default namespace with local name.
     * @param name local name of attribute to remove
     */
    void removeAttribute(String name);

    /**
     * Remove attribute.
     * @param qname qualifed name of attribute to remove.
     */
    void removeAttribute(HibQName qname);

    /**
     * Get attribute in default namespace with local name.
     * @param name local name of attribute
     * @return attribute in default namespace with given name
     */
    Attribute getAttribute(String name);

    /**
     * Get attribute with qualified name.
     * @param qname qualified name of attribute to retrieve
     * @return attribute with qualified name.
     */
    Attribute getAttribute(HibQName qname);

    /**
     * Get attribute value with qualified name
     * @param qname qualified name of attribute
     * @return attribute value
     */
    Object getAttributeValue(HibQName qname);

    /**
     * Set attribute value of attribute with local name in default
     * namespace.
     * @param name local name of attribute
     * @param value value to update attribute
     */
    void setAttribute(String name, Object value);

    /**
     * Set attribute value attribute with qualified name
     * @param key qualified name of attribute
     * @param value value to update attribute
     */
    void setAttribute(HibQName key, Object value);

    Date getClientCreationDate();

    void setClientCreationDate(Date clientCreationDate);

    Date getClientModifiedDate();

    void setClientModifiedDate(Date clientModifiedDate);

    String getName();

    void setName(String name);

    /**
     * @return Item's human readable name
     */
    String getDisplayName();

    /**
     * @param displayName Item's human readable name
     */
    void setDisplayName(String displayName);

    User getOwner();

    void setOwner(User owner);

    String getUid();

    void setUid(String uid);

    Set<CollectionItem> getParents();
    
    /**
     * Each collection an item belongs to contains additional
     * attributes, and is represented as a CollectionItemDetails object.
     * @param parent parent collection
     * @return details about parent<-->child relationship
     */
    CollectionItemDetails getParentDetails(CollectionItem parent);

    /**
     * Return a single parent.
     * @deprecated
     */
    CollectionItem getParent();

}