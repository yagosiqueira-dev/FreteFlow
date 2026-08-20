package br.com.freteflow.service;

import br.com.freteflow.dto.vehicle.VehicleRequestDTO;
import br.com.freteflow.dto.vehicle.VehicleResponseDTO;
import br.com.freteflow.entity.Vehicle;
import br.com.freteflow.exception.LicensePlateAlreadyExistsException;
import br.com.freteflow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import br.com.freteflow.exception.VehicleNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> listVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable)
                .map(VehicleResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO findById(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        return VehicleResponseDTO.fromEntity(vehicle);
    }

    @Transactional
    public VehicleResponseDTO createVehicle(VehicleRequestDTO request) {
        String normalizedPlate = request.licensePlate().toUpperCase();

        if (vehicleRepository.existsByLicensePlate(normalizedPlate)) {
            throw new LicensePlateAlreadyExistsException(normalizedPlate);
        }

        Vehicle vehicle = Vehicle.builder()
                .licensePlate(normalizedPlate)
                .type(request.type())
                .model(request.model())
                .year(request.year())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);

        return VehicleResponseDTO.fromEntity(saved);
    }
    @Transactional
    public VehicleResponseDTO updateVehicle(UUID id, VehicleRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        String normalizedPlate = request.licensePlate().toUpperCase();

        if (!normalizedPlate.equals(vehicle.getLicensePlate())
                && vehicleRepository.existsByLicensePlate(normalizedPlate)) {
            throw new LicensePlateAlreadyExistsException(normalizedPlate);
        }

        vehicle.setLicensePlate(normalizedPlate);
        vehicle.setType(request.type());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());

        Vehicle updated = vehicleRepository.save(vehicle);

        return VehicleResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void deactivateVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        vehicle.setEnabled(false);
        vehicleRepository.save(vehicle);
    }
}