package carldav.repository;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

import java.util.List;

/**
 * @author Kamill Sokol
 */
class CollectionDaoImpl implements CollectionDao {

    private final SessionFactory sessionFactory;

    CollectionDaoImpl(SessionFactory sessionFactory) {
        Assert.notNull(sessionFactory, "sessionFactory is null");
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<HibCollectionItem> findByParentId(Long id) {
        Query hibQuery = sessionFactory.getCurrentSession().getNamedQuery("collections")
                .setParameter("parent", id);
        return hibQuery.list();
    }

    @Override
    public HibCollectionItem save(HibCollectionItem item) {
        sessionFactory.getCurrentSession().saveOrUpdate(item);
        sessionFactory.getCurrentSession().flush();
        return item;
    }

    @Override
    public void remove(HibCollectionItem item) {
        sessionFactory.getCurrentSession().refresh(item);
        item.setCollection(null);
        sessionFactory.getCurrentSession().delete(item);
        sessionFactory.getCurrentSession().flush();
    }
}
