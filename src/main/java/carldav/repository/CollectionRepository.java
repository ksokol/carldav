package carldav.repository;

import carldav.entity.CollectionItem;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionRepository extends CrudRepository<CollectionItem, Long> {

  @Query(value = "select c.* from COLLECTION c join USERS u on c.ownerid = u.id where c.itemname = :name and u.email = :ownerEmail")
  CollectionItem findByOwnerEmailAndName(@Param("ownerEmail") String ownerEmail, @Param("name") String name);

  @Query(value = "select c.* from COLLECTION c join USERS u on c.ownerid = u.id where c.itemname = :ownerEmail and u.email = :ownerEmail")
  CollectionItem findHomeCollectionByOwnerEmail(@Param("ownerEmail") String ownerEmail);

  @Query(value = "select c.* from COLLECTION c join USERS u on c.ownerid = u.id where u.email = :ownerEmail")
  List<CollectionItem> findByOwnerEmail(@Param("ownerEmail") String ownerEmail);

  @Query(value = "select * from COLLECTION where collectionid = :id")
  List<CollectionItem> findByParentId(@Param("id") Long id);
}
