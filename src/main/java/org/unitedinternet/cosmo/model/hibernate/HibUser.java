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
import org.unitedinternet.cosmo.dao.ModelValidationException;
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

    /**
     */
    private static final long serialVersionUID = -5401963358519490736L;
   
    /**
     */
    public static final int USERNAME_LEN_MIN = 3;
    /**
     */
    public static final int USERNAME_LEN_MAX = 64;

    /**
     */
    public static final int EMAIL_LEN_MIN = 1;
    /**
     */
    public static final int EMAIL_LEN_MAX = 128;

    @Column(name = "username", nullable=false)
    @NotNull
    @NaturalId
    @Length(min=USERNAME_LEN_MIN, max=USERNAME_LEN_MAX)
    //per bug 11599:
    // Usernames must be between 3 and 64 characters; may contain any Unicode
    //character in the following range of unicode code points: [#x20-#xD7FF] |
    //[#xE000-#xFFFD] EXCEPT #x7F or #x3A
    // Oh and don't allow ';' or '/' because there are problems with encoding
    // them in urls (tomcat doesn't support it)
    @javax.validation.constraints.Pattern(regexp="^[\\u0020-\\ud7ff\\ue000-\\ufffd&&[^\\u007f\\u003a;/\\\\]]+$")
    private String username;

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

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<HibItem> items;

    /**
     */
    public HibUser() {
        admin = Boolean.FALSE;
        locked = Boolean.FALSE;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#getUsername()
     */
    public String getUsername() {
        return username;
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

    /**
     * Username determines equality 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || username == null) {
            return false;
        }
        if (! (obj instanceof User)) {
            return false;
        }
        
        return username.equals(((User) obj).getUsername());
    }

    @Override
        public int hashCode() {
        if (username == null) {
            return super.hashCode();
        }
        else {
            return username.hashCode();
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#validateRawPassword()
     */
    public void validateRawPassword() {
        if (password == null) {
            throw new ModelValidationException("UserName" + this.getUsername() + " EMAIL " + this.getEmail(),
                    "Password not specified");
        }
        if (password.length() < PASSWORD_LEN_MIN ||
            password.length() > PASSWORD_LEN_MAX) {
            
            throw new ModelValidationException("UserName" + this.getUsername() + " EMAIL " + this.getEmail(),
                                               "Password must be " +
                                               PASSWORD_LEN_MIN + " to " +
                                               PASSWORD_LEN_MAX +
                                               " characters in length");
        }
    }

    public String calculateEntityTag() {
        String username = getUsername() != null ? getUsername() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = username + ":" + modTime;
        return encodeEntityTag(etag.getBytes(Charset.forName("UTF-8")));
    }
}
