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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.UserService;

import java.util.Set;

/**
 * Standard implementation of {@link UserService}.
 */
public class StandardUserService implements UserService {

    private static final Log LOG = LogFactory.getLog(StandardUserService.class);

    private final ContentDao contentDao;
    private final UserDao userDao;

    public StandardUserService(final ContentDao contentDao, final UserDao userDao) {
        Assert.notNull(contentDao, "contentDao is null");
        Assert.notNull(userDao, "userDao is null");
        this.contentDao = contentDao;
        this.userDao = userDao;
    }

    public Set<User> getUsers() {
        return userDao.getUsers();
    }

    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
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

        try {
            userDao.createUser(user);
            LOG.info("created new user:" + user.getEmail());
        } catch (DataIntegrityViolationException e) {
            if (userDao.getUserByEmail(user.getEmail()) != null) {
                throw new DuplicateEmailException(user.getEmail());
            }
            throw e;
        }

        User newUser = userDao.getUserByEmail(user.getEmail());
        contentDao.createRootItem(newUser);
        return newUser;
    }

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(String username) {
        User user = userDao.getUserByEmail(username);
        if(user==null) {
            return;
        }
        HomeCollectionItem home = contentDao.getRootItem(user);
        // remove collections/subcollections
        contentDao.removeCollection(home);
        // remove dangling items
        // (items that only exist in other user's collections)
        contentDao.removeUserContent(user);
        userDao.removeUser(user.getEmail());
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

    @Deprecated
    private static void validateRawPassword(User user) {
        if (user.getPassword() == null) {
            throw new ModelValidationException(" EMAIL " + user.getEmail(),
                    "Password not specified");
        }
    }
}
