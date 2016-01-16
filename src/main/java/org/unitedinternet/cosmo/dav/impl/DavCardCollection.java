package org.unitedinternet.cosmo.dav.impl;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.SUPPORTEDADDRESSDATA;

import carldav.card.CardQueryProcessor;
import carldav.service.generator.IdGenerator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedAddressData;
import org.unitedinternet.cosmo.dav.caldav.report.AddressbookMultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.AddressbookQueryReport;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @author Kamill Sokol
 */
public class DavCardCollection extends DavCollectionBase {

    private final CardQueryProcessor cardQueryProcessor;

    public DavCardCollection(final CollectionItem collection, final DavResourceLocator locator, final DavResourceFactory factory,
            final IdGenerator idGenerator, final CardQueryProcessor cardQueryProcessor) throws CosmoDavException {
        super(collection, locator, factory, idGenerator);
        registerLiveProperty(SUPPORTEDADDRESSDATA);

        this.cardQueryProcessor = cardQueryProcessor;

        reportTypes.add(AddressbookMultigetReport.REPORT_TYPE_CARDDAV_MULTIGET);
        reportTypes.add(AddressbookQueryReport.REPORT_TYPE_CARDDAV_QUERY);
    }

    @Override
    protected Set<QName> getResourceTypes() {
        final Set<QName> resourceTypes = super.getResourceTypes();
        resourceTypes.add(RESOURCE_TYPE_ADDRESSBOOK);
        return resourceTypes;
    }

    public Set<DavContentBase> findMembers(AddressbookFilter filter) throws CosmoDavException {
        Set<DavContentBase> members = new HashSet<>();

        CollectionItem collection = (CollectionItem) getItem();
        for (Item memberItem : cardQueryProcessor.filterQuery(collection, filter)) {
            WebDavResource resource = memberToResource(memberItem);
            if (resource != null) {
                members.add((DavContentBase) resource);
            }
        }

        return members;
    }

    @Override
    protected void loadLiveProperties(final DavPropertySet properties) {
        super.loadLiveProperties(properties);
        properties.add(new SupportedAddressData());
    }

    @Override
    protected void setLiveProperty(final WebDavProperty property, final boolean create) throws CosmoDavException {
        super.setLiveProperty(property, create);
        final DavPropertyName name = property.getName();
        if(!(create && name.equals(SUPPORTEDADDRESSDATA))) {
            throw new ProtectedPropertyModificationException(name);
        }
    }

    @Override
    protected void removeLiveProperty(final DavPropertyName name) throws CosmoDavException {
        super.removeLiveProperty(name);
        if (name.equals(SUPPORTEDADDRESSDATA)) {
            throw new ProtectedPropertyModificationException(name);
        }
        getProperties().remove(name);
    }
}
