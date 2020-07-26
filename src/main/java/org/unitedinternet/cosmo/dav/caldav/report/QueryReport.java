package org.unitedinternet.cosmo.dav.caldav.report;

import carldav.jackrabbit.webdav.version.report.ReportInfo;
import carldav.jackrabbit.webdav.version.report.ReportType;
import carldav.jackrabbit.webdav.xml.DomUtils;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.UnsupportedCollationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.SupportedCollationException;
import org.unitedinternet.cosmo.dav.caldav.TimeZoneExtractor;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;

import javax.xml.namespace.QName;
import java.text.ParseException;

import static carldav.CarldavConstants.c;
import static carldav.CarldavConstants.caldav;
import static carldav.jackrabbit.webdav.DavConstants.ALLPROP;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_ALL_PROP;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_BY_PROPERTY;
import static carldav.jackrabbit.webdav.DavConstants.PROPFIND_PROPERTY_NAMES;
import static carldav.jackrabbit.webdav.DavConstants.PROPNAME;
import static org.unitedinternet.cosmo.dav.ExtendedDavConstants.XML_PROP;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CALDAV_CALENDAR_QUERY;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CALDAV_FILTER;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.ELEMENT_CALDAV_TIMEZONE;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.NS_CALDAV;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.PRE_CALDAV;

/**
 * <p>
 * Represents the <code>CALDAV:calendar-query</code> report that
 * provides a mechanism for finding calendar resources matching
 * specified criteria.
 * </p>
 */
public class QueryReport extends CaldavMultiStatusReport {

    public static final ReportType REPORT_TYPE_CALDAV_QUERY =
            ReportType.register(new QName(NS_CALDAV, ELEMENT_CALDAV_CALENDAR_QUERY, PRE_CALDAV), QueryReport.class);

    private CalendarFilter queryFilter;

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_QUERY;
    }

    protected void parseReport(ReportInfo info) {
        if (!getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " + getType());
        }

        setPropFindProps(info.getPropertyNameSet());
        if (info.containsContentElement(ALLPROP)) {
            setPropFindType(PROPFIND_ALL_PROP);
        } else if (info.containsContentElement(PROPNAME)) {
            setPropFindType(PROPFIND_PROPERTY_NAMES);
        } else {
            setPropFindType(PROPFIND_BY_PROPERTY);
            setOutputFilter(findOutputFilter(info));
        }

        VTimeZone tz = findTimeZone(info);
        queryFilter = findQueryFilter(info, tz);
    }

    protected void doQuerySelf(WebDavResource resource) {
        if (resource instanceof DavCalendarResource) {
            var dcr = (DavCalendarResource) resource;
            if (dcr.matches(queryFilter)) {
                getResults().add(dcr);
            }
        }
        // if the resource is a collection, it will not match a calendar
        // query, which only matches calendar resource, so we can ignore it
    }

    protected void doQueryChildren(DavCollection collection) {
        if (collection instanceof DavCalendarCollection) {
            var dcc = (DavCalendarCollection) collection;
            getResults().addAll(dcc.findMembers(queryFilter));
        }
        // if it's a regular collection, there won't be any calendar resources
        // within it to match the query
    }

    private static VTimeZone findTimeZone(ReportInfo info) {
        var propdata = DomUtils.getChildElement(getReportElementFrom(info), caldav(XML_PROP));
        if (propdata == null) {
            return null;
        }

        var tzdata = DomUtils.getChildElement(propdata, c(ELEMENT_CALDAV_TIMEZONE));
        if (tzdata == null) {
            return null;
        }

        var icaltz = DomUtils.getTextTrim(tzdata);
        if (icaltz == null) {
            throw new UnprocessableEntityException("Expected text content for " + ELEMENT_CALDAV_TIMEZONE);
        }

        return TimeZoneExtractor.extract(icaltz);
    }

    private static CalendarFilter findQueryFilter(ReportInfo info, VTimeZone tz) {
        var filterdata =  DomUtils.getChildElement(getReportElementFrom(info), c(ELEMENT_CALDAV_FILTER));
        if (filterdata == null) {
            return null;
        }

        try {
            var filter = new CalendarFilter(filterdata, tz);
            filter.validate();
            return filter;
        } catch (ParseException e) {
            throw new InvalidFilterException(e);
        } catch (UnsupportedCollationException e) {
            throw new SupportedCollationException();
        }
    }
}
