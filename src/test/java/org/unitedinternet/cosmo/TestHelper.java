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
package org.unitedinternet.cosmo;

import org.junit.Ignore;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;

import java.io.InputStream;

/**
 */
@Ignore
public class TestHelper {

    static int iseq = 0;
    static int lseq = 0;
    static int useq = 0;

    private EntityFactory entityFactory = new MockEntityFactory();

    /**
     * Makes dummy user.
     * @param username The username.
     * @param password The password.
     * @return The user.
     */
    public User makeDummyUser(String username,
                              String password) {
        if (username == null) {
            throw new IllegalArgumentException("username required");
        }
        
        if (password == null) {
            throw new IllegalArgumentException("password required");
        }

        User user = entityFactory.createUser();
        user.setEmail(username + "@localhost");
        user.setPassword(password);

        return user;
    }

    /**
     * Makes dummy user.
     * @return The user.
     */
    public User makeDummyUser() {
        String serial = Integer.toString(++useq);;
        String username = "dummy" + serial;
        return makeDummyUser(username, username);
    }

    /**
     * Makes dummy item.
     * @param user The user.
     * @param name The name.
     * @return The note item.
     */
    public NoteItem makeDummyItem(User user,
                                  String name) {
        String serial =Integer.toString(++iseq);
        if (name == null) {
            name = "test item " + serial;
        }

        NoteItem note = entityFactory.createNote();

        note.setUid(name);
        note.setName(name);
        note.setOwner(user);
        note.setIcalUid(serial);
        note.setBody("This is a note. I love notes.");
       
        return note;
    }

    /**
     * Makes dummy calendar collection.
     * @param user The user.
     * @param name The name.
     * @return The collection item.
     */
    public CollectionItem makeDummyCalendarCollection(User user, String name) {
        String serial = Integer.toString(++lseq);
        if (name == null) {
            name = "test calendar collection " + serial;
        }

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);
       
        collection.addStamp(entityFactory.createCalendarCollectionStamp(collection));

        return collection;
    }

    /**
     * Gets input stream.
     * @param name The name.
     * @return The input stream.
     */
    public InputStream getInputStream(String name){
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return in;
    }

}
