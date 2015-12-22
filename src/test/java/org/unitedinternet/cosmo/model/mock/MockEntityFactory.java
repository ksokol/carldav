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
import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;
import org.unitedinternet.cosmo.model.hibernate.QName;
import org.unitedinternet.cosmo.model.hibernate.TriageStatus;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

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
        return new HibNoteItem();
    }

    /**
     * Creates calendar collection stamp.
     * {@inheritDoc}
     * @param col The collecton item.
     * @return calendar collection stamp.
     */
    public CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col) {
        return new HibCalendarCollectionStamp(col);
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
        return new QName(namespace, localname);
    }

    /**
     * Creates triage status.
     * {@inheritDoc}
     * @return The triage status.
     */
    public TriageStatus createTriageStatus() {
        return new TriageStatus();
    }

    /**
     * Creates user.
     * {@inheritDoc}
     * @return The user.
     */
    public User createUser() {
        return new User();
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
