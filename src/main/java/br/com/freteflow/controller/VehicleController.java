package br.com.freteflow.controller;

import br.com.freteflow.dto.vehicle.VehicleRequestDTO;
import br.com.freteflow.dto.vehicle.VehicleResponseDTO;
import br.com.freteflow.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.UUID;

import java.net.URI;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody VehicleRequestDTO request) {
        VehicleResponseDTO created = vehicleService.createVehicle(request);

        URI location = URI.create("/api/vehicles/" + created.id());

        return ResponseEntity.created(location).body(created);
    }
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody VehicleRequestDTO request) {

        VehicleResponseDTO updated = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        vehicleService.deactivateVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseDTO>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<VehicleResponseDTO> vehicles = vehicleService.listVehicles(pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> findById(@PathVariable UUID id) {
        VehicleResponseDTO vehicle = vehicleService.findById(id);
        return ResponseEntity.ok(vehicle);
    }
}