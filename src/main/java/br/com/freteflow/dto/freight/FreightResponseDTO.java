package br.com.freteflow.dto.freight;

import br.com.freteflow.entity.Freight;
import br.com.freteflow.entity.Store;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record FreightResponseDTO(
        UUID id,
        UUID driverId,
        String driverName,
        UUID vehicleId,
        String vehiclePlate,
        List<String> storeNames,
        String origin,
        String destinations,
        BigDecimal freightValue,
        LocalDateTime freightDate,
        String status
) {
    public static FreightResponseDTO fromEntity(Freight freight) {
        List<String> names = freight.getStores().stream().map(Store::getName).toList();
        String origin = freight.getStores().isEmpty() ? "" : freight.getStores().get(0).getOrigin();
        String destinations = freight.getStores().stream()
                .map(Store::getDestination)
                .collect(Collectors.joining(", "));

        return new FreightResponseDTO(
                freight.getId(),
                freight.getDriver().getId(),
                freight.getDriver().getName(),
                freight.getVehicle().getId(),
                freight.getVehicle().getLicensePlate(),
                names,
                origin,
                destinations,
                freight.getFreightValue(),
                freight.getFreightDate(),
                freight.getStatus().name()
        );
    }
}