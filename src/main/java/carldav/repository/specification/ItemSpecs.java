package carldav.repository.specification;

import carldav.entity.Item;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

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

    private ItemSpecs() {
        //private
    }

    public static Specification<Item> combine(List<Specification<Item>> specifications) {
        return (root, query, cb) -> {
            final Optional<Specification<Item>> result = specifications.stream().reduce((left, right) -> Specifications.where(left).and(right));
            if(result.isPresent()) {
                return result.get().toPredicate(root, query, cb);
            }
            return null;
        };
    }

    public static Specification<Item> propertyLike(String property, String value, boolean caseless, boolean negate) {
        return (root, query, cb) -> {
            final Path<String> uid = root.get(property);
            Expression<String> lower = uid;
            String pattern = "%" + value + "%";

            if(caseless) {
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
            final Path<Long> collectionId = root.get("collection").get("id");
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
                final Predicate startDate1 = cb.lessThan(root.get("startDate"), end);
                final Predicate endDate1 = cb.greaterThan(root.get("endDate"), start);

                final Predicate startDate1AndEndDate1 = cb.and(startDate1, endDate1);

                final Predicate startDateEqualEndDate = cb.equal(root.<Date>get("startDate"), root.<Date>get("endDate"));
                final Predicate startDateEqual = cb.equal(root.<Date>get("startDate"), start);
                final Predicate endDateEqual = cb.equal(root.<Date>get("startDate"), end);

                final Predicate startDateEqualOrEndDateEqual = cb.or(startDateEqual, endDateEqual);
                final Predicate startDateEqualEndDateAndStartDateEqualOrEndDateEqual = cb.and(startDateEqualEndDate, startDateEqualOrEndDateEqual);

                // edge case where start==end
                final Predicate or = cb.or(startDate1AndEndDate1, startDateEqualEndDateAndStartDateEqualOrEndDateEqual);
                predicates.add(or);
            }

            return predicates.stream().reduce((left, right) -> cb.and(left, right)).orElse(null);
        };
    }
}
