package carldav.repository;

import carldav.entity.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public final class ItemQuery {

  private final List<String> predicates = new ArrayList<>();
  private final Map<String, Object> sqlParams = new HashMap<>(5);

  public void add(ItemQuery query) {
    predicates.addAll(query.predicates);
    sqlParams.putAll(query.sqlParams);
  }

  public ItemQuery propertyLike(String property, String value, boolean caseless, boolean negate) {
    var key = property;
    var pattern = "%" + value + "%";

    if (caseless) {
      pattern = pattern.toLowerCase(ENGLISH);
      key = "lower(" + property + ")";
    }

    if (negate) {
      predicates.add(key + " not like :pattern");
      sqlParams.put("pattern", pattern);
      return this;
    }

    predicates.add(key + " like :pattern");
    sqlParams.put("pattern", pattern);

    return this;
  }

  public ItemQuery parent(Long id) {
    predicates.add("collectionid = :id");
    sqlParams.put("id", id);
    return this;
  }

  public ItemQuery stamp(Item.Type type, Boolean recurring, Date start, Date end) {
    if (type != null) {
      predicates.add("type = :type");
      sqlParams.put("type", type.toString());
    }

    if (recurring != null) {
      predicates.add("recurring = :recurring");
      sqlParams.put("recurring", recurring);
    }

    if (start != null && end != null) {
      predicates.add("(startDate < :endDate and endDate > :startDate) or (startDate = endDate and (startDate = :startDate or startDate = :endDate))");
      sqlParams.put("startDate", start);
      sqlParams.put("endDate", end);
    }

    return this;
  }

  public String getSql() {
    var predicatesString = String.join(" and ", predicates);
    return "select id from ITEM" + (predicatesString.isEmpty() ? "" : " where " + predicatesString);
  }

  public Map<String, Object> getParams() {
    return Map.copyOf(sqlParams);
  }
}
