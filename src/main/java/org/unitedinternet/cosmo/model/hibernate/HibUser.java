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

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="users")
public class HibUser implements User {

    /**
     */
    public static final int EMAIL_LEN_MIN = 1;
    /**
     */
    public static final int EMAIL_LEN_MAX = 128;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id = Long.valueOf(-1);

    @Column(name = "uid", nullable=false, unique=true, length=255)
    @NotNull
    @Length(min=1, max=255)
    private String uid;

    @NotNull
    private String password;

    @NotNull
    @NaturalId
    @Column(name = "email", nullable=true, unique=true)
    @Length(min=EMAIL_LEN_MIN, max=EMAIL_LEN_MAX)
    @Email
    private String email;

    private Boolean locked = Boolean.FALSE;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<HibItem> items;

    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#getUid()
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.User#setUid(java.lang.String)
     */
    public void setUid(String uid) {
        this.uid = uid;
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
     * @see org.unitedinternet.cosmo.model.User#isLocked()
     */
    public Boolean isLocked() {
        return locked;
    }
}
