/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.event.aop;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.dao.mock.MockContentDao;
import org.unitedinternet.cosmo.dao.mock.MockDaoStorage;
import org.unitedinternet.cosmo.dao.mock.MockEventLogDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.ItemChangeRecord;
import org.unitedinternet.cosmo.model.ItemChangeRecord.Action;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;
import org.unitedinternet.cosmo.security.mock.MockSecurityContext;
import org.unitedinternet.cosmo.security.mock.MockSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.impl.StandardContentService;
import org.unitedinternet.cosmo.service.lock.SingleVMLockManager;

import java.util.Date;
import java.util.List;

/**
 * Test Case for <code>EventLogAdvice/code>
 */
public class EventLogAdviceTest {

    private MockContentDao contentDao;
    private MockEventLogDao eventLogDao;
    private TestHelper testHelper;
    private ContentService proxyService;
    private MockSecurityManager securityManager;
    
   
    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        testHelper = new TestHelper();
        securityManager = new MockSecurityManager();
        contentDao = new MockContentDao(new MockDaoStorage());
        eventLogDao = new MockEventLogDao();
        final StandardContentService service = new StandardContentService(contentDao, new SingleVMLockManager());

        // create a factory that can generate a proxy for the given target object
        AspectJProxyFactory factory = new AspectJProxyFactory(service); 

        // add aspect
        EventLogAdvice eva = new EventLogAdvice(securityManager, eventLogDao);
        eva.setEnabled(true);
        factory.addAspect(eva);

        // now get the proxy object...
        proxyService = factory.getProxy();
    }

    /**
     * Tests event log aspect with user.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventLogAspectWithUser() throws Exception {
        
        Date startDate = new Date();
        
        User user1 = testHelper.makeDummyUser("user1", "password");
        testHelper.makeDummyUser("user2", "password");
        CollectionItem rootCollection = contentDao.createRootItem(user1);
        ContentItem dummyContent = new MockNoteItem();
        dummyContent.setName("foo");
        dummyContent.setOwner(user1);
        dummyContent.setUid("1");
        dummyContent = contentDao.createContent(rootCollection, dummyContent);
        
        // login as user1
        initiateContext(user1);
        
        // update content
        proxyService.updateContent(dummyContent);
        
        Date endDate = new Date();
        
        
        // query ItemChangeRecords
        List<ItemChangeRecord> records = eventLogDao.findChangesForCollection(rootCollection, startDate, endDate);
        Assert.assertEquals(1, records.size());
        
        ItemChangeRecord record = records.get(0);
        Assert.assertEquals(dummyContent.getUid(), record.getItemUuid());
        Assert.assertEquals(dummyContent.getDisplayName(), record.getItemDisplayName());
        Assert.assertEquals(user1.getEmail(), record.getModifiedBy());
        Assert.assertEquals(Action.ITEM_CHANGED, record.getAction());
    }
    
    /**
     * Initiate context.
     * @param user The user.
     */
    private void initiateContext(User user) {
        securityManager.initiateSecurityContext(new MockSecurityContext(new MockUserPrincipal(user)));
    }

}
