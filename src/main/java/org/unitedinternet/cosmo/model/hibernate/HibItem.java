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

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

    @Column(name = "displayname")
    @NotEmpty
    private String displayName;

    @Column(name = "clientcreatedate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date clientCreationDate;

    @Column(name = "clientmodifieddate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date clientModifiedDate;

    @Version
    @Column(name="version", nullable = false)
    private Integer version;

    @OneToMany(targetEntity=HibStamp.class, mappedBy = "item", fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<HibStamp> stamps = new HashSet<>();

    @ManyToOne(targetEntity=HibCollectionItem.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "collectionid")
    private HibCollectionItem collection;

    @ManyToOne(targetEntity=User.class, fetch=FetchType.LAZY)
    @JoinColumn(name="ownerid", nullable = false)
    @NotNull
    private User owner;

    public Set<HibStamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }

    public void addStamp(HibStamp stamp) {
        if (stamp == null) {
            throw new IllegalArgumentException("stamp cannot be null");
        }

        stamp.setItem(this);
        stamps.add(stamp);
    }

    public void removeStamp(HibStamp stamp) {
        // only remove stamps that belong to item
        if(!stamps.contains(stamp)) {
            return;
        }

        stamps.remove(stamp);
    }

    public HibStamp getStamp(Class clazz) {
        for(HibStamp stamp : stamps) {
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp)) {
                return stamp;
            }
        }

        return null;
    }

    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }

    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setCollection(HibCollectionItem parent) {
        collection = parent;
    }

    public HibCollectionItem getCollection() {
        return collection;
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
