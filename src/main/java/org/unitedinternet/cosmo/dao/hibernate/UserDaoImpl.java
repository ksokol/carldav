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
package org.unitedinternet.cosmo.dao.hibernate;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserDaoImpl extends AbstractDaoImpl implements UserDao {

    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is required");
        }

        if (user.getId() != -1) {
            throw new IllegalArgumentException("new user is required");
        }

        if (findUserByEmailIgnoreCase(user.getEmail()) != null) {
            throw new DuplicateEmailException(user.getEmail());
        }

        getSession().save(user);
        getSession().flush();
        return user;
    }

    public User getUser(String email) {
        return findUserByEmailIgnoreCase(email);
    }

    public Set<User> getUsers() {
        Set<User> users = new HashSet<>();
        Iterator it = getSession().getNamedQuery("user.all").iterate();
        while (it.hasNext()) {
            users.add((User) it.next());
        }

        return users;
    }

    public void removeUser(String email) {
        User user = findUserByEmailIgnoreCase(email);
        // delete user
        if (user != null) {
            removeUser(user);
        }
    }

    public void removeUser(User user) {
        getSession().delete(user);
        getSession().flush();
    }

    private User findUserByEmailIgnoreCase(String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byEmail.ignorecase").setParameter(
                "email", email);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }
}
