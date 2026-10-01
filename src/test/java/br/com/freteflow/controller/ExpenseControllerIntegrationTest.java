package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExpenseControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void operatorShouldCreateAllAllowedExpenseTypesSuccessfully() throws Exception {
        String adminToken = createAdminAndGetToken("admin-expense-1@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-expense-1@freteflow.com");

        String vehiclePayload = """
                {
                  "licensePlate": "ABC1D23",
                  "type": "Carreta",
                  "model": "Volvo FH",
                  "year": 2020
                }
                """;

        String vehicleResponse = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(vehiclePayload))
                .andReturn().getResponse().getContentAsString();

        String vehicleId = vehicleResponse.split("\"id\":\"")[1].split("\"")[0];

        String dieselPayload = """
                { "vehicleId": "%s", "description": "Diesel", "amount": 1500.50, "expenseDate": "2026-08-26" }
                """.formatted(vehicleId);

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(dieselPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Diesel"));

        String pedagioPayload = """
                { "vehicleId": "%s", "description": "Pedágio", "amount": 120.00, "expenseDate": "2026-08-26" }
                """.formatted(vehicleId);

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(pedagioPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Pedágio"));

        String manutencaoPayload = """
                { "vehicleId": "%s", "description": "Manutenção", "amount": 500.00, "expenseDate": "2026-08-26" }
                """.formatted(vehicleId);

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(manutencaoPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Manutenção"));
    }

    @Test
    void shouldBlockInvalidExpenseCategories() throws Exception {
        String operatorToken = createOperatorAndGetToken("operator-expense-2@freteflow.com");

        String expensePayload = """
                {
                  "vehicleId": "123e4567-e89b-12d3-a456-426614174000",
                  "description": "Almoço",
                  "amount": 50.00,
                  "expenseDate": "2026-08-26"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(expensePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenVehicleDoesNotExist() throws Exception {
        String operatorToken = createOperatorAndGetToken("operator-expense-3@freteflow.com");

        String expensePayload = """
                {
                  "vehicleId": "123e4567-e89b-12d3-a456-426614174000",
                  "description": "Manutenção",
                  "amount": 350.00,
                  "expenseDate": "2026-08-26"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(expensePayload))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldUpdateExpenseSuccessfully() throws Exception {
        String adminToken = createAdminAndGetToken("admin-expense-update@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-expense-update@freteflow.com");

        String vehiclePayload = """
                {
                  "licensePlate": "UPD1D23",
                  "type": "Caminhão",
                  "model": "Scania",
                  "year": 2022
                }
                """;

        String vehicleResponse = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(vehiclePayload))
                .andReturn().getResponse().getContentAsString();

        String vehicleId = vehicleResponse.split("\"id\":\"")[1].split("\"")[0];

        String expensePayload = """
                { "vehicleId": "%s", "description": "Pedágio", "amount": 50.00, "expenseDate": "2026-10-01" }
                """.formatted(vehicleId);

        String expenseResponse = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(expensePayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String expenseId = expenseResponse.split("\"id\":\"")[1].split("\"")[0];

        String updatePayload = """
                { "vehicleId": "%s", "description": "Manutenção", "amount": 750.00, "expenseDate": "2026-10-02" }
                """.formatted(vehicleId);

        mockMvc.perform(put("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(updatePayload))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Manutenção"))
                .andExpect(jsonPath("$.amount").value(750.00));
    }

    @Test
    void shouldListExpensesPaginated() throws Exception {
        String operatorToken = createOperatorAndGetToken("operator-expense-list@freteflow.com");

        mockMvc.perform(get("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }
    @Test
    void shouldDeactivateExpenseSuccessfully() throws Exception {
        String adminToken = createAdminAndGetToken("admin-expense-delete@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-expense-delete@freteflow.com");

        String vehiclePayload = """
                {
                  "licensePlate": "DEL1D23",
                  "type": "Caminhão",
                  "model": "Volvo",
                  "year": 2021
                }
                """;

        String vehicleResponse = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(vehiclePayload))
                .andReturn().getResponse().getContentAsString();

        String vehicleId = vehicleResponse.split("\"id\":\"")[1].split("\"")[0];

        String expensePayload = """
                { "vehicleId": "%s", "description": "Manutenção", "amount": 100.00, "expenseDate": "2026-10-01" }
                """.formatted(vehicleId);

        String expenseResponse = mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(expensePayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String expenseId = expenseResponse.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/expenses/" + expenseId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isNoContent());
    }
}