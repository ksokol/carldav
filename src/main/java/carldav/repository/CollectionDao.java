package carldav.repository;

import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

/**
 * @author Kamill Sokol
 */
public interface CollectionDao {

    HibCollectionItem save(HibCollectionItem item);

    void remove(HibCollectionItem item);

    HibCollectionItem findByOwnerAndName(String owner, String name);
}
