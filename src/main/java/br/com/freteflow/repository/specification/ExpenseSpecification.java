package br.com.freteflow.repository.specification;

import br.com.freteflow.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class ExpenseSpecification {

    public static Specification<Expense> vehicleIdEquals(UUID vehicleId) {
        return (root, query, cb) -> vehicleId == null ? null : cb.equal(root.get("vehicle").get("id"), vehicleId);
    }

    public static Specification<Expense> descriptionEquals(String description) {
        return (root, query, cb) -> description == null || description.isEmpty() ? null : cb.equal(root.get("description"), description);
    }

    public static Specification<Expense> expenseDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) return cb.between(root.get("expenseDate"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("expenseDate"), start);
            if (end != null) return cb.lessThanOrEqualTo(root.get("expenseDate"), end);
            return null;
        };
    }
}