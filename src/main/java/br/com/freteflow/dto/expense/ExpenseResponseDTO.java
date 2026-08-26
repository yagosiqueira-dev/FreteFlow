package br.com.freteflow.dto.expense;

import br.com.freteflow.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseResponseDTO(
        UUID id,
        UUID vehicleId,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        LocalDateTime createdAt
) {
    public static ExpenseResponseDTO fromEntity(Expense expense) {
        return new ExpenseResponseDTO(
                expense.getId(),
                expense.getVehicle().getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getCreatedAt()
        );
    }
}
