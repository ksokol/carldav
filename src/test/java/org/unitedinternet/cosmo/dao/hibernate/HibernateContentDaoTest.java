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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.UidInUseException;
import org.unitedinternet.cosmo.model.hibernate.*;

import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for HibernateContentDao
 *
 */
public class HibernateContentDaoTest extends IntegrationTestSupport {

    @Autowired
    private UserDaoImpl userDao;
    @Autowired
    private ContentDaoImpl contentDao;
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
     * Test for content dao create content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateContent() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibItem item = generateTestContent();
        item.setName("test");

        HibItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() != null);
        Assert.assertNotNull(newItem.getUid());

        HibItem queryItem = contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
    }

    /**
     * Test content dao create content duplicate uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateContentDuplicateUid() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibItem item1 = generateTestContent();
        item1.setName("test");
        item1.setUid("uid");

        contentDao.createContent(root, item1);

        HibItem item2 = generateTestContent();
        item2.setName("test2");
        item2.setUid("uid");

        try {
            contentDao.createContent(root, item2);

            Assert.fail("able to create duplicate uid");
        } catch (UidInUseException e) {
        }
    }

    /**
     * Test content dao create note duplicate Ical uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateNoteDuplicateUid() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibNoteItem note1 = generateTestNote("note1", "testuser");
        note1.setUid("icaluid");

        contentDao.createContent(root, note1);

        HibNoteItem note2 = generateTestNote("note2", "testuser");
        note2.setUid("icaluid");

        try {
            contentDao.createContent(root, note2);
            Assert.fail("able to create duplicate icaluid");
        } catch (IcalUidInUseException e) {}

    }

    /**
     * Test content dao invalid content empty name.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoInvalidContentEmptyName() throws Exception {

        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);
        HibItem item = generateTestContent();
        item.setName("");

        try {
            contentDao.createContent(root, item);
            Assert.fail("able to create invalid content.");
        } catch (ConstraintViolationException e) {
            // FIXME catched InvalidStateException and tested Assert.assertEquals
            //("name", e.getInvalidValues()[0].getPropertyName());
            // before migration to Hibernate 4, does any code depend on the old Exception?
            Assert.assertEquals("name", e.getConstraintViolations().iterator().next().getPropertyPath().toString());
       }
    }

    /**
     * Test create duplicate root item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCreateDuplicateRootItem() throws Exception {
        User testuser = getUser(userDao, "testuser");
        try {
            contentDao.createRootItem(testuser);
            Assert.fail("able to create duplicate root item");
        } catch (RuntimeException re) {
        }
    }

    /**
     * Test find item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testFindItem() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");

        HibCollectionItem root = (HibCollectionItem) contentDao
                .getRootItem(testuser2);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);



        HibItem queryHibItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryHibItem);
        Assert.assertTrue(queryHibItem instanceof HibCollectionItem);

        queryHibItem = contentDao.findItemByPath("/testuser2@testem/a");
        Assert.assertNotNull(queryHibItem);
        Assert.assertTrue(queryHibItem instanceof HibCollectionItem);

        HibItem item = generateTestContent();

        a = (HibCollectionItem) contentDao.findItemByUid(a.getUid());
        item = contentDao.createContent(a, item);



        queryHibItem = contentDao.findItemByPath("/testuser2@testem/a/test");
        Assert.assertNotNull(queryHibItem);
        Assert.assertTrue(queryHibItem instanceof HibItem);



        queryHibItem = contentDao.findItemParentByPath("/testuser2@testem/a/test");
        Assert.assertNotNull(queryHibItem);
        Assert.assertEquals(a.getUid(), queryHibItem.getUid());
    }

    /**
     * Test content dao update content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateContent() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibCardItem item = generateTestContent();

        HibItem newItem = contentDao.createContent(root, item);
        Date newItemModifyDate = newItem.getModifiedDate();



        HibCardItem queryItem = (HibCardItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);

        queryItem.setName("test2");
        queryItem.setDisplayName("this is a test item2");
        queryItem.setCalendar(helper.getString("testdata/testdata2.txt"));

        // Make sure modified date changes
        Thread.sleep(1000);

        queryItem = (HibCardItem) contentDao.updateContent(queryItem);


        Thread.sleep(200);
        HibItem queryItem2 = contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(queryItem, queryItem2);

        Assert.assertTrue(newItemModifyDate.before(
                queryItem2.getModifiedDate()));
    }

    /**
     * Test content dao delete content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoDeleteContent() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibItem item = generateTestContent();
        HibItem newItem = contentDao.createContent(root, item);

        HibItem queryItem = contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeContent(queryItem);

        queryItem = contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);

        root = contentDao.getRootItem(user);
        Assert.assertTrue(root.getItems().size() == 0);

    }

    /**
     * Test content dao delete user content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoDeleteUserContent() throws Exception {
        User user1 = getUser(userDao, "testuser1");
        User user2 = getUser(userDao, "testuser2");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user1);

        // Create test content, with owner of user2
        HibItem item = generateTestContent();
        item.setOwner(user2);

        // create content in user1's home collection
        contentDao.createContent(root, item);



        user1 = getUser(userDao, "testuser1");
        user2 = getUser(userDao, "testuser2");

        // remove user2's content, which should include the item created
        // in user1's home collections
        contentDao.removeUserContent(user2);

        root = (HibCollectionItem) contentDao.getRootItem(user1);
        Assert.assertEquals(0, root.getItems().size());
    }

    /**
     * Test delete content by path.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteContentByPath() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibItem item = generateTestContent();

        HibItem newItem = contentDao.createContent(root, item);

        HibItem queryItem = contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByPath("/testuser@testem/test");

        queryItem = contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Test delete content by uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteContentByUid() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibItem item = generateTestContent();
        HibItem newItem = contentDao.createContent(root, item);

        HibItem queryItem = contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByUid(queryItem.getUid());

        queryItem = contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Test tombstone delete content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testTombstoneDeleteContent() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibItem item = generateTestContent();
        HibItem newItem = contentDao.createContent(root, item);
        HibItem queryItem = contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeContent(queryItem);

        queryItem = contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(queryItem);

        root = contentDao.getRootItem(user);

        item = generateTestContent();
        item.setUid(newItem.getUid());

        contentDao.createContent(root, item);

        queryItem = contentDao.findItemByUid(newItem.getUid());

        Assert.assertNotNull(queryItem);
    }

    /**
     * Test content dao create collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        Assert.assertTrue(getHibItem(a).getId() != null);
        Assert.assertNotNull(a.getUid());



        HibCollectionItem queryItem = (HibCollectionItem) contentDao.findItemByUid(a.getUid());
        helper.verifyItem(a, queryItem);
    }

    /**
     * Test content dao update collection timestamp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateCollectionTimestamp() throws Exception {
        User user = getUser(userDao, "testuser2");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);
        Date timestamp = a.getModifiedDate();


        // FIXME this test is timing dependant!
        Thread.sleep(3);

        a = contentDao.updateCollectionTimestamp(a);
        Assert.assertTrue(timestamp.before(a.getModifiedDate()));
    }

    /**
     * Tests content dao delete collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoDeleteCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);



        HibCollectionItem queryItem = (HibCollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);

        contentDao.removeCollection(queryItem);



        queryItem = (HibCollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Tests content dao advanced.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoAdvanced() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        HibCollectionItem root = (HibCollectionItem) contentDao
                .getRootItem(testuser2);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        HibCollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setDisplayName("displayName");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        HibItem c = generateTestContent("c", "testuser2");

        c = contentDao.createContent(b, c);

        HibItem d = generateTestContent("d", "testuser2");

        d = contentDao.createContent(a, d);

        session.clear();

        a = (HibCollectionItem) contentDao.findItemByUid(a.getUid());
        b = (HibCollectionItem) contentDao.findItemByUid(b.getUid());
        c = contentDao.findItemByUid(c.getUid());
        d = contentDao.findItemByUid(d.getUid());
        root = contentDao.getRootItem(testuser2);

        Assert.assertNotNull(a);
        Assert.assertNotNull(b);
        Assert.assertNotNull(d);
        Assert.assertNotNull(root);

        // test children
        @SuppressWarnings("rawtypes")
        Collection children = a.getItems();
        Assert.assertEquals(2, children.size());
        verifyContains(children, b);
        verifyContains(children, d);

        children = root.getItems();
        Assert.assertEquals(1, children.size());
        verifyContains(children, a);

        // test get by path
        HibItem queryC = contentDao.findItemByPath("/testuser2@testem/a/b/c");
        Assert.assertNotNull(queryC);
        //helper.verifyInputStream(, ((HibFileItem) queryC).getContent());
        assertThat(helper.getString("testdata/testdata1.txt"), is(((HibCardItem) queryC).getCalendar()));

        Assert.assertEquals("c", queryC.getName());

        // test get path/uid abstract
        HibItem queryHibItem = contentDao.findItemByPath("/testuser2@testem/a/b/c");
        Assert.assertNotNull(queryHibItem);
        Assert.assertTrue(queryHibItem instanceof HibItem);

        queryHibItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryHibItem);
        Assert.assertTrue(queryHibItem instanceof HibCollectionItem);

        // test delete
        contentDao.removeContent(c);
        queryC = contentDao.findItemByUid(c.getUid());
        Assert.assertNull(queryC);

        contentDao.removeCollection(a);

        HibCollectionItem queryA = (HibCollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNull(queryA);

        HibItem queryD = contentDao.findItemByUid(d.getUid());
        Assert.assertNull(queryD);
    }


    /**
     * Tests content dao advanced.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteItemsFromCollection() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(testuser2);

        HibCollectionItem collection = new HibCollectionItem();
        collection.setName("collection");
        collection.setDisplayName("displayName");
        collection.setOwner(getUser(userDao, "testuser2"));

        collection = contentDao.createCollection(root, collection);

        HibItem item1 = generateTestContent("item1", "testuser2");

        item1 = contentDao.createContent(collection, item1);

        HibItem item2 = generateTestContent("item2", "testuser2");

        item2 = contentDao.createContent(collection, item2);

        session.clear();

        collection = (HibCollectionItem) contentDao.findItemByUid(collection.getUid());
        item1 = contentDao.findItemByUid(item1.getUid());
        item2 = contentDao.findItemByUid(item2.getUid());
        root = contentDao.getRootItem(testuser2);

        Assert.assertNotNull(collection);
        Assert.assertNotNull(item2);
        Assert.assertNotNull(root);

        // test delete

        contentDao.removeItemsFromCollection(collection);

        session.flush();

        HibCollectionItem queryA = (HibCollectionItem) contentDao.findItemByUid(collection.getUid());
        Assert.assertNotNull(queryA);

        HibItem queryD = contentDao.findItemByUid(item2.getUid());
        Assert.assertNull(queryD);
    }

    /**
     * Tests home collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testHomeCollection() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        HibHomeCollectionItem root = contentDao.getRootItem(testuser2);

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
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        HibItem item = generateTestContent();
        item.setName("test");

        HibItem newItem = contentDao.createContent(a, item);



        HibItem queryItem = contentDao.findItemByUid(newItem.getUid());
        Assert.assertNotNull(queryItem.getCollection());

        HibCollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setDisplayName("displayName");
        b.setOwner(user);

        b = contentDao.createCollection(root, b);

        HibItem item2 = generateTestContent();
        item2.setName("test");
        contentDao.createContent(b, item2);

        // should get DuplicateItemName here
        try {
            contentDao.addItemToCollection(queryItem, b);
            Assert.fail("able to add item with same name to collection");
        } catch (DuplicateItemNameException e) {
        }
    }

    private void verifyContains(@SuppressWarnings("rawtypes") Collection items, HibCollectionItem collection) {
        for (@SuppressWarnings("rawtypes")
        Iterator it = items.iterator(); it.hasNext();) {
            HibItem hibItem = (HibItem) it.next();
            if (hibItem instanceof HibCollectionItem
                    && hibItem.getName().equals(collection.getName()))
                return;
        }
        Assert.fail("collection not found");
    }

    private void verifyContains(@SuppressWarnings("rawtypes") Collection items, HibItem content) {
        for (@SuppressWarnings("rawtypes")
        Iterator it = items.iterator(); it.hasNext();) {
            HibItem hibItem = (HibItem) it.next();
            if (hibItem instanceof HibItem && hibItem.getName().equals(content.getName()))
                return;
        }
        Assert.fail("content not found");



    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
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

    private HibNoteItem generateTestNote(String name, String owner)
            throws Exception {
        HibNoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setUid(UUID.randomUUID().toString());
        content.setOwner(getUser(userDao, owner));
        return content;
    }

    private HibItem getHibItem(HibItem hibItem) {
        return hibItem;
    }

}
