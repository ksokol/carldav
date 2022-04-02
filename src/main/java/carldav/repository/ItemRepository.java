package carldav.repository;

import carldav.entity.Item;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    List<Item> findByCollectionIdAndType(Long id, Item.Type type);

    List<Item> findByCollectionId(Long id);

    @Query("select i from Item i where i.collection.name = ?2 and i.name = ?3 and i.collection.owner.email = ?1")
    Item findByOwnerEmailAndCollectionNameAndName(String ownerEmail, String collectionName, String name);
}
