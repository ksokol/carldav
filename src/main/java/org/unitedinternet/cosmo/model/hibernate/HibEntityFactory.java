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
package org.unitedinternet.cosmo.model.hibernate;

import carldav.service.generator.IdGenerator;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.model.AvailabilityItem;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.TaskStamp;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.XmlAttribute;
import org.w3c.dom.Element;

/**
 * EntityFactory implementation that uses Hibernate 
 * persistent objects.
 */
public class HibEntityFactory implements EntityFactory {

    private final IdGenerator idGenerator;

    public HibEntityFactory(final IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "idGenerator is null");
        this.idGenerator = idGenerator;
    }

    public CollectionItem createCollection() {
        return new HibCollectionItem();
    }

    public NoteItem createNote() {
        return new HibNoteItem();
    }

    public AvailabilityItem createAvailability() {
        return new HibAvailabilityItem();
    }

    public CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col) {
        return new HibCalendarCollectionStamp(col);
    }

    public EventExceptionStamp createEventExceptionStamp(NoteItem note) {
        return new HibEventExceptionStamp(note);
    }

    public EventStamp createEventStamp(NoteItem note) {
        return new HibEventStamp(note);
    }

    public FileItem createFileItem() {
        return new HibFileItem();
    }

    public FreeBusyItem createFreeBusy() {
        return new HibFreeBusyItem();
    }

    public XmlAttribute createXMLAttribute(QName qname, Element e) {
        return new HibXmlAttribute(qname, e);
    }

    public QName createQName(String namespace, String localname) {
        return new HibQName(namespace, localname);
    }

    public TaskStamp createTaskStamp() {
        return new HibTaskStamp();
    }

    public TriageStatus createTriageStatus() {
        return new HibTriageStatus();
    }

    public User createUser() {
        return new HibUser();
    }

    public String generateUid() {
        return idGenerator.nextStringIdentifier();
    }

}
