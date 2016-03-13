package carldav.repository;

import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CollectionDao {

    List<HibCollectionItem> findByParentId(Long id);

    HibCollectionItem save(HibCollectionItem item);

    void remove(HibCollectionItem item);

    HibCollectionItem findByOwnerAndName(String owner, String name);
}
