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
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.unitedinternet.cosmo.model.User;

import java.nio.charset.Charset;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Hibernate persistent User.
 */
@Entity
@Table(name="users")
public class HibUser extends HibAuditableObject implements User {

    private static final long serialVersionUID = -5401963358519490736L;

    public static final int EMAIL_LEN_MIN = 1;
    public static final int EMAIL_LEN_MAX = 128;

    @Column(name = "username", nullable=false)
    @NaturalId
    private String username = "";

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "email", nullable=true, unique=true)
    @Length(min=EMAIL_LEN_MIN, max=EMAIL_LEN_MAX)
    @Email
    private String email;

    @Column(name = "admin")
    private Boolean admin;

    @Column(name = "locked")
    private Boolean locked;

    @Column(name = "roles")
    private String roles;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<HibItem> items;

    /**
     */
    public HibUser() {
        admin = Boolean.FALSE;
        locked = Boolean.FALSE;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setUsername(java.lang.String)
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#getEmail()
     */
    public String getEmail() {
        return email;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setEmail(java.lang.String)
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#getAdmin()
     */
    public Boolean getAdmin() {
        return admin;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#isLocked()
     */
    public Boolean isLocked() {
        return locked;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setLocked(java.lang.Boolean)
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    @Override
    public String getRoles() {
        return roles;
    }

    @Override
    public void setRoles(final String roles) {
        this.roles = roles;
    }

    /**
     * Username determines equality 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || email == null) {
            return false;
        }
        if (! (obj instanceof User)) {
            return false;
        }
        
        return email.equals(((User) obj).getEmail());
    }

    @Override
        public int hashCode() {
        if (email == null) {
            return super.hashCode();
        }
        else {
            return email.hashCode();
        }
    }

    public String calculateEntityTag() {
        String username = getEmail() != null ? getEmail() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = username + ":" + modTime;
        return encodeEntityTag(etag.getBytes(Charset.forName("UTF-8")));
    }
}
