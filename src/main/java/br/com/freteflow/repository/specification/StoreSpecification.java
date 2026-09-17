package br.com.freteflow.repository.specification;

import br.com.freteflow.entity.Store;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class StoreSpecification {

    public static Specification<Store> nameContains(String name) {
        return (root, query, cb) ->
                (name == null || name.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Store> originContains(String origin) {
        return (root, query, cb) ->
                (origin == null || origin.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("origin")), "%" + origin.toLowerCase() + "%");
    }

    public static Specification<Store> destinationContains(String destination) {
        return (root, query, cb) ->
                (destination == null || destination.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("destination")), "%" + destination.toLowerCase() + "%");
    }

    public static Specification<Store> enabledEquals(Boolean enabled) {
        return (root, query, cb) ->
                enabled == null ? null : cb.equal(root.get("enabled"), enabled);
    }

    public static Specification<Store> defaultValueBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("defaultValue"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("defaultValue"), min);
            return cb.lessThanOrEqualTo(root.get("defaultValue"), max);
        };
    }
}
