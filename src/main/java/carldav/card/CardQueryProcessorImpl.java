package carldav.card;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.AddressbookFilter;
import org.unitedinternet.cosmo.dao.hibernate.ContentDaoImpl;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
class CardQueryProcessorImpl implements CardQueryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CardQueryProcessorImpl.class);

    private final ContentDaoImpl contentDao;

    CardQueryProcessorImpl(final ContentDaoImpl contentDao) {
        Assert.notNull(contentDao, "contentDao is null");
        this.contentDao = contentDao;
    }

    @Override
    public Set<HibItem> filterQuery(final HibCollectionItem collection, final AddressbookFilter filter) {
        LOG.debug("finding vacards in collection {} by filter {}", collection.getUid(), filter);
        //TODO filter is not in use yet
        return contentDao.findCollectionFileItems(collection);
    }
}
