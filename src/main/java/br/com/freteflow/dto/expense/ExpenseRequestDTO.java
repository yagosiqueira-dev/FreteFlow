package br.com.freteflow.dto.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseRequestDTO(

        @NotNull(message = "O ID do veículo é obrigatório")
        UUID vehicleId,

        @NotBlank(message = "A descrição é obrigatória")
        @Pattern(
                regexp = "(?i)^(diesel|ped[aá]gio|manuten[cç][aã]o)$",
                message = "Categoria inválida. Aceito apenas: Diesel, Pedágio ou Manutenção"
        )
        String description,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "A data da despesa é obrigatória")
        LocalDate expenseDate
) {
}