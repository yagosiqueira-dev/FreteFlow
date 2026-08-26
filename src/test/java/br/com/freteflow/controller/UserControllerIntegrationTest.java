package br.com.freteflow.controller;

import br.com.freteflow.AbstractIntegrationTest;
import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void adminShouldPromoteOperatorToAdminSuccessfully() throws Exception {
        String adminToken = createAdminAndGetToken("admin-user-test@freteflow.com");

        User operator = User.builder()
                .name("Operador Comum")
                .email("operator-to-promote@freteflow.com")
                .password(passwordEncoder.encode("senha123"))
                .role(UserRole.OPERATOR)
                .enabled(true)
                .build();
        userRepository.save(operator);

        mockMvc.perform(patch("/api/users/" + operator.getId() + "/promote")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void operatorShouldBeForbiddenFromPromotingUsers() throws Exception {
        String operatorToken = createOperatorAndGetToken("operator-hacker@freteflow.com");

        User target = User.builder()
                .name("Alvo Inocente")
                .email("target-user@freteflow.com")
                .password(passwordEncoder.encode("senha123"))
                .role(UserRole.OPERATOR)
                .enabled(true)
                .build();
        userRepository.save(target);

        mockMvc.perform(patch("/api/users/" + target.getId() + "/promote")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestToPromoteShouldBeRejected() throws Exception {
        // Tenta promover sem enviar o Token JWT (deve tomar 403)
        mockMvc.perform(patch("/api/users/123e4567-e89b-12d3-a456-426614174000/promote"))
                .andExpect(status().isForbidden());
    }
}
