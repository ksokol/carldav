package org.unitedinternet.cosmo.dao;

import org.springframework.data.repository.CrudRepository;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
public interface CollectionDao extends CrudRepository<HibCollectionItem, Long> {

    HibCollectionItem findByOwnerEmailAndName(String owner, String name);

    List<HibCollectionItem> findByOwnerEmail(String owner);

    List<HibCollectionItem> findByParentId(Long id);
}
