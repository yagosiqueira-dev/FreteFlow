package br.com.freteflow.service;

import br.com.freteflow.dto.report.BiWeeklyReportDTO;
import br.com.freteflow.dto.report.FreightReportItemDTO;
import br.com.freteflow.entity.Driver;
import br.com.freteflow.repository.DriverRepository;
import br.com.freteflow.exception.DriverNotFoundException;
import br.com.freteflow.repository.FreightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final FreightRepository freightRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public BiWeeklyReportDTO generateBiWeeklyReport(UUID driverId, LocalDate startDate, LocalDate endDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException(driverId));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        var freights = freightRepository.findByDriverIdAndFreightDateBetweenOrderByFreightDateAsc(driverId, startDateTime, endDateTime);

        List<FreightReportItemDTO> items = freights.stream()
                .map(f -> new FreightReportItemDTO(
                        f.getFreightDate().toLocalDate(),
                        f.getStore().getOrigin(),
                        f.getStore().getDestination(),
                        f.getFreightValue()
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(FreightReportItemDTO::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BiWeeklyReportDTO(startDate, endDate, driver.getName(), total, items);
    }
}