package br.com.freteflow.service;

import br.com.freteflow.dto.store.StoreRequestDTO;
import br.com.freteflow.dto.store.StoreResponseDTO;
import br.com.freteflow.entity.Store;
import br.com.freteflow.exception.StoreNameAlreadyExistsException;
import br.com.freteflow.exception.StoreNotFoundException;
import br.com.freteflow.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponseDTO createStore(StoreRequestDTO request) {
        if (storeRepository.existsByName(request.name())) {
            throw new StoreNameAlreadyExistsException(request.name());
        }

        Store store = Store.builder()
                .name(request.name())
                .origin(request.origin())
                .destination(request.destination())
                .defaultValue(request.defaultValue())
                .build();

        Store saved = storeRepository.save(store);

        return StoreResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<StoreResponseDTO> listStores(Pageable pageable) {
        return storeRepository.findAll(pageable)
                .map(StoreResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public StoreResponseDTO findById(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException(id));

        return StoreResponseDTO.fromEntity(store);
    }

    @Transactional
    public StoreResponseDTO updateStore(UUID id, StoreRequestDTO request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException(id));

        if (!request.name().equals(store.getName())
                && storeRepository.existsByName(request.name())) {
            throw new StoreNameAlreadyExistsException(request.name());
        }

        store.setName(request.name());
        store.setOrigin(request.origin());
        store.setDestination(request.destination());
        store.setDefaultValue(request.defaultValue());

        Store updated = storeRepository.save(store);

        return StoreResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void deactivateStore(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException(id));

        store.setEnabled(false);
        storeRepository.save(store);
    }
    @Transactional
    public StoreResponseDTO activateStore(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new StoreNotFoundException(id));

        store.setEnabled(true);
        Store updated = storeRepository.save(store);

        return StoreResponseDTO.fromEntity(updated);
    }
}
