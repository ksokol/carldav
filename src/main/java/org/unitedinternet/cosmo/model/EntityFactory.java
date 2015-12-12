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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;

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
     * Create new CollectionSubscription
     * @return new CollectionSubscription
     */
    CollectionSubscription createCollectionSubscription();
    
    
    /**
     * Create new AvailabilityItem
     * @return new AvailabilityItem
     */
    AvailabilityItem createAvailability();
    
    
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
     * Create new FileItem
     * @return new FileItem
     */
    FileItem createFileItem();
    
    
    /**
     * Create new FreeBusyItem
     * @return new FreeBusyItem
     */
    FreeBusyItem createFreeBusy();
    
    
    /**
     * Create new QName
     * @param namespace 
     * @param localname
     * @return new QName
     */
    QName createQName(String namespace, String localname);
    
    
    /**
     * Create new TaskStamp
     * @return new TaskStamp
     */
    TaskStamp createTaskStamp();


    /**
     * Create new Preference
     * @return new Preference
     */
    Preference createPreference();
    
    
    /**
     * Create new Preference with specified key and value
     * @param key
     * @param value
     * @return new Preference
     */
    Preference createPreference(String key, String value);
    
    
    /**
     * Create new BinaryAttribute using InpuStream.
     * @param qname QName of attribute
     * @param is data
     * @return new BinaryAttribute
     */
    BinaryAttribute createBinaryAttribute(QName qname, InputStream is);
    
    /**
     * Create new BinaryAttribute using byte array.
     * @param qname QName of attribute
     * @param is data
     * @return new BinaryAttribute
     */
    BinaryAttribute createBinaryAttribute(QName qname, byte[] bytes);
    
    /**
     * Create new TextAttribute using Reader.
     * @param qname QName of attribute
     * @param reader text value
     * @return new TextAttribute
     */
    TextAttribute createTextAttribute(QName qname, Reader reader);
    
    
    /**
     * Create new DecimalAttribute using BigDecimal
     * @param qname QName of attribute
     * @param bd decimal value
     * @return new DecimalAttribute
     */
    DecimalAttribute createDecimalAttribute(QName qname, BigDecimal bd);
    
    
    /**
     * Create new CalendarAttribute using Calendar
     * @param qname QName of attribute
     * @param cal calendar value
     * @return new CalendarAttribute
     */
    CalendarAttribute createCalendarAttribute(QName qname, Calendar cal);

    /**
     * Create new StringAttribute using string value
     * @param qname QName of attribute
     * @param str string value
     * @return new StringAttribute
     */
    StringAttribute createStringAttribute(QName qname, String str);
    
    /**
     * Create new XMLAttribute using element value
     * @param qname QName of attribute
     * @param element element value
     * @return new XMLAttribute
     */
    XmlAttribute createXMLAttribute(QName qname, Element e);
}
