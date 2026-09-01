package br.com.freteflow.controller;

import br.com.freteflow.dto.report.BiWeeklyReportDTO;
import br.com.freteflow.dto.report.VehicleProfitReportDTO;
import br.com.freteflow.service.ExcelReportService;
import br.com.freteflow.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ExcelReportService excelReportService;

    @GetMapping("/driver/{driverId}/bi-weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<BiWeeklyReportDTO> getBiWeeklyReport(
            @PathVariable UUID driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BiWeeklyReportDTO report = reportService.generateBiWeeklyReport(driverId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
    @GetMapping("/vehicle/{vehicleId}/profit")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<VehicleProfitReportDTO> getVehicleProfitReport(
            @PathVariable UUID vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        VehicleProfitReportDTO report = reportService.generateVehicleProfitReport(vehicleId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
    @GetMapping("/vehicle/{vehicleId}/profit/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<byte[]> exportVehicleProfitReport(
            @PathVariable UUID vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException, IOException {

        VehicleProfitReportDTO report = reportService.generateVehicleProfitReport(vehicleId, startDate, endDate);
        byte[] excelFile = excelReportService.generateVehicleProfitExcel(report);

        String filename = "relatorio_lucro_%s_%s_a_%s.xlsx".formatted(
                report.licensePlate(), startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile);
    }
}