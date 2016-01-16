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
package org.unitedinternet.cosmo.dav.provider;

import carldav.service.generator.IdGenerator;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.caldav.SupportedCalendarComponentException;
import org.unitedinternet.cosmo.dav.impl.DavEvent;
import org.unitedinternet.cosmo.dav.impl.DavJournal;
import org.unitedinternet.cosmo.dav.impl.DavTask;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.model.EntityFactory;

import java.io.IOException;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavCalendarResource</code> resources.
 * </p>
 *
 * @see DavProvider
 */
public class CalendarResourceProvider extends FileProvider {

    public CalendarResourceProvider(DavResourceFactory resourceFactory,
            IdGenerator idGenerator) {
        super(resourceFactory, idGenerator);
    }
    
    // DavProvider methods

    public void put(DavRequest request,
                    DavResponse response,
                    DavContent content)
        throws CosmoDavException, IOException {

        // do content.getParent() check only for normal auth only, ticket auth is on the item only, not its parent.
        if (!content.getParent().exists()) {
            throw new ConflictException("One or more intermediate collections must be created");
        }

        int status = content.exists() ? 204 : 201;
        DavInputContext ctx = (DavInputContext) createInputContext(request);
        if (! content.exists()) {
            content = createCalendarResource(request, response,
                                             content.getResourceLocator(),
                                             ctx.getCalendar());
        }
        
        content.getParent().addContent(content, ctx);

        response.setStatus(status);
        // since the iCalendar body is parsed and re-serialized for storage,
        // it's possible that what will be served for subsequent GETs is
        // slightly different than what was provided in the PUT, so send a
        // weak etag
        response.setHeader("ETag", content.getETag());
    }

    protected DavContent createCalendarResource(DavRequest request,
                                                DavResponse response,
                                                DavResourceLocator locator,
                                                Calendar calendar)
        throws CosmoDavException {
        if (!calendar.getComponents(Component.VEVENT).isEmpty()) {
            return new DavEvent(locator, getResourceFactory(), getIdGenerator());
        }
        if (!calendar.getComponents(Component.VTODO).isEmpty()) {
            return new DavTask(locator, getResourceFactory(), getIdGenerator());
        }
        if (!calendar.getComponents(Component.VJOURNAL).isEmpty()) {
            return new DavJournal(locator, getResourceFactory(), getIdGenerator());
        }
        throw new SupportedCalendarComponentException();
  }
}
