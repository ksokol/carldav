package org.unitedinternet.cosmo.dav.impl;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.caldav.report.AddressbookMultigetReport;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;

import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @author Kamill Sokol
 */
public class DavCardCollection extends DavCollectionBase {

    public DavCardCollection(final CollectionItem collection, final DavResourceLocator locator, final DavResourceFactory factory, final EntityFactory entityFactory) throws CosmoDavException {
        super(collection, locator, factory, entityFactory);

        REPORT_TYPES.add(AddressbookMultigetReport.REPORT_TYPE_CARDDAV_MULTIGET);
    }

    @Override
    protected Set<QName> getResourceTypes() {
        final Set<QName> resourceTypes = super.getResourceTypes();
        resourceTypes.add(RESOURCE_TYPE_ADDRESSBOOK);
        return resourceTypes;
    }
}
