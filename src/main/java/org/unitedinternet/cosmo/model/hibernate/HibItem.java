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

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.model.User;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)


@Table(name = "item",
        indexes={@Index(name = "idx_itemtype",columnList = "itemtype" ),
                 @Index(name = "idx_itemuid",columnList = "uid" ),
                 @Index(name = "idx_itemname",columnList = "itemname" ),
        }
)
@DiscriminatorColumn(
        name="itemtype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class HibItem extends HibAuditableObject {

    @Column(name = "uid", nullable = false, length=255)
    @NotNull
    @NaturalId
    @Length(min = 1, max = 255)
    private String uid;

    @Column(name = "itemname", nullable = false, length=255)
    @NotNull
    @Length(min = 1, max = 255)
    private String name;

    @Column(name = "displayname", length=1024)
    private String displayName;

    @Column(name = "clientcreatedate")
    @Type(type="long_timestamp")
    private Date clientCreationDate;

    @Column(name = "clientmodifieddate")
    @Type(type="long_timestamp")
    private Date clientModifiedDate;

    @Version
    @Column(name="version", nullable = false)
    private Integer version;

    @OneToMany(targetEntity=HibAttribute.class, mappedBy = "item", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    @MapKeyClass(HibQName.class)
    @BatchSize(size=50)
    private Map<HibQName, HibAttribute> attributes = new HashMap<>();

    @OneToMany(targetEntity=HibStamp.class, mappedBy = "item", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    @BatchSize(size=50)
    private Set<HibStamp> stamps = new HashSet<>();

    @OneToMany(targetEntity=HibCollectionItemDetails.class, mappedBy="item", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<HibCollectionItemDetails> parentDetails = new HashSet<>();

    private transient Set<HibCollectionItem> parents = null;

    @ManyToOne(targetEntity=User.class, fetch=FetchType.LAZY)
    @JoinColumn(name="ownerid", nullable = false)
    @NotNull
    private User owner;


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getStamps()
     */
    public Set<HibStamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#addStamp(org.unitedinternet.cosmo.model.Stamp)
     */
    public void addStamp(HibStamp stamp) {
        if (stamp == null) {
            throw new IllegalArgumentException("stamp cannot be null");
        }

        stamp.setItem(this);
        stamps.add(stamp);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeStamp(org.unitedinternet.cosmo.model.Stamp)
     */
    public void removeStamp(HibStamp stamp) {
        // only remove stamps that belong to item
        if(!stamps.contains(stamp)) {
            return;
        }

        stamps.remove(stamp);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getStamp(java.lang.String)
     */
    public HibStamp getStamp(String type) {
        for(HibStamp stamp : stamps) {
            // only return stamp if it matches class and is active
            if(stamp.getType().equals(type)) {
                return stamp;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getStamp(java.lang.Class)
     */
    public HibStamp getStamp(Class clazz) {
        for(HibStamp stamp : stamps) {
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp)) {
                return stamp;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttributes()
     */
    public Map<HibQName, HibAttribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#addAttribute(org.unitedinternet.cosmo.model.Attribute)
     */
    public void addAttribute(HibAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute cannot be null");
        }

        attribute.setItem(this);
        attributes.put(attribute.getQName(), attribute);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        removeAttribute(new HibQName(name));
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeAttribute(org.unitedinternet.cosmo.model.QName)
     */
    public void removeAttribute(HibQName qname) {
        if(attributes.containsKey(qname)) {
            attributes.remove(qname);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttribute(java.lang.String)
     */
    public HibAttribute getAttribute(String name) {
        return getAttribute(new HibQName(name));
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttribute(org.unitedinternet.cosmo.model.QName)
     */
    public HibAttribute getAttribute(HibQName qname) {
        return attributes.get(qname);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttributeValue(org.unitedinternet.cosmo.model.QName)
     */
    public Object getAttributeValue(HibQName qname) {
        HibAttribute attr = attributes.get(qname);
        if (attr == null) {
            return attr;
        }
        return attr.getValue();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        setAttribute(new HibQName(name),value);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setAttribute(org.unitedinternet.cosmo.model.QName, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setAttribute(HibQName key, Object value) {
        HibAttribute attr = (HibAttribute) attributes.get(key);

        if(attr!=null) {
            attr.setValue(value);
        }
        else {
            throw new IllegalArgumentException("attribute " + key + " not found");
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getClientCreationDate()
     */
    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setClientCreationDate(java.util.Date)
     */
    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getClientModifiedDate()
     */
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setClientModifiedDate(java.util.Date)
     */
    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getOwner()
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setOwner(org.unitedinternet.cosmo.model.User)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getUid()
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setUid(java.lang.String)
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getVersion()
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param parent collection to add item to
     */
    public void addParent(HibCollectionItem parent) {
        parentDetails.add(new HibCollectionItemDetails(parent,this));

        // clear cached parents
        parents = null;
    }

    public void removeParent(HibCollectionItem parent) {
        HibCollectionItemDetails cid = getParentDetails(parent);
        if(cid!=null) {
            parentDetails.remove(cid);
            // clear cached parents
            parents = null;
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParents()
     */
    public Set<HibCollectionItem> getParents() {
        if(parents!=null) {
            return parents;
        }

        parents = new HashSet<>();
        for(HibCollectionItemDetails cid: parentDetails) {
            parents.add(cid.getCollection());
        }

        parents = Collections.unmodifiableSet(parents);

        return parents;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParent()
     */
    public HibCollectionItem getParent() {
        if(getParents().size()==0) {
            return null;
        }

        return getParents().iterator().next();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParentDetails(org.unitedinternet.cosmo.model.CollectionItem)
     */
    public HibCollectionItemDetails getParentDetails(HibCollectionItem parent) {
        for(HibCollectionItemDetails cid: parentDetails) {
            if(cid.getCollection().equals(parent)) {
                return cid;
            }
        }
        
        return null;
    }

    /**
     * Item uid determines equality 
     */
    @Override
    public boolean equals(Object obj) {
        if(obj==null || uid==null) {
            return false;
        }
        if( ! (obj instanceof HibItem)) {
            return false;
        }

        return uid.equals(((HibItem) obj).getUid());
    }

    @Override
    public int hashCode() {
        if(uid==null) {
            return super.hashCode();
        }
        else {
            return uid.hashCode();
        }
    }

    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
                Long.valueOf(getModifiedDate().getTime()).toString() : "-";
                String etag = uid + ":" + modTime;
                return encodeEntityTag(etag.getBytes(Charset.forName("UTF-8")));
    }
}
