package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private String extractUserIdByEmail(String responseBody, String email) {
        List<String> ids = com.jayway.jsonpath.JsonPath.read(
                responseBody,
                "$.content[?(@.email=='" + email + "')].id"
        );
        return ids.get(0);
    }

    @Test
    void adminShouldListUsers() throws Exception {
        String token = createAdminAndGetToken("admin-user-list@test.com");
        createOperatorAndGetToken("operator-user-list@test.com");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void operatorShouldBeForbiddenFromListingUsers() throws Exception {
        String token = createOperatorAndGetToken("operator-user-forbidden@test.com");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldUpdateUserSuccessfully() throws Exception {
        String adminToken = createAdminAndGetToken("admin-user-update@test.com");
        createOperatorAndGetToken("operator-user-update-target@test.com");

        String responseBody = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "50"))
                .andReturn().getResponse().getContentAsString();

        String targetId = extractUserIdByEmail(responseBody, "operator-user-update-target@test.com");

        String payload = """
                {
                  "name": "Nome Atualizado",
                  "email": "operator-user-update-target@test.com"
                }
                """;

        mockMvc.perform(put("/api/users/{id}", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"));
    }

    @Test
    void shouldReturnConflict_whenUpdatingToDuplicateEmail() throws Exception {
        String adminToken = createAdminAndGetToken("admin-user-dup@test.com");

        String payload = """
                {
                  "name": "Tentativa Duplicada",
                  "email": "admin-user-dup@test.com"
                }
                """;

        String adminListResponse = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "50"))
                .andReturn().getResponse().getContentAsString();

        String otherId = extractUserIdByEmail(adminListResponse, "admin-user-dup@test.com");

        // Cria um segundo usuário e tenta atualizar seu e-mail para o do admin acima
        createOperatorAndGetToken("operator-user-dup-target@test.com");

        String updatedListResponse = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "50"))
                .andReturn().getResponse().getContentAsString();

        String targetId = extractUserIdByEmail(updatedListResponse, "operator-user-dup-target@test.com");

        mockMvc.perform(put("/api/users/{id}", targetId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void adminShouldDeactivateAndReactivateOtherUser() throws Exception {
        String adminToken = createAdminAndGetToken("admin-user-deact@test.com");
        createOperatorAndGetToken("operator-user-deact-target@test.com");

        String responseBody = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "50"))
                .andReturn().getResponse().getContentAsString();

        String targetId = extractUserIdByEmail(responseBody, "operator-user-deact-target@test.com");

        mockMvc.perform(delete("/api/users/{id}", targetId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", targetId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(patch("/api/users/{id}/activate", targetId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void adminCannotDeactivateSelf() throws Exception {
        String adminToken = createAdminAndGetToken("admin-user-self@test.com");

        String responseBody = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "50"))
                .andReturn().getResponse().getContentAsString();

        String selfId = extractUserIdByEmail(responseBody, "admin-user-self@test.com");

        mockMvc.perform(delete("/api/users/{id}", selfId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Você não pode desativar sua própria conta"));
    }

    @Test
    void shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        String token = createAdminAndGetToken("admin-user-404@test.com");

        mockMvc.perform(get("/api/users/123e4567-e89b-12d3-a456-426614174000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}