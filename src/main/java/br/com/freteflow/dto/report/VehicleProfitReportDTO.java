package br.com.freteflow.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VehicleProfitReportDTO(
        LocalDate startDate,
        LocalDate endDate,
        UUID vehicleId,
        String licensePlate,
        BigDecimal totalFreightValue,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        List<FreightSummaryDTO> freights,
        List<ExpenseSummaryDTO> expenses
) {}
