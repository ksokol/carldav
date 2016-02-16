/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.io;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.io.InputContextImpl;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarDataException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.unitedinternet.cosmo.dav.caldav.UnsupportedCalendarDataException;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * An <code>InputContext</code> that supports the semantics of DAV extensions like CalDAV.
 *
 * @see org.apache.jackrabbit.webdav.io.InputContext
 */
public class DavInputContext extends InputContextImpl implements CaldavConstants {

    private Calendar calendar;

    public DavInputContext(HttpServletRequest request, InputStream in) {
        super(request, in);
    }

    @Deprecated
    public Calendar getCalendar() throws CosmoDavException {
        return getCalendar(false);
    }

    public String getCalendarString() throws CosmoDavException {
        return getCalendar().toString();
    }

    /**
     * Parses the input stream into a calendar object.
     * 
     * @param allowCalendarWithMethod
     *            don't break on Calendars with METHOD property
     * @return Calendar parsed
     * @throws CosmoDavException
     *             - if something is wrong this exception is thrown.
     */
    public Calendar getCalendar(boolean allowCalendarWithMethod) throws CosmoDavException {
        if (calendar != null) {
            return calendar;
        }

        if (!hasStream()) {
            return null;
        }

        if (getContentType() == null) {
            throw new BadRequestException("No media type specified");
        }
        String mediaType = IOUtil.getMimeType(getContentType());
        if (!IOUtil.getMimeType(mediaType).equals(CT_ICALENDAR) && !IOUtil.getMimeType(mediaType).equals(ICalendarConstants.CARD_MEDIA_TYPE)) {
            throw new UnsupportedCalendarDataException(mediaType);
        }

        try {
            Calendar c = CalendarUtils.parseCalendar(getInputStream());
            c.validate(true);

            if (CalendarUtils.hasMultipleComponentTypes(c)) {
                throw new InvalidCalendarResourceException("Calendar object contains more than one type of component");
            }
            if (!allowCalendarWithMethod && c.getProperties().getProperty(Property.METHOD) != null) {
                throw new InvalidCalendarResourceException("Calendar object contains METHOD property");
            }

            calendar = c;
        } catch (IOException e) {
            throw new CosmoDavException(e);
        } catch (ParserException e) {
            throw new InvalidCalendarDataException("Failed to parse calendar object: " + e.getMessage());
        } catch (ValidationException e) {
            throw new InvalidCalendarDataException("Invalid calendar object: " + e.getMessage());
        }

        return calendar;
    }
}
