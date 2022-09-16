package org.unitedinternet.cosmo.dao.hibernate;

import carldav.entity.CollectionItem;
import carldav.entity.Item;
import carldav.entity.User;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import carldav.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.unitedinternet.cosmo.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HibernateContentDaoTest extends IntegrationTestSupport {

  public static final String TEST_01_LOCALHOST_DE = "test01@localhost.de";
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private CollectionRepository collectionRepository;

  @Test
  void multipleItemsError() {
    var user = userRepository.findByEmailIgnoreCase("test01@localhost.de");
    var root = collectionRepository.findHomeCollectionByOwnerEmail("test01@localhost.de");

    var a = new CollectionItem();
    a.setName("a");
    a.setDisplayName("displayName");
    a.setOwnerId(user.getId());
    a.setParentId(root.getId());

    collectionRepository.save(a);

    var item1 = generateTestContent(user);
    item1.setUid("1");
    item1.setCollectionid(a.getId());
    itemRepository.save(item1);

    var item2 = generateTestContent(user);
    item2.setUid("1");
    item2.setCollectionid(a.getId());

    var expectedException = assertThrows(DbActionExecutionException.class, () -> itemRepository.save(item2));
    assertThat(expectedException.getCause().getMessage())
      .contains("integrity constraint violation: unique constraint or index violation; UID_COLLECTION");
  }

  @Test
  void multipleCollectionsError() {
    var user = userRepository.findByEmailIgnoreCase(TEST_01_LOCALHOST_DE);
    var root = collectionRepository.findHomeCollectionByOwnerEmail("test01@localhost.de");

    var a = new CollectionItem();
    a.setName("a");
    a.setDisplayName("displayName");
    a.setOwnerId(user.getId());
    a.setParentId(root.getId());

    collectionRepository.save(a);

    var b = new CollectionItem();
    b.setName("a");
    b.setDisplayName("displayName");
    b.setOwnerId(user.getId());
    b.setParentId(root.getId());

    var expectedException = assertThrows(DbActionExecutionException.class, () -> collectionRepository.save(b));
    assertThat(expectedException.getCause().getMessage())
      .contains("integrity constraint violation: unique constraint or index violation; DISPLAYNAME_OWNER table: COLLECTION");
  }

  private Item generateTestContent(User owner) {
    var content = new Item();
    content.setName(owner.getEmail());
    content.setDisplayName(owner.getEmail());
    content.setMimetype("irrelevant");
    return content;
  }
}
