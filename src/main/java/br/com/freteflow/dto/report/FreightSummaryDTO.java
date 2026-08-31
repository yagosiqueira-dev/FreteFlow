package br.com.freteflow.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FreightSummaryDTO(
        LocalDate date,
        String driverName,
        String storeName,
        BigDecimal value
) {}