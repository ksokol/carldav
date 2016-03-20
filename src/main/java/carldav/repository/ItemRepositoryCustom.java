package carldav.repository;

import org.unitedinternet.cosmo.model.filter.ItemFilter;
import carldav.entity.Item;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
interface ItemRepositoryCustom {

    Set<Item> findCalendarItems(ItemFilter itemFilter);
}
