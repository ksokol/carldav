package org.unitedinternet.cosmo.dav.impl;

import carldav.card.CardQueryProcessor;
import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedAddressData;
import org.unitedinternet.cosmo.dav.caldav.report.AddressbookMultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.AddressbookQueryReport;
import org.unitedinternet.cosmo.dav.property.CurrentUserPrincipal;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

import static carldav.CarldavConstants.CURRENT_USER_PRINCIPAL;
import static carldav.CarldavConstants.SUPPORTED_ADDRESS_DATA;
import static carldav.CarldavConstants.carddav;

/**
 * @author Kamill Sokol
 */
public class DavCardCollection extends DavCollectionBase {

    private final CardQueryProcessor cardQueryProcessor;

    public DavCardCollection(
            CollectionItem collection,
            DavResourceLocator locator,
            DavResourceFactory factory,
            CardQueryProcessor cardQueryProcessor) {
        super(collection, locator, factory);
        registerLiveProperty(SUPPORTED_ADDRESS_DATA);
        registerLiveProperty(CURRENT_USER_PRINCIPAL);

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

    public Set<DavItemResourceBase> findMembers(AddressbookFilter filter) {
        Set<DavItemResourceBase> members = new HashSet<>();

        CollectionItem collection = getItem();
        for (Item memberItem : cardQueryProcessor.filterQuery(collection, filter)) {
            WebDavResource resource = memberToResource(memberItem);
            members.add((DavItemResourceBase) resource);
        }

        return members;
    }

    @Override
    protected void loadLiveProperties(final DavPropertySet properties) {
        super.loadLiveProperties(properties);
        properties.add(new SupportedAddressData());
        properties.add(new CurrentUserPrincipal(getResourceLocator(), getUsername()));
    }
}
