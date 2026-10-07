package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldRegisterUserAsOperatorRegardlessOfRoleInPayload() throws Exception {
        String adminToken = createAdminAndGetToken("admin-register@freteflow.com");
        String payload = """
                {
                  "name": "Usuário Teste",
                  "email": "teste-register@freteflow.com",
                  "password": "senha123",
                  "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk());

        var user = userRepository.findByEmail("teste-register@freteflow.com").orElseThrow();

        org.assertj.core.api.Assertions.assertThat(user.getRole().name()).isEqualTo("OPERATOR");
    }

    @Test
    void shouldRejectRegisterWithDuplicateEmail() throws Exception {
        String adminToken = createAdminAndGetToken("admin-duplicate@freteflow.com");
        String payload = """
                {
                  "name": "Usuário Duplicado",
                  "email": "duplicado@freteflow.com",
                  "password": "senha123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginWithValidCredentialsAndReturnToken() throws Exception {
        createOperatorAndGetToken("login-teste@freteflow.com");

        String loginPayload = """
                {
                  "email": "login-teste@freteflow.com",
                  "password": "senha123"
                }
                """;

        var result = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        org.assertj.core.api.Assertions.assertThat(tokenService.validateToken(token))
                .isEqualTo("login-teste@freteflow.com");
    }

    @Test
    void shouldRejectLoginWithWrongPassword() throws Exception {
        createOperatorAndGetToken("senha-errada@freteflow.com");

        String loginPayload = """
                {
                  "email": "senha-errada@freteflow.com",
                  "password": "senhaErrada"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(loginPayload))
                .andExpect(status().isUnauthorized());
    }
}