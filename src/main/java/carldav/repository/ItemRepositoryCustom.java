package carldav.repository;

import carldav.entity.Item;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;

import java.util.List;

/**
 * @author Kamill Sokol
 */
interface ItemRepositoryCustom {

    List<Item> findCalendarItems(CalendarFilter filter);
}
