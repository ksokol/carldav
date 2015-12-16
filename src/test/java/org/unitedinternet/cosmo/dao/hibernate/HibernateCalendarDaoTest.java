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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

/**
 * Test CalendarDaoImpl
 */
public class HibernateCalendarDaoTest extends AbstractHibernateDaoTestCase {

    @Autowired
    protected CalendarDaoImpl calendarDao;

    @Autowired
    protected ContentDaoImpl contentDao;

    @Autowired
    protected UserDaoImpl userDao;

    /**
     * Tests calendar dao basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCalendarDaoBasic() throws Exception {
        CollectionItem calendar = generateCalendar();
        CollectionItem root = contentDao.getRootItem(getUser(userDao));
        
        contentDao.createCollection(root, calendar);

        clearSession();

        CollectionItem queryItem = (CollectionItem) contentDao
                .findItemByUid(calendar.getUid());

        CalendarCollectionStamp ccs = (CalendarCollectionStamp) queryItem
                .getStamp(CalendarCollectionStamp.class);
        
        Assert.assertNotNull(queryItem);
        Assert.assertEquals("test", queryItem.getName());
        Assert.assertEquals("en", ccs.getLanguage());
        Assert.assertEquals("test description", ccs.getDescription());

        // test update
        queryItem.setName("test2");
        ccs.setLanguage("es");
        ccs.setDescription("test description2");

        contentDao.updateCollection(queryItem);
        Assert.assertNotNull(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ccs = (CalendarCollectionStamp) queryItem.getStamp(CalendarCollectionStamp.class);
        
        Assert.assertEquals("test2", queryItem.getName());
        Assert.assertEquals("es", ccs.getLanguage());
        Assert.assertEquals("test description2", ccs.getDescription());

        // test add event
        ContentItem event = generateEvent("test.ics", "testdata/cal1.ics"
        );
        
        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        ContentItem newEvent = contentDao.createContent(calendar, event);
        
        clearSession();

        // test query event
        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        EventStamp evs = (EventStamp) queryEvent.getStamp(EventStamp.class);
        
        Assert.assertEquals("test.ics", queryEvent.getName());
        Assert.assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());

        // test update event
        queryEvent.setName("test2.ics");
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes("testdata/cal2.ics")));
        
        contentDao.updateContent(queryEvent);

        Calendar cal = evs.getEventCalendar();
        
        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        evs = (EventStamp) queryEvent.getStamp(EventStamp.class);
        
        Assert.assertEquals("test2.ics", queryEvent.getName());
        Assert.assertEquals(evs.getEventCalendar().toString(), cal.toString());
        

        // test delete
        contentDao.removeContent(queryEvent);

        clearSession();

        queryEvent = (ContentItem) contentDao.findItemByUid(newEvent.getUid());
        Assert.assertNull(queryEvent);

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        contentDao.removeCollection(queryItem);

        clearSession();

        queryItem = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        Assert.assertNull(queryItem);
    }

    /**
     * Tests long property value.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testLongPropertyValue() throws Exception {
        CollectionItem calendar = generateCalendar();
        CollectionItem root = contentDao.getRootItem(getUser(userDao));
        
        contentDao.createCollection(root, calendar);

        ContentItem event = generateEvent("testdata/big.ics", "testdata/big.ics"
        );

        event = contentDao.createContent(calendar, event);

        clearSession();

        ContentItem queryEvent = (ContentItem) contentDao.findItemByUid(event.getUid());
        Assert.assertEquals(getCalendar(event).toString(), getCalendar(queryEvent).toString());
    }

    /**
     * Tests find by Ical uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testFindByEventIcalUid() throws Exception {
        CollectionItem calendar = generateCalendar();
        CollectionItem root = contentDao.getRootItem(getUser(userDao));
        
        contentDao.createCollection(root, calendar);

        NoteItem event = generateEvent("testdata/test.ics", "testdata/cal1.ics"
        );

        event = (NoteItem) contentDao.createContent(calendar, event);
        
        clearSession();

        calendar = (CollectionItem) contentDao.findItemByUid(calendar.getUid());
        String uid = "68ADA955-67FF-4D49-BBAC-AF182C620CF6";
        ContentItem queryEvent = calendarDao.findEventByIcalUid(uid,
                calendar);
        Assert.assertNotNull(queryEvent);
        Assert.assertEquals(event.getUid(), queryEvent.getUid());
    }

    /**
     * Gets user.
     * @param userDao The userDao.
     * @return The user.
     */
    private User getUser(UserDao userDao) {
        return helper.getUser(userDao, contentDao, "testuser");
    }

    /**
     * Generates calendar.
     * @return The collection item.
     */
    private CollectionItem generateCalendar() {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setName("test");
        calendar.setOwner(getUser(userDao));
        
        CalendarCollectionStamp ccs = new HibCalendarCollectionStamp();
        calendar.addStamp(ccs);
        
        ccs.setDescription("test description");
        ccs.setLanguage("en");
        
        return calendar;
    }
    
    /**
     * Generates event.
     * @param name The name.
     * @param file The file.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private NoteItem generateEvent(String name, String file) throws Exception {
        NoteItem event = new HibNoteItem();
        event.setName(name);
        event.setOwner(getUser(userDao));
       
        EventStamp evs = new HibEventStamp();
        event.addStamp(evs);
        evs.setEventCalendar(CalendarUtils.parseCalendar(helper.getBytes(file)));
        event.setIcalUid(evs.getIcalUid());
        if (evs.getEvent().getDescription() != null) {
            event.setBody(evs.getEvent().getDescription().getValue());
        }
        if (evs.getEvent().getSummary() != null) {
            event.setDisplayName(evs.getEvent().getSummary().getValue());
        }
        
        return event;
    }

    /**
     * Gets calendar.
     * @param item The content item.
     * @return The calendar.
     */
    private Calendar getCalendar(ContentItem item) {
        return new EntityConverter(null).convertContent(item);
    }
}
