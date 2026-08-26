package br.com.freteflow.service;

import br.com.freteflow.dto.driver.DriverRequestDTO;
import br.com.freteflow.dto.driver.DriverResponseDTO;
import br.com.freteflow.entity.Driver;
import br.com.freteflow.exception.CpfAlreadyExistsException;
import br.com.freteflow.exception.DriverNotFoundException;
import br.com.freteflow.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional
    public DriverResponseDTO createDriver(DriverRequestDTO request) {
        String normalizedCpf = request.cpf().replaceAll("[^0-9]", "");

        if (driverRepository.existsByCpf(normalizedCpf)) {
            throw new CpfAlreadyExistsException(normalizedCpf);
        }

        Driver driver = Driver.builder()
                .name(request.name())
                .phone(request.phone())
                .cpf(normalizedCpf)
                .build();

        Driver saved = driverRepository.save(driver);

        return DriverResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> listDrivers(Pageable pageable) {
        return driverRepository.findAll(pageable)
                .map(DriverResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> searchByName(String name, Pageable pageable) {
        return driverRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(DriverResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public DriverResponseDTO findById(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));

        return DriverResponseDTO.fromEntity(driver);
    }

    @Transactional
    public DriverResponseDTO updateDriver(UUID id, DriverRequestDTO request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));

        String normalizedCpf = request.cpf().replaceAll("[^0-9]", "");

        if (!normalizedCpf.equals(driver.getCpf())
                && driverRepository.existsByCpf(normalizedCpf)) {
            throw new CpfAlreadyExistsException(normalizedCpf);
        }

        driver.setName(request.name());
        driver.setPhone(request.phone());
        driver.setCpf(normalizedCpf);

        Driver updated = driverRepository.save(driver);

        return DriverResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void deactivateDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));

        driver.setEnabled(false);
        driverRepository.save(driver);
    }
    @Transactional
    public DriverResponseDTO activateDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException(id));

        driver.setEnabled(true);
        Driver updated = driverRepository.save(driver);

        return DriverResponseDTO.fromEntity(updated);
    }
}