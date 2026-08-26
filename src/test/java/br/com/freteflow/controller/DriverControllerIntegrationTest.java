package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DriverControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void adminShouldCreateDriverSuccessfully() throws Exception {
        String token = createAdminAndGetToken("admin-driver-1@freteflow.com");

        String payload = """
                {
                  "name": "João da Silva",
                  "phone": "11987654321",
                  "cpf": "111.444.777-35"
                }
                """;

        mockMvc.perform(post("/api/drivers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("11144477735"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void operatorShouldBeForbiddenFromCreatingDriver() throws Exception {
        String token = createOperatorAndGetToken("operator-driver-1@freteflow.com");

        String payload = """
                {
                  "name": "Maria Souza",
                  "phone": "11912345678",
                  "cpf": "529.982.247-25"
                }
                """;

        mockMvc.perform(post("/api/drivers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestShouldBeRejected() throws Exception {
        String payload = """
                {
                  "name": "Pedro Santos",
                  "phone": "11911112222",
                  "cpf": "168.995.350-09"
                }
                """;

        mockMvc.perform(post("/api/drivers")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void bothRolesShouldBeAbleToListDrivers() throws Exception {
        String adminToken = createAdminAndGetToken("admin-driver-2@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-driver-2@freteflow.com");

        mockMvc.perform(get("/api/drivers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/drivers")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectDuplicateCpf() throws Exception {
        String token = createAdminAndGetToken("admin-driver-3@freteflow.com");

        String payload = """
                {
                  "name": "Carlos Lima",
                  "phone": "11933334444",
                  "cpf": "874.140.070-49"
                }
                """;

        mockMvc.perform(post("/api/drivers")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(payload));

        mockMvc.perform(post("/api/drivers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectMathematicallyInvalidCpf() throws Exception {
        String token = createAdminAndGetToken("admin-driver-4@freteflow.com");

        String payload = """
                {
                  "name": "Ana Costa",
                  "phone": "11955556666",
                  "cpf": "111.111.111-11"
                }
                """;

        mockMvc.perform(post("/api/drivers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForNonExistentDriver() throws Exception {
        String token = createAdminAndGetToken("admin-driver-5@freteflow.com");

        mockMvc.perform(get("/api/drivers/123e4567-e89b-12d3-a456-426614174000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminShouldDeactivateAndReactivateDriver() throws Exception {
        String token = createAdminAndGetToken("admin-driver-6@freteflow.com");

        String payload = """
                {
                  "name": "Roberto Alves",
                  "phone": "11977778888",
                  "cpf": "637.469.370-19"
                }
                """;

        String response = mockMvc.perform(post("/api/drivers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andReturn().getResponse().getContentAsString();

        String id = response.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/drivers/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/drivers/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(patch("/api/drivers/" + id + "/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
}
