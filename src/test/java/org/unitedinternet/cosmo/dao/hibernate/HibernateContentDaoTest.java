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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import carldav.repository.UserRepository;
import carldav.entity.HibCollectionItem;
import carldav.entity.HibItem;
import carldav.entity.User;

public class HibernateContentDaoTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CollectionRepository collectionRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void multipleItemsError() throws Exception {
        User user = getUser("testuser");
        HibCollectionItem root = collectionRepository.findByOwnerEmailAndName(user.getEmail(), user.getEmail());

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setParent(root);

        collectionRepository.save(a);

        HibItem item1 = generateTestContent();
        item1.setUid("1");
        item1.setCollection(a);
        itemRepository.save(item1);

        HibItem item2 = generateTestContent();
        item2.setUid("1");
        item2.setCollection(a);

        expectedException.expect(org.springframework.dao.DataIntegrityViolationException.class);
        expectedException.expectMessage("could not execute statement; SQL [n/a]; constraint [UID_OWNER_COLLECTION]");

        itemRepository.save(item2);
    }

    @Test
    public void multipleCollectionsError() throws Exception {
        User user = getUser("testuser");
        HibCollectionItem root = collectionRepository.findByOwnerEmailAndName(user.getEmail(), user.getEmail());

        HibCollectionItem a = new HibCollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setParent(root);

        collectionRepository.save(a);

        HibCollectionItem b = new HibCollectionItem();
        b.setName("a");
        b.setDisplayName("displayName");
        b.setOwner(user);
        b.setParent(root);

        expectedException.expect(org.springframework.dao.DataIntegrityViolationException.class);
        expectedException.expectMessage("could not execute statement; SQL [n/a]; constraint [DISPLAYNAME_OWNER]");

        collectionRepository.save(b);
    }

    public User getUser(String username) {
        final String email = username + "@testem";
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            user = new User();
            user.setPassword(username);
            user.setEmail(email);
            userRepository.save(user);

            user = userRepository.findByEmailIgnoreCase(email);

            HibCollectionItem newItem = new HibCollectionItem();

            newItem.setOwner(user);
            //TODO
            newItem.setName(user.getEmail());
            newItem.setDisplayName("homeCollection");
            collectionRepository.save(newItem);

            // create root item
            collectionRepository.save(newItem);
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
