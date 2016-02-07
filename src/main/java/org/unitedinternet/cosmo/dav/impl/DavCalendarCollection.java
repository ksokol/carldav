package org.unitedinternet.cosmo.dav.impl;

import carldav.service.generator.IdGenerator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.dav.caldav.property.AddressbookHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.GetCTag;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCalendarData;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCollationSet;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibJournalItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

public class DavCalendarCollection extends DavCollectionBase implements CaldavConstants, ICalendarConstants {

    private static final Logger LOG =  LoggerFactory.getLogger(DavCalendarCollection.class);

    private final Set<String> deadPropertyFilter = new HashSet<>(10);

    public DavCalendarCollection(HibCollectionItem collection,
                                 DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 IdGenerator idGenerator)
        throws CosmoDavException {
        super(collection, locator, factory, idGenerator);

        registerLiveProperty(SUPPORTEDCALENDARCOMPONENTSET);
        registerLiveProperty(SUPPORTEDCALENDARDATA);
        registerLiveProperty(GET_CTAG);

        reportTypes.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        reportTypes.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
    }

    /** */
    public String getSupportedMethods() {
        // calendar collections not allowed inside calendar collections
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PUT, DELETE, REPORT";
    }

    public boolean isCalendarCollection() {
        return true;
    }

    // our methods

    /**
     * Returns the member resources in this calendar collection matching
     * the given filter.
     */
    public Set<DavCalendarResource> findMembers(CalendarFilter filter)
        throws CosmoDavException {
        Set<DavCalendarResource> members =
            new HashSet<>();

        HibCollectionItem collection = (HibCollectionItem) getItem();
        for (HibItem memberItem :
             getCalendarQueryProcesor().filterQuery(collection, filter)) {
            WebDavResource resource = memberToResource(memberItem);
            if(resource!=null) {
                members.add((DavCalendarResource) resource);
            }
        }

        return members;
    }

    protected Set<QName> getResourceTypes() {
        Set<QName> rt = super.getResourceTypes();
        rt.add(RESOURCE_TYPE_CALENDAR);
        return rt;
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        // add CS:getctag property, which is the collection's entitytag
        // if it exists
        HibItem hibItem = getItem();
        if(hibItem !=null && hibItem.getEntityTag()!=null) {
            properties.add(new GetCTag(hibItem.getEntityTag()));
        }

        properties.add(new SupportedCalendarComponentSet());
        properties.add(new SupportedCollationSet());
        properties.add(new SupportedCalendarData());
        properties.add(new AddressbookHomeSet(getResourceLocator(), getSecurityManager().getSecurityContext().getUser()));

        if(getItem().getDisplayName() != null){
            properties.add(new DisplayName(getItem().getDisplayName()));
        }
    }

    /**
     * The CALDAV:supported-calendar-component-set property is
      used to specify restrictions on the calendar component types that
      calendar object resources may contain in a calendar collection.
      Any attempt by the client to store calendar object resources with
      component types not listed in this property, if it exists, MUST
      result in an error, with the CALDAV:supported-calendar-component
      precondition (Section 5.3.2.1) being violated.  Since this
      property is protected, it cannot be changed by clients using a
      PROPPATCH request.
     * */
    protected void setLiveProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        super.setLiveProperty(property, create);

        DavPropertyName name = property.getName();
        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property " + name + " requires a value");
        }

        if(!(create && name.equals(SUPPORTEDCALENDARCOMPONENTSET)) &&
            (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
                name.equals(SUPPORTEDCALENDARDATA) ||
                name.equals(GET_CTAG))) {
                throw new ProtectedPropertyModificationException(name);
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
        throws CosmoDavException {
        super.removeLiveProperty(name);

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
            name.equals(SUPPORTEDCALENDARDATA) ||
            name.equals(GET_CTAG)) {
            throw new ProtectedPropertyModificationException(name);
        }
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        Set<String> copy = new HashSet<>();
        copy.addAll(super.getDeadPropertyFilter());
        copy.addAll(deadPropertyFilter);
        return copy;
    }

    /** */
    protected void saveContent(DavItemContent member)
        throws CosmoDavException {
        if (! (member instanceof DavCalendarResource)) {
            throw new IllegalArgumentException("member not DavCalendarResource");
        }

        if (member instanceof DavEvent) {
            saveEvent(member);
        } else if(member instanceof DavJournal) {
            saveJournal(member);
        } else {
            try {
                super.saveContent(member);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            }
        }
    }

    private void saveEvent(DavItemContent member)
        throws CosmoDavException {

        HibItem content = member.getItem();
        HibEventStamp event = (HibEventStamp) content.getStamp(HibEventStamp.class);
        EntityConverter converter = new EntityConverter(getIdGenerator());
        Set<HibItem> toUpdate = new LinkedHashSet<>();

        try {
            // convert icalendar representation to cosmo data model
            toUpdate.addAll(converter.convertEventCalendar(
                    (HibNoteItem) content, event.getEventCalendar()));
        } catch (ModelValidationException e) {
            throw new InvalidCalendarResourceException(e.getMessage());
        }

        if (event.getId()!= null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("updating event " + member.getResourcePath());
            }

            try {
                getContentService().updateContentItems(Collections.singleton(content.getCollection()), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating event " + member.getResourcePath());
            }

            try {
                getContentService().createContentItems(
                        (HibCollectionItem) getItem(), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        }

        member.setItem(content);
    }

    private void saveJournal(DavItemContent member) throws CosmoDavException {
        HibJournalItem content = (HibJournalItem) member.getItem();
        EntityConverter converter = new EntityConverter(getIdGenerator());
        Set<HibItem> toUpdate = new LinkedHashSet<>();

        try {
            // convert icalendar representation to cosmo data model
            toUpdate.add(converter.convertJournalCalendar(content, content.getCalendar()));
        } catch (ModelValidationException e) {
            throw new InvalidCalendarResourceException(e.getMessage());
        }

        if (content.getId()!= null) {
            LOG.debug("updating journal {}", member.getResourcePath());

            try {
                getContentService().updateContentItems(Collections.singleton(content.getCollection()), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        } else {
            LOG.debug("creating journal {}", member.getResourcePath());

            try {
                getContentService().createContentItems((HibCollectionItem) getItem(), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        }

        member.setItem(content);
    }
}
