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

import carldav.jackrabbit.webdav.io.DavInputContext;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import org.unitedinternet.cosmo.dav.*;
import org.unitedinternet.cosmo.dav.caldav.SupportedCalendarComponentException;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.model.hibernate.HibEventItem;
import org.unitedinternet.cosmo.model.hibernate.HibJournalItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalendarResourceProvider extends FileProvider {

    private static final List<String> SUPPORTED_COMPONENT_TYPES = new ArrayList<String>() {{
        add(Component.VEVENT);
        add(Component.VTODO);
        add(Component.VJOURNAL);
    }};

    public CalendarResourceProvider(DavResourceFactory resourceFactory) {
        super(resourceFactory);
    }

    public void put(HttpServletRequest request,
                    HttpServletResponse response,
                    WebDavResource content)
        throws CosmoDavException, IOException {

        // do content.getCollection() check only for normal auth only, ticket auth is on the item only, not its parent.
        if (!content.getParent().exists()) {
            throw new ConflictException("One or more intermediate collections must be created");
        }

        int status = content.exists() ? 204 : 201;
        DavInputContext ctx = createInputContext(request);
        if (! content.exists()) {
            content = createCalendarResource(
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

    protected WebDavResource createCalendarResource(DavResourceLocator locator, Calendar calendar) throws CosmoDavException {

        if (!calendar.getComponents(Component.VEVENT).isEmpty()) {
            return new DavCalendarResource(new HibEventItem(), locator, getResourceFactory());
        }
        if (!calendar.getComponents(Component.VTODO).isEmpty()) {
            return new DavCalendarResource(new HibNoteItem(), locator, getResourceFactory());
        }
        if (!calendar.getComponents(Component.VJOURNAL).isEmpty()) {
            return new DavCalendarResource(new HibJournalItem(), locator, getResourceFactory());
        }
        throw new SupportedCalendarComponentException(SUPPORTED_COMPONENT_TYPES);
  }
}
