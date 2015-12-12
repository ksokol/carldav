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
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscription;

import java.util.Set;

/**
 * Test HibernateUserDaoSubscription.
 *
 */
public class HibernateUserDaoSubscriptionTest extends AbstractHibernateDaoTestCase {
    @Autowired
    protected ContentDaoImpl contentDao;
    @Autowired
    protected UserDaoImpl userDao;

    /**
     * Test subscribe.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSubscribe() throws Exception {
        User user = getUser(userDao, "subuser1");
        CollectionItem root = (CollectionItem) contentDao.getRootItem(user);
        CollectionItem collection = getCollection(root, "subcoll1");

        CollectionSubscription sub1 = new HibCollectionSubscription();
        sub1.setDisplayName("sub1");
        sub1.setCollection(collection);
        user.addSubscription(sub1);
        userDao.updateUser(user);

        clearSession();
        
        user = getUser(userDao, "subuser1");
        
        Assert.assertFalse("no subscriptions saved",
                    user.getCollectionSubscriptions().isEmpty());

        CollectionSubscription querySub = user
                .getSubscription("sub1");
        Assert.assertNotNull("sub1 not found", querySub);
        Assert.assertEquals("sub1 not same subscriber", user.getUid(), querySub
                .getOwner().getUid());
        Assert.assertEquals("sub1 not same collection", collection.getUid(), querySub
                .getCollectionUid());

        querySub.setDisplayName("sub2");
        userDao.updateUser(user);
        
        clearSession();
        
        user = getUser(userDao, "subuser1");
        
        querySub = user.getSubscription("sub1");
        Assert.assertNull("sub1 mistakenly found", querySub);

        querySub = user.getSubscription("sub2");
        Assert.assertNotNull("sub2 not found", querySub);

        user.removeSubscription(querySub);
        userDao.updateUser(user);

        clearSession();
        
        user = getUser(userDao, "subuser1");
        
        querySub = user.getSubscription("sub1");
        Assert.assertNull("sub1 mistakenly found", querySub);

        querySub = user.getSubscription("sub2");
        Assert.assertNull("sub2 mistakenly found", querySub);
    }

    /**
     * Gets user.
     * @param userDao UserDao.
     * @param username Username
     * @return The user.
     */
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    /**
     * Gets collection.
     * @param parent The parent.
     * @param name The name.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private CollectionItem getCollection(CollectionItem parent,
                                         String name)
        throws Exception {
        for (Item child : (Set<Item>) parent.getChildren()) {
            if (child.getName().equals(name)) {
                return (CollectionItem) child;
            }
        }
        CollectionItem collection = new HibCollectionItem();
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(parent.getOwner());
        return contentDao.createCollection(parent, collection);
    }

}
