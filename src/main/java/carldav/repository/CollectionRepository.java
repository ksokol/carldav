package carldav.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import carldav.entity.CollectionItem;

import java.util.List;

public interface CollectionRepository extends CrudRepository<CollectionItem, Long> {

    CollectionItem findByOwnerEmailAndName(String ownerEmail, String name);

    @Query("select c from CollectionItem c where c.name = ?1 and c.owner.email = ?1")
    CollectionItem findHomeCollectionByOwnerEmail(String ownerEmail);

    List<CollectionItem> findByOwnerEmail(String ownerEmail);

    List<CollectionItem> findByParentId(Long id);
}
