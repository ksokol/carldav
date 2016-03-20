package org.unitedinternet.cosmo.dav.impl;

import carldav.jackrabbit.webdav.property.CustomDavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dav.*;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.*;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import carldav.entity.CollectionItem;
import carldav.entity.Item;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

import static carldav.CarldavConstants.*;

public class DavCalendarCollection extends DavCollectionBase implements CaldavConstants, ICalendarConstants {

    private static final Logger LOG = LoggerFactory.getLogger(DavCalendarCollection.class);

    public DavCalendarCollection(CollectionItem collection, DavResourceLocator locator, DavResourceFactory factory) throws CosmoDavException {
        super(collection, locator, factory);

        registerLiveProperty(SUPPORTED_CALENDAR_COMPONENT_SET);
        registerLiveProperty(SUPPORTED_CALENDAR_DATA);
        registerLiveProperty(GET_CTAG);

        reportTypes.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        reportTypes.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
    }

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PUT, DELETE, REPORT";
    }

    public Set<DavCalendarResource> findMembers(CalendarFilter filter) throws CosmoDavException {
        Set<DavCalendarResource> members = new HashSet<>();

        CollectionItem collection = getItem();
        filter.setParent(collection.getId());

        for (Item memberItem : getCalendarQueryProcesor().filterQuery(filter)) {
            WebDavResource resource = memberToResource(memberItem);
            members.add((DavCalendarResource) resource);
        }

        return members;
    }

    protected Set<QName> getResourceTypes() {
        Set<QName> rt = super.getResourceTypes();
        rt.add(RESOURCE_TYPE_CALENDAR);
        return rt;
    }

    protected void loadLiveProperties(CustomDavPropertySet properties) {
        super.loadLiveProperties(properties);

        properties.add(new GetCTag(ETagUtil.createETag(getItem().getId(), getItem().getModifiedDate())));
        properties.add(new SupportedCalendarComponentSet());
        properties.add(new SupportedCollationSet());
        properties.add(new SupportedCalendarData());
        properties.add(new AddressbookHomeSet(getResourceLocator(), getSecurityManager().getSecurityContext().getUser()));
        properties.add(new DisplayName(getItem().getDisplayName()));
    }

    protected void saveContent(DavItemResource member) throws CosmoDavException {
        Item content = member.getItem();
        final Item converted = converter.convert(content);

        if (content.getId() != null) {
            LOG.debug("updating {} {} ", content.getMimetype(), member.getResourcePath());
            getContentService().updateContent(converted);
        } else {
            LOG.debug("creating {} {}", content.getMimetype(), member.getResourcePath());
            getContentService().createContent(getItem(), converted);
        }

        member.setItem(content);
    }
}
