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
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockUserDao;
import org.unitedinternet.cosmo.model.User;

import java.util.Set;

/**
 * Test Case for {@link StandardUserService}.
 */
public class StandardUserServiceTest {

    private StandardUserService service;
    private MockUserDao userDao;
    private TestHelper testHelper;

    /**
     * Setup.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        testHelper = new TestHelper();
        userDao = new MockUserDao();
        service = new StandardUserService(new MockContentDao(new MockDaoStorage()), userDao);
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
}
