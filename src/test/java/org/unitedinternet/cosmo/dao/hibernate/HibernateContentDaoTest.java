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

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.property.ProdId;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.ItemNotFoundException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.AvailabilityItem;
import org.unitedinternet.cosmo.model.BooleanAttribute;
import org.unitedinternet.cosmo.model.CalendarAttribute;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.ICalendarAttribute;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.ItemTombstone;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.TimestampAttribute;
import org.unitedinternet.cosmo.model.Tombstone;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.TriageStatusUtil;
import org.unitedinternet.cosmo.model.UidInUseException;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.XmlAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibAvailabilityItem;
import org.unitedinternet.cosmo.model.hibernate.HibBooleanAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibContentItem;
import org.unitedinternet.cosmo.model.hibernate.HibFileItem;
import org.unitedinternet.cosmo.model.hibernate.HibFreeBusyItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.model.hibernate.HibQName;
import org.unitedinternet.cosmo.model.hibernate.HibStringAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibTimestampAttribute;
import org.unitedinternet.cosmo.model.hibernate.HibTriageStatus;
import org.unitedinternet.cosmo.model.hibernate.HibXmlAttribute;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.Calendar;
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
public class HibernateContentDaoTest extends AbstractHibernateDaoTestCase {

    @Autowired
    private UserDaoImpl userDao;
    @Autowired
    private ContentDaoImpl contentDao;

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

        clearSession();

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
            clearSession();
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
        BooleanAttribute ba = new HibBooleanAttribute(new HibQName("booleanattribute"), Boolean.TRUE);
        item.addAttribute(ba);

        // TODO: figure out db date type is handled because i'm seeing
        // issues with accuracy
        // item.addAttribute(new DateAttribute("dateattribute", new Date()));

        HashSet<String> values = new HashSet<String>();
        values.add("value1");
        values.add("value2");

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute custom = queryItem.getAttribute("customattribute");
        Assert.assertEquals("customattributevalue", custom.getValue());

        helper.verifyItem(newItem, queryItem);

        // set attribute value to null
        custom.setValue(null);

        queryItem.removeAttribute("intattribute");

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Attribute queryAttribute = queryItem.getAttribute("customattribute");

        Assert.assertNotNull(queryAttribute);
        Assert.assertNull(queryAttribute.getValue());
        Assert.assertNull(queryItem.getAttribute("intattribute"));
    }
    
    /**
     * Test calendar attribute.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCalendarAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        
        CalendarAttribute calAttr = 
            new HibCalendarAttribute(new HibQName("calendarattribute"), "2002-10-10T00:00:00+05:00"); 
        item.addAttribute(calAttr);
        
        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("calendarattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof CalendarAttribute);
        
        Calendar cal = (Calendar) attr.getValue();
        Assert.assertEquals("GMT+05:00", cal.getTimeZone().getID());
        Assert.assertEquals(calAttr.getValue(), cal);
        
        attr.setValue("2003-10-10T00:00:00+02:00");

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Attribute queryAttr = queryItem.getAttribute(new HibQName("calendarattribute"));
        Assert.assertNotNull(queryAttr);
        Assert.assertTrue(queryAttr instanceof CalendarAttribute);
        
        cal = (Calendar) queryAttr.getValue();
        Assert.assertEquals("GMT+02:00", cal.getTimeZone().getID());
        Assert.assertEquals(attr.getValue(), cal);
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

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("timestampattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof TimestampAttribute);
        
        Date val = (Date) attr.getValue();
        Assert.assertTrue(dateVal.equals(val));
        
        dateVal.setTime(dateVal.getTime() + 101);
        attr.setValue(dateVal);

        contentDao.updateContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Attribute queryAttr = queryItem.getAttribute(new HibQName("timestampattribute"));
        Assert.assertNotNull(queryAttr);
        Assert.assertTrue(queryAttr instanceof TimestampAttribute);
        
        val = (Date) queryAttr.getValue();
        Assert.assertTrue(dateVal.equals(val));
    }
    
    /**
     * Test xml attribute.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testXmlAttribute() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();
        
        org.w3c.dom.Element testElement = createTestElement();
        org.w3c.dom.Element testElement2 = createTestElement();
        
        testElement2.setAttribute("foo", "bar");
        
        Assert.assertFalse(testElement.isEqualNode(testElement2));
        
        XmlAttribute xmlAttr = 
            new HibXmlAttribute(new HibQName("xmlattribute"), testElement ); 
        item.addAttribute(xmlAttr);
        
        ContentItem newItem = contentDao.createContent(root, item);

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("xmlattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof XmlAttribute);
        
        org.w3c.dom.Element element = (org.w3c.dom.Element) attr.getValue();
        Assert.assertNotNull(element);
        Assert.assertEquals(DomWriter.write(testElement),DomWriter.write(element));

        Date modifyDate = attr.getModifiedDate();
        
        // Sleep a couple millis to make sure modifyDate doesn't change
        Thread.sleep(2);
        
        contentDao.updateContent(queryItem);

        clearSession();
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        attr = queryItem.getAttribute(new HibQName("xmlattribute"));
        
        // Attribute shouldn't have been updated
        Assert.assertEquals(modifyDate, attr.getModifiedDate());
        
        attr.setValue(testElement2);

        // Sleep a couple millis to make sure modifyDate doesn't change
        Thread.sleep(2);
        modifyDate = attr.getModifiedDate();
        
        contentDao.updateContent(queryItem);

        clearSession();
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        attr = queryItem.getAttribute(new HibQName("xmlattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof XmlAttribute);
        // Attribute should have been updated
        Assert.assertTrue(modifyDate.before(attr.getModifiedDate()));
        
        element = (org.w3c.dom.Element) attr.getValue();
        
        Assert.assertEquals(DomWriter.write(testElement2),DomWriter.write(element));
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

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        Attribute attr = queryItem.getAttribute(new HibQName("icalattribute"));
        Assert.assertNotNull(attr);
        Assert.assertTrue(attr instanceof ICalendarAttribute);
        
        net.fortuna.ical4j.model.Calendar calendar = (net.fortuna.ical4j.model.Calendar) attr.getValue();
        Assert.assertNotNull(calendar);
        
        net.fortuna.ical4j.model.Calendar expected = CalendarUtils.parseCalendar(helper.getInputStream("testdata/vjournal.ics"));
        
        Assert.assertEquals(expected.toString(),calendar.toString());
        
        calendar.getProperties().add(new ProdId("blah"));
        contentDao.updateContent(queryItem);
        
        clearSession();
        
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

        clearSession();

        Item queryItem = contentDao.findItemByUid(a.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        queryItem = contentDao.findItemByPath("/testuser2/a");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof CollectionItem);

        ContentItem item = generateTestContent();
        
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        item = contentDao.createContent(a, item);

        clearSession();

        queryItem = contentDao.findItemByPath("/testuser2/a/test");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);

        clearSession();

        queryItem = contentDao.findItemParentByPath("/testuser2/a/test");
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
        
        clearSession();

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
        
        clearSession();
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
        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(queryItem.getUid());
        Assert.assertNull(queryItem);
        
        clearSession();
        
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

        clearSession();

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

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByPath("/testuser/test");

        clearSession();

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

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);

        contentDao.removeItemByUid(queryItem.getUid());

        clearSession();

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

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        helper.verifyItem(newItem, queryItem);
        
        Assert.assertTrue(((HibItem)queryItem).getVersion().equals(0));

        contentDao.removeContent(queryItem);

        clearSession();

        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(queryItem);
        
        root = (CollectionItem) contentDao.getRootItem(user);
        Assert.assertEquals(root.getTombstones().size(), 1);
        
        Tombstone ts = root.getTombstones().iterator().next();
        
        Assert.assertTrue(ts instanceof ItemTombstone);
        Assert.assertEquals(((ItemTombstone) ts).getItemUid(), newItem.getUid());
        
        item = generateTestContent();
        item.setUid(newItem.getUid());
        
        contentDao.createContent(root, item);

        clearSession();
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        
        Assert.assertNotNull(queryItem);
        
        root = (CollectionItem) contentDao.getRootItem(user);
        Assert.assertEquals(root.getTombstones().size(), 0);
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

        clearSession();

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
        ContentItem queryC = (ContentItem) contentDao.findItemByPath("/testuser2/a/b/c");
        Assert.assertNotNull(queryC);
        helper.verifyInputStream(
                helper.getInputStream("testdata/testdata1.txt"), ((FileItem) queryC)
                        .getContent());
        Assert.assertEquals("c", queryC.getName());

        // test get path/uid abstract
        Item queryItem = contentDao.findItemByPath("/testuser2/a/b/c");
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

        clearSession();

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
        Assert.assertEquals(root.getName(), "testuser2");

    }

    /**
     * Tests item dao move.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testItemDaoMove() throws Exception {
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

        CollectionItem c = new HibCollectionItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));

        c = contentDao.createCollection(b, c);

        ContentItem d = generateTestContent("d", "testuser2");

        d = contentDao.createContent(c, d);

        CollectionItem e = new HibCollectionItem();
        e.setName("e");
        e.setOwner(getUser(userDao, "testuser2"));

        e = contentDao.createCollection(a, e);

        clearSession();

        root = (CollectionItem) contentDao.getRootItem(testuser2);
        e = (CollectionItem) contentDao.findItemByUid(e.getUid());
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());

        // verify can't move root collection
        try {
            contentDao.moveItem("/testuser2", "/testuser2/a/blah");
            Assert.fail("able to move root collection");
        } catch (IllegalArgumentException iae) {
        }

        // verify can't move to root collection
        try {
            contentDao.moveItem("/testuser2/a/e", "/testuser2");
            Assert.fail("able to move to root collection");
        } catch (ItemNotFoundException infe) {
        }

        // verify can't create loop
        try {
            contentDao.moveItem("/testuser2/a/b", "/testuser2/a/b/c/new");
            Assert.fail("able to create loop");
        } catch (ModelValidationException iae) {
        }

        clearSession();

        // verify that move works
        b = (CollectionItem) contentDao.findItemByPath("/testuser2/a/b");

        contentDao.moveItem("/testuser2/a/b", "/testuser2/a/e/b");

        clearSession();

        CollectionItem queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/b");
        Assert.assertNotNull(queryCollection);

        contentDao.moveItem("/testuser2/a/e/b", "/testuser2/a/e/bnew");

        clearSession();
        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bnew");
        Assert.assertNotNull(queryCollection);

        Item queryItem = contentDao.findItemByPath("/testuser2/a/e/bnew/c/d");
        Assert.assertNotNull(queryItem);
        Assert.assertTrue(queryItem instanceof ContentItem);
    }

    /**
     * Tests item dao copy.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testItemDaoCopy() throws Exception {
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

        CollectionItem c = new HibCollectionItem();
        c.setName("c");
        c.setOwner(getUser(userDao, "testuser2"));

        c = contentDao.createCollection(b, c);

        ContentItem d = generateTestContent("d", "testuser2");

        d = contentDao.createContent(c, d);

        CollectionItem e = new HibCollectionItem();
        e.setName("e");
        e.setOwner(getUser(userDao, "testuser2"));

        e = contentDao.createCollection(a, e);

        clearSession();

        root = (CollectionItem) contentDao.getRootItem(testuser2);
        e = (CollectionItem) contentDao.findItemByUid(e.getUid());
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());

        // verify can't copy root collection
        try {
            contentDao.copyItem(root, "/testuser2/a/blah", true);
            Assert.fail("able to copy root collection");
        } catch (IllegalArgumentException iae) {
        }

        // verify can't move to root collection
        try {
            contentDao.copyItem(e, "/testuser2", true);
            Assert.fail("able to move to root collection");
        } catch (ItemNotFoundException infe) {
        }

        // verify can't create loop
        try {
            contentDao.copyItem(b, "/testuser2/a/b/c/new", true);
            Assert.fail("able to create loop");
        } catch (ModelValidationException iae) {
        }

        clearSession();

        // verify that copy works
        b = (CollectionItem) contentDao.findItemByPath("/testuser2/a/b");

        contentDao.copyItem(b, "/testuser2/a/e/bcopy", true);

        clearSession();

        CollectionItem queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopy");
        Assert.assertNotNull(queryCollection);

        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopy/c");
        Assert.assertNotNull(queryCollection);

        d = (ContentItem) contentDao.findItemByUid(d.getUid());
        ContentItem dcopy = (ContentItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopy/c/d");
        Assert.assertNotNull(dcopy);
        Assert.assertEquals(d.getName(), dcopy.getName());
        Assert.assertNotSame(d.getUid(), dcopy.getUid());
        helper.verifyBytes(((FileItem) d).getContent(), ((FileItem) dcopy).getContent());

        clearSession();

        b = (CollectionItem) contentDao.findItemByPath("/testuser2/a/b");

        contentDao.copyItem(b,"/testuser2/a/e/bcopyshallow", false);

        clearSession();

        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopyshallow");
        Assert.assertNotNull(queryCollection);

        queryCollection = (CollectionItem) contentDao
                .findItemByPath("/testuser2/a/e/bcopyshallow/c");
        Assert.assertNull(queryCollection);

        clearSession();
        d = (ContentItem) contentDao.findItemByUid(d.getUid());
        contentDao.copyItem(d,"/testuser2/dcopy", true);

        clearSession();

        dcopy = (ContentItem) contentDao.findItemByPath("/testuser2/dcopy");
        Assert.assertNotNull(dcopy);
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

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);
        
        b = contentDao.createCollection(root, b);
        
        contentDao.addItemToCollection(queryItem, b);
        
        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 2);
        
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        contentDao.removeItemFromCollection(queryItem, b);
        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        contentDao.removeItemFromCollection(queryItem, a);
        clearSession();
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

        clearSession();

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

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        CollectionItem b = new HibCollectionItem();
        b.setName("b");
        b.setOwner(user);
        
        b = contentDao.createCollection(root, b);
        
        contentDao.addItemToCollection(queryItem, b);
        
        clearSession();
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(queryItem.getParents().size(), 2);
        
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        contentDao.removeCollection(b);
        
        clearSession();
        b = (CollectionItem) contentDao.findItemByUid(b.getUid());
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNull(b);
        Assert.assertEquals(queryItem.getParents().size(), 1);
        
        a = (CollectionItem) contentDao.findItemByUid(a.getUid());
        contentDao.removeCollection(a);
        clearSession();
        
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
        TriageStatus initialTriageStatus = new HibTriageStatus();
        TriageStatusUtil.initialize(initialTriageStatus);
        item.setTriageStatus(initialTriageStatus);

        ContentItem newItem = contentDao.createContent(root, item);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        TriageStatus triageStatus = queryItem.getTriageStatus();
        Assert.assertEquals(initialTriageStatus, triageStatus);

        triageStatus.setCode(TriageStatus.CODE_LATER);
        triageStatus.setAutoTriage(false);
        BigDecimal rank = new BigDecimal("-98765.43");
        triageStatus.setRank(rank);
        
        contentDao.updateContent(queryItem);
        clearSession();
        
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        triageStatus = queryItem.getTriageStatus();
        Assert.assertEquals(triageStatus.getAutoTriage(), Boolean.FALSE);
        Assert.assertEquals(triageStatus.getCode(),
                            Integer.valueOf(TriageStatus.CODE_LATER));
        Assert.assertEquals(triageStatus.getRank(), rank);
        
        queryItem.setTriageStatus(null);
        contentDao.updateContent(queryItem);
        clearSession();
        // should be null triagestatus
        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        triageStatus = queryItem.getTriageStatus();
        Assert.assertNull(triageStatus);
    }
    
    /**
     * Tests content dao create freeBusy.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateFreeBusy() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        FreeBusyItem newItem = new HibFreeBusyItem();
        newItem.setOwner(user);
        newItem.setName("test");
        newItem.setIcalUid("icaluid");
        
        CalendarBuilder cb = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = cb.build(helper.getInputStream("testdata/vfreebusy.ics"));
        
        newItem.setFreeBusyCalendar(calendar);
        
        newItem = (FreeBusyItem) contentDao.createContent(root, newItem);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
    }
    
    /**
     * Tests content dao create availability.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentDaoCreateAvailability() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        AvailabilityItem newItem = new HibAvailabilityItem();
        newItem.setOwner(user);
        newItem.setName("test");
        newItem.setIcalUid("icaluid");
        
        CalendarBuilder cb = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar calendar = cb.build(helper.getInputStream("testdata/vavailability.ics"));
        
        newItem.setAvailabilityCalendar(calendar);
        
        newItem = (AvailabilityItem) contentDao.createContent(root, newItem);

        Assert.assertTrue(getHibItem(newItem).getId() > -1);
        Assert.assertNotNull(newItem.getUid());

        clearSession();

        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        helper.verifyItem(newItem, queryItem);
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
