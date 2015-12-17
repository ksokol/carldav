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
package org.unitedinternet.cosmo.model;

import org.w3c.dom.Element;

/**
 * Factory api for creating model objects.
 */
public interface EntityFactory {
    
    /**
     * Generate a unique identifier that can be used as
     * the uid of an entity.
     * @return unique identifier
     */
    String generateUid();

    
    /**
     * Create new CollectionItem
     * @return new ColletionItem
     */
    CollectionItem createCollection();
    
    
    /**
     * Create new NoteItem
     * @return new NoteItem
     */
    NoteItem createNote();

    
    /**
     * Create new User
     * @return new User
     */
    User createUser();

    
    /**
     * Create new CalendarCollectionStamp
     * @param col associated CollectionItem
     * @return new CalendarCollectionStamp
     */
    CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col);
    
    
    /**
     * Create new TriageStatus
     * @return new TriageStatus
     */
    TriageStatus createTriageStatus();
    
    
    /**
     * Create new EventStamp
     * @param note associated NoteItem
     * @return new EventStamp
     */
    EventStamp createEventStamp(NoteItem note);
    
    
    /**
     * Create new EventExceptionStamp
     * @param note associated NoteItem
     * @return new EventExceptionStamp
     */
    EventExceptionStamp createEventExceptionStamp(NoteItem note);

    /**
     * Create new QName
     * @param namespace 
     * @param localname
     * @return new QName
     */
    QName createQName(String namespace, String localname);

    /**
     * Create new XMLAttribute using element value
     * @param qname QName of attribute
     * @param element element value
     * @return new XMLAttribute
     */
    XmlAttribute createXMLAttribute(QName qname, Element e);
}
