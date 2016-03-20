package carldav.card;

import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import carldav.entity.CollectionItem;
import carldav.entity.HibItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CardQueryProcessor {

    List<HibItem> filterQuery(CollectionItem collection, AddressbookFilter filter);
}
