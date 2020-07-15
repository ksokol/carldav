package carldav.repository.specification;

import carldav.entity.Item;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Locale.ENGLISH;

/**
 * @author Kamill Sokol
 */
public final class ItemSpecs {

    private static final String START_DATE = "startDate";

    private ItemSpecs() {
        //private
    }

    public static Specification<Item> combine(List<Specification<Item>> specifications) {
        return (root, query, cb) -> {
            Optional<Specification<Item>> result = specifications.stream().reduce((left, right) -> Specification.where(left).and(right));
            return result.map(itemSpecification -> itemSpecification.toPredicate(root, query, cb)).orElse(null);
        };
    }

    public static Specification<Item> propertyLike(String property, String value, boolean caseless, boolean negate) {
        return (root, query, cb) -> {
            Path<String> uid = root.get(property);
            Expression<String> lower = uid;
            String pattern = "%" + value + "%";

            if (caseless) {
                lower = cb.lower(uid);
                pattern = pattern.toLowerCase(ENGLISH);
            }

            if (negate) {
                return cb.notLike(lower, pattern);
            }

            return cb.like(lower, pattern);
        };
    }

    public static Specification<Item> parent(Long id) {
        return (root, query, cb) -> {
            Path<Long> collectionId = root.get("collection").get("id");
            return cb.equal(collectionId, id);
        };
    }

    public static Specification<Item> stamp(Item.Type type, Boolean recurring, Date start, Date end) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(3);

            if (type != null) {
                predicates.add(cb.equal(root.<String>get("type"), type));
            }

            // handle recurring event filter
            if (recurring != null) {
                predicates.add(cb.equal(root.<String>get("recurring"), recurring));
            }

            if (start != null && end != null) {
                Predicate startDate1 = cb.lessThan(root.get(START_DATE), end);
                Predicate endDate1 = cb.greaterThan(root.get("endDate"), start);

                Predicate startDate1AndEndDate1 = cb.and(startDate1, endDate1);

                Predicate startDateEqualEndDate = cb.equal(root.<Date>get(START_DATE), root.<Date>get("endDate"));
                Predicate startDateEqual = cb.equal(root.<Date>get(START_DATE), start);
                Predicate endDateEqual = cb.equal(root.<Date>get(START_DATE), end);

                Predicate startDateEqualOrEndDateEqual = cb.or(startDateEqual, endDateEqual);
                Predicate startDateEqualEndDateAndStartDateEqualOrEndDateEqual = cb.and(startDateEqualEndDate, startDateEqualOrEndDateEqual);

                // edge case where start==end
                Predicate or = cb.or(startDate1AndEndDate1, startDateEqualEndDateAndStartDateEqualOrEndDateEqual);
                predicates.add(or);
            }

            return predicates.stream().reduce(cb::and).orElse(null);
        };
    }
}
