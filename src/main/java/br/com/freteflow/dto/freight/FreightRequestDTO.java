package br.com.freteflow.dto.freight;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FreightRequestDTO(
        @NotNull(message = "O ID do motorista é obrigatório")
        UUID driverId,

        @NotNull(message = "O ID do veículo é obrigatório")
        UUID vehicleId,

        @NotNull(message = "O ID da loja é obrigatório")
        UUID storeId,

        @NotNull(message = "A data do frete é obrigatória")
        LocalDateTime freightDate
) {}