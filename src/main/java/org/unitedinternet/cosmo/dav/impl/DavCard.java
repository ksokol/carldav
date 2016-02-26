/*
 * Copyright 2006 Open Source Applications Foundation
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

import carldav.jackrabbit.webdav.io.DavInputContext;
import carldav.jackrabbit.webdav.property.CustomDavPropertySet;
import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.model.hibernate.HibCardItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.LAST_MODIFIED;
import static org.unitedinternet.cosmo.icalendar.ICalendarConstants.CARD_MEDIA_TYPE;

public class DavCard extends DavItemResourceBase {

    public DavCard(HibCardItem item, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(item, locator, factory);
    }

    public DavCard(DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        this(new HibCardItem(), locator, factory);
    }

    public void writeHead(final HttpServletResponse response) throws IOException {
        HibICalendarItem content = (HibICalendarItem) getItem();
        final byte[] calendar = content.getCalendar().getBytes(StandardCharsets.UTF_8);

        response.setContentType(CARD_MEDIA_TYPE);
        response.setContentLength(calendar.length);
        if (getModificationTime() >= 0) {
            response.addDateHeader(LAST_MODIFIED, getModificationTime());
        }
        if (getETag() != null) {
            response.setHeader(ETAG, getETag());
        }
    }

    public void writeBody(final HttpServletResponse response) throws IOException {
        HibICalendarItem content = (HibICalendarItem) getItem();
        final byte[] calendar = content.getCalendar().getBytes(StandardCharsets.UTF_8);
        IOUtils.copy(new ByteArrayInputStream(calendar), response.getOutputStream());
    }

    protected void populateItem(DavInputContext inputContext) throws CosmoDavException {
        super.populateItem(inputContext);

        HibICalendarItem file = (HibICalendarItem) getItem();
        Scanner scanner = new Scanner(inputContext.getInputStream()).useDelimiter("\\A");
        file.setCalendar(scanner.next());
        converter.convertCard(file);
    }

    protected void loadLiveProperties(CustomDavPropertySet properties) {
        super.loadLiveProperties(properties);
        properties.add(new ContentType(CARD_MEDIA_TYPE, null));
    }
}
