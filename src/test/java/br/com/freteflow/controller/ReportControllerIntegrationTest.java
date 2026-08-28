package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldGenerateBiWeeklyReportSuccessfully() throws Exception {
        String adminToken = createAdminAndGetToken("admin-report-1@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-report-1@freteflow.com");

        String driverPayload = """
                {
                  "name": "Motorista Teste",
                  "cnh": "12345678900",
                  "cpf": "111.444.777-35",
                  "phone": "11999999999"
                }
                """;
        String driverResponse = mockMvc.perform(post("/api/drivers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(driverPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String driverId = driverResponse.split("\"id\":\"")[1].split("\"")[0];

        String vehiclePayload = """
                {
                  "licensePlate": "ABC1D23",
                  "type": "Carreta",
                  "model": "Modelo Teste",
                  "year": 2021
                }
                """;
        String vehicleResponse = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(vehiclePayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String vehicleId = vehicleResponse.split("\"id\":\"")[1].split("\"")[0];

        String storePayload = """
                {
                  "name": "Ceasa",
                  "origin": "São Paulo",
                  "destination": "Carapicuiba",
                  "defaultValue": 500.00
                }
                """;
        String storeResponse = mockMvc.perform(post("/api/stores")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(storePayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String storeId = storeResponse.split("\"id\":\"")[1].split("\"")[0];

        String freightPayload = """
                {
                  "driverId": "%s",
                  "vehicleId": "%s",
                  "storeId": "%s",
                  "freightValue": 850.00,
                  "freightDate": "2026-08-15T08:00:00"
                }
                """.formatted(driverId, vehicleId, storeId);

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(freightPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/driver/" + driverId + "/bi-weekly")
                        .header("Authorization", "Bearer " + operatorToken)
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25")
                        .param("driverName", "Motorista Teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverName").value("Motorista Teste"))
                .andExpect(jsonPath("$.totalAmount").value(500.00))
                .andExpect(jsonPath("$.freights").isArray())
                .andExpect(jsonPath("$.freights[0].loadingLocation").value("São Paulo"))
                .andExpect(jsonPath("$.freights[0].fullRoute").value("Carapicuiba"))
                .andExpect(jsonPath("$.freights[0].value").value(500.00));
    }
}