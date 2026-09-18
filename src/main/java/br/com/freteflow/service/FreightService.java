package br.com.freteflow.service;

import br.com.freteflow.dto.freight.FreightRequestDTO;
import br.com.freteflow.dto.freight.FreightResponseDTO;
import br.com.freteflow.entity.Driver;
import br.com.freteflow.entity.Freight;
import br.com.freteflow.entity.FreightStatus;
import br.com.freteflow.entity.Store;
import br.com.freteflow.entity.Vehicle;
import br.com.freteflow.exception.*;
import br.com.freteflow.repository.DriverRepository;
import br.com.freteflow.repository.FreightRepository;
import br.com.freteflow.repository.StoreRepository;
import br.com.freteflow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreightService {

    private final FreightRepository freightRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final StoreRepository storeRepository;
    private void validateSameOrigin(List<Store> stores) {
        long distinctOrigins = stores.stream()
                .map(Store::getOrigin)
                .distinct()
                .count();

        if (distinctOrigins > 1) {
            throw new MixedStoreOriginException();
        }
    }

    @Transactional
    public FreightResponseDTO createFreight(FreightRequestDTO request) {
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new DriverNotFoundException(request.driverId()));

        if (!driver.isEnabled()) throw new InactiveResourceException("Motorista");

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(request.vehicleId()));

        if (!vehicle.isEnabled()) throw new InactiveResourceException("Veículo");

        List<Store> stores = request.storeIds().stream()
                .map(id -> storeRepository.findById(id).orElseThrow(() -> new StoreNotFoundException(id)))
                .collect(Collectors.toList());

        stores.forEach(store -> {
            if (!store.isEnabled()) throw new InactiveResourceException("Loja: " + store.getName());
        });
        validateSameOrigin(stores);

        BigDecimal maxFreightValue = stores.stream()
                .map(Store::getDefaultValue)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        Freight freight = Freight.builder()
                .driver(driver)
                .vehicle(vehicle)
                .stores(stores)
                .freightValue(maxFreightValue)
                .freightDate(request.freightDate())
                .status(FreightStatus.DELIVERED)
                .build();

        Freight saved = freightRepository.save(freight);
        return FreightResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<FreightResponseDTO> listFreights(Pageable pageable) {
        return freightRepository.findAll(pageable).map(FreightResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public FreightResponseDTO findById(UUID id) {
        Freight freight = freightRepository.findById(id)
                .orElseThrow(() -> new FreightNotFoundException(id));
        return FreightResponseDTO.fromEntity(freight);
    }

    @Transactional
    public FreightResponseDTO updateFreight(UUID id, FreightRequestDTO request) {
        Freight freight = freightRepository.findById(id)
                .orElseThrow(() -> new FreightNotFoundException(id));

        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new DriverNotFoundException(request.driverId()));

        if (!driver.isEnabled()) throw new InactiveResourceException("Motorista");

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(request.vehicleId()));

        if (!vehicle.isEnabled()) throw new InactiveResourceException("Veículo");

        List<Store> stores = request.storeIds().stream()
                .map(storeId -> storeRepository.findById(storeId).orElseThrow(() -> new StoreNotFoundException(storeId)))
                .collect(Collectors.toList());

        stores.forEach(store -> {
            if (!store.isEnabled()) throw new InactiveResourceException("Loja: " + store.getName());
        });
        validateSameOrigin(stores);

        BigDecimal maxFreightValue = stores.stream()
                .map(Store::getDefaultValue)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        freight.setDriver(driver);
        freight.setVehicle(vehicle);
        freight.setStores(stores);
        freight.setFreightValue(maxFreightValue);
        freight.setFreightDate(request.freightDate());

        Freight updated = freightRepository.save(freight);
        return FreightResponseDTO.fromEntity(updated);
    }

    @Transactional
    public FreightResponseDTO updateStatus(UUID id, FreightStatus newStatus) {
         Freight freight = freightRepository.findById(id)
            .orElseThrow(() -> new FreightNotFoundException(id));

        if (!isAdmin() && !freight.getStatus().canTransitionTo(newStatus)) {
        throw new InvalidFreightStatusTransitionException(freight.getStatus(), newStatus);
         }

        freight.setStatus(newStatus);
        Freight updated = freightRepository.save(freight);
        return FreightResponseDTO.fromEntity(updated);
    }

    private boolean isAdmin() {
    User currentUser = (User) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        return currentUser.getRole() == UserRole.ADMIN;
    }
}