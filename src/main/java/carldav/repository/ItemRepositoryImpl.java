package carldav.repository;

import carldav.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;

import java.util.List;

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
    public List<Item> findCalendarItems(CalendarFilter filter) {
        return itemFilterProcessor.processFilter(filter);
    }
}
