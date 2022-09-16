package carldav.repository;

import carldav.entity.Item;

import java.util.List;

public interface ItemRepositoryCustom {

  List<Item> findAll(ItemQuery query);

}
