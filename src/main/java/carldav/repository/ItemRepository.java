package carldav.repository;

import carldav.entity.Item;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Long>, ItemRepositoryCustom {

  @Query(value = "select * from item where collectionid = :id and type = :type")
  List<Item> findByCollectionIdAndType(@Param("id") Long id, @Param("type") String type);

  @Query(value = "select * from item where collectionid = :id")
  List<Item> findByCollectionId(@Param("id") Long id);

  @Query(value = "select i.* from Item i join collection c on i.collectionid = c.id join users u on c.ownerid = u.id where c.itemname = :collectionName and i.itemname = :name and u.email = :ownerEmail")
  Item findByOwnerEmailAndCollectionNameAndName(@Param("ownerEmail") String ownerEmail, @Param("collectionName") String collectionName, @Param("name") String name);

  @Modifying
  @Query(value = "delete from item where collectionid = :id")
  void deleteAllByParentId(@Param("id") Long id);
}
