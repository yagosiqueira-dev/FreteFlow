package br.com.freteflow.dto.freight;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FreightRequestDTO(
        @NotNull(message = "O ID do motorista é obrigatório")
        UUID driverId,

        @NotNull(message = "O ID do veículo é obrigatório")
        UUID vehicleId,

        @NotEmpty(message = "Pelo menos uma loja deve ser informada")
        List<UUID> storeIds,

        @NotNull(message = "A data do frete é obrigatória")
        LocalDateTime freightDate
) {}