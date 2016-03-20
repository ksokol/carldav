package carldav.card;

import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import carldav.repository.ItemRepository;
import carldav.entity.CollectionItem;
import carldav.entity.HibItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
class CardQueryProcessorImpl implements CardQueryProcessor {

    private final ItemRepository itemRepository;

    CardQueryProcessorImpl(final ItemRepository itemRepository) {
        Assert.notNull(itemRepository, "itemRepository is null");
        this.itemRepository = itemRepository;
    }

    @Override
    public List<HibItem> filterQuery(final CollectionItem collection, final AddressbookFilter filter) {
        //TODO filter is not in use yet
        return itemRepository.findByCollectionIdAndType(collection.getId(), HibItem.Type.VCARD);
    }
}
