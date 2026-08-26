package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import br.com.freteflow.entity.Driver;
import br.com.freteflow.entity.Store;
import br.com.freteflow.entity.Vehicle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class FreightControllerIntegrationTest extends AbstractIntegrationTest {

    private static String generateValidCpf() {
        Random random = new Random();
        int[] base = new int[9];
        for (int i = 0; i < 9; i++) {
            base[i] = random.nextInt(10);
        }
        int d1 = calculateDigit(base, 10, 9);
        int[] withD1 = new int[10];
        System.arraycopy(base, 0, withD1, 0, 9);
        withD1[9] = d1;
        int d2 = calculateDigit(withD1, 11, 10);

        StringBuilder sb = new StringBuilder();
        for (int d : base) sb.append(d);
        sb.append(d1).append(d2);
        return sb.toString();
    }

    private static int calculateDigit(int[] digits, int startWeight, int length) {
        int sum = 0;
        int weight = startWeight;
        for (int i = 0; i < length; i++) {
            sum += digits[i] * weight--;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static String uniquePlate() {
        return "TST" + (1000 + new Random().nextInt(9000));
    }

    private String freightRequestJson(UUID driverId, UUID vehicleId, UUID storeId, String freightDate) {
        return """
                {
                    "driverId": "%s",
                    "vehicleId": "%s",
                    "storeId": "%s",
                    "freightDate": "%s"
                }
                """.formatted(driverId, vehicleId, storeId, freightDate);
    }

    private String now() {
        return LocalDateTime.now().withNano(0).toString();
    }

    @Test
    void shouldCreateFreightSuccessfully_whenAdmin() throws Exception {
        String token = createAdminAndGetToken("admin-freight-1@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja Create Admin", new BigDecimal("450.00"), true);

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.freightValue").value(450.00))
                .andExpect(jsonPath("$.driverId").value(driver.getId().toString()))
                .andExpect(jsonPath("$.vehicleId").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.storeId").value(store.getId().toString()));
    }

    @Test
    void shouldCreateFreightSuccessfully_whenOperator() throws Exception {
        String token = createOperatorAndGetToken("operator-freight-1@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja Create Operator", new BigDecimal("300.00"), true);

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldInheritFreightValueFromStoreDefaultValue() throws Exception {
        String token = createAdminAndGetToken("admin-freight-value@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja Valor Padrao", new BigDecimal("777.77"), true);

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.freightValue").value(777.77));
    }

    @Test
    void shouldReturnConflict_whenDriverIsDisabled() throws Exception {
        String token = createAdminAndGetToken("admin-freight-driver-disabled@test.com");
        Driver driver = createDriver(generateValidCpf(), false);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja Driver Disabled", new BigDecimal("100.00"), true);

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Motorista está desativado(a) e não pode ser usado(a) em um novo frete"));
    }

    @Test
    void shouldReturnConflict_whenVehicleIsDisabled() throws Exception {
        String token = createAdminAndGetToken("admin-freight-vehicle-disabled@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), false);
        Store store = createStore("Loja Vehicle Disabled", new BigDecimal("100.00"), true);

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Veículo está desativado(a) e não pode ser usado(a) em um novo frete"));
    }

    @Test
    void shouldReturnConflict_whenStoreIsDisabled() throws Exception {
        String token = createAdminAndGetToken("admin-freight-store-disabled@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja Store Disabled", new BigDecimal("100.00"), false);

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Loja está desativado(a) e não pode ser usado(a) em um novo frete"));
    }

    @Test
    void shouldReturnNotFound_whenDriverDoesNotExist() throws Exception {
        String token = createAdminAndGetToken("admin-freight-no-driver@test.com");
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja No Driver", new BigDecimal("100.00"), true);
        UUID fakeDriverId = UUID.randomUUID();

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(fakeDriverId, vehicle.getId(), store.getId(), now())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound_whenVehicleDoesNotExist() throws Exception {
        String token = createAdminAndGetToken("admin-freight-no-vehicle@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Store store = createStore("Loja No Vehicle", new BigDecimal("100.00"), true);
        UUID fakeVehicleId = UUID.randomUUID();

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), fakeVehicleId, store.getId(), now())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound_whenStoreDoesNotExist() throws Exception {
        String token = createAdminAndGetToken("admin-freight-no-store@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        UUID fakeStoreId = UUID.randomUUID();

        mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), fakeStoreId, now())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindFreightById() throws Exception {
        String token = createAdminAndGetToken("admin-freight-find@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja Find", new BigDecimal("200.00"), true);

        String response = mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/freights/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void shouldReturnNotFound_whenFreightIdDoesNotExist() throws Exception {
        String token = createAdminAndGetToken("admin-freight-find-404@test.com");

        mockMvc.perform(get("/api/freights/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListFreightsPaginated() throws Exception {
        String token = createAdminAndGetToken("admin-freight-list@test.com");
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja List", new BigDecimal("150.00"), true);

        mockMvc.perform(post("/api/freights")
                .header("Authorization", "Bearer " + token)
                .contentType(APPLICATION_JSON)
                .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())));

        mockMvc.perform(get("/api/freights")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldTransitionStatus_pendingToInProgressToDelivered() throws Exception {
        String token = createAdminAndGetToken("admin-freight-transition-1@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Transition 1");

        mockMvc.perform(patch("/api/freights/{id}/status", freightId)
                        .header("Authorization", "Bearer " + token)
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/freights/{id}/status", freightId)
                        .header("Authorization", "Bearer " + token)
                        .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void shouldTransitionStatus_pendingToCanceled() throws Exception {
        String token = createAdminAndGetToken("admin-freight-transition-2@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Transition 2");

        mockMvc.perform(patch("/api/freights/{id}/status", freightId)
                        .header("Authorization", "Bearer " + token)
                        .param("status", "CANCELED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturnConflict_whenTransitioningFromFinalState() throws Exception {
        String token = createAdminAndGetToken("admin-freight-transition-3@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Transition 3");

        mockMvc.perform(patch("/api/freights/{id}/status", freightId)
                .header("Authorization", "Bearer " + token)
                .param("status", "CANCELED"));

        mockMvc.perform(patch("/api/freights/{id}/status", freightId)
                        .header("Authorization", "Bearer " + token)
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Não é possível mudar o status de CANCELED para IN_PROGRESS"));
    }

    @Test
    void shouldReturnConflict_whenSkippingStatus() throws Exception {
        String token = createAdminAndGetToken("admin-freight-transition-4@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Transition 4");

        mockMvc.perform(patch("/api/freights/{id}/status", freightId)
                        .header("Authorization", "Bearer " + token)
                        .param("status", "DELIVERED"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Não é possível mudar o status de PENDING para DELIVERED"));
    }

    @Test
    void shouldUpdateFreightSuccessfully() throws Exception {
        String token = createAdminAndGetToken("admin-freight-update@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Update Original");

        Driver newDriver = createDriver(generateValidCpf(), true);
        Vehicle newVehicle = createVehicle(uniquePlate(), true);
        Store newStore = createStore("Loja Update Nova", new BigDecimal("999.00"), true);

        mockMvc.perform(put("/api/freights/{id}", freightId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(newDriver.getId(), newVehicle.getId(), newStore.getId(), now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(newDriver.getId().toString()))
                .andExpect(jsonPath("$.freightValue").value(999.00));
    }
    @Test
    void shouldReturnConflict_whenUpdatingToDisabledDriver() throws Exception {
        String token = createAdminAndGetToken("admin-freight-update-driver-disabled@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Update Driver Disabled");

        Driver disabledDriver = createDriver(generateValidCpf(), false);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore("Loja Update Driver Disabled 2", new BigDecimal("100.00"), true);

        mockMvc.perform(put("/api/freights/{id}", freightId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(disabledDriver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Motorista está desativado(a) e não pode ser usado(a) em um novo frete"));
    }

    @Test
    void shouldReturnConflict_whenUpdatingToDisabledVehicle() throws Exception {
        String token = createAdminAndGetToken("admin-freight-update-vehicle-disabled@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Update Vehicle Disabled");

        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle disabledVehicle = createVehicle(uniquePlate(), false);
        Store store = createStore("Loja Update Vehicle Disabled 2", new BigDecimal("100.00"), true);

        mockMvc.perform(put("/api/freights/{id}", freightId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), disabledVehicle.getId(), store.getId(), now())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Veículo está desativado(a) e não pode ser usado(a) em um novo frete"));
    }

    @Test
    void shouldReturnConflict_whenUpdatingToDisabledStore() throws Exception {
        String token = createAdminAndGetToken("admin-freight-update-store-disabled@test.com");
        UUID freightId = createFreightAndGetId(token, "Loja Update Store Disabled");

        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store disabledStore = createStore("Loja Update Store Disabled 2", new BigDecimal("100.00"), false);

        mockMvc.perform(put("/api/freights/{id}", freightId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), disabledStore.getId(), now())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Loja está desativado(a) e não pode ser usado(a) em um novo frete"));
    }

    private UUID createFreightAndGetId(String token, String storeName) throws Exception {
        Driver driver = createDriver(generateValidCpf(), true);
        Vehicle vehicle = createVehicle(uniquePlate(), true);
        Store store = createStore(storeName, new BigDecimal("500.00"), true);

        String response = mockMvc.perform(post("/api/freights")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(freightRequestJson(driver.getId(), vehicle.getId(), store.getId(), now())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = com.jayway.jsonpath.JsonPath.read(response, "$.id");
        return UUID.fromString(id);
    }
}
