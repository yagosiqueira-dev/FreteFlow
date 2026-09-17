package br.com.freteflow.controller;

import br.com.freteflow.dto.store.StoreRequestDTO;
import br.com.freteflow.dto.store.StoreResponseDTO;
import br.com.freteflow.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDTO> create(@Valid @RequestBody StoreRequestDTO request) {
        StoreResponseDTO created = storeService.createStore(request);
        URI location = URI.create("/api/stores/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDTO> findById(@PathVariable UUID id) {
        StoreResponseDTO store = storeService.findById(id);
        return ResponseEntity.ok(store);
    }
    @GetMapping
    public ResponseEntity<Page<StoreResponseDTO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<StoreResponseDTO> stores = storeService.listStores(
                name, origin, destination, enabled, minValue, maxValue, pageable);
        return ResponseEntity.ok(stores);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody StoreRequestDTO request) {

        StoreResponseDTO updated = storeService.updateStore(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        storeService.deactivateStore(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StoreResponseDTO> activate(@PathVariable UUID id) {
        StoreResponseDTO activated = storeService.activateStore(id);
        return ResponseEntity.ok(activated);
    }
}