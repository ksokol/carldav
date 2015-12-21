/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo;

import org.junit.After;
import org.junit.Before;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockUserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.impl.StandardContentService;
import org.unitedinternet.cosmo.service.impl.StandardUserService;
import org.unitedinternet.cosmo.service.lock.SingleVMLockManager;

import java.security.SecureRandom;

/**
 */
public class MockHelper extends TestHelper {
    private MockEntityFactory entityFactory;
    private MockSecurityManager securityManager;
    private StandardContentService contentService;
    private User user;
    private HomeCollectionItem homeCollection;
    
    public MockHelper() {
        securityManager = new MockSecurityManager();

        MockDaoStorage storage = new MockDaoStorage();
        MockContentDao contentDao = new MockContentDao(storage);
        MockUserDao userDao = new MockUserDao();
        SingleVMLockManager lockManager = new SingleVMLockManager();
        
        entityFactory = new MockEntityFactory();
        contentService = new StandardContentService(contentDao, lockManager);

        final StandardUserService userService = new StandardUserService(contentDao, userDao);
        KeyBasedPersistenceTokenService keyBasedPersistenceTokenService = new KeyBasedPersistenceTokenService();
        keyBasedPersistenceTokenService.setServerSecret("cosmossecret");
        keyBasedPersistenceTokenService.setServerInteger(123);
        keyBasedPersistenceTokenService.setSecureRandom(new SecureRandom());

        user = userService.getUserByEmail("test@localhost");
        if (user == null) {
            user = makeDummyUser("test", "password");
            userService.createUser(user);
        }
        homeCollection = contentService.getRootItem(user);
    }
    
    /**
     * Setup.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {}
    
    /**
     * TearDown method.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @After
    public void tearDown() throws Exception {}

    /**
     * Log in.
     */
    public void logIn() {
        logInUser(user);
    }

    /**
     * Log in user.
     * @param u - the user.
     */
    public void logInUser(User u) {
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(u));
    }

    /**
     * Returns the security manager.
     * @return The security manager.
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Gets content service.
     * @return The content service.
     */
    public ContentService getContentService() {
        return contentService;
    }
    
    /**
     * Gets entity factory.
     * @return EntityFactory.
     */
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    /**
     * Gets the user.
     * @return The user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets home collection.
     * @return Home collection.
     */
    public HomeCollectionItem getHomeCollection() {
        return homeCollection;
    }

    /**
     * Makes and stores dummy collection.
     * @param parent The collection item parent.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionItem makeAndStoreDummyCollection(CollectionItem parent)
        throws Exception {
        CollectionItem c = makeDummyCollection(user);
        return contentService.createCollection(parent, c);
    }
    
    /**
     * Makes and stores dummy calendar collection.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionItem makeAndStoreDummyCalendarCollection()
        throws Exception {
        return makeAndStoreDummyCalendarCollection(null);
    }
    
    /**
     * Makes and stores dummy calendar collection.
     * @param name The name.
     * @return The collection item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CollectionItem makeAndStoreDummyCalendarCollection(String name)
            throws Exception {
        CollectionItem c = makeDummyCalendarCollection(user, name);
        return contentService.createCollection(homeCollection, c);
    }

    /**
     * Makes and store dummy item.
     * @param parent The collection item parent.
     * @param name The name.
     * @return The note item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public NoteItem makeAndStoreDummyItem(CollectionItem parent,
                                          String name)
        throws Exception {
        NoteItem i = makeDummyItem(user, name);
        return (NoteItem) contentService.createContent(parent, i);
    }
}
