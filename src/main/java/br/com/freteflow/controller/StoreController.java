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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponseDTO> create(@Valid @RequestBody StoreRequestDTO request) {
        StoreResponseDTO created = storeService.createStore(request);
        URI location = URI.create("/api/stores/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<StoreResponseDTO>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<StoreResponseDTO> stores = storeService.listStores(pageable);
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDTO> findById(@PathVariable UUID id) {
        StoreResponseDTO store = storeService.findById(id);
        return ResponseEntity.ok(store);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody StoreRequestDTO request) {

        StoreResponseDTO updated = storeService.updateStore(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        storeService.deactivateStore(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/activate")
    public ResponseEntity<StoreResponseDTO> activate(@PathVariable UUID id) {
        StoreResponseDTO activated = storeService.activateStore(id);
        return ResponseEntity.ok(activated);
    }
}