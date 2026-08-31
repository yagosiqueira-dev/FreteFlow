package br.com.freteflow.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseSummaryDTO(
        LocalDate date,
        String description,
        BigDecimal amount
) {}