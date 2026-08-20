package br.com.freteflow.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

public record VehicleRequestDTO(

        @NotBlank(message = "A placa é obrigatória")
        @Pattern(
                regexp = "^[A-Za-z]{3}[0-9][A-Za-z0-9][0-9]{2}$",
                message = "Placa inválida. Use o formato Mercosul (ex: ABC1D23) ou padrão antigo (ex: ABC1234)"
        )
        String licensePlate,

        @NotBlank(message = "O tipo do veículo é obrigatório")
        String type,

        @NotBlank(message = "O modelo é obrigatório")
        String model,

        @NotNull(message = "O ano é obrigatório")
        @Min(value = 1970, message = "Ano inválido")
        Integer year

) {
}