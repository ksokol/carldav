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

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.util.List;

public class UserDaoImpl implements UserDao {

    private final SessionFactory sessionFactory;

    public UserDaoImpl(SessionFactory sessionFactory) {
        Assert.notNull(sessionFactory, "sessionFactory is null");
        this.sessionFactory = sessionFactory;
    }

    public User save(User user) {
        sessionFactory.getCurrentSession().saveOrUpdate(user);
        sessionFactory.getCurrentSession().flush();
        return user;
    }

    public User findByEmailIgnoreCase(String email) {
        return findUserByEmailIgnoreCase(email);
    }

    public List<User> findAll() {
        return sessionFactory.getCurrentSession().getNamedQuery("user.all").list();
    }

    public void remove(User user) {
        sessionFactory.getCurrentSession().delete(user);
    }

    private User findUserByEmailIgnoreCase(String email) {
        return (User) sessionFactory.getCurrentSession().getNamedQuery("user.byEmail.ignorecase").setParameter("email", email).uniqueResult();
    }
}
