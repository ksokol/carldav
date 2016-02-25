package org.unitedinternet.cosmo.dav.impl;

import static carldav.CarldavConstants.SUPPORTED_ADDRESS_DATA;
import static carldav.CarldavConstants.carddav;

import carldav.card.CardQueryProcessor;
import carldav.jackrabbit.webdav.CustomDavPropertySet;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedAddressData;
import org.unitedinternet.cosmo.dav.caldav.report.AddressbookMultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.AddressbookQueryReport;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @author Kamill Sokol
 */
public class DavCardCollection extends DavCollectionBase {

    private final CardQueryProcessor cardQueryProcessor;

    public DavCardCollection(final HibCollectionItem collection, final DavResourceLocator locator, final DavResourceFactory factory,
            final CardQueryProcessor cardQueryProcessor) throws CosmoDavException {
        super(collection, locator, factory);
        registerLiveProperty(SUPPORTED_ADDRESS_DATA);

        this.cardQueryProcessor = cardQueryProcessor;

        reportTypes.add(AddressbookMultigetReport.REPORT_TYPE_CARDDAV_MULTIGET);
        reportTypes.add(AddressbookQueryReport.REPORT_TYPE_CARDDAV_QUERY);
    }

    @Override
    protected Set<QName> getResourceTypes() {
        final Set<QName> resourceTypes = super.getResourceTypes();
        resourceTypes.add(carddav(ADDRESSBOOK));
        return resourceTypes;
    }

    public Set<DavItemResourceBase> findMembers(AddressbookFilter filter) throws CosmoDavException {
        Set<DavItemResourceBase> members = new HashSet<>();

        HibCollectionItem collection = getItem();
        for (HibItem memberHibItem : cardQueryProcessor.filterQuery(collection, filter)) {
            WebDavResource resource = memberToResource(memberHibItem);
            if (resource != null) {
                members.add((DavItemResourceBase) resource);
            }
        }

        return members;
    }

    @Override
    protected void loadLiveProperties(final CustomDavPropertySet properties) {
        super.loadLiveProperties(properties);
        properties.add(new SupportedAddressData());
    }
}
