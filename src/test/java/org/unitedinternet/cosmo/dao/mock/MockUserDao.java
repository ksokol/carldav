/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.mock;

import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockAuditableObject;
import org.unitedinternet.cosmo.model.mock.MockUser;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Mock implementation of {@link UserDao} useful for testing.
 */
public class MockUserDao implements UserDao {

    @SuppressWarnings("rawtypes")
    private HashMap emailIdx;
    @SuppressWarnings("rawtypes")
    private HashMap uidIdx;

    public MockUserDao() {
        emailIdx = new HashMap();
        uidIdx = new HashMap();

        // add overlord user
        MockUser overlord = new MockUser();
        overlord.setUsername("username");
        overlord.setPassword("32a8bd4d676f4fef0920c7da8db2bad7");
        overlord.setEmail("root@localhost");
        overlord.setCreationDate(new Date());
        overlord.setModifiedDate(new Date());
        createUser(overlord);
    }

    // UserDao methods

    /**
     * Gets users.
     * @return The users.
     */
    @SuppressWarnings("unchecked")
    public Set<User> getUsers() {
        @SuppressWarnings("rawtypes")
        Set tmp = new HashSet();
        for (@SuppressWarnings("rawtypes")
        Iterator i=emailIdx.values().iterator(); i.hasNext();) {
            tmp.add(i.next());
        }
        return tmp;
    }

    /**
     * Gets user by email.
     * {@inheritDoc}
     * @param email The email.
     * @return The user.
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return (User) emailIdx.get(email);
    }

    /**
     * Creates user.
     * {@inheritDoc}
     * @param user The user.
     * @return The user.
     */
    @SuppressWarnings("unchecked")
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("null user");
        }

        // Set create/modified date, etag for User and associated subscriptions
        // and perferences.
        ((MockAuditableObject) user).setModifiedDate(new Date());
        ((MockAuditableObject) user).setCreationDate(new Date());
        ((MockAuditableObject) user).setEntityTag(user
                .calculateEntityTag());

        ((MockUser) user).validate();
        if (emailIdx.containsKey(user.getEmail())) {
            throw new DuplicateEmailException(user.getEmail());
        }

        emailIdx.put(user.getEmail(), user);
        uidIdx.put(user.getEmail(), user);
        return user;
    }

    /**
     * Removes user.
     * {@inheritDoc}
     * @param username The username.
     */
    public void removeUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("null username");
        }
        if (emailIdx.containsKey(username)) {
            User user = (User) emailIdx.get(username);
            emailIdx.remove(username);
            emailIdx.remove(user.getEmail());
        }
    }
}
