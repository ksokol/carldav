package carldav.repository;

import org.unitedinternet.cosmo.model.filter.ItemFilter;
import carldav.entity.HibItem;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
interface ItemDaoCustom {

    Set<HibItem> findCalendarItems(ItemFilter itemFilter);
}
