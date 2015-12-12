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
package org.unitedinternet.cosmo.dao;

import org.unitedinternet.cosmo.model.User;

import java.util.Set;

/**
 * Interface for DAOs that manage user resources.
 *
 * A user resource stores properties about a user account and acts as
 * the root collection for an account's shared data (its "home
 * directory").
 */
public interface UserDao extends Dao {

    /**
     * Returns an unordered set of all user accounts in the repository.
     * @return All user accounts.
     */
    Set<User> getUsers();

    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     * exist
     * @return The user account.
     */
    User getUser(String username);
    
    
    /**
     * Returns the user account identified by the given uid.
     *
     * @param uid the uid of the account to return
     * exist
     * @return The user account identified by the given uid.
     */
    User getUserByUid(String uid);

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     * @return The user account identified by the given email address.
     */
    User getUserByEmail(String email);
    
    /**
     * Returns a set of users that contain a user preference that
     * matches a specific key and value.
     * @param key user preference key to match
     * @param value user preference value to match
     * @return set of users containing a user preference that matches
     *         key and value
     */
    Set<User> findUsersByPreference(String key, String value);

    /**
     * Creates a user account in the repository. Returns a new
     * instance of <code>User</code> after saving the original one.
     *
     * @param user the account to create.
     * @return The user account.
     *
     */
    User createUser(User user);

    /**
     * Updates a user account that exists in the repository. Returns a
     * new instance of <code>User</code>  after saving the original
     * one.
     *
     * @param user the account to update
     * @return The updated account of the user.
     *
     */
    User updateUser(User user);

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    void removeUser(String username);

    /**
     * Removes a user account from the repository.
     *
     * @param user the user to remove
     */
    void removeUser(User user);
}
