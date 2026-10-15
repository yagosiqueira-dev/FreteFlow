package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import br.com.freteflow.entity.User;
import br.com.freteflow.security.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LoginAttemptService loginAttemptService;

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

    @Test
    void shouldAllowValidLoginAfterFiveAnonymousFailures() throws Exception {
        String email = "lockout-resistance@freteflow.com";
        createOperatorAndGetToken(email);

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/auth/login")
                            .contentType("application/json")
                            .content("""
                                    {"email":"%s","password":"wrong"}
                                    """.formatted(email)))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"senha123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void arbitraryEmailTrackingSaturationDoesNotBlockLegitimateAuthentication() throws Exception {
        String email = "capacity-legitimate@freteflow.com";
        createOperatorAndGetToken(email);
        java.util.List<String> arbitraryEmails = new java.util.ArrayList<>();
        try {
            for (int index = 0; index < 10_000; index++) {
                String arbitrary = "arbitrary-" + index + "@example.invalid";
                arbitraryEmails.add(arbitrary);
                loginAttemptService.loginFailed(arbitrary);
            }

            mockMvc.perform(post("/auth/login")
                            .contentType("application/json")
                            .content("""
                                    {"email":"%s","password":"senha123"}
                                    """.formatted(email)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty());
        } finally {
            arbitraryEmails.forEach(loginAttemptService::loginSucceeded);
        }
    }

    @Test
    void invalidAndDisabledAccountResponsesDoNotRevealAccountState() throws Exception {
        String knownEmail = "known-invalid-login@freteflow.com";
        createOperatorAndGetToken(knownEmail);
        String disabledEmail = "disabled-login@freteflow.com";
        createOperatorAndGetToken(disabledEmail);
        User disabledUser = userRepository.findByEmail(disabledEmail).orElseThrow();
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        var disabledResponse = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"senha123"}
                                """.formatted(disabledEmail)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        var knownWrongPasswordResponse = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"wrong"}
                                """.formatted(knownEmail)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        var unknownResponse = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"not-registered@freteflow.com","password":"senha123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String disabledMessage = com.jayway.jsonpath.JsonPath.read(
                disabledResponse.getResponse().getContentAsString(), "$.message");
        String unknownMessage = com.jayway.jsonpath.JsonPath.read(
                unknownResponse.getResponse().getContentAsString(), "$.message");
        String knownWrongPasswordMessage = com.jayway.jsonpath.JsonPath.read(
                knownWrongPasswordResponse.getResponse().getContentAsString(), "$.message");
        assertThat(disabledMessage).isEqualTo("E-mail ou senha inválidos");
        assertThat(unknownMessage).isEqualTo(disabledMessage);
        assertThat(knownWrongPasswordMessage).isEqualTo(disabledMessage);
        loginAttemptService.loginSucceeded(knownEmail);
    }
}
