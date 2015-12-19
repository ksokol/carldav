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

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 */
@Ignore
public class TestHelper {
    protected static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

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
     * Loads xml.
     * @param name The name.
     * @return The document xml loaded.
     * @throws IOException - if something is wrong this exception is thrown.
     * @throws SAXException - if something is wrong this exception is thrown.
     * @throws ParserConfigurationException - if something is wrong this exception is thrown.
     */
    public Document loadXml(String name) throws SAXException,
            ParserConfigurationException, IOException {
        InputStream in = getInputStream(name);
        BUILDER_FACTORY.setNamespaceAware(true);
        DocumentBuilder docBuilder = BUILDER_FACTORY.newDocumentBuilder();
        return docBuilder.parse(in);
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
     * Makes dummy collection.
     * @param user The user.
     * @return The collection item.
     */
    public CollectionItem makeDummyCollection(User user) {
        String serial = Integer.toString(++lseq);
        String name = "test collection " + serial;

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);
       
        return collection;
    }
    
    /**
     * Makes dummy calendar collection.
     * @param user The user.
     * @return The collection item.
     */
    public CollectionItem makeDummyCalendarCollection(User user) {
        return makeDummyCalendarCollection(user, null);
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
    
    /**
     * Gets bytes.
     * @param name The name.
     * @return The bytes.
     * @throws IOException - if something is wrong this exception is thrown.
     */
    public byte[] getBytes(String name) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        return bos.toByteArray();
    }

    /**
     * Gets entity factory.
     * @return The entity factory.
     */
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

}
