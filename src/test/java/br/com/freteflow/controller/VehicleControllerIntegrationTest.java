package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VehicleControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void adminShouldCreateVehicleSuccessfully() throws Exception {
        String token = createAdminAndGetToken("admin-vehicle-1@freteflow.com");

        String payload = """
                {
                  "licensePlate": "abc1d23",
                  "type": "Truck",
                  "model": "Volvo FH",
                  "year": 2022
                }
                """;

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("ABC1D23"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void operatorShouldBeForbiddenFromCreatingVehicle() throws Exception {
        String token = createOperatorAndGetToken("operator-vehicle-1@freteflow.com");

        String payload = """
                {
                  "licensePlate": "abc1d24",
                  "type": "Truck",
                  "model": "Volvo FH",
                  "year": 2022
                }
                """;

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestShouldBeRejected() throws Exception {
        String payload = """
                {
                  "licensePlate": "abc1d25",
                  "type": "Truck",
                  "model": "Volvo FH",
                  "year": 2022
                }
                """;

        mockMvc.perform(post("/api/vehicles")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void bothRolesShouldBeAbleToListVehicles() throws Exception {
        String adminToken = createAdminAndGetToken("admin-vehicle-2@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-vehicle-2@freteflow.com");

        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectDuplicateLicensePlate() throws Exception {
        String token = createAdminAndGetToken("admin-vehicle-3@freteflow.com");

        String payload = """
                {
                  "licensePlate": "XYZ9A87",
                  "type": "Van",
                  "model": "Fiorino",
                  "year": 2020
                }
                """;

        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(payload));

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidLicensePlateFormat() throws Exception {
        String token = createAdminAndGetToken("admin-vehicle-4@freteflow.com");

        String payload = """
                {
                  "licensePlate": "123",
                  "type": "Truck",
                  "model": "Volvo FH",
                  "year": 2022
                }
                """;

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForNonExistentVehicle() throws Exception {
        String token = createAdminAndGetToken("admin-vehicle-5@freteflow.com");

        mockMvc.perform(get("/api/vehicles/123e4567-e89b-12d3-a456-426614174000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminShouldDeactivateAndReactivateVehicle() throws Exception {
        String token = createAdminAndGetToken("admin-vehicle-6@freteflow.com");

        String payload = """
                {
                  "licensePlate": "DEA1C23",
                  "type": "Truck",
                  "model": "Scania",
                  "year": 2021
                }
                """;

        String response = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andReturn().getResponse().getContentAsString();

        String id = response.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(patch("/api/vehicles/" + id + "/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
}