package br.com.freteflow.dto.driver;

import br.com.freteflow.entity.Driver;

import java.time.LocalDateTime;
import java.util.UUID;

public record DriverResponseDTO(
        UUID id,
        String name,
        String phone,
        String cpf,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DriverResponseDTO fromEntity(Driver driver) {
        return new DriverResponseDTO(
                driver.getId(),
                driver.getName(),
                driver.getPhone(),
                driver.getCpf(),
                driver.isEnabled(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}