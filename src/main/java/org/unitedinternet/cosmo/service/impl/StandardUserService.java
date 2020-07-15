/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.service.impl;

import carldav.entity.CollectionItem;
import carldav.entity.User;
import carldav.repository.CollectionRepository;
import carldav.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

import java.util.List;

public class StandardUserService implements UserService {

    private final ContentService contentService;
    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final PasswordEncoder passwordEncoder;

    public StandardUserService(
            ContentService contentService,
            UserRepository userRepository,
            CollectionRepository collectionRepository,
            PasswordEncoder passwordEncoder
    ) {
        Assert.notNull(contentService, "contentService is null");
        Assert.notNull(userRepository, "userRepository is null");
        Assert.notNull(collectionRepository, "collectionRepository is null");
        Assert.notNull(passwordEncoder, "passwordEncoder is null");
        this.contentService = contentService;
        this.userRepository = userRepository;
        this.collectionRepository = collectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(String username) {
        return userRepository.findByEmailIgnoreCase(username);
    }

    /**
     * Creates a user account in the repository. Digests the raw
     * password and uses the result to replace the raw
     * password. Returns a new instance of <code>User</code>
     * after saving the original one.
     *
     * @param user the account to create
     *
     * @throws DataIntegrityViolationException if the username or
     * email address is already in use
     */
    public User createUser(User user) {
        user.setPassword(digestPassword(user.getPassword()));

        User newUser = userRepository.save(user);

        CollectionItem calendar = new CollectionItem();
        calendar.setOwner(user);
        calendar.setName("calendar");
        calendar.setDisplayName("calendarDisplayName");

        final CollectionItem homeCollection = contentService.createRootItem(newUser);
        contentService.createCollection(homeCollection, calendar);

        CollectionItem addressbook = new CollectionItem();
        addressbook.setOwner(user);
        addressbook.setName("contacts");
        addressbook.setDisplayName("contactDisplayName");

        contentService.createCollection(homeCollection, addressbook);

        return newUser;
    }

    /**
     * Removes a user account from the repository.
     *
     * @param user the account to remove
     */
    public void removeUser(User user) {
        final List<CollectionItem> byOwnerEmail = collectionRepository.findByOwnerEmail(user.getEmail());
        collectionRepository.deleteAll(byOwnerEmail);
        userRepository.delete(user);
    }

    private String digestPassword(String password) {
        if (password == null) {
            return null;
        }
        return passwordEncoder.encode(password);
    }
}
