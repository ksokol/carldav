package carldav.card;

import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCardItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CardQueryProcessor {

    List<HibCardItem> filterQuery(HibCollectionItem collection, AddressbookFilter filter);
}
