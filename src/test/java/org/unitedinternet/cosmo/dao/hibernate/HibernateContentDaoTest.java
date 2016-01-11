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

import net.fortuna.ical4j.model.property.ProdId;
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
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.ICalendarAttribute;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.TimestampAttribute;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.TriageStatusUtil;
import org.unitedinternet.cosmo.model.UidInUseException;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibContentItem;
import org.unitedinternet.cosmo.model.hibernate.HibFileItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.model.hibernate.HibQName;
import org.unitedinternet.cosmo.model.hibernate.HibStringAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibTimestampAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.ConstraintViolationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
    }

    /**
     * Test content dao create content duplicate uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateContentDuplicateUid() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item1 = generateTestContent();
        item1.setName("test");
        item1.setUid("uid");

        contentDao.createContent(root, item1);
        
        ContentItem item2 = generateTestContent();
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
    public void testContentDaoCreateNoteDuplicateIcalUid() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("note1", "testuser");
        note1.setIcalUid("icaluid");

        contentDao.createContent(root, note1);
        
        NoteItem note2 = generateTestNote("note2", "testuser");
        note2.setIcalUid("icaluid");
         

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
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        ContentItem item = generateTestContent();
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
     * Test content attributes.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testContentAttributes() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        // TODO: figure out db date type is handled because i'm seeing
        // issues with accuracy
        // item.addAttribute(new DateAttribute("dateattribute", new Date()));

        HashSet<String> values = new HashSet<String>();
        values.add("value1");
        values.add("value2");

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute custom = queryItem.getAttribute("customattribute");
        Assert.assertEquals("customattributevalue", custom.getValue());

        helper.verifyItem(newItem, queryItem);

        // set attribute value to null
        custom.setValue(null);

        queryItem.removeAttribute("intattribute");

        contentDao.updateContent(queryItem);

        

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Attribute queryAttribute = queryItem.getAttribute("customattribute");

        Assert.assertNotNull(queryAttribute);
        Assert.assertNull(queryAttribute.getValue());
        Assert.assertNull(queryItem.getAttribute("intattribute"));
    }

    /**
     * Test timestamp attribute.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testTimestampAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        Date dateVal = new Date();
        TimestampAttribute tsAttr = 
            new HibTimestampAttribute(new HibQName("timestampattribute"), dateVal); 
        item.addAttribute(tsAttr);
        
        ContentItem newItem = contentDao.createContent(root, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("timestampattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof TimestampAttribute);
        
        Date val = (Date) attr.getValue();
        Assert.assertTrue(dateVal.equals(val));
        
        dateVal.setTime(dateVal.getTime() + 101);
        attr.setValue(dateVal);

        contentDao.updateContent(queryItem);

        

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Attribute queryAttr = queryItem.getAttribute(new HibQName("timestampattribute"));
        Assert.assertNotNull(queryAttr);
        Assert.assertTrue(queryAttr instanceof TimestampAttribute);
        
        val = (Date) queryAttr.getValue();
        Assert.assertTrue(dateVal.equals(val));
    }

    /**
     * Test ICalendar attribute.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testICalendarAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
       
        ICalendarAttribute icalAttr = new HibICalendarAttribute(); 
        icalAttr.setQName(new HibQName("icalattribute"));
        icalAttr.setValue(helper.getInputStream("testdata/vjournal.ics"));
        item.addAttribute(icalAttr);
        
        ContentItem newItem = contentDao.createContent(root, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("icalattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof ICalendarAttribute);
        
        net.fortuna.ical4j.model.Calendar calendar = (net.fortuna.ical4j.model.Calendar) attr.getValue();
        Assert.assertNotNull(calendar);
        
        net.fortuna.ical4j.model.Calendar expected = CalendarUtils.parseCalendar(helper.getInputStream("testdata/vjournal.ics"));
        
        Assert.assertEquals(expected.toString(), calendar.toString());
        
        calendar.getProperties().add(new ProdId("blah"));
        contentDao.updateContent(queryItem);
        
        
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        ICalendarAttribute ica = (ICalendarAttribute) queryItem.getAttribute(new HibQName("icalattribute"));
        Assert.assertEquals(calendar, ica.getValue());
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

        CollectionItem root = (CollectionItem) contentDao
                .getRootItem(testuser2);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        

        Item queryItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        queryItem = contentDao.findItemByPath("/testuser2@testem/a");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        ContentItem item = generateTestContent();
        
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        item = contentDao.createContent(a, item);

        

        queryItem = contentDao.findItemByPath("/testuser2@testem/a/test");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);

        

        queryItem = contentDao.findItemParentByPath("/testuser2@testem/a/test");
        Assert.assertNotNull(queryItem);
        Assert.assertEquals(a.getUid(), queryItem.getUid());
    }

    /**
     * Test content dao update content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateContent() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        FileItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);
        Date newItemModifyDate = newItem.getModifiedDate();
        
        

        HibFileItem queryItem = (HibFileItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
        Assert.assertEquals(0, queryItem.getVersion().intValue());

        queryItem.setName("test2");
        queryItem.setDisplayName("this is a test item2");
        queryItem.removeAttribute("customattribute");
        queryItem.setContentLanguage("es");
        queryItem.setContent(helper.getBytes("testdata/testdata2.txt"));

        // Make sure modified date changes
        Thread.sleep(1000);
        
        queryItem = (HibFileItem) contentDao.updateContent(queryItem);
        
        
        Thread.sleep(200);
        HibContentItem queryItem2 = (HibContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertTrue(queryItem2.getVersion().intValue() > 0);
        
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
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);
        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeContent(queryItem);

        

        queryItem = (ContentItem) contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
        
        
        
        root = (CollectionItem) contentDao.getRootItem(user);
        Assert.assertTrue(root.getChildren().size()==0);
        
    }
    
    /**
     * Test content dao delete user content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoDeleteUserContent() throws Exception {
        User user1 = getUser(userDao, "testuser1");
        User user2 = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user1);

        // Create test content, with owner of user2
        ContentItem item = generateTestContent();
        item.setOwner(user2);
        
        // create content in user1's home collection
        contentDao.createContent(root, item);

        

        user1 = getUser(userDao, "testuser1");
        user2 = getUser(userDao, "testuser2");
       
        // remove user2's content, which should include the item created
        // in user1's home collections
        contentDao.removeUserContent(user2);
        
        root = (CollectionItem) contentDao.getRootItem(user1);
        Assert.assertEquals(0, root.getChildren().size());
    }

    /**
     * Test delete content by path.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteContentByPath() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByPath("/testuser@testem/test");

        

        queryItem = (ContentItem) contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Test delete content by uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteContentByUid() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByUid(queryItem.getUid());

        

        queryItem = (ContentItem) contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Test tombstone delete content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testTombstoneDeleteContent() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ContentItem newItem = contentDao.createContent(root, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);
        
        Assert.assertTrue(((HibItem) queryItem).getVersion().equals(0));

        contentDao.removeContent(queryItem);

        

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(queryItem);
        
        root = (CollectionItem) contentDao.getRootItem(user);

        item = generateTestContent();
        item.setUid(newItem.getUid());
        
        contentDao.createContent(root, item);

        
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        
        Assert.assertNotNull(queryItem);
    }

    /**
     * Test content dao create collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        Assert.assertTrue(getHibItem(a).getId() > -1);
        Assert.assertNotNull(a.getUid());

        

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        helper.verifyItem(a, queryItem);
    }
    
    /**
     * Test content dao update collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        

        Assert.assertTrue(getHibItem(a).getId() > -1);
        Assert.assertNotNull(a.getUid());

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        helper.verifyItem(a, queryItem);

        queryItem.setName("b");
        contentDao.updateCollection(queryItem);

        

        queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertEquals("b", queryItem.getName());
    }
    
    /**
     * Test content dao update collection timestamp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateCollectionTimestamp() throws Exception {
        User user = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);
        Integer ver = ((HibItem) a).getVersion();
        Date timestamp = a.getModifiedDate();
        
        
        // FIXME this test is timing dependant!
        Thread.sleep(3);
        
        a = contentDao.updateCollectionTimestamp(a);
        Assert.assertTrue(((HibItem) a).getVersion()==ver + 1);
        Assert.assertTrue(timestamp.before(a.getModifiedDate()));
    }

    /**
     * Tests content dao delete collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoDeleteCollection() throws Exception {
        User user = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);

        

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);

        contentDao.removeCollection(queryItem);

        

        queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Tests content dao advanced.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoAdvanced() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao
                .getRootItem(testuser2);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(getUser(userDao, "testuser2"));

        a = contentDao.createCollection(root, a);

        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(getUser(userDao, "testuser2"));

        b = contentDao.createCollection(a, b);

        ContentItem c = generateTestContent("c", "testuser2");

        c = contentDao.createContent(b, c);

        ContentItem d = generateTestContent("d", "testuser2");

        d = contentDao.createContent(a, d);

        session.clear();

        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        c = (ContentItem) contentDao.findItemByUid(c.getUid());
        d = (ContentItem) contentDao.findItemByUid(d.getUid());
        root = contentDao.getRootItem(testuser2);

        Assert.assertNotNull(a);
        Assert.assertNotNull(b);
        Assert.assertNotNull(d);
        Assert.assertNotNull(root);

        // test children
        @SuppressWarnings("rawtypes")
        Collection children = a.getChildren();
        Assert.assertEquals(2, children.size());
        verifyContains(children, b);
        verifyContains(children, d);

        children = root.getChildren();
        Assert.assertEquals(1, children.size());
        verifyContains(children, a);

        // test get by path
        ContentItem queryC = (ContentItem) contentDao.findItemByPath("/testuser2@testem/a/b/c");
        Assert.assertNotNull(queryC);
        helper.verifyInputStream(
                helper.getInputStream("testdata/testdata1.txt"), ((FileItem) queryC)
                        .getContent());
        Assert.assertEquals("c", queryC.getName());

        // test get path/uid abstract
        Item queryItem = contentDao.findItemByPath("/testuser2@testem/a/b/c");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);

        queryItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        // test delete
        contentDao.removeContent(c);
        queryC = (ContentItem) contentDao.findItemByUid(c.getUid());
        Assert.assertNull(queryC);

        contentDao.removeCollection(a);

        CollectionItem queryA = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNull(queryA);

        ContentItem queryD = (ContentItem) contentDao.findItemByUid(d.getUid());
        Assert.assertNull(queryD);
    }
    
    
    /**
     * Tests content dao advanced.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDeleteItemsFromCollection() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(testuser2);

        CollectionItem collection = new HibCollectionItem();
        collection.setName("collection");
        collection.setOwner(getUser(userDao, "testuser2"));

        collection = contentDao.createCollection(root, collection);

        ContentItem item1 = generateTestContent("item1", "testuser2");

        item1 = contentDao.createContent(collection, item1);

        ContentItem item2 = generateTestContent("item2", "testuser2");

        item2 = contentDao.createContent(collection, item2);

        session.clear();

        collection = (CollectionItem) contentDao.findItemByUid(collection.getUid());
        item1 = (ContentItem) contentDao.findItemByUid(item1.getUid());
        item2 = (ContentItem) contentDao.findItemByUid(item2.getUid());
        root = contentDao.getRootItem(testuser2);

        Assert.assertNotNull(collection);
        Assert.assertNotNull(item2);
        Assert.assertNotNull(root);

        // test delete

        contentDao.removeItemsFromCollection(collection);

        CollectionItem queryA = (CollectionItem) contentDao.findItemByUid(collection.getUid());
        Assert.assertNotNull(queryA);

        ContentItem queryD = (ContentItem) contentDao.findItemByUid(item2.getUid());
        Assert.assertNull(queryD);
    }

    /**
     * Tests home collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testHomeCollection() throws Exception {
        User testuser2 = getUser(userDao, "testuser2");
        HomeCollectionItem root = contentDao.getRootItem(testuser2);

        Assert.assertNotNull(root);
        root.setName("alsfjal;skfjasd");
        Assert.assertEquals(root.getName(), "testuser2@testem");

    }

    /**
     * Tests item in multiple collections.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testItemInMutipleCollections() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);
        
        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(a, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);
        
        b = contentDao.createCollection(root, b);
        
        contentDao.addItemToCollection(queryItem, b);
        
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 2);
        
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        contentDao.removeItemFromCollection(queryItem, b);
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        contentDao.removeItemFromCollection(queryItem, a);
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(queryItem);
    }
    
    /**
     * Tests item in multiple collections error.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testItemInMutipleCollectionsError() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);
        
        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(a, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);
        
        b = contentDao.createCollection(root, b);
        
        ContentItem item2 = generateTestContent();
        item2.setName("test");
        contentDao.createContent(b, item2);
        
        // should get DuplicateItemName here
        try {
            contentDao.addItemToCollection(queryItem, b);
            Assert.fail("able to add item with same name to collection");
        } catch (DuplicateItemNameException e) {
        }
    }
    
    /**
     * Tests item in multiple collections delete collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testItemInMutipleCollectionsDeleteCollection() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setOwner(user);

        a = contentDao.createCollection(root, a);
        
        ContentItem item = generateTestContent();
        item.setName("test");

        ContentItem newItem = contentDao.createContent(a, item);

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);
        
        b = contentDao.createCollection(root, b);
        
        contentDao.addItemToCollection(queryItem, b);
        
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 2);
        
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        contentDao.removeCollection(b);
        
        
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(b);
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        contentDao.removeCollection(a);
        
        
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(a);
        Assert.assertNull(queryItem);
    }
    
    /**
     * Tests content dao.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoTriageStatus() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        item.setName("test");
        TriageStatus initialTriageStatus = new TriageStatus();
        TriageStatusUtil.initialize(initialTriageStatus);
        item.setTriageStatus(initialTriageStatus);

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        TriageStatus triageStatus = queryItem.getTriageStatus();
        Assert.assertEquals(initialTriageStatus, triageStatus);

        triageStatus.setCode(TriageStatusUtil.CODE_LATER);
        BigDecimal rank = new BigDecimal("-98765.43");
        triageStatus.setRank(rank);
        
        contentDao.updateContent(queryItem);
        
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        triageStatus = queryItem.getTriageStatus();
        Assert.assertEquals(triageStatus.getCode(),
                            Integer.valueOf(TriageStatusUtil.CODE_LATER));
        Assert.assertEquals(triageStatus.getRank(), rank);
        
        queryItem.setTriageStatus(null);
        contentDao.updateContent(queryItem);
        
        // should be null triagestatus
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        triageStatus = queryItem.getTriageStatus();
        Assert.assertNull(triageStatus);
    }

    private void verifyContains(@SuppressWarnings("rawtypes") Collection items, CollectionItem collection) {
        for (@SuppressWarnings("rawtypes")
        Iterator it = items.iterator(); it.hasNext();) {
            Item item = (Item) it.next();
            if (item instanceof CollectionItem
                    && item.getName().equals(collection.getName()))
                return;
        }
        Assert.fail("collection not found");
    }

    private void verifyContains(@SuppressWarnings("rawtypes") Collection items, ContentItem content) {
        for (@SuppressWarnings("rawtypes")
        Iterator it = items.iterator(); it.hasNext();) {
            Item item = (Item) it.next();
            if (item instanceof ContentItem
                    && item.getName().equals(content.getName()))
                return;
        }
        Assert.fail("content not found");
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private FileItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    private FileItem generateTestContent(String name, String owner)
            throws Exception {
        FileItem content = new HibFileItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setContent(helper.getBytes("testdata/testdata1.txt"));
        content.setContentLanguage("en");
        content.setContentEncoding("UTF8");
        content.setContentType("text/text");
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new HibStringAttribute(new HibQName("customattribute"),
                "customattributevalue"));
        return content;
    }
    
    private NoteItem generateTestNote(String name, String owner)
            throws Exception {
        NoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        return content;
    }
    
    private org.w3c.dom.Element createTestElement() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement( "root" );
        doc.appendChild(root);
        
        Element author1 = doc.createElement("author");
        author1.setAttribute("name", "James");
        author1.setAttribute("location", "UK");
        author1.setTextContent("James Strachan");
        
        root.appendChild(author1);
        
        Element author2 = doc.createElement("author");
        author2.setAttribute("name", "Bob");
        author2.setAttribute("location", "US");
        author2.setTextContent("Bob McWhirter");

        root.appendChild(author2);
        
        return root;
    }
    
    private HibItem getHibItem(Item item) {
        return (HibItem) item;
    }

}
