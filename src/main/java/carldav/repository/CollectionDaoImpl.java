package carldav.repository;

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

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
    public HibCollectionItem save(HibCollectionItem item) {
        sessionFactory.getCurrentSession().saveOrUpdate(item);
        sessionFactory.getCurrentSession().flush();
        return item;
    }

    @Override
    public void remove(HibCollectionItem item) {
        sessionFactory.getCurrentSession().delete(item);
        sessionFactory.getCurrentSession().flush();
    }

    @Override
    public HibCollectionItem findByOwnerAndName(String owner, String name) {
        return (HibCollectionItem) sessionFactory.getCurrentSession().getNamedQuery("collection.findByOwnerAndName")
                .setParameter("owner",owner)
                .setParameter("name", name)
                .uniqueResult();
    }
}
