package carldav.card;

import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import carldav.entity.HibCollectionItem;
import carldav.entity.HibItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CardQueryProcessor {

    List<HibItem> filterQuery(HibCollectionItem collection, AddressbookFilter filter);
}
