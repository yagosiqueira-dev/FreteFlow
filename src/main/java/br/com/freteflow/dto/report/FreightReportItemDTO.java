package br.com.freteflow.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FreightReportItemDTO(
        LocalDate date,
        String loadingLocation,
        String fullRoute,
        BigDecimal value
) {}