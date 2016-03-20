package carldav.repository;

import org.springframework.data.repository.CrudRepository;
import carldav.entity.HibCollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CollectionDao extends CrudRepository<HibCollectionItem, Long> {

    HibCollectionItem findByOwnerEmailAndName(String owner, String name);

    List<HibCollectionItem> findByOwnerEmail(String owner);

    List<HibCollectionItem> findByParentId(Long id);
}
