package carldav.card;

import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import carldav.entity.CollectionItem;
import carldav.entity.Item;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CardQueryProcessor {

    List<Item> filterQuery(CollectionItem collection, AddressbookFilter filter);
}
