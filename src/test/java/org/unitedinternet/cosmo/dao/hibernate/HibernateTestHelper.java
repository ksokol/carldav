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

import carldav.repository.CollectionDao;
import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class HibernateTestHelper {

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
     * Gets user.
     * @param userDao UserDao.
     * @param itemDao ContentDao.
     * @param username The username.
     * @return The user.
     */
    public User getUser(UserDao userDao, CollectionDao itemDao, String username) {
        final String email = username + "@testem";
        User user = userDao.getUser(email);
        if (user == null) {
            user = new User();
            user.setPassword(username);
            user.setEmail(email);
            userDao.createUser(user);

            user = userDao.getUser(email);

            HibHomeCollectionItem newItem = new HibHomeCollectionItem();

            newItem.setOwner(user);
            //TODO
            newItem.setName(user.getEmail());
            newItem.setDisplayName("homeCollection");
            itemDao.save(newItem);

            // create root item
            itemDao.save(newItem);
        }
        return user;
    }
}
