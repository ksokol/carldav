package carldav.repository;

import carldav.entity.CollectionItem;
import carldav.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.unitedinternet.cosmo.IntegrationTestSupport;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryTests extends IntegrationTestSupport {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private JdbcAggregateOperations template;

  private final LocalDate now = LocalDate.now();
  private Item item;

  @BeforeEach
  void before() {
    assertThat(template.count(Item.class)).isZero();

    var startDate = now.minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    var endDate = now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    var collection = Optional.ofNullable(template.findById(1L, CollectionItem.class)).orElseThrow(AssertionFailedError::new);

    item = new Item(Item.Type.VEVENT.toString());
    item.setUid("uid");
    item.setDisplayName("displayName");
    item.setMimetype("mimetype");
    item.setName("name");
    item.setStartDate(Date.from(startDate));
    item.setEndDate(Date.from(endDate));
    item.setCollectionid(collection.getId());

    item = template.save(item);
  }

  @Test
  void displayNameCaseSensitive() {
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayna", false, false))).isEmpty();
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayNa", false, false)))
      .extracting("id")
      .contains(item.getId());
  }

  @Test
  void displayNameCaseSensitiveNot() {
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayna", false, true)))
      .extracting("id")
      .contains(item.getId());
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayNa", false, true))).isEmpty();
  }

  @Test
  void displayNameCaseInsensitive() {
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayna", true, false)))
      .extracting("id")
      .contains(item.getId());
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayNa", true, false)))
      .extracting("id")
      .contains(item.getId());
  }

  @Test
  void displayNameCaseInsensitiveNot() {
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayna", true, true))).isEmpty();
    assertThat(itemRepository.findAll(new ItemQuery().propertyLike("displayName", "isplayNa", true, true))).isEmpty();
  }

  @Test
  void parent() {
    assertThat(itemRepository.findAll(new ItemQuery().parent(1L)))
      .extracting("id")
      .contains(item.getId());
  }

  @Test
  void stampWithType() {
    assertThat(itemRepository.findAll(new ItemQuery().stamp(Item.Type.VEVENT, null, null, null)))
      .extracting("id")
      .containsExactly(item.getId());
    assertThat(itemRepository.findAll(new ItemQuery().stamp(Item.Type.VCARD, null, null, null))).isEmpty();
  }

  @Test
  void stampWithRecurring() {
    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, null, null)))
      .extracting("id")
      .containsExactly(item.getId());
    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, true, null, null))).isEmpty();
  }

  @Test
  void stampWithRecurringInvalidDate() {
    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, new Date(), null)))
      .extracting("id")
      .containsExactly(item.getId());
    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, null, new Date())))
      .extracting("id")
      .containsExactly(item.getId());
  }

  @Test
  void stampStartDateEndDateEqual() {
    var sameDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, sameDate, sameDate)))
      .extracting("id")
      .containsExactly(item.getId());
  }

  @Test
  void stampStartDateLowerThanEndDate() {
    var startDate = Date.from(now.minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, startDate, endDate)))
      .extracting("id")
      .containsExactly(item.getId());
  }

  @Test
  void stampStartDateHigherThanEndDate() {
    var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, startDate, endDate))).isEmpty();
  }

  @Test
  void stampStartDateEndDateEqualHitStartDate() {
    var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

    item.setStartDate(startDate);
    item.setEndDate(startDate);
    template.save(item);

    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, startDate, endDate)))
      .extracting("id")
      .containsExactly(item.getId());
  }

  @Test
  void stampStartDateEndDateEqualNoHit() {
    var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

    item.setStartDate(startDate);
    item.setEndDate(startDate);
    template.save(item);

    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, endDate, endDate))).isEmpty();
  }

  @Test
  void stampStartDateEndDateEqualHitEndDate() {
    var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

    item.setStartDate(endDate);
    item.setEndDate(endDate);
    template.save(item);

    assertThat(itemRepository.findAll(new ItemQuery().stamp(null, null, startDate, endDate)))
      .extracting("id")
      .containsExactly(item.getId());
  }
}
