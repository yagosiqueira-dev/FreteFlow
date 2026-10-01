package br.com.freteflow.repository.specification;

import br.com.freteflow.entity.Freight;
import br.com.freteflow.entity.FreightStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class FreightSpecification {

    public static Specification<Freight> statusEquals(FreightStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Freight> driverNameContains(String driverName) {
        return (root, query, cb) ->
                (driverName == null || driverName.isBlank())
                        ? null
                        : cb.like(cb.lower(root.join("driver").get("name")), "%" + driverName.toLowerCase() + "%");
    }

    public static Specification<Freight> freightDateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) return cb.between(root.get("freightDate"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("freightDate"), start);
            return cb.lessThanOrEqualTo(root.get("freightDate"), end);
        };
    }
    public static Specification<Freight> vehicleIdEquals(UUID vehicleId) {
        return (root, query, cb) -> vehicleId == null ? null : cb.equal(root.get("vehicle").get("id"), vehicleId);
    }
}