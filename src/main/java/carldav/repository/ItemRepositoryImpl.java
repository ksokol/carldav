package carldav.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import carldav.entity.Item;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final ItemFilterProcessor itemFilterProcessor;

    @Autowired
    public ItemRepositoryImpl(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }

    @Override
    public Set<Item> findCalendarItems(ItemFilter itemFilter) {
        return itemFilterProcessor.processFilter(itemFilter);
    }
}