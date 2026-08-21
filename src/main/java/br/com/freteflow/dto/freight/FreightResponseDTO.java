package br.com.freteflow.dto.freight;

import br.com.freteflow.entity.Freight;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FreightResponseDTO(
        UUID id,
        UUID driverId,
        String driverName,
        UUID vehicleId,
        String vehiclePlate,
        UUID storeId,
        String storeName,
        String origin,
        String destination,
        BigDecimal freightValue,
        LocalDateTime freightDate,
        String status
) {
    public static FreightResponseDTO fromEntity(Freight freight) {
        return new FreightResponseDTO(
                freight.getId(),
                freight.getDriver().getId(),
                freight.getDriver().getName(),
                freight.getVehicle().getId(),
                freight.getVehicle().getLicensePlate(),
                freight.getStore().getId(),
                freight.getStore().getName(),
                freight.getStore().getOrigin(),
                freight.getStore().getDestination(),
                freight.getFreightValue(),
                freight.getFreightDate(),
                freight.getStatus().name()
        );
    }
}