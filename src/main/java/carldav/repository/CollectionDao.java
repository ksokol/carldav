package carldav.repository;

import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CollectionDao {

    List<HibCollectionItem> findByParentId(Long id);

}
