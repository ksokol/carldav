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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibEventExceptionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import javax.validation.ConstraintViolationException;

/**
 * Test for hibernate content dao stamping.
 * @author ccoman
 *
 */
public class HibernateContentDaoStampingTest extends AbstractHibernateDaoTestCase {
    
    @Autowired
    private UserDaoImpl userDao;
    @Autowired
    private ContentDaoImpl contentDao;

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
            clearSession();
            Assert.fail("able to create invalid event!");
        } catch (IllegalStateException is) {}
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
        calendarStamp.setTimezoneCalendar(testCal);

        root.addStamp(calendarStamp);
        
        contentDao.updateCollection(root);
        clearSession();
        
        root = (CollectionItem) contentDao.findItemByUid(root.getUid());
        
        ContentItem item = generateTestContent();
        EventStamp event = new HibEventStamp();
        event.setEventCalendar(helper.getCalendar("testdata/cal1.ics"));
        item.addStamp(event);
        
        contentDao.createContent(root, item);
        
        clearSession();
        
        CollectionItem queryCol = (CollectionItem) contentDao.findItemByUid(root.getUid());
        Assert.assertEquals(1, queryCol.getStamps().size());
        Stamp stamp = queryCol.getStamp(CalendarCollectionStamp.class);
        Assert.assertTrue(stamp instanceof CalendarCollectionStamp);
        Assert.assertEquals("calendar", stamp.getType());
        CalendarCollectionStamp ccs = (CalendarCollectionStamp) stamp;
        Assert.assertEquals(testCal.toString(), ccs.getTimezoneCalendar().toString());

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
            clearSession();
            Assert.fail("able to save invalid timezone, is TimezoneValidator active?");
        } catch (ConstraintViolationException cve) {
            
        } 
    }

    public void shouldAllowLegalDisplayNames() throws Exception {
        User user = getUser(userDao, "testuser");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        
        
        CalendarCollectionStamp calendarStamp = new HibCalendarCollectionStamp(root);
        root.addStamp(calendarStamp);
        try{
            contentDao.updateCollection(root);
        }catch(ConstraintViolationException ex){
            Assert.fail("Valid display name was used");
        }
        clearSession();
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
        clearSession();

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
            clearSession();
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
        return content;
    }

}
