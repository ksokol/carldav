package carldav.repository;

import org.springframework.data.repository.CrudRepository;
import carldav.entity.CollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CollectionRepository extends CrudRepository<CollectionItem, Long> {

    CollectionItem findByOwnerEmailAndName(String owner, String name);

    List<CollectionItem> findByOwnerEmail(String owner);

    List<CollectionItem> findByParentId(Long id);
}
