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

import net.fortuna.ical4j.model.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibContentItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventExceptionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import javax.validation.ConstraintViolationException;

/**
 * Test for hibernate content dao stamping.
 * @author ccoman
 *
 */
public class HibernateContentDaoStampingTest extends IntegrationTestSupport {

    private static final Log LOG = LogFactory.getLog(HibernateContentDaoStampingTest.class);

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
     * Test stamps create.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testStampsCreate() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibNoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        HibEventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(event);

        HibContentItem newItem = contentDao.createContent(root, item);

        HibContentItem queryItem = (HibContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());

        HibEventStamp stamp = (HibEventStamp) queryItem.getStamp(HibEventStamp.class);
        Assert.assertNotNull(stamp.getCreationDate());
        Assert.assertNotNull(stamp.getModifiedDate());
        Assert.assertTrue(stamp.getCreationDate().equals(stamp.getModifiedDate()));
        Assert.assertTrue(stamp instanceof HibEventStamp);
        Assert.assertEquals(stamp.getEventCalendar().toString(), event.getEventCalendar().toString());
        Assert.assertEquals("icaluid", ((HibNoteItem) queryItem).getIcalUid());
        Assert.assertEquals("this is a body", ((HibNoteItem) queryItem).getBody());
    }

    /**
     * Test stamp handlers.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testStampHandlers() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibNoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        HibEventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(event);

        Assert.assertNull(event.getTimeRangeIndex());

        HibContentItem newItem = contentDao.createContent(root, item);


        HibContentItem queryItem = (HibContentItem) contentDao.findItemByUid(newItem.getUid());

        event = (HibEventStamp) queryItem.getStamp(HibEventStamp.class);
        Assert.assertEquals("20050817T115000Z", event.getTimeRangeIndex().getStartDate());
        Assert.assertEquals("20050817T131500Z",event.getTimeRangeIndex().getEndDate());
        Assert.assertFalse(event.getTimeRangeIndex().getIsFloating().booleanValue());

        event.setStartDate(new Date("20070101"));
        //event.setEntityTag("foo"); // FIXME setStartDate does not modify any persistent field, so object is not marked dirty
        event.setEndDate(null);

        contentDao.updateContent(queryItem);


        queryItem = (HibContentItem) contentDao.findItemByUid(newItem.getUid());

        event = (HibEventStamp) queryItem.getStamp(HibEventStamp.class);
        Assert.assertEquals("20070101", event.getTimeRangeIndex().getStartDate());
        Assert.assertEquals("20070101",event.getTimeRangeIndex().getEndDate());
        Assert.assertTrue(event.getTimeRangeIndex().getIsFloating().booleanValue());
    }

    /**
     * Test event stamp validation.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibContentItem item = generateTestContent();

        HibEventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/noevent.ics"));
        item.addStamp(event);

        try {
            contentDao.createContent(root, item);

            Assert.fail("able to create invalid event!");
        } catch (IllegalStateException is) {}
    }

    /**
     * Test for removing stamp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testRemoveStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        HibNoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        HibEventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(event);

        HibContentItem newItem = contentDao.createContent(root, item);


        HibContentItem queryItem = (HibContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());

        HibEventStamp stamp = (HibEventStamp) queryItem.getStamp(HibEventStamp.class);
        queryItem.removeStamp(stamp);
        contentDao.updateContent(queryItem);


        queryItem = (HibContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertEquals(queryItem.getStamps().size(),0);

        event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));
        queryItem.addStamp(event);

        contentDao.updateContent(queryItem);


        queryItem = (HibContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
    }

    public void shouldAllowLegalDisplayNames() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = contentDao.getRootItem(user);

        try{
            contentDao.updateCollection(root);
        }catch(ConstraintViolationException ex){
            Assert.fail("Valid display name was used");
        }

    }
    /**
     * Test event exception stamp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventExceptionStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibNoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        HibEventExceptionStamp eventex = new HibEventExceptionStamp();
        eventex.setEventCalendar(helper.getCalendar("testdata/exception.ics"));

        item.addStamp(eventex);

        HibContentItem newItem = contentDao.createContent(root, item);


        HibContentItem queryItem = (HibContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());

        HibEventExceptionStamp stamp = (HibEventExceptionStamp) queryItem.getStamp(HibEventExceptionStamp.class);
        Assert.assertNotNull(stamp.getCreationDate());
        Assert.assertNotNull(stamp.getModifiedDate());
        Assert.assertTrue(stamp.getCreationDate().equals(stamp.getModifiedDate()));
        Assert.assertTrue(stamp instanceof HibEventExceptionStamp);
        HibEventExceptionStamp ees = (HibEventExceptionStamp) stamp;
        Assert.assertEquals(ees.getEventCalendar().toString(), eventex.getEventCalendar()
                .toString());
    }

    /**
     * Test event exception stamp validation.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventExceptionStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        HibCollectionItem root = (HibCollectionItem) contentDao.getRootItem(user);

        HibNoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        HibEventExceptionStamp eventex = new HibEventExceptionStamp();
        eventex.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(eventex);

        try {
            contentDao.createContent(root, item);

            Assert.fail("able to save invalid exception event, is TimezoneValidator active?");
        } catch (ConstraintViolationException cve) {
        }
    }

    /**
     * Gets user.
     * @param userDao The userDao.
     * @param username The username.
     * @return The user.
     */
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    /**
     * Generates test content.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private HibNoteItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    /**
     * Generates test content.
     * @param name The name. 
     * @param owner The owner.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private HibNoteItem generateTestContent(String name, String owner)
            throws Exception {
        HibNoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        return content;
    }

}
