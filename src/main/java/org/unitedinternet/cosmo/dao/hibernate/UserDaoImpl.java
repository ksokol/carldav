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

import java.util.List;

public class UserDaoImpl extends AbstractDaoImpl implements UserDao {

    public User save(User user) {
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

    public User findByEmailIgnoreCase(String email) {
        return findUserByEmailIgnoreCase(email);
    }

    public List<User> findAll() {
        return getSession().getNamedQuery("user.all").list();
    }

    public void remove(User user) {
        getSession().delete(user);
      //  getSession().flush();
    }

    private User findUserByEmailIgnoreCase(String email) {
        return (User) getSession().getNamedQuery("user.byEmail.ignorecase").setParameter("email", email).uniqueResult();
    }
}
