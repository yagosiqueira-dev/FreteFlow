package br.com.freteflow.controller;

import br.com.freteflow.dto.freight.FreightRequestDTO;
import br.com.freteflow.dto.freight.FreightResponseDTO;
import br.com.freteflow.entity.FreightStatus;
import br.com.freteflow.service.FreightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;


import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/freights")
@RequiredArgsConstructor
public class FreightController {

    private final FreightService freightService;

    @PostMapping
    public ResponseEntity<FreightResponseDTO> create(@Valid @RequestBody FreightRequestDTO request) {
        FreightResponseDTO created = freightService.createFreight(request);
        URI location = URI.create("/api/freights/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FreightResponseDTO> findById(@PathVariable UUID id) {
        FreightResponseDTO freight = freightService.findById(id);
        return ResponseEntity.ok(freight);
    }

    @GetMapping
    public ResponseEntity<Page<FreightResponseDTO>> list(
            @RequestParam(required = false) FreightStatus status,
            @RequestParam(required = false) String driverName,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<FreightResponseDTO> freights = freightService.listFreights(status, driverName, startDate, endDate, pageable);
        return ResponseEntity.ok(freights);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FreightResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody FreightRequestDTO request) {

        FreightResponseDTO updated = freightService.updateFreight(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FreightResponseDTO> updateStatus(
            @PathVariable UUID id, @RequestParam FreightStatus status) {

        FreightResponseDTO updated = freightService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}