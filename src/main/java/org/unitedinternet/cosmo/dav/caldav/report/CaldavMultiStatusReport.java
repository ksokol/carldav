package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.jackrabbit.webdav.MultiStatusResponse;
import carldav.jackrabbit.webdav.property.DavPropertyNameSet;
import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.xml.DomUtils;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import org.unitedinternet.cosmo.calendar.data.OutputFilter;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarData;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.report.MultiStatusReport;

import java.io.StringReader;

import static carldav.CarldavConstants.CALENDAR_DATA;
import static carldav.CarldavConstants.c;
import static carldav.CarldavConstants.caldav;
import static carldav.jackrabbit.webdav.DavConstants.XML_PROP;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CALDAV_CALENDAR_DATA;

public abstract class CaldavMultiStatusReport extends MultiStatusReport {

    private OutputFilter outputFilter;

    protected DavPropertyNameSet createResultPropSpec() {
        var spec = super.createResultPropSpec();
        spec.remove(CALENDAR_DATA);
        return spec;
    }

    protected MultiStatusResponse buildMultiStatusResponse(WebDavResource resource, DavPropertyNameSet props) {
        var msr = super.buildMultiStatusResponse(resource, props);

        var dcr = (DavCalendarResource) resource;
        if (getPropFindProps().contains(CALENDAR_DATA)) {
            msr.add(new CalendarData(readCalendarData(dcr)));
        }

        return msr;
    }

    public void setOutputFilter(OutputFilter outputFilter) {
        this.outputFilter = outputFilter;
    }

    protected OutputFilter findOutputFilter(ReportInfo info) {
        var propdata = DomUtils.getChildElement(getReportElementFrom(info), caldav(XML_PROP));
        if (propdata == null) {
            return null;
        }

        var cdata = DomUtils.getChildElement(propdata, c(ELEMENT_CALDAV_CALENDAR_DATA));
        if (cdata == null) {
            return null;
        }

        return new CaldavOutputFilter().createFromXml(cdata);
    }

    private String readCalendarData(DavCalendarResource resource) {
        if (! resource.exists()) {
            return null;
        }
        var calendarString = resource.getCalendar();
        var buffer = new StringBuffer();
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
