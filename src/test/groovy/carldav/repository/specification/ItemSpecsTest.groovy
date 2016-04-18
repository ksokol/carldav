package carldav.repository.specification

import carldav.entity.Item
import carldav.repository.CollectionRepository
import carldav.repository.ItemRepository
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.unitedinternet.cosmo.IntegrationTestSupport

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

public class ItemSpecsTest extends IntegrationTestSupport {

    @Autowired
    private ItemRepository itemRepository

    @Autowired
    private CollectionRepository collectionRepository

    def now = LocalDate.now()
    def item

    @Before
    void before() {
        assert itemRepository.findAll(ItemSpecs.parent(1L)) == []

        Instant startDate = now.minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        Instant endDate = now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()

        item = new Item(
                uid: "uid",
                displayName: "displayName",
                mimetype: "mimetype",
                name: "name",
                type: Item.Type.VEVENT,
                startDate: Date.from(startDate),
                endDate: Date.from(endDate),
                collection: collectionRepository.findOne(1L)
        )

        item = itemRepository.save(item)
    }

    @Test
    public void displayNameCaseSensitive() {
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", false, false)) == []
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", false, false)) == [item]
    }

    @Test
    public void displayNameCaseSensitiveNot() {
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", false, true)) == [item]
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", false, true)) == []
    }

    @Test
    public void displayNameCaseInsensitive() {
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", true, false)) == [item]
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", true, false)) == [item]
    }

    @Test
    public void displayNameCaseInsensitiveNot() {
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayna", true, true)) == []
        assert itemRepository.findAll(ItemSpecs.propertyLike("displayName", "isplayNa", true, true)) == []
    }

    @Test
    public void parent() {
        def findOne = itemRepository.findOne(ItemSpecs.parent(1L))
        assert item == findOne
    }

    @Test
    public void stampWithType() {
        assert itemRepository.findAll(ItemSpecs.stamp(Item.Type.VEVENT, null, null, null)) == [item]
        assert itemRepository.findAll(ItemSpecs.stamp(Item.Type.VCARD, null, null, null)) == []
    }

    @Test
    public void stampWithRecurring() {
        assert itemRepository.findAll(ItemSpecs.stamp(null, null, null, null)) == [item]
        assert itemRepository.findAll(ItemSpecs.stamp(null, true, null, null)) == []
    }

    @Test
    public void stampWithRecurringInvalidDate() {
        assert itemRepository.findAll(ItemSpecs.stamp(null, null, new Date(), null)) == [item]
        assert itemRepository.findAll(ItemSpecs.stamp(null, null, null, new Date())) == [item]
    }

    @Test
    public void stampStartDateEndDateEqual() {
        Date sameDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        assert itemRepository.findAll(ItemSpecs.stamp(null, null, sameDate, sameDate)) == [item]
    }

    @Test
    public void stampStartDateLowerThanEndDate() {
        Date startDate = Date.from(now.minusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        assert itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate)) == [item]
    }

    @Test
    public void stampStartDateHigherThanEndDate() {
        Date startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        assert itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate)) == []
    }

    @Test
    public void stampStartDateEndDateEqualHitStartDate() {
        Date startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())

        item.setStartDate(startDate)
        item.setEndDate(startDate)

        assert itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate)) == [item]
    }

    @Test
    public void stampStartDateEndDateEqualNoHit() {
        Date startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())

        item.setStartDate(startDate)
        item.setEndDate(startDate)

        assert itemRepository.findAll(ItemSpecs.stamp(null, null, endDate, endDate)) == []
    }

    @Test
    public void stampStartDateEndDateEqualHitEndDate() {
        Date startDate = Date.from(now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())

        item.setStartDate(endDate)
        item.setEndDate(endDate)

        assert itemRepository.findAll(ItemSpecs.stamp(null, null, startDate, endDate)) == [item]
    }
}