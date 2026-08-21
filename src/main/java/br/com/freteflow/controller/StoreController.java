package br.com.freteflow.controller;

import br.com.freteflow.entity.Store;
import br.com.freteflow.repository.StoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreRepository storeRepository;

    @PostMapping
    public ResponseEntity<Store> createStore(@RequestBody @Valid Store store) {
        Store savedStore = storeRepository.save(store);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStore);
    }

    @GetMapping
    public ResponseEntity<List<Store>> listStores() {
        List<Store> stores = storeRepository.findAll();
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Store> findById(@PathVariable UUID id) {
        return storeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
