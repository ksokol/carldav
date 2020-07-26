package carldav.repository.specification;

import carldav.entity.Item;
import carldav.repository.CollectionRepository;
import carldav.repository.ItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ItemSpecsTest extends IntegrationTestSupport {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    private final LocalDate now = LocalDate.now();
    private Item item;

    @BeforeEach
    void before() {
        assertThat(itemRepository.findAll(ItemSpecs.parent(1L))).isEmpty();

        var startDate = now.minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        var endDate = now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();

        var collection = collectionRepository.findById(1L);
        if (collection.isEmpty()) {
            Assertions.fail("collection not found");
        }

        item = new Item(Item.Type.VEVENT);
        item.setUid("uid");
        item.setDisplayName("displayName");
        item.setMimetype("mimetype");
        item.setName("name");
        item.setStartDate(Date.from(startDate));
        item.setEndDate(Date.from(endDate));
        item.setCollection(collection.get());

        item = itemRepository.save(item);
    }

    @Test
    void displayNameCaseSensitive() {
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", false, false))).isEmpty();
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", false, false))).containsExactly(item);
    }

    @Test
    void displayNameCaseSensitiveNot() {
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", false, true))).containsExactly(item);
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", false, true))).isEmpty();
    }

    @Test
    void displayNameCaseInsensitive() {
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", true, false))).containsExactly(item);
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", true, false))).containsExactly(item);
    }

    @Test
    void displayNameCaseInsensitiveNot() {
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", true, true))).isEmpty();
        assertThat(itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", true, true))).isEmpty();
    }

    @Test
    void parent() {
        var findOne = itemRepository.findOne(ItemSpecs.parent(1L));
        if (findOne.isEmpty()) {
            Assertions.fail("item not found");
        }

        assertThat(findOne).contains(this.item);
    }

    @Test
    void stampWithType() {
        assertThat(itemRepository.findAll(ItemSpecs.stamp(Item.Type.VEVENT, null, null, null))).containsExactly(item);
        assertThat(itemRepository.findAll(ItemSpecs.stamp(Item.Type.VCARD, null, null, null))).isEmpty();
    }

    @Test
    void stampWithRecurring() {
        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, null, null))).containsExactly(item);
        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, true, null, null))).isEmpty();
    }

    @Test
    void stampWithRecurringInvalidDate() {
        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, new Date(), null))).containsExactly(item);
        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, null, new Date()))).containsExactly(item);
    }

    @Test
    void stampStartDateEndDateEqual() {
        var sameDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, sameDate, sameDate))).containsExactly(item);
    }

    @Test
    void stampStartDateLowerThanEndDate() {
        var startDate = Date.from(now.minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate))).containsExactly(item);
    }

    @Test
    void stampStartDateHigherThanEndDate() {
        var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate))).isEmpty();
    }

    @Test
    void stampStartDateEndDateEqualHitStartDate() {
        var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        item.setStartDate(startDate);
        item.setEndDate(startDate);

        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate))).containsExactly(item);
    }

    @Test
    void stampStartDateEndDateEqualNoHit() {
        var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        item.setStartDate(startDate);
        item.setEndDate(startDate);

        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, endDate, endDate))).isEmpty();
    }

    @Test
    void stampStartDateEndDateEqualHitEndDate() {
        var startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        var endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        item.setStartDate(endDate);
        item.setEndDate(endDate);

        assertThat(itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate))).containsExactly(item);
    }
}
