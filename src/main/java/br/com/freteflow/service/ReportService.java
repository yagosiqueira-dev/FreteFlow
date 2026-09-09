package br.com.freteflow.service;

import br.com.freteflow.dto.report.BiWeeklyReportDTO;
import br.com.freteflow.dto.report.ExpenseSummaryDTO;
import br.com.freteflow.dto.report.FreightReportItemDTO;
import br.com.freteflow.dto.report.FreightSummaryDTO;
import br.com.freteflow.dto.report.VehicleProfitReportDTO;
import br.com.freteflow.entity.Driver;
import br.com.freteflow.entity.Store;
import br.com.freteflow.entity.Vehicle;
import br.com.freteflow.exception.DriverNotFoundException;
import br.com.freteflow.exception.VehicleNotFoundException;
import br.com.freteflow.repository.DriverRepository;
import br.com.freteflow.repository.ExpenseRepository;
import br.com.freteflow.repository.FreightRepository;
import br.com.freteflow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final FreightRepository freightRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public BiWeeklyReportDTO generateBiWeeklyReport(UUID driverId, LocalDate startDate, LocalDate endDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(driverId));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        var freights = freightRepository.findByDriverIdAndFreightDateBetweenOrderByFreightDateAsc(driverId, startDateTime, endDateTime);

        List<FreightReportItemDTO> items = freights.stream()
                .map(f -> {
                    String origin = f.getStores().isEmpty() ? "" : f.getStores().get(0).getOrigin();
                    String destinations = f.getStores().stream()
                            .map(Store::getDestination)
                            .collect(Collectors.joining(", "));

                    return new FreightReportItemDTO(
                            f.getFreightDate().toLocalDate(),
                            origin,
                            destinations,
                            f.getFreightValue()
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(FreightReportItemDTO::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BiWeeklyReportDTO(startDate, endDate, driver.getName(), total, items);
    }

    @Transactional(readOnly = true)
    public VehicleProfitReportDTO generateVehicleProfitReport(UUID vehicleId, LocalDate startDate, LocalDate endDate) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        var freights = freightRepository.findByVehicleIdAndFreightDateBetweenOrderByFreightDateAsc(
                vehicleId, startDateTime, endDateTime);

        var expenses = expenseRepository.findByVehicleIdAndExpenseDateBetweenOrderByExpenseDateAsc(
                vehicleId, startDate, endDate);

        List<FreightSummaryDTO> freightItems = freights.stream()
                .map(f -> {
                    String storeNames = f.getStores().stream()
                            .map(Store::getName)
                            .collect(Collectors.joining(", "));

                    return new FreightSummaryDTO(
                            f.getFreightDate().toLocalDate(),
                            f.getDriver().getName(),
                            storeNames,
                            f.getFreightValue()
                    );
                })
                .toList();

        List<ExpenseSummaryDTO> expenseItems = expenses.stream()
                .map(e -> new ExpenseSummaryDTO(
                        e.getExpenseDate(),
                        e.getDescription(),
                        e.getAmount()
                ))
                .toList();

        BigDecimal totalFreightValue = freightItems.stream()
                .map(FreightSummaryDTO::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenseItems.stream()
                .map(ExpenseSummaryDTO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalFreightValue.subtract(totalExpenses);

        return new VehicleProfitReportDTO(
                startDate, endDate, vehicleId, vehicle.getLicensePlate(),
                totalFreightValue, totalExpenses, netProfit,
                freightItems, expenseItems
        );
    }
}