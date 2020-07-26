package carldav.repository;

import carldav.entity.Item;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    List<Item> findByCollectionIdAndType(Long id, Item.Type type);

    List<Item> findByCollectionId(Long id);

    @Query("select i from Item i where i.collection.name = ?1 and i.name = ?2 and i.collection.owner.email = ?#{ principal.username }")
    Item findByCurrentOwnerEmailAndCollectionNameAndName(String collectionName, String name);

}
