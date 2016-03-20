package carldav.repository;

import carldav.entity.Item;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
interface ItemRepositoryCustom {

    Set<Item> findCalendarItems(CalendarFilter filter);
}
