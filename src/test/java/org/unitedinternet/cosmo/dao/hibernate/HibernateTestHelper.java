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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Test Hibernate helper.
 *
 */
public class HibernateTestHelper {

    /**
     * Verify item.
     * @param hibItem1 Item1.
     * @param hibItem2 Item2.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public void verifyItem(HibItem hibItem1, HibItem hibItem2) throws Exception {
        Assert.assertEquals(hibItem1.getName(), hibItem2.getName());
        Assert.assertEquals(hibItem1.getModifiedDate(), hibItem2.getModifiedDate());
        Assert.assertEquals(hibItem1.getDisplayName(), hibItem2.getDisplayName());
        Assert.assertEquals(getHibItem(hibItem1).getId(), getHibItem(hibItem2).getId());
        Assert.assertEquals(hibItem1.getUid(), hibItem2.getUid());
    }

    public String getString(String name) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        return new String(bos.toByteArray(), "UTF-8");
    }

    /**
     * Gets calendar.
     * @param name The name.
     * @return The calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public Calendar getCalendar(String name) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        return CalendarUtils.parseCalendar(in);
    }

    /**
     * Gets user.
     * @param userDao UserDao.
     * @param contentDao ContentDao.
     * @param username The username.
     * @return The user.
     */
    public User getUser(UserDao userDao, ContentDao contentDao, String username) {
        final String email = username + "@testem";
        User user = userDao.getUser(email);
        if (user == null) {
            user = new User();
            user.setPassword(username);
            user.setEmail(email);
            userDao.createUser(user);

            user = userDao.getUser(email);

            // create root item
            contentDao.createRootItem(user);
        }
        return user;
    }
    
    /**
     * Gets Hib Item.
     * @param hibItem The item.
     * @return HibItem.
     */
    private HibItem getHibItem(HibItem hibItem) {
        return hibItem;
    }
}
