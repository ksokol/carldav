package org.unitedinternet.cosmo.dao;

import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
public interface ItemDaoCustom {

    Set<HibItem> findCalendarItems(ItemFilter itemFilter);
}
