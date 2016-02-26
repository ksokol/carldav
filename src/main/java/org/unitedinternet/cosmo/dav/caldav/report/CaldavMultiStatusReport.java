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
package org.unitedinternet.cosmo.dav.caldav.report;

import static carldav.CarldavConstants.CALENDAR_DATA;
import static carldav.CarldavConstants.c;
import static carldav.CarldavConstants.caldav;

import carldav.jackrabbit.webdav.property.CustomDavPropertyNameSet;
import carldav.jackrabbit.webdav.xml.CustomDomUtils;
import carldav.jackrabbit.webdav.CustomMultiStatusResponse;
import carldav.jackrabbit.webdav.version.report.CustomReportInfo;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarData;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.report.MultiStatusReport;
import org.w3c.dom.Element;

import java.io.StringReader;

/**
 * <p>
 * Extends <code>MultiStatusReport</code> to handle CalDAV report features.
 * </p>
 * <p>
 * A report request may contain the pseudo-property
 * <code>CALDAV:calendar-data</code>. If so, the calendar data for the resource
 * is to be returned in the response as the value of a property of the same
 * name. The value for the request property may specify
 * an <em>output filter</em> that restricts the components, properties and
 * parameters included in the calendar data in the response. Subclasses are
 * responsible for setting the output filter when parsing the report info.
 * </p>
 */
public abstract class CaldavMultiStatusReport extends MultiStatusReport implements CaldavConstants {

    private OutputFilter outputFilter;

    /**
     * Removes <code>CALDAV:calendar-data</code> from the property spec
     * since it doesn't represent a real property.
     */
    protected CustomDavPropertyNameSet createResultPropSpec() {
        CustomDavPropertyNameSet spec = super.createResultPropSpec();
        spec.remove(CALENDAR_DATA);
        return spec;
    }

    /**
     * Includes the resource's calendar data in the response as the
     * <code>CALDAV:calendar-data</code> property if it was requested. The
     * calendar data is filtered if a filter was included in the request.
     */
    protected CustomMultiStatusResponse buildMultiStatusResponse(WebDavResource resource, CustomDavPropertyNameSet props) {
        CustomMultiStatusResponse msr = super.buildMultiStatusResponse(resource, props);

        DavCalendarResource dcr = (DavCalendarResource) resource;
        if (getPropFindProps().contains(CALENDAR_DATA)) {
            msr.add(new CalendarData(readCalendarData(dcr)));
        }

        return msr;
    }

    public void setOutputFilter(OutputFilter outputFilter) {
        this.outputFilter = outputFilter;
    }

    protected OutputFilter findOutputFilter(CustomReportInfo info) throws CosmoDavException {
        Element propdata = CustomDomUtils.getChildElement(getReportElementFrom(info), caldav(XML_PROP));
        if (propdata == null) {
            return null;
        }

        Element cdata = CustomDomUtils.getChildElement(propdata, c(ELEMENT_CALDAV_CALENDAR_DATA));
        if (cdata == null) {
            return null;
        }

        return new CaldavOutputFilter().createFromXml(cdata);
    }

    private String readCalendarData(DavCalendarResource resource)
        throws CosmoDavException {
        if (! resource.exists()) {
            return null;
        }
        final String calendarString = resource.getCalendar();
        StringBuffer buffer = new StringBuffer();
        if (outputFilter != null) {
            try {
                final Calendar calendar = new CalendarBuilder().build(new StringReader(calendarString));
                outputFilter.filter(calendar, buffer);
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
        }
        else {
            buffer.append(calendarString);
        }
        return buffer.toString();
    }
}
