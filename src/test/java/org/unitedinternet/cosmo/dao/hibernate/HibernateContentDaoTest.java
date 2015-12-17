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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Test for HibernateContentDao
 *
 */
public class HibernateContentDaoTest extends AbstractHibernateDaoTestCase {

    @Autowired
    private UserDaoImpl userDao;
    @Autowired
    private ContentDaoImpl contentDao;

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

        clearSession();

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

        clearSession();

        Assert.assertTrue(getHibItem(a).getId() > -1);
        Assert.assertNotNull(a.getUid());

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        helper.verifyItem(a, queryItem);

        queryItem.setName("b");
        contentDao.updateCollection(queryItem);

        clearSession();

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
        
        clearSession();
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

        clearSession();

        CollectionItem queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);

        contentDao.removeCollection(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(a.getUid());
        Assert.assertNull(queryItem);
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
        Assert.assertEquals(root.getName(), "testuser2");

    }

    /**
     * Tests content dao update collection2.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateCollection2() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("test1", "testuser");
        NoteItem note2 = generateTestNote("test2", "testuser");

        note1.setUid("1");
        note2.setUid("2");
        
        Set<ContentItem> items = new HashSet<ContentItem>();
        items.add(note1);
        items.add(note2);

        contentDao.updateCollection(root, items);

        items.clear();
        
        note1 = (NoteItem) contentDao.findItemByUid("1");
        note2 = (NoteItem) contentDao.findItemByUid("2");
        
        items.add(note1);
        items.add(note2);
        
        Assert.assertNotNull(note1);
        Assert.assertNotNull(note2);
        
        note1.setDisplayName("changed");
        note2.setIsActive(false);
       
        contentDao.updateCollection(root, items);
        
        note1 = (NoteItem) contentDao.findItemByUid("1");
        note2 = (NoteItem) contentDao.findItemByUid("2");
        
        Assert.assertNotNull(note1);
        Assert.assertEquals("changed", note1.getDisplayName());
        Assert.assertNull(note2);
    }
    
    /**
     * Tests content dao update collection with mods.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateCollectionWithMods() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("test1", "testuser");
        NoteItem note2 = generateTestNote("test2", "testuser");

        note1.setUid("1");
        note2.setUid("1:20070101");
        
        note2.setModifies(note1);
        
        Set<ContentItem> items = new LinkedHashSet<ContentItem>();
        items.add(note2);
        items.add(note1);

        
        // should fail because modification is processed before master
        try {
            contentDao.updateCollection(root, items);
            Assert.fail("able to create invalid mod");
        } catch (ModelValidationException e) {
        }
        
        items.clear();
        
        // now make sure master is processed before mod
        items.add(note1);
        items.add(note2);
       
        contentDao.updateCollection(root, items);
        
        note1 = (NoteItem) contentDao.findItemByUid("1");
        Assert.assertNotNull(note1);
        Assert.assertTrue(1==note1.getModifications().size());
        note2 = (NoteItem) contentDao.findItemByUid("1:20070101");
        Assert.assertNotNull(note2);
        Assert.assertNotNull(note2.getModifies());  
        
        // now create new collection
        CollectionItem a = new HibCollectionItem();
        a.setUid("a");
        a.setName("a");
        a.setOwner(user);
        
        a = contentDao.createCollection(root, a);
        
        // try to add mod to another collection before adding master
        items.clear();
        items.add(note2);
        
        // should fail because modification is added before master
        try {
            contentDao.updateCollection(a, items);
            Assert.fail("able to add mod before master");
        } catch (ModelValidationException e) {
        }
        
        items.clear();
        items.add(note1);
        items.add(note2);
        
        contentDao.updateCollection(a, items);
        
        // now create new collection
        CollectionItem b = new HibCollectionItem();
        b.setUid("b");
        b.setName("b");
        b.setOwner(user);
        
        b = contentDao.createCollection(root, b);
        
        // only add master
        items.clear();
        items.add(note1);
        
        contentDao.updateCollection(b, items);
        
        // adding master should add mods too
        clearSession();
        b = (CollectionItem) contentDao.findItemByUid("b");
        Assert.assertNotNull(b);
        Assert.assertEquals(2, b.getChildren().size());
    }
    
    /**
     * Tests content dao update collection with duplicate Ical Uids.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoUpdateCollectionWithDuplicateIcalUids() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem note1 = generateTestNote("test1", "testuser");
        NoteItem note2 = generateTestNote("test2", "testuser");

        note1.setUid("1");
        note1.setIcalUid("1");
        note2.setUid("2");
        note2.setIcalUid("1");
        
        Set<ContentItem> items = new HashSet<ContentItem>();
        items.add(note1);
        items.add(note2);

        try {
            contentDao.updateCollection(root, items);
            Assert.fail("able to create duplicate icaluids!");
        } catch (IcalUidInUseException e) {
        }
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
