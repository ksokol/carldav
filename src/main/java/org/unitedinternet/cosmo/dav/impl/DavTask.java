/*
 * Copyright 2007Task Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.impl;

import carldav.service.generator.IdGenerator;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

/**
 * Extends <code>DavCalendarResource</code> to adapt the Cosmo
 * <code>ContentItem</code> with an <code>TaskStamp</code> to 
 * the DAV resource model.
 *
 * This class does not define any live properties.
 *
 * @see DavCalendarResource
 */
public class DavTask extends DavCalendarResource {


    /** */
    public DavTask(DavResourceLocator locator,
                  DavResourceFactory factory,
                  IdGenerator idGenerator)
        throws CosmoDavException {
        this(new HibNoteItem(), locator, factory, idGenerator);
    }

    /** */
    public DavTask(HibNoteItem item,
                   DavResourceLocator locator,
                   DavResourceFactory factory,
                   IdGenerator idGenerator)
        throws CosmoDavException {
        super(item, locator, factory, idGenerator);
    }

    /**
     * <p>
     * Imports a calendar object containing a VTODO. Sets the
     * following properties:
     * </p>
     * <ul>
     * <li>display name: the VTODO's SUMMARY (or the item's name, if the
     * SUMMARY is blank)</li>
     * <li>icalUid: the VTODO's UID</li>
     * <li>body: the VTODO's DESCRIPTION</li>
     * <li>reminderTime: if the VTODO has a DISPLAY VALARM
     *     the reminderTime will be set to the trigger time</li>
     * </ul>
     * @param cal The calendar imported.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    public void setCalendar(Calendar cal)
        throws CosmoDavException {
        HibNoteItem note = (HibNoteItem) getItem();
        
        ComponentList vtodos = cal.getComponents(Component.VTODO);
        if (vtodos.isEmpty()) {
            throw new UnprocessableEntityException("VCALENDAR does not contain any VTODOS");
        }

        EntityConverter converter = new EntityConverter();
        converter.convert(note, cal.toString(), Component.VTODO);
    }
}
