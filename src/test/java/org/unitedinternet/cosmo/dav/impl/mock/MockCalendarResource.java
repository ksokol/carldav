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
package org.unitedinternet.cosmo.dav.impl.mock;

import net.fortuna.ical4j.model.Calendar;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;

/**
 * <p>
 * Mock extension of {@link DavCalendarResource}. Does not persist the
 * backing calendar. Allowing classes using this mock to be tested in isolation.
 */
public class MockCalendarResource extends DavCalendarResource {
    private Calendar calendar;

    /**
     * Constructor.
     * @param item The content item.
     * @param locator The dav resource locator.
     * @param factory The dav resource factory.
     * @param entityFactory The entity factory.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    public MockCalendarResource(ContentItem item,
                                DavResourceLocator locator,
                                DavResourceFactory factory,
                                EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }

    /**
     * Mock calendar resource.
     * @param locator The dav resource locator.
     * @param factory The dav resource factory.
     * @param entityFactory The entity factory.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    public MockCalendarResource(DavResourceLocator locator,
                                DavResourceFactory factory,
                                EntityFactory entityFactory)
        throws CosmoDavException {
        this(entityFactory.createNote(), locator, factory, entityFactory);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
