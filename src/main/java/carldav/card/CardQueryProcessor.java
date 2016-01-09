package carldav.card;

import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
public interface CardQueryProcessor {

    Set<Item> filterQuery(CollectionItem collection, AddressbookFilter filter);
}
