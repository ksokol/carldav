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
package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.Email;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

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
public class User {

    private Long id = Long.valueOf(-1);
    private String password;
    private String email;
    private boolean locked;
    private Set<HibItem> items;
    private String role;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    @NotNull
    @NaturalId
    @Column(name = "email", nullable=false, unique=true)
    @Email
    public String getEmail() {
        return email;
    }

    public boolean isLocked() {
        return locked;
    }

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    public Set<HibItem> getItems() {
        return items;
    }

    public String getRole() {
        return role;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public void setItems(final Set<HibItem> items) {
        this.items = items;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}