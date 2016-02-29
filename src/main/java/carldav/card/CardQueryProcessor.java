package carldav.card;

import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CardQueryProcessor {

    List<HibItem> filterQuery(HibCollectionItem collection, AddressbookFilter filter);
}
