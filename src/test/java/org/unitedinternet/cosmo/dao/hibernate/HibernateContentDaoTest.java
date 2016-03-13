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
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;

import static org.junit.Assert.assertEquals;

public class HibernateContentDaoTest extends IntegrationTestSupport {

    @Autowired
    private UserDaoImpl userDao;
    @Autowired
    private ItemDaoImpl itemDao;
    @Autowired
    private CollectionDao collectionDao;

    @Test
    public void multipleItemsError() throws Exception {
        User user = getUser("testuser");
        HibCollectionItem root = collectionDao.findByOwnerAndName(user.getEmail(), user.getEmail());

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setParent(root);

        collectionDao.save(a);

        HibItem item1 = generateTestContent();
        item1.setUid("1");
        item1.setCollection(a);
        itemDao.save(item1);

        HibItem item2 = generateTestContent();
        item2.setUid("1");
        item2.setCollection(a);

        try {
            itemDao.save(item2);
            Assert.fail("able to add item with same name to collection");
        } catch (ConstraintViolationException e) {
            assertEquals("UID_OWNER_COLLECTION", e.getConstraintName());
        }
    }

    @Test
    public void multipleCollectionsError() throws Exception {
        User user = getUser("testuser");
        HibCollectionItem root = collectionDao.findByOwnerAndName(user.getEmail(), user.getEmail());

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setParent(root);

        collectionDao.save(a);

        HibCollectionItem b = new HibCollectionItem();
        b.setName("a");
        b.setDisplayName("displayName");
        b.setOwner(user);
        b.setParent(root);

        try {
            collectionDao.save(b);
            Assert.fail("able to add item with same name to collection");
        } catch (ConstraintViolationException e) {
            assertEquals("DISPLAYNAME_OWNER", e.getConstraintName());
        }
    }

    public User getUser(String username) {
        final String email = username + "@testem";
        User user = userDao.findByEmailIgnoreCase(email);
        if (user == null) {
            user = new User();
            user.setPassword(username);
            user.setEmail(email);
            userDao.save(user);

            user = userDao.findByEmailIgnoreCase(email);

            HibCollectionItem newItem = new HibCollectionItem();

            newItem.setOwner(user);
            //TODO
            newItem.setName(user.getEmail());
            newItem.setDisplayName("homeCollection");
            collectionDao.save(newItem);

            // create root item
            collectionDao.save(newItem);
        }
        return user;
    }

    private HibItem generateTestContent() {
        return generateTestContent("test", "testuser");
    }

    private HibItem generateTestContent(String name, String owner) {
        HibItem content = new HibItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(owner));
        content.setMimetype("irrelevant");
        return content;
    }
}
