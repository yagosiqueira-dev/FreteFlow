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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FreightService {

    private final FreightRepository freightRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public FreightResponseDTO createFreight(FreightRequestDTO request) {
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new DriverNotFoundException(request.driverId()));

        if (!driver.isEnabled()) {
            throw new InactiveResourceException("Motorista");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(request.vehicleId()));

        if (!vehicle.isEnabled()) {
            throw new InactiveResourceException("Veículo");
        }

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new StoreNotFoundException(request.storeId()));

        if (!store.isEnabled()) {
            throw new InactiveResourceException("Loja");
        }

        Freight freight = Freight.builder()
                .driver(driver)
                .vehicle(vehicle)
                .store(store)
                .freightValue(store.getDefaultValue())
                .freightDate(request.freightDate())
                .status(FreightStatus.PENDING)
                .build();

        Freight saved = freightRepository.save(freight);

        return FreightResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<FreightResponseDTO> listFreights(Pageable pageable) {
        return freightRepository.findAll(pageable)
                .map(FreightResponseDTO::fromEntity);
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

        if (!driver.isEnabled()) {
            throw new InactiveResourceException("Motorista");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(request.vehicleId()));

        if (!vehicle.isEnabled()) {
            throw new InactiveResourceException("Veículo");
        }

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new StoreNotFoundException(request.storeId()));

        if (!store.isEnabled()) {
            throw new InactiveResourceException("Loja");
        }

        freight.setDriver(driver);
        freight.setVehicle(vehicle);
        freight.setStore(store);
        freight.setFreightValue(store.getDefaultValue());
        freight.setFreightDate(request.freightDate());

        Freight updated = freightRepository.save(freight);

        return FreightResponseDTO.fromEntity(updated);
    }

    @Transactional
    public FreightResponseDTO updateStatus(UUID id, FreightStatus newStatus) {
        Freight freight = freightRepository.findById(id)
                .orElseThrow(() -> new FreightNotFoundException(id));

        if (!freight.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidFreightStatusTransitionException(freight.getStatus(), newStatus);
        }

        freight.setStatus(newStatus);
        Freight updated = freightRepository.save(freight);

        return FreightResponseDTO.fromEntity(updated);
    }
}