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
package org.unitedinternet.cosmo.security.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.TestHelper;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.mock.MockSecurityContext;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;

/**
 * Test Case for <code>SecurityHelper/code> which uses mock
 * model objects.
 */
public class SecurityHelperTest {

    private TestHelper testHelper;
 
    private SecurityHelper securityHelper;

    /**
     * Sets up.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        testHelper = new TestHelper();
        securityHelper = new SecurityHelper();
    }

    /**
     * Tests collection user access.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testCollectionUserAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        User admin = testHelper.makeDummyUser();
        admin.setAdmin(true);
        CollectionItem col = testHelper.makeDummyCalendarCollection(user1);
        CosmoSecurityContext context = getSecurityContext(user1);
        
        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(user2);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, col));
        context = getSecurityContext(admin);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, col));
    }

    /**
     * Tests content user access.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testContentUserAccess() throws Exception {
        User user1 = testHelper.makeDummyUser("user1","password");
        User user2 = testHelper.makeDummyUser("user2","password");
        User user3 = testHelper.makeDummyUser("user3","password");
        User admin = testHelper.makeDummyUser();
        admin.setAdmin(true);
        MockCollectionItem col1 = new MockCollectionItem();
        MockCollectionItem col2 = new MockCollectionItem();
        col1.setOwner(user1);
        col2.setOwner(user2);
        col1.setUid("col1");
        col2.setUid("col2");
        MockNoteItem note = new MockNoteItem();
        note.setUid("note1");
        note.setOwner(user1);
        note.addParent(col1);
        note.addParent(col2);
        
        CosmoSecurityContext context = getSecurityContext(user1);
        
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(user2);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(user3);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        context = getSecurityContext(admin);
        Assert.assertTrue(securityHelper.hasWriteAccess(context, note));
        
        // remove note from col2, so user2 doesn't have access
        note.removeParent(col2);
        
        context = getSecurityContext(user2);
        Assert.assertFalse(securityHelper.hasWriteAccess(context, note));
        
    }


    @Test
    public void testHasUserAccessIsNull() {
        final CosmoSecurityContext cosmoSecurityContext = mock(CosmoSecurityContext.class);

        when(cosmoSecurityContext.getUser()).thenReturn(null);
        final boolean result = securityHelper.hasUserAccess(cosmoSecurityContext, null);

        assertThat(result, is(false));
    }

    @Test
    public void testHasUserAccessIsAdmin() {
        final CosmoSecurityContext cosmoSecurityContext = mock(CosmoSecurityContext.class, RETURNS_DEEP_STUBS);

        when(cosmoSecurityContext.getUser().getAdmin()).thenReturn(true);
        final boolean result = securityHelper.hasUserAccess(cosmoSecurityContext, null);

        assertThat(result, is(true));
    }

    @Test
    public void testHasUserAccessIsNoAdmin() {
        final CosmoSecurityContext cosmoSecurityContext = mock(CosmoSecurityContext.class, RETURNS_DEEP_STUBS);

        when(cosmoSecurityContext.getUser().getAdmin()).thenReturn(false);
        final boolean result = securityHelper.hasUserAccess(cosmoSecurityContext, null);

        assertThat(result, is(false));
    }

    /**
     * Gets security context.
     * @param user The user.
     * @return The cosmo security context.
     */
    private CosmoSecurityContext getSecurityContext(User user) {
        return new MockSecurityContext(new MockUserPrincipal(user));
    }

}
