package org.unitedinternet.cosmo.calendar.query.impl;

import carldav.entity.Item;
import carldav.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dao.query.hibernate.CalendarFilterConverter;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import java.util.List;
import java.util.Objects;

public class StandardCalendarQueryProcessor implements CalendarQueryProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(StandardCalendarQueryProcessor.class);

  private static final CalendarFilterConverter filterConverter = new CalendarFilterConverter();
  private static final EntityConverter entityConverter = new EntityConverter();

  private final ItemRepository itemRepository;

  public StandardCalendarQueryProcessor(ItemRepository itemRepository) {
    this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository is null");
  }

  public List<Item> filterQuery(CalendarFilter filter) {
    return itemRepository.findAll(filterConverter.translateToItemFilter(filter));
  }

  public boolean filterQuery(Item item, CalendarFilter filter) {
    LOG.debug("matching item {} to filter {}", item.getUid(), filter);
    var calendar = entityConverter.convertContent(item);
    if (calendar != null) {
      return new CalendarFilterEvaluater().evaluate(calendar, filter);
    }
    return false;
  }
}
