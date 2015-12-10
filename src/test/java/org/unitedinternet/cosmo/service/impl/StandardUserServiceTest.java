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
package org.unitedinternet.cosmo.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockUserDao;
import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibPasswordRecovery;

import java.security.SecureRandom;
import java.util.Set;

/**
 * Test Case for {@link StandardUserService}.
 */
public class StandardUserServiceTest {

    private StandardUserService service;
    private MockContentDao contentDao;
    private MockUserDao userDao;
    private TestHelper testHelper;

    /**
     * Setup.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        testHelper = new TestHelper();
        contentDao = new MockContentDao(new MockDaoStorage());
        userDao = new MockUserDao(new MockDaoStorage());
        service = new StandardUserService();
        service.setContentDao(contentDao);
        service.setUserDao(userDao);
        KeyBasedPersistenceTokenService keyBasedPersistenceTokenService = new KeyBasedPersistenceTokenService();
        keyBasedPersistenceTokenService.setServerSecret("cosmossecret");
        keyBasedPersistenceTokenService.setServerInteger(123);
        keyBasedPersistenceTokenService.setSecureRandom(new SecureRandom());
        service.setPasswordGenerator(keyBasedPersistenceTokenService);
        service.init();
    }

    /**
     * Tests get users.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetUsers() throws Exception {
        User u1 = testHelper.makeDummyUser();
        userDao.createUser(u1);
        User u2 = testHelper.makeDummyUser();
        userDao.createUser(u2);
        User u3 = testHelper.makeDummyUser();
        userDao.createUser(u3);

        Set<User> users = service.getUsers();

        Assert.assertTrue(users.size() == 4); // account for overlord
        Assert.assertTrue("User 1 not found in users", users.contains(u1));
        Assert.assertTrue("User 2 not found in users", users.contains(u2));
        Assert.assertTrue("User 3 not found in users", users.contains(u3));
    }

    /**
     * Tests get user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        String username1 = u1.getUsername();
        userDao.createUser(u1);

        User user = service.getUser(username1);
        Assert.assertNotNull("User " + username1 + " null", user);
    }

    /**
     * Tests get user by email.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetUserByEmail() throws Exception {
        User u1 = testHelper.makeDummyUser();
        String email1 = u1.getEmail();
        userDao.createUser(u1);

        User user = service.getUserByEmail(email1);
        Assert.assertNotNull("User " + email1 + " null", user);
    }

    /**
     * Tests create user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCreateUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        String password = u1.getPassword();

        User user = service.createUser(u1);
        Assert.assertNotNull("User not stored", userDao.getUser(u1.getUsername()));
        Assert.assertFalse("Original and stored password are the same",
                    user.getPassword().equals(password));
        Assert.assertEquals(user.getCreationDate(), user.getModifiedDate());
    }

    /**
     * Tests update user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUpdateUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1.setPassword(service.digestPassword(u1.getPassword()));
        String digestedPassword = u1.getPassword();
        
        userDao.createUser(u1);

        // change password
        u1.setPassword("changedpwd");

        Thread.sleep(1000); // let modified date change
        User user = service.updateUser(u1);
        try {
            userDao.getUser(user.getUsername());
        } catch (DataRetrievalFailureException e) {
            Assert.fail("User not stored");
        }
        Assert.assertFalse("Original and stored password are the same",
                    user.getPassword().equals(digestedPassword));
        Assert.assertTrue("Created and modified dates are the same",
                   ! user.getCreationDate().equals(user.getModifiedDate()));

        // leave password
        Thread.sleep(1000); // let modified date change
        User user2 = service.updateUser(u1);
        try {
            userDao.getUser(user.getUsername());
        } catch (DataRetrievalFailureException e) {
            Assert.fail("User not stored");
        }
        Assert.assertTrue("Original and stored password are not the same",
                    user2.getPassword().equals(user.getPassword()));
        Assert.assertTrue("Created and modified dates are the same",
                   ! user2.getCreationDate().equals(user2.getModifiedDate()));
    }

    /**
     * Tests remove user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRemoveUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        service.createUser(u1);

        service.removeUser(u1);

        Assert.assertFalse("User not removed", userDao.getUsers().contains(u1));
    }

    /**
     * Tests remove user by username.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRemoveUserByUsername() throws Exception {
        User u1 = testHelper.makeDummyUser();
        service.createUser(u1);

        service.removeUser(u1.getUsername());

        Assert.assertFalse("User not removed", userDao.getUsers().contains(u1));
    }

    /**
     * Tests generate password.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGeneratePassword() throws Exception {
        String pwd = service.generatePassword();

        Assert.assertTrue("Password too long", pwd.length() <= User.PASSWORD_LEN_MAX);
        Assert.assertTrue("Password too short", pwd.length() >= User.PASSWORD_LEN_MIN);
    }

    /**
     * Tests null user dao.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testNullUserDao() throws Exception {
        service.setUserDao(null);
        try {
            service.init();
            Assert.fail("Should not be able to initialize service without userDao");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * Tests null password generator.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testNullPasswordGenerator() throws Exception {
        service.setPasswordGenerator(null);
        try {
            service.init();
            Assert.fail("Should not be able to initialize service without passwordGenerator");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * Tests default digest algorithm.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDefaultDigestAlgorithm() throws Exception {
        Assert.assertEquals(service.getDigestAlgorithm(), "MD5");
    }

    /**
     * Test digest password.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDigestPassword() throws Exception {
        String password = "deadbeef";

        String digested = service.digestPassword(password);

        // tests MD5
        Assert.assertTrue("Digest not correct length", digested.length() == 32);

        // tests hex
        Assert.assertTrue("Digest not hex encoded", digested.matches("^[0-9a-f]+$"));
    }
    
    /**
     * Tests create password recovery.
     */
    @Test
    public void testCreatePasswordRecovery(){
        User user = testHelper.makeDummyUser();
        user = userDao.createUser(user);
        
        PasswordRecovery passwordRecovery = 
            new HibPasswordRecovery(user, "pwrecovery1");
        
        passwordRecovery = service.createPasswordRecovery(passwordRecovery);

        PasswordRecovery storedPasswordRecovery = 
            service.getPasswordRecovery(passwordRecovery.getKey());

        Assert.assertEquals(passwordRecovery, storedPasswordRecovery);
        
        service.deletePasswordRecovery(storedPasswordRecovery);
        
        storedPasswordRecovery = 
            service.getPasswordRecovery(storedPasswordRecovery.getKey());
        
        Assert.assertNull(storedPasswordRecovery);
    }
    
    /**
     * Tests recover password.
     */
    @Test
    public void testRecoverPassword(){
        User user = testHelper.makeDummyUser();
        
        userDao.createUser(user);

        PasswordRecovery passwordRecovery = new HibPasswordRecovery(user, "pwrecovery2");
        
        passwordRecovery = service.createPasswordRecovery(passwordRecovery);
        
        Assert.assertEquals(user, passwordRecovery.getUser());
        
        // Recover password
        
        PasswordRecovery storedPasswordRecovery = 
            service.getPasswordRecovery(passwordRecovery.getKey());
        
        User changingUser = storedPasswordRecovery.getUser();
        
        String newPassword = service.generatePassword();

        changingUser.setPassword(newPassword);
        
        changingUser = service.updateUser(changingUser);
        
        String changedPassword = changingUser.getPassword();
        
        User changedUser = service.getUser(changingUser.getUsername());
        
        Assert.assertEquals(changedUser, changingUser);
        
        Assert.assertEquals(changedPassword, changedUser.getPassword());
       
    }
}
