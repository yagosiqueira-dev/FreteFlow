package br.com.freteflow.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BiWeeklyReportDTO(
        LocalDate startDate,
        LocalDate endDate,
        String driverName,
        BigDecimal totalAmount,
        List<FreightReportItemDTO> freights
) {}