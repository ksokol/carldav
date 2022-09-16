package carldav.card;

import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.repository.ItemRepository;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;

import java.util.List;

class CardQueryProcessorImpl implements CardQueryProcessor {

  private final ItemRepository itemRepository;

  CardQueryProcessorImpl(final ItemRepository itemRepository) {
    Assert.notNull(itemRepository, "itemRepository is null");
    this.itemRepository = itemRepository;
  }

  @Override
  public List<Item> filterQuery(final CollectionItem collection, final AddressbookFilter filter) {
    //TODO filter is not in use yet
    return itemRepository.findByCollectionIdAndType(collection.getId(), Item.Type.VCARD.toString());
  }
}
