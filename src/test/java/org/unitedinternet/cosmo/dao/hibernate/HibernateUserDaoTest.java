/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.hibernate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class HibernateUserDaoTest extends IntegrationTestSupport {
    
    @Autowired
    private UserDaoImpl userDao;

    @Before
    public void cleanup() {
        final Set<User> usersToDelete = userDao.getUsers();
        for (final User user : usersToDelete) {
            userDao.removeUser(user);
        }
    }

    @Test
    public void testCreateUser() {
        User user1 = new User();
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");

        user1 = userDao.createUser(user1);

        User user2 = new User();
        user2.setEmail("user2@user2.com");
        user2.setPassword("user2password");

        user2 = userDao.createUser(user2);

        // find by username
        User queryUser1 = userDao.getUser("user1@user1.com");
        Assert.assertNotNull(queryUser1);
        Assert.assertNotNull(queryUser1.getEmail());
        verifyUser(user1, queryUser1);

        // Get all
        @SuppressWarnings("rawtypes")
        Set users = userDao.getUsers();
        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());
        verifyUserInCollection(user1, users);
        verifyUserInCollection(user2, users);

        // try to create duplicate
        User user3 = new User();
        user3.setEmail("user1@user1.com");
        user3.setPassword("user1password");

        try {
            userDao.createUser(user3);
            Assert.fail("able to create user with duplicate username");
        } catch (DuplicateEmailException due) {
        }

        try {
            userDao.createUser(user3);
            Assert.fail("able to create user with duplicate email");
        } catch (DuplicateEmailException dee) {
        }

        // delete user
        userDao.removeUser("user1@user1.com");
    }

    /**
     * Tests create duplicate user email.
     */
    @Test
    public void testCreateDuplicateUserEmail() {
        User user1 = new User();
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");

        user1 = userDao.createUser(user1);

        User user2 = new User();
        user2.setEmail("user1@user1.com");
        user2.setPassword("user2password");

        try {
            user2 = userDao.createUser(user2);
            Assert.fail("able to create duplicate emails!");
        } catch (DuplicateEmailException e) {
        }
        
        
        user2.setEmail("user2@user1.com");
        user2 = userDao.createUser(user2);
    }

    /**
     * Tests delete user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteUser() throws Exception {
        User user1 = new User();
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");

        userDao.createUser(user1);
        
        User queryUser1 = userDao.getUser("user1@user1.com");
        Assert.assertNotNull(queryUser1);
        userDao.removeUser(queryUser1);

        queryUser1 = userDao.getUser("user1");
        Assert.assertNull(queryUser1);
    }

    /**
     * Tests delete user by username.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteUserByEmail() throws Exception {
        User user1 = new User();
        user1.setEmail("user1@user1.com");
        user1.setPassword("user1password");

        userDao.createUser(user1);

        User queryUser1 = userDao.getUser("user1@user1.com");
        Assert.assertNotNull(queryUser1);
        userDao.removeUser(user1.getEmail());

        queryUser1 = userDao.getUser("user1@user1.com");
        Assert.assertNull(queryUser1);
    }

    /**
     * Tests verify user.
     * @param user1 User1.
     * @param user2 User2.
     */
    private void verifyUser(User user1, User user2) {
        Assert.assertEquals(user1.getEmail(), user2.getEmail());
        Assert.assertEquals(user1.getPassword(), user2.getPassword());
    }

    /**
     * Verify user in collection.
     * @param user The user.
     * @param users The users.
     */
    private void verifyUserInCollection(User user, @SuppressWarnings("rawtypes") Collection users) {
        @SuppressWarnings("rawtypes")
        Iterator it = users.iterator();
        while (it.hasNext()) {
            User nextUser = (User) it.next();
            if (nextUser.getEmail().equals(user.getEmail())) {
                verifyUser(user, nextUser);
                return;
            }
        }
        Assert.fail("specified User doesn't exist in Set: "
                + user.getEmail());
    }

}
