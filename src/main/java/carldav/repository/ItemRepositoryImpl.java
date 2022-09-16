package carldav.repository;

import carldav.entity.Item;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ItemRepositoryImpl implements ItemRepositoryCustom {

  private final NamedParameterJdbcOperations jdbcTemplate;
  private final JdbcAggregateOperations template;

  public ItemRepositoryImpl(NamedParameterJdbcOperations jdbcTemplate, JdbcAggregateOperations template) {
    this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate is null");
    this.template = Objects.requireNonNull(template, "template is null");
  }

  @Override
  public List<Item> findAll(ItemQuery query) {
    var ids = jdbcTemplate.queryForList(query.getSql(), query.getParams(), Long.class);
    var resultList = new ArrayList<Item>();
    template.findAllById(ids, Item.class).iterator().forEachRemaining(resultList::add);
    return resultList;
  }
}
