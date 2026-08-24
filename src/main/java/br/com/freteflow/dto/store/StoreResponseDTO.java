package br.com.freteflow.dto.store;

import br.com.freteflow.entity.Store;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StoreResponseDTO(
        UUID id,
        String name,
        String origin,
        String destination,
        BigDecimal defaultValue,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StoreResponseDTO fromEntity(Store store) {
        return new StoreResponseDTO(
                store.getId(),
                store.getName(),
                store.getOrigin(),
                store.getDestination(),
                store.getDefaultValue(),
                store.isEnabled(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}