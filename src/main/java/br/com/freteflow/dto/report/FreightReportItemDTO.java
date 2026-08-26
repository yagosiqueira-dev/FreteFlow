package br.com.freteflow.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FreightReportItemDTO(
        LocalDate date,
        String loadingLocation, // Ex: "itaqua" ou "ceasa"
        String fullRoute,       // Ex: "praia grande - guarapiranga - osasco"
        BigDecimal value
) {}