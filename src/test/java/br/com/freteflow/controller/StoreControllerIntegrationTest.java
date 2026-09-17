package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoreControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void adminShouldCreateStoreSuccessfully() throws Exception {
        String token = createAdminAndGetToken("admin-store-1@freteflow.com");

        String payload = """
                {
                  "name": "Loja Campinas",
                  "origin": "Campinas",
                  "destination": "São Paulo",
                  "defaultValue": 1800.00
                }
                """;

        mockMvc.perform(post("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Loja Campinas"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void operatorShouldBeForbiddenFromCreatingStore() throws Exception {
        String token = createOperatorAndGetToken("operator-store-1@freteflow.com");

        String payload = """
                {
                  "name": "Loja Sorocaba",
                  "origin": "Sorocaba",
                  "destination": "São Paulo",
                  "defaultValue": 1500.00
                }
                """;

        mockMvc.perform(post("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestShouldBeRejected() throws Exception {
        String payload = """
                {
                  "name": "Loja Santos",
                  "origin": "Santos",
                  "destination": "São Paulo",
                  "defaultValue": 2000.00
                }
                """;

        mockMvc.perform(post("/api/stores")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void bothRolesShouldBeAbleToListStores() throws Exception {
        String adminToken = createAdminAndGetToken("admin-store-2@freteflow.com");
        String operatorToken = createOperatorAndGetToken("operator-store-2@freteflow.com");

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectDuplicateStoreName() throws Exception {
        String token = createAdminAndGetToken("admin-store-3@freteflow.com");

        String payload = """
                {
                  "name": "Loja Guarulhos",
                  "origin": "Guarulhos",
                  "destination": "São Paulo",
                  "defaultValue": 900.00
                }
                """;

        mockMvc.perform(post("/api/stores")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(payload));

        mockMvc.perform(post("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectZeroOrNegativeDefaultValue() throws Exception {
        String token = createAdminAndGetToken("admin-store-4@freteflow.com");

        String payload = """
                {
                  "name": "Loja Osasco",
                  "origin": "Osasco",
                  "destination": "São Paulo",
                  "defaultValue": 0
                }
                """;

        mockMvc.perform(post("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void shouldReturnNotFoundForNonExistentStore() throws Exception {
        String token = createAdminAndGetToken("admin-store-5@freteflow.com");

        mockMvc.perform(get("/api/stores/123e4567-e89b-12d3-a456-426614174000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminShouldDeactivateAndReactivateStore() throws Exception {
        String token = createAdminAndGetToken("admin-store-6@freteflow.com");

        String payload = """
                {
                  "name": "Loja Diadema",
                  "origin": "Diadema",
                  "destination": "São Paulo",
                  "defaultValue": 700.00
                }
                """;

        String response = mockMvc.perform(post("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(payload))
                .andReturn().getResponse().getContentAsString();

        String id = response.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/stores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/stores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(patch("/api/stores/" + id + "/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void shouldFilterStoresByNamePartialMatch() throws Exception {
        String token = createAdminAndGetToken("admin-store-filter-1@freteflow.com");

        mockMvc.perform(post("/api/stores")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                          "name": "Loja Filtro Nome Unico",
                          "origin": "Origem X",
                          "destination": "Destino X",
                          "defaultValue": 500.00
                        }
                        """));

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "filtro nome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name=='Loja Filtro Nome Unico')]").exists());
    }

    @Test
    void shouldFilterStoresByEnabledStatus() throws Exception {
        String token = createAdminAndGetToken("admin-store-filter-2@freteflow.com");

        String response = mockMvc.perform(post("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Loja Filtro Status",
                                  "origin": "Origem Y",
                                  "destination": "Destino Y",
                                  "defaultValue": 600.00
                                }
                                """))
                .andReturn().getResponse().getContentAsString();

        String id = response.split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(delete("/api/stores/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .param("enabled", "false")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name=='Loja Filtro Status')].enabled")
                        .value(org.hamcrest.Matchers.contains(false)));
    }

    @Test
    void shouldFilterStoresByValueRange() throws Exception {
        String token = createAdminAndGetToken("admin-store-filter-3@freteflow.com");

        mockMvc.perform(post("/api/stores")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                          "name": "Loja Faixa De Valor",
                          "origin": "Origem Z",
                          "destination": "Destino Z",
                          "defaultValue": 1234.56
                        }
                        """));

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .param("minValue", "1000")
                        .param("maxValue", "1500")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name=='Loja Faixa De Valor')]").exists());
    }

    @Test
    void shouldReturnEmptyWhenFilterMatchesNothing() throws Exception {
        String token = createAdminAndGetToken("admin-store-filter-4@freteflow.com");

        mockMvc.perform(get("/api/stores")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "nome-que-nao-existe-em-lugar-nenhum-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}