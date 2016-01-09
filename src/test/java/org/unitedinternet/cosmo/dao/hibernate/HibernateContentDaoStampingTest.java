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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
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
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.MessageStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibEventExceptionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibMessageStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.model.hibernate.HibQName;
import org.unitedinternet.cosmo.model.hibernate.HibStringAttribute;

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
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        MessageStamp message = new HibMessageStamp(item);
        message.setBcc("bcc");
        message.setTo("to");
        message.setFrom("from");
        message.setCc("cc");

        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(message);
        item.addStamp(event);

        ContentItem newItem = contentDao.createContent(root, item);


        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(2, queryItem.getStamps().size());

        Stamp stamp = queryItem.getStamp(EventStamp.class);
        Assert.assertNotNull(stamp.getCreationDate());
        Assert.assertNotNull(stamp.getModifiedDate());
        Assert.assertTrue(stamp.getCreationDate().equals(stamp.getModifiedDate()));
        Assert.assertTrue(stamp instanceof EventStamp);
        Assert.assertEquals("event", stamp.getType());
        EventStamp es = (EventStamp) stamp;
        Assert.assertEquals(es.getEventCalendar().toString(), event.getEventCalendar()
                .toString());

        Assert.assertEquals("icaluid", ((NoteItem) queryItem).getIcalUid());
        Assert.assertEquals("this is a body", ((NoteItem) queryItem).getBody());

        stamp = queryItem.getStamp(MessageStamp.class);
        Assert.assertTrue(stamp instanceof MessageStamp);
        Assert.assertEquals("message", stamp.getType());
        MessageStamp ms = (MessageStamp) stamp;
        Assert.assertEquals(ms.getBcc(), message.getBcc());
        Assert.assertEquals(ms.getCc(), message.getCc());
        Assert.assertEquals(ms.getTo(), message.getTo());
        Assert.assertEquals(ms.getFrom(), message.getFrom());
    }

    /**
     * Test stamp handlers.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testStampHandlers() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        HibEventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(event);

        Assert.assertNull(event.getTimeRangeIndex());

        ContentItem newItem = contentDao.createContent(root, item);


        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        event = (HibEventStamp) queryItem.getStamp(EventStamp.class);
        Assert.assertEquals("20050817T115000Z", event.getTimeRangeIndex().getStartDate());
        Assert.assertEquals("20050817T131500Z",event.getTimeRangeIndex().getEndDate());
        Assert.assertFalse(event.getTimeRangeIndex().getIsFloating().booleanValue());

        event.setStartDate(new Date("20070101"));
        //event.setEntityTag("foo"); // FIXME setStartDate does not modify any persistent field, so object is not marked dirty
        event.setEndDate(null);

        contentDao.updateContent(queryItem);


        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());

        event = (HibEventStamp) queryItem.getStamp(EventStamp.class);
        Assert.assertEquals("20070101", event.getTimeRangeIndex().getStartDate());
        Assert.assertEquals("20070101",event.getTimeRangeIndex().getEndDate());
        Assert.assertTrue(event.getTimeRangeIndex().getIsFloating().booleanValue());
    }

    /**
     * Test stamps update. 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testStampsUpdate() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        ((NoteItem) item).setBody("this is a body");
        ((NoteItem) item).setIcalUid("icaluid");

        MessageStamp message = new HibMessageStamp(item);
        message.setBcc("bcc");
        message.setTo("to");
        message.setFrom("from");
        message.setCc("cc");

        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(message);
        item.addStamp(event);

        ContentItem newItem = contentDao.createContent(root, item);


        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(2, queryItem.getStamps().size());

        Stamp stamp = queryItem.getStamp(MessageStamp.class);
        queryItem.removeStamp(stamp);

        stamp = queryItem.getStamp(EventStamp.class);
        EventStamp es = (EventStamp) stamp;
        queryItem.setClientModifiedDate(new Date());
        es.setEventCalendar(helper.getCalendar("testdata/cal2.ics"));
        Calendar newCal = es.getEventCalendar();
        Thread.sleep(10);

        contentDao.updateContent(queryItem);


        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
        Assert.assertNull(queryItem.getStamp(MessageStamp.class));
        stamp = queryItem.getStamp(EventStamp.class);
        es = (EventStamp) stamp;

        Assert.assertTrue(stamp.getModifiedDate().after(stamp.getCreationDate()));

        if(!es.getEventCalendar().toString().equals(newCal.toString())) {
            LOG.error(es.getEventCalendar().toString());
            LOG.error(newCal.toString());
        }
        Assert.assertEquals(es.getEventCalendar().toString(), newCal.toString());
    }

    /**
     * Test event stamp validation.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        ContentItem item = generateTestContent();

        EventStamp event = new HibEventStamp();
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
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));

        item.addStamp(event);

        ContentItem newItem = contentDao.createContent(root, item);


        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());

        Stamp stamp = queryItem.getStamp(EventStamp.class);
        queryItem.removeStamp(stamp);
        contentDao.updateContent(queryItem);


        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertNotNull(queryItem);
        Assert.assertEquals(queryItem.getStamps().size(),0);

        event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));
        queryItem.addStamp(event);

        contentDao.updateContent(queryItem);


        queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());
    }

    /**
     * test calendar collection stamp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCalendarCollectionStamp() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        Calendar testCal = helper.getCalendar("testdata/timezone.ics");

        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setDescription("description");
        calendarStamp.setTimezoneCalendar(testCal);
        calendarStamp.setLanguage("en");
        calendarStamp.setColor("#123123");
        calendarStamp.setVisibility(true);

        root.addStamp(calendarStamp);

        contentDao.updateCollection(root);


        root = (CollectionItem) contentDao.findItemByUid(root.getUid());

        ContentItem item = generateTestContent();
        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));
        item.addStamp(event);

        contentDao.createContent(root, item);
        session.clear();

        CollectionItem queryCol = (CollectionItem) contentDao.findItemByUid(root.getUid());
        Assert.assertEquals(1, queryCol.getStamps().size());
        Stamp stamp = queryCol.getStamp(CalendarCollectionStamp.class);
        Assert.assertTrue(stamp instanceof CalendarCollectionStamp);
        Assert.assertEquals("calendar", stamp.getType());
        CalendarCollectionStamp ccs = (CalendarCollectionStamp) stamp;
        Assert.assertEquals("description", ccs.getDescription());
        Assert.assertEquals(testCal.toString(), ccs.getTimezoneCalendar().toString());
        Assert.assertEquals("en", ccs.getLanguage());
        Assert.assertEquals("#123123", ccs.getColor());
        Assert.assertEquals(true, ccs.getVisibility());

        Calendar cal = new EntityConverter(null).convertCollection(queryCol);
        Assert.assertEquals(1, cal.getComponents().getComponents(Component.VEVENT).size());
    }

    /**
     * Test calendar collection stamp validation.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCalendarCollectionStampValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        Calendar testCal = helper.getCalendar("testdata/cal1.ics");

        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setTimezoneCalendar(testCal);

        root.addStamp(calendarStamp);

        try {
            contentDao.updateCollection(root);

            Assert.fail("able to save invalid timezone, is TimezoneValidator active?");
        } catch (ConstraintViolationException cve) {

        }
    }

    /**
     * Test calendar collection stamp validation.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCalendarCollectionStampColorValidation() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        helper.getCalendar("testdata/cal1.ics");

        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setColor("red");

        root.addStamp(calendarStamp);

        try {
            contentDao.updateCollection(root);

            Assert.fail("able to save invalid color, is ColorValidator active?");
        } catch (ConstraintViolationException cve) {

        }
    }

    @Test(expected=ConstraintViolationException.class)
    public void shouldNotAllowDisplayNamesWithLengthGreaterThan64() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setDisplayName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        root.addStamp(calendarStamp);

        contentDao.updateCollection(root);

    }

    @Test(expected=ConstraintViolationException.class)
    public void shouldNotAllowEmptyDisplayNames() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setDisplayName("");
        root.addStamp(calendarStamp);

        contentDao.updateCollection(root);

    }

    public void shouldAllowLegalDisplayNames() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);


        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        calendarStamp.setDisplayName("Valid display name");
        root.addStamp(calendarStamp);
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
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        EventExceptionStamp eventex = new HibEventExceptionStamp();
        eventex.setEventCalendar(helper.getCalendar("testdata/exception.ics"));

        item.addStamp(eventex);

        ContentItem newItem = contentDao.createContent(root, item);


        ContentItem queryItem = (ContentItem) contentDao.findItemByUid(newItem.getUid());
        Assert.assertEquals(1, queryItem.getStamps().size());

        Stamp stamp = queryItem.getStamp(EventExceptionStamp.class);
        Assert.assertNotNull(stamp.getCreationDate());
        Assert.assertNotNull(stamp.getModifiedDate());
        Assert.assertTrue(stamp.getCreationDate().equals(stamp.getModifiedDate()));
        Assert.assertTrue(stamp instanceof EventExceptionStamp);
        Assert.assertEquals("eventexception", stamp.getType());
        EventExceptionStamp ees = (EventExceptionStamp) stamp;
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
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

        NoteItem item = generateTestContent();

        item.setIcalUid("icaluid");
        item.setBody("this is a body");

        EventExceptionStamp eventex = new HibEventExceptionStamp();
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
    private NoteItem generateTestContent() throws Exception {
        return generateTestContent("test", "testuser");
    }

    /**
     * Generates test content.
     * @param name The name. 
     * @param owner The owner.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private NoteItem generateTestContent(String name, String owner)
            throws Exception {
        NoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new HibStringAttribute(new HibQName("customattribute"),
                "customattributevalue"));
        return content;
    }

}
