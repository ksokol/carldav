/*
 * AbstractDaoImpl.java Feb 16, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Abstract Dao implementation.
 * It is used for general purpose operations.
 *
 * @author ccoman
 */
public abstract class AbstractDaoImpl {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @return Returns the current Hibernate session.
     */
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
