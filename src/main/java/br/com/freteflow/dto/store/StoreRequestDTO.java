package br.com.freteflow.dto.store;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StoreRequestDTO(

        @NotBlank(message = "O nome da loja é obrigatório")
        String name,

        @NotBlank(message = "A origem é obrigatória")
        String origin,

        @NotBlank(message = "O destino é obrigatório")
        String destination,

        @NotNull(message = "O valor padrão do frete é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor padrão deve ser maior que zero")
        BigDecimal defaultValue

) {
}