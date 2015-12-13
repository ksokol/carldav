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

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.model.AuditableObject;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Hibernate persistent AuditableObject.
 */
@MappedSuperclass
public abstract class HibAuditableObject extends BaseModelObject implements AuditableObject {

    @Column(name = "createdate")
    @Type(type="long_timestamp")
    private Date creationDate;
    
    @Column(name = "modifydate")
    @Type(type="long_timestamp")
    private Date modifiedDate;
    
    @Column(name="etag")
    private String etag = "";
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.AuditableObject#getCreationDate()
     */
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.AuditableObject#getModifiedDate()
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.AuditableObject#updateTimestamp()
     */
    public void updateTimestamp() {
        modifiedDate = new Date();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.AuditableObject#getEntityTag()
     */
    public String getEntityTag() {
        return etag;
    }
    
    public void setEntityTag(String etag) {
        this.etag = etag;
    }
    
    /**
     * Calculates object's entity tag. Returns the empty string. Subclasses should override this.
     */
    public abstract String calculateEntityTag();

    /**
     * <p>
     * Returns a Base64-encoded SHA-1 digest of the provided bytes.
     * </p>
     */
    protected static String encodeEntityTag(byte[] bytes) {
        return DigestUtils.md5Hex(bytes);
    }
}
