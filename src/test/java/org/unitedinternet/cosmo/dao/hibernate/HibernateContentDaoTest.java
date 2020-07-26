package org.unitedinternet.cosmo.dao.hibernate;

import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.entity.User;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import carldav.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.unitedinternet.cosmo.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WithUserDetails("test01@localhost.de")
class HibernateContentDaoTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Test
    void multipleItemsError() {
        var user = userRepository.findByEmailIgnoreCase("test01@localhost.de");
        var root = collectionRepository.findHomeCollectionByCurrentUser();

        var a = new CollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setParent(root);

        collectionRepository.save(a);

        var item1 = generateTestContent(user);
        item1.setUid("1");
        item1.setCollection(a);
        itemRepository.save(item1);

        var item2 = generateTestContent(user);
        item2.setUid("1");
        item2.setCollection(a);

        var expectedException = assertThrows(DataIntegrityViolationException.class, () -> itemRepository.save(item2));
        assertThat(expectedException.getMessage()).startsWith("could not execute statement; SQL [n/a]; constraint [UID_COLLECTION]");
    }

    @Test
    void multipleCollectionsError() {
        var user = userRepository.findByEmailIgnoreCase("test01@localhost.de");
        var root = collectionRepository.findHomeCollectionByCurrentUser();

        var a = new CollectionItem();
        a.setName("a");
        a.setDisplayName("displayName");
        a.setOwner(user);
        a.setParent(root);

        collectionRepository.save(a);

        var b = new CollectionItem();
        b.setName("a");
        b.setDisplayName("displayName");
        b.setOwner(user);
        b.setParent(root);

        var expectedException = assertThrows(DataIntegrityViolationException.class, () -> collectionRepository.save(b));
        assertThat(expectedException.getMessage()).startsWith("could not execute statement; SQL [n/a]; constraint [DISPLAYNAME_OWNER]");
    }

    private Item generateTestContent(User owner) {
        var content = new Item();
        content.setName(owner.getEmail());
        content.setDisplayName(owner.getEmail());
        content.setMimetype("irrelevant");
        return content;
    }
}
