/*
 * Copyright 2006 Open Source Applications Foundation
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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.*;

import static org.junit.Assert.assertEquals;

/**
 * Test for HibernateContentDao
 *
 */
public class HibernateContentDaoTest extends IntegrationTestSupport {

    @Autowired
    private UserDaoImpl userDao;
    @Autowired
    private ItemDaoImpl itemDao;
    @Autowired
    protected SessionFactory sessionFactory;

    private HibernateTestHelper helper = new HibernateTestHelper();

    private Session session;

    @BeforeTransaction
    public void onSetUpBeforeTransaction() throws Exception {
        // Unbind session from TransactionManager
        session = sessionFactory.openSession();
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
    }

    @AfterTransaction
    public void onTearDownAfterTransaction() throws Exception {
        SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        Session s = holder.getSession();
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.closeSession(s);
    }

    /**
     * Test create duplicate root item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCreateDuplicateRootItem() throws Exception {
        User testuser = getUser(userDao, "testuser");
        try {
            itemDao.createRootItem(testuser);
            Assert.fail("able to create duplicate root item");
        } catch (RuntimeException re) {
        }
    }

    /**
     * Test content dao create collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        HibCollectionItem root = itemDao.getRootItem(user);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setCollection(root);

        itemDao.save(a);

        Assert.assertTrue(getHibItem(a).getId() != null);
        Assert.assertNotNull(a.getUid());

        HibCollectionItem queryItem = (HibCollectionItem) itemDao.findItemByPath("/testuser2@testem/a");
        helper.verifyItem(a, queryItem);
    }

    /**
     * Tests content dao delete collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoDeleteCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        HibCollectionItem root = itemDao.getRootItem(user);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setCollection(root);

        itemDao.save(a);

        HibCollectionItem queryItem = (HibCollectionItem) itemDao.findItemByPath("/testuser2@testem/a");
        Assert.assertNotNull(queryItem);

        itemDao.removeItem(queryItem);

        queryItem = (HibCollectionItem) itemDao.findItemByPath("/testuser2@testem/a");
        Assert.assertNull(queryItem);
    }

    /**
     * Tests home collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testHomeCollection() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        HibHomeCollectionItem root = itemDao.getRootItem(testuser2);

        Assert.assertNotNull(root);
        root.setName("alsfjal;skfjasd");
        Assert.assertEquals(root.getName(), "testuser2@testem");

    }

    /**
     * Tests item in multiple collections error.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testItemInMutipleCollectionsError() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = itemDao.getRootItem(user);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setCollection(root);

        itemDao.save(a);

        HibItem item = generateTestContent();
        item.setName("test");
        item.setCollection(a);

        itemDao.save(item);

        HibCollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setDisplayName("bdisplayName");
        b.setOwner(user);
        b.setCollection(root);

        itemDao.save(b);

        HibItem item2 = generateTestContent();
        item2.setName("test");
        item2.setDisplayName("test");

        // should get DuplicateItemName here
        try {
            itemDao.save(item2);
            Assert.fail("able to add item with same name to collection");
        } catch (ConstraintViolationException e) {
            assertEquals("DISPLAYNAME_OWNER", e.getConstraintName());
        }
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, itemDao, username);
    }

    private HibCardItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    private HibCardItem generateTestContent(String name, String owner)
            throws Exception {
        HibCardItem content = new HibCardItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setCalendar(helper.getString("testdata/testdata1.txt"));
        content.setOwner(getUser(userDao, owner));
        return content;
    }

    private HibItem getHibItem(HibItem hibItem) {
        return hibItem;
    }

}
