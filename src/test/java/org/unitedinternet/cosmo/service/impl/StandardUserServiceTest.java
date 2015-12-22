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
import org.unitedinternet.cosmo.model.hibernate.User;

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
     * Tests create user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCreateUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        String password = u1.getPassword();

        User user = service.createUser(u1);
        Assert.assertNotNull("User not stored", userDao.getUserByEmail(u1.getEmail()));
        Assert.assertFalse("Original and stored password are the same",
                    user.getPassword().equals(password));
    }

    /**
     * Tests remove user by username.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRemoveUserByEmail() throws Exception {
        User u1 = testHelper.makeDummyUser();
        service.createUser(u1);

        service.removeUser(u1.getEmail());

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
