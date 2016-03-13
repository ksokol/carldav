/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

import java.util.Set;

public class StandardUserService implements UserService {

    private final ContentService contentService;
    private final UserDao userDao;

    public StandardUserService(final ContentService contentService, final UserDao userDao) {
        Assert.notNull(contentService, "contentService is null");
        Assert.notNull(userDao, "userDao is null");
        this.contentService = contentService;
        this.userDao = userDao;
    }

    public Set<User> getUsers() {
        return userDao.getUsers();
    }

    public User getUser(String username) {
        return userDao.getUser(username);
    }

    public User getUserByEmail(String email) {
        return userDao.getUser(email);
    }

    /**
     * Creates a user account in the repository. Digests the raw
     * password and uses the result to replace the raw
     * password. Returns a new instance of <code>User</code>
     * after saving the original one.
     *
     * @param user the account to create
     *
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User createUser(User user) {

        validateRawPassword(user);

        user.setPassword(digestPassword(user.getPassword()));

        userDao.createUser(user);

        User newUser = userDao.getUser(user.getEmail());

        HibCollectionItem calendar = new HibCollectionItem();
        calendar.setOwner(user);
        calendar.setName("calendar");
        calendar.setDisplayName("calendarDisplayName");

        final HibCollectionItem homeCollection = contentService.createRootItem(newUser);
        contentService.createCollection(homeCollection, calendar);

        HibCollectionItem addressbook = new HibCollectionItem();
        addressbook.setOwner(user);
        addressbook.setName("contacts");
        addressbook.setDisplayName("contactDisplayName");

        contentService.createCollection(homeCollection, addressbook);

        return newUser;
    }

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(String username) {
        User user = userDao.getUser(username);
        userDao.removeUser(user);
    }

    /**
     * Removes a user account from the repository.
     *
     * @param user the account to remove
     */
    public void removeUser(User user) {
        userDao.removeUser(user);
    }

    /**
     * Digests the given password using the set message digest and hex
     * encodes it.
     */
    protected String digestPassword(String password) {
        if (password == null) {
            return null;
        }

        return DigestUtils.md5Hex(password);
    }

    private static void validateRawPassword(final User user) {
        if (user.getPassword() == null) {
            throw new ModelValidationException(" EMAIL " + user.getEmail(),
                    "Password not specified");
        }
    }
}
