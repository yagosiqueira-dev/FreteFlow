package br.com.freteflow.security;

import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // Injeta manualmente um secret de teste no atributo privado @Value("${api.security.token.secret}")
        ReflectionTestUtils.setField(tokenService, "secret", "secret_de_teste_super_seguro_123");
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        // ARRANGE
        User user = User.builder()
                .email("joao@freteflow.com")
                .role(UserRole.OPERATOR)
                .build();

        // ACT
        String token = tokenService.generateToken(user);
        String subject = tokenService.validateToken(token);

        // ASSERT
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(subject).isEqualTo("joao@freteflow.com");
    }

    @Test
    void shouldReturnEmptyStringForInvalidToken() {
        // ARRANGE
        String invalidToken = "token_invalido_qualquer";

        // ACT
        String subject = tokenService.validateToken(invalidToken);

        // ASSERT
        assertThat(subject).isEmpty();
    }
}
