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
                maskPhone(driver.getPhone()),
                maskCpf(driver.getCpf()),
                driver.isEnabled(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }


    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }


    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) {
            return phone;
        }


        String numeric = phone.replaceAll("[^0-9]", "");


        if (numeric.length() == 11) {
            return "(" + numeric.substring(0, 2) + ") *****-" + numeric.substring(7);
        }

        else if (numeric.length() == 10) {
            return "(" + numeric.substring(0, 2) + ") ****-" + numeric.substring(6);
        }

        return phone;
    }
}