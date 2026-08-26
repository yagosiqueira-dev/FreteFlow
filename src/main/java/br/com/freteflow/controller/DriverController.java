package br.com.freteflow.controller;

import br.com.freteflow.dto.driver.DriverRequestDTO;
import br.com.freteflow.dto.driver.DriverResponseDTO;
import br.com.freteflow.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponseDTO> create(@Valid @RequestBody DriverRequestDTO request) {
        DriverResponseDTO created = driverService.createDriver(request);
        URI location = URI.create("/api/drivers/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<DriverResponseDTO>> list(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<DriverResponseDTO> drivers;

        if (name != null && !name.trim().isEmpty()) {
            drivers = driverService.searchByName(name, pageable);
        } else {
            drivers = driverService.listDrivers(pageable);
        }

        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDTO> findById(@PathVariable UUID id) {
        DriverResponseDTO driver = driverService.findById(id);
        return ResponseEntity.ok(driver);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody DriverRequestDTO request) {

        DriverResponseDTO updated = driverService.updateDriver(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        driverService.deactivateDriver(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponseDTO> activate(@PathVariable UUID id) {
        DriverResponseDTO activated = driverService.activateDriver(id);
        return ResponseEntity.ok(activated);
    }
}