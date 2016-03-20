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

import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name="users", uniqueConstraints = {@UniqueConstraint(name = "user_email", columnNames = "email")})
public class User {

    private Long id;
    private String password;
    private String email;
    private boolean locked;
    private Set<HibCollectionItem> collections;
    private String role;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    @NotNull
    @Column(name = "email", nullable=false)
    @Email
    public String getEmail() {
        return email;
    }

    public boolean isLocked() {
        return locked;
    }

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    public Set<HibCollectionItem> getCollections() {
        return collections;
    }

    public void setCollections(Set<HibCollectionItem> collections) {
        this.collections = collections;
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

    public void setRole(final String role) {
        this.role = role;
    }
}