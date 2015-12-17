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
package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.TaskStamp;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.XmlAttribute;
import org.unitedinternet.cosmo.util.VersionFourGenerator;
import org.w3c.dom.Element;

/**
 * EntityFactory implementation that uses mock objects.
 */
public class MockEntityFactory implements EntityFactory {

    private VersionFourGenerator idGenerator = new VersionFourGenerator();
    
    /**
     * Creates collection.
     * {@inheritDoc}
     * @return collection item.
     */
    public CollectionItem createCollection() {
        return new MockCollectionItem();
    }
    
    /**
     * Creates note.
     * {@inheritDoc}
     * @return note item.
     */
    public NoteItem createNote() {
        return new MockNoteItem();
    }

    /**
     * Creates calendar collection stamp.
     * {@inheritDoc}
     * @param col The collecton item.
     * @return calendar collection stamp.
     */
    public CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col) {
        return new MockCalendarCollectionStamp(col);
    }

    /**
     * Creates xml attribute.
     * @param qname The name.
     * @param e The element.
     * @return xml attribute.
     * {@inheritDoc}
     */
    public XmlAttribute createXMLAttribute(QName qname, Element e) {
        return new MockXmlAttribute(qname, e);
    }

    /**
     * Creates event exception stamp.
     * @param note The note item.
     * {@inheritDoc}
     * @return The event exception stamp.
     */
    public EventExceptionStamp createEventExceptionStamp(NoteItem note) {
        return new MockEventExceptionStamp(note);
    }

    /**
     * Creates event stamp.
     * {@inheritDoc}
     * @param note The note item.
     * @return The event stamp.
     */
    public EventStamp createEventStamp(NoteItem note) {
        return new MockEventStamp(note);
    }

    /**
     * Creates QName.
     * @param namespace The namespace.
     * @param localname The local name.
     * @return The QName.
     * {@inheritDoc}
     */
    public QName createQName(String namespace, String localname) {
        return new MockQName(namespace, localname);
    }

    /**
     * Creates task stamp.
     * {@inheritDoc}
     * @return The task stamp.
     */
    public TaskStamp createTaskStamp() {
        return new MockTaskStamp();
    }

    /**
     * Creates triage status.
     * {@inheritDoc}
     * @return The triage status.
     */
    public TriageStatus createTriageStatus() {
        return new MockTriageStatus();
    }

    /**
     * Creates user.
     * {@inheritDoc}
     * @return The user.
     */
    public User createUser() {
        return new MockUser();
    }

    /**
     * generates uid.
     * {@inheritDoc}
     * @return The id generated.
     */
    public String generateUid() {
        return idGenerator.nextStringIdentifier();
    }

}
