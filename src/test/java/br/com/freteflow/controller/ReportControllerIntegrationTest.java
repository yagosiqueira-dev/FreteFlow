package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import br.com.freteflow.entity.Driver;
import br.com.freteflow.entity.Store;
import br.com.freteflow.entity.Vehicle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
    @Test
    void shouldCalculateVehicleProfitCorrectly() throws Exception {
        String adminToken = createAdminAndGetToken("admin-profit-1@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-profit-1@freteflow.com");

        Driver driver = createDriver("52998224725", true);
        Vehicle vehicle = createVehicle("PRF1A23", true);
        Store store = createStore("Loja Lucro Teste", new BigDecimal("1000.00"), true);

        String freightPayload = """
            {
              "driverId": "%s",
              "vehicleId": "%s",
              "storeId": "%s",
              "freightDate": "2026-08-15T08:00:00"
            }
            """.formatted(driver.getId(), vehicle.getId(), store.getId());

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(freightPayload))
                .andExpect(status().isCreated());

        String expensePayload = """
            {
              "vehicleId": "%s",
              "description": "Diesel",
              "amount": 300.00,
              "expenseDate": "2026-08-16"
            }
            """.formatted(vehicle.getId());

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType("application/json")
                        .content(expensePayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reports/vehicle/" + vehicle.getId() + "/profit")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFreightValue").value(1000.00))
                .andExpect(jsonPath("$.totalExpenses").value(300.00))
                .andExpect(jsonPath("$.netProfit").value(700.00));
    }

    @Test
    void shouldReturnZeroProfitWhenNoFreightsOrExpenses() throws Exception {
        String token = createAdminAndGetToken("admin-profit-2@freteflow.com");
        Vehicle vehicle = createVehicle("PRF2B34", true);

        mockMvc.perform(get("/api/reports/vehicle/" + vehicle.getId() + "/profit")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFreightValue").value(0))
                .andExpect(jsonPath("$.netProfit").value(0));
    }

    @Test
    void shouldReturnNotFoundForNonExistentVehicle() throws Exception {
        String token = createAdminAndGetToken("admin-profit-3@freteflow.com");

        mockMvc.perform(get("/api/reports/vehicle/123e4567-e89b-12d3-a456-426614174000/profit")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isNotFound());
    }
    @Test
    void shouldReturnNotFoundForNonExistentDriverInBiWeeklyReport() throws Exception {
        String token = createAdminAndGetToken("admin-report-2@freteflow.com");

        mockMvc.perform(get("/api/reports/driver/123e4567-e89b-12d3-a456-426614174000/bi-weekly")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedRequestShouldBeRejectedForBiWeeklyReport() throws Exception {
        mockMvc.perform(get("/api/reports/driver/123e4567-e89b-12d3-a456-426614174000/bi-weekly")
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestShouldBeRejectedForVehicleProfitReport() throws Exception {
        mockMvc.perform(get("/api/reports/vehicle/123e4567-e89b-12d3-a456-426614174000/profit")
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSumMultipleFreightsAndExpensesCorrectly() throws Exception {
        String token = createAdminAndGetToken("admin-report-3@freteflow.com");

        Driver driver = createDriver("16899535009", true);
        Vehicle vehicle = createVehicle("MUL1T23", true);
        Store storeA = createStore("Loja Multi A", new BigDecimal("800.00"), true);
        Store storeB = createStore("Loja Multi B", new BigDecimal("650.00"), true);

        String freightPayloadA = """
            {
              "driverId": "%s",
              "vehicleId": "%s",
              "storeId": "%s",
              "freightDate": "2026-08-12T08:00:00"
            }
            """.formatted(driver.getId(), vehicle.getId(), storeA.getId());

        String freightPayloadB = """
            {
              "driverId": "%s",
              "vehicleId": "%s",
              "storeId": "%s",
              "freightDate": "2026-08-18T08:00:00"
            }
            """.formatted(driver.getId(), vehicle.getId(), storeB.getId());

        mockMvc.perform(post("/api/freights")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(freightPayloadA));

        mockMvc.perform(post("/api/freights")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(freightPayloadB));

        String expensePayloadA = """
            { "vehicleId": "%s", "description": "Diesel", "amount": 150.00, "expenseDate": "2026-08-13" }
            """.formatted(vehicle.getId());

        String expensePayloadB = """
            { "vehicleId": "%s", "description": "Pedágio", "amount": 45.00, "expenseDate": "2026-08-19" }
            """.formatted(vehicle.getId());

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(expensePayloadA));

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(expensePayloadB));

        mockMvc.perform(get("/api/reports/vehicle/" + vehicle.getId() + "/profit")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFreightValue").value(1450.00))
                .andExpect(jsonPath("$.totalExpenses").value(195.00))
                .andExpect(jsonPath("$.netProfit").value(1255.00))
                .andExpect(jsonPath("$.freights.length()").value(2))
                .andExpect(jsonPath("$.expenses.length()").value(2));
    }

    @Test
    void shouldExcludeFreightsAndExpensesOutsideDateRange() throws Exception {
        String token = createAdminAndGetToken("admin-report-4@freteflow.com");

        Driver driver = createDriver("15350946056", true);
        Vehicle vehicle = createVehicle("OUT1S23", true);
        Store store = createStore("Loja Fora do Periodo", new BigDecimal("900.00"), true);

        String freightInsideRange = """
            {
              "driverId": "%s",
              "vehicleId": "%s",
              "storeId": "%s",
              "freightDate": "2026-08-15T08:00:00"
            }
            """.formatted(driver.getId(), vehicle.getId(), store.getId());

        String freightOutsideRange = """
            {
              "driverId": "%s",
              "vehicleId": "%s",
              "storeId": "%s",
              "freightDate": "2026-09-01T08:00:00"
            }
            """.formatted(driver.getId(), vehicle.getId(), store.getId());

        mockMvc.perform(post("/api/freights")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(freightInsideRange));

        mockMvc.perform(post("/api/freights")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(freightOutsideRange));

        mockMvc.perform(get("/api/reports/vehicle/" + vehicle.getId() + "/profit")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2026-08-11")
                        .param("endDate", "2026-08-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFreightValue").value(900.00))
                .andExpect(jsonPath("$.freights.length()").value(1));
    }
}