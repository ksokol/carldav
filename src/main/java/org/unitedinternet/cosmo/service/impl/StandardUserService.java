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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.token.TokenService;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.ServiceEvent;
import org.unitedinternet.cosmo.service.ServiceListener;
import org.unitedinternet.cosmo.service.UserService;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Set;

/**
 * Standard implementation of {@link UserService}.
 */
public class StandardUserService extends BaseService implements UserService {

    private static final Log LOG = LogFactory.getLog(StandardUserService.class);

    /**
     * The service uses MD5 if no digest algorithm is explicitly set.
     */
    public static final String DEFAULT_DIGEST_ALGORITHM = "MD5";
    
    private String digestAlgorithm;
    private TokenService passwordGenerator;
    private ContentDao contentDao;
    private UserDao userDao;

    // UserService methods

    /**
     * Returns an unordered set of all user accounts in the repository.
     */
    public Set<User> getUsers() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getting all users");
        }
        return userDao.getUsers();
    }

    /**
     * Returns the user account identified by the given username.
     *
     * @param username the username of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUser(String username) {
        if (LOG.isDebugEnabled()) {
            //Fix Log Forging - fortify
            //Writing unvalidated user input to log files can allow an attacker to forge log entries
            //or inject malicious content into the logs.
            LOG.debug("getting user " + username);
        }
        return userDao.getUser(username);
    }

    /**
     * Returns the user account identified by the given email address.
     *
     * @param email the email address of the account to return
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     */
    public User getUserByEmail(String email) {
        if (LOG.isDebugEnabled()) {
            //Fix Log Forging - fortify
            //Writing unvalidated user input to log files can allow an attacker to forge log entries
            //or inject malicious content into the logs.
            LOG.debug("getting user with email address " + email);
        }
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
        return createUser(user, new ServiceListener[] {});
    }

    /**
     * Creates a user account in the repository as per
     * {@link #createUser(User)}. Sends the <code>CREATE_USER</code>
     * event to each provided listener, providing the newly created
     * user and home collection as state.
     *
     * @param user the account to create
     * @param listeners an array of listeners to notify
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User createUser(User user,
                           ServiceListener[] listeners) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating user " + user.getUsername());
        }

        user.validateRawPassword();

        user.setPassword(digestPassword(user.getPassword()));
        
        fireBeforeEvent(new ServiceEvent("CREATE_USER", user), listeners);

        try {
            userDao.createUser(user);
            LOG.info("created new user:" + user.getUid());
        } catch (DataIntegrityViolationException e) {
            if (userDao.getUser(user.getUsername()) != null) {
                throw new DuplicateUsernameException(user.getUsername());
            }
            if (userDao.getUserByEmail(user.getEmail()) != null) {
                throw new DuplicateEmailException(user.getEmail());
            }
            throw e;
        }

        User newUser = userDao.getUser(user.getUsername());

        HomeCollectionItem home = null;
        if (! newUser.isOverlord()) {
            home = contentDao.createRootItem(newUser);
        }

        fireAfterEvent(new ServiceEvent("CREATE_USER", user, home), listeners);

        return newUser;
    }

    /**
     * Updates a user account that exists in the repository. If the
     * password has been changed, digests the raw new password and
     * uses the result to replace the stored password. Returns a new
     * instance of <code>User</code>  after saving the original one.
     *
     * @param user the account to update
     *
     * @throws DataRetrievalFailureException if the account does not
     * exist
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User updateUser(User user) {
        boolean isUsernameChanged = user.isUsernameChanged();
        if (LOG.isDebugEnabled()) {
            //Fix Log Forging - fortify
            //Writing unvalidated user input to log files can allow an attacker to forge
            //log entries or inject malicious content into the logs.
            LOG.debug("updating user " + user.getOldUsername());
            if (isUsernameChanged) {
                //Fix Log Forging - fortify
                //Writing unvalidated user input to log files can allow an attacker to forge log entries
                //or inject malicious content into the logs.
                LOG.debug("... changing username to " + user.getUsername());
            }
        }

        if (user.getPassword().length() < 32) {
            user.validateRawPassword();
            user.setPassword(digestPassword(user.getPassword()));
        }
       
        userDao.updateUser(user);

        User newUser = userDao.getUser(user.getUsername());

        if (isUsernameChanged) {
            if (LOG.isDebugEnabled()) {
                //Fix Log Forging - fortify
                //Writing unvalidated user input to log files can allow an attacker to forge log entries
                //or inject malicious content into the logs.
                LOG.debug("renaming root item for user " + newUser.getUsername());
            }
            HomeCollectionItem rootCollection = contentDao.getRootItem(newUser);
            rootCollection.setName(newUser.getUsername());
            contentDao.updateCollection(rootCollection);
        }

        return newUser;
    }

    /**
     * Removes the user account identified by the given username from
     * the repository.
     *
     * @param username the username of the account to return
     */
    public void removeUser(String username) {
        if (LOG.isDebugEnabled()) {
        	//Fix Log Forging - fortify
        	//Writing unvalidated user input to log files can allow an attacker to
        	//forge log entries or inject malicious content into the logs.
            LOG.debug("removing user " + username);
        }
        User user = userDao.getUser(username);
        removeUserAndItems(user);
    }

    /**
     * Removes a user account from the repository.
     *
     * @param user the account to remove
     */
    public void removeUser(User user) {
        if (LOG.isDebugEnabled()) {
            //Fix Log Forging - fortify
            //Writing unvalidated user input to log files can allow an attacker to forge log entries
            //or inject malicious content into the logs.
            LOG.debug("removing user " + user.getUsername());
        }
        removeUserAndItems(user);
    }

    /**
     * Generates a random password in a format suitable for
     * presentation as an authentication credential.
     */
    public String generatePassword() {
        String password = passwordGenerator.allocateToken("").getKey();
        return password.length() <= User.PASSWORD_LEN_MAX ?
            password :
            password.substring(0, User.PASSWORD_LEN_MAX - 1);
    }

    // Service methods

    /**
     * Initializes the service, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        if (contentDao == null) {
            throw new IllegalStateException("contentDao is required");
        }
        if (userDao == null) {
            throw new IllegalStateException("userDao is required");
        }
        if (passwordGenerator == null) {
            throw new IllegalStateException("passwordGenerator is required");
        }
        if (digestAlgorithm == null) {
            digestAlgorithm = DEFAULT_DIGEST_ALGORITHM;
        }
    }

    /**
     * Readies the service for garbage collection, shutting down any
     * resources used.
     */
    public void destroy() {
        // does nothing
    }

    // our methods

    /**
     * Digests the given password using the set message digest and hex
     * encodes it.
     */
    protected String digestPassword(String password) {
        if (password == null) {
            return password;
        }
        try {
            return new String(Hex.encodeHex(MessageDigest.getInstance(
                    digestAlgorithm).digest(password.getBytes(Charset.forName("UTF-8")))));
        } catch (Exception e) {
            throw new CosmoException("cannot get digest for algorithm "
                    + digestAlgorithm, e);
        }
    }

    /**
     */
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    /**
     */
    public void setPasswordGenerator(TokenService generator) {
        this.passwordGenerator = generator;
    }

    /**
     */
    public ContentDao getContentDao() {
        return this.contentDao;
    }

    /**
     */
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    /**
     */
    public UserDao getUserDao() {
        return this.userDao;
    }

    /**
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Remove all Items associated to User.
     * This is done by removing the HomeCollectionItem, which is the root
     * collection of all the user's items.
     */
    private void removeUserAndItems(User user) {
        if(user==null) {
            return;
        }
        HomeCollectionItem home = contentDao.getRootItem(user);
        // remove collections/subcollections
        contentDao.removeCollection(home);
        // remove dangling items 
        // (items that only exist in other user's collections)
        contentDao.removeUserContent(user);
        userDao.removeUser(user);
    }
}
