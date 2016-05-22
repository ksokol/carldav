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

import carldav.entity.Item;
import carldav.jackrabbit.webdav.io.DavInputContext;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.SupportedCalendarComponentException;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CalendarResourceProvider extends BaseProvider {

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
            final Item item = new Item();
            item.setType(Item.Type.VEVENT);
            return new DavCalendarResource(item, locator, getResourceFactory());
        }
        if (!calendar.getComponents(Component.VTODO).isEmpty()) {
            final Item item = new Item();
            item.setType(Item.Type.VTODO);
            return new DavCalendarResource(item, locator, getResourceFactory());
        }
        if (!calendar.getComponents(Component.VJOURNAL).isEmpty()) {
            final Item item = new Item();
            item.setType(Item.Type.VJOURNAL);
            return new DavCalendarResource(item, locator, getResourceFactory());
        }
        throw new SupportedCalendarComponentException(SUPPORTED_COMPONENT_TYPES);
  }
}
