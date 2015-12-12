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

import carldav.service.generator.IdGenerator;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.unitedinternet.cosmo.dao.DuplicateEmailException;
import org.unitedinternet.cosmo.dao.DuplicateUsernameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibUser;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;


/**
 * Implemtation of UserDao using Hibernate persistence objects.
 */
public class UserDaoImpl extends AbstractDaoImpl implements UserDao {

    private IdGenerator idGenerator;

    public User createUser(User user) {

        try {
            if (user == null) {
                throw new IllegalArgumentException("user is required");
            }

            if (getBaseModelObject(user).getId() != -1) {
                throw new IllegalArgumentException("new user is required");
            }

            if (findUserByUsernameIgnoreCase(user.getUsername()) != null) {
                throw new DuplicateUsernameException(user.getUsername());
            }

            if (findUserByEmailIgnoreCase(user.getEmail()) != null) {
                throw new DuplicateEmailException(user.getEmail());
            }

            if (user.getUid() == null || "".equals(user.getUid())) {
                user.setUid(getIdGenerator().nextStringIdentifier());
            }

            getSession().save(user);
            getSession().flush();
            return user;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }

    }

    public User getUser(String username) {
        try {
            return findUserByUsername(username);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByUid(String uid) {
        if (uid == null) {
            throw new IllegalArgumentException("uid required");
        }

        try {
            return findUserByUid(uid);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User getUserByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email required");
        }
        try {
            return findUserByEmail(email);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<User> getUsers() {
        try {
            HashSet<User> users = new HashSet<User>();
            Iterator it = getSession().getNamedQuery("user.all").iterate();
            while (it.hasNext()) {
                users.add((User) it.next());
            }

            return users;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<User> findUsersByPreference(String key, String value) {
        try {
            Query hibQuery = getSession().getNamedQuery("users.byPreference");
            hibQuery.setParameter("key", key).setParameter("value", value);
            List<User> results = hibQuery.list();

            Set<User> users = new HashSet<User>();

            // TODO figure out how to load all properties using HQL
            for (User user : results) {
                Hibernate.initialize(user);
                users.add(user);
            }

            return users;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(String username) {
        try {
            User user = findUserByUsername(username);
            // delete user
            if (user != null) {
                removeUser(user);
            }
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeUser(User user) {
        try {
            getSession().delete(user);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public User updateUser(User user) {
        try {
            // prevent auto flushing when querying for existing users
            getSession().setFlushMode(FlushMode.MANUAL);

            User findUser = findUserByUsernameOrEmailIgnoreCaseAndId(getBaseModelObject(user)
                    .getId(), user.getUsername(), user.getEmail());

            if (findUser != null) {
                if (findUser.getEmail().equals(user.getEmail())) {
                    throw new DuplicateEmailException(user.getEmail());
                } else {
                    throw new DuplicateUsernameException(user.getUsername());
                }
            }

            user.updateTimestamp();
            getSession().update(user);
            getSession().flush();

            return user;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private User findUserByUsername(String username) {
        return (User) getSession().byNaturalId(HibUser.class).using("username", username).load();
    }

    private User findUserByUsernameIgnoreCase(String username) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byUsername.ignorecase").setParameter(
                "username", username);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByUsernameOrEmailIgnoreCaseAndId(Long userId,
                                                          String username, String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery(
                "user.byUsernameOrEmail.ignorecase.ingoreId").setParameter(
                "username", username).setParameter("email", email)
                .setParameter("userid", userId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        List users = hibQuery.list();
        if (users.size() > 0) {
            return (User) users.get(0);
        } else {
            return null;
        }
    }

    private User findUserByEmail(String email) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byEmail").setParameter(
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

    private User findUserByUid(String uid) {
        Session session = getSession();
        Query hibQuery = session.getNamedQuery("user.byUid").setParameter(
                "uid", uid);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);
        return (User) hibQuery.uniqueResult();
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }

}
