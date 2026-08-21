package br.com.freteflow.dto.driver;

import br.com.freteflow.validation.ValidCPF;
import jakarta.validation.constraints.NotBlank;

public record DriverRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "O telefone é obrigatório")
        String phone,

        @NotBlank(message = "O CPF é obrigatório")
        @ValidCPF
        String cpf

) {
}
