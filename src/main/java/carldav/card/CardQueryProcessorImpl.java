package carldav.card;

import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.model.hibernate.HibCardItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
class CardQueryProcessorImpl implements CardQueryProcessor {

    private final ItemDao itemDao;

    CardQueryProcessorImpl(final ItemDao itemDao) {
        Assert.notNull(itemDao, "itemDao is null");
        this.itemDao = itemDao;
    }

    @Override
    public List<HibCardItem> filterQuery(final HibCollectionItem collection, final AddressbookFilter filter) {
        //TODO filter is not in use yet
        final List cards = itemDao.findCollectionFileItems(collection.getId());
        return (List<HibCardItem>) cards;
    }
}
