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
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.QName;

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

    public EventStamp createJournalStamp(NoteItem note) {
        return new HibJournalStamp(note);
    }

    public QName createQName(String namespace, String localname) {
        return new HibQName(namespace, localname);
    }

    public String generateUid() {
        return idGenerator.nextStringIdentifier();
    }

}
