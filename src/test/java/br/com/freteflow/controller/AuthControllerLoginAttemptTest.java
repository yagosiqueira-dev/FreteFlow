package br.com.freteflow.controller;

import br.com.freteflow.dto.auth.LoginDTO;
import br.com.freteflow.dto.error.ErrorResponseDTO;
import br.com.freteflow.entity.User;
import br.com.freteflow.entity.UserRole;
import br.com.freteflow.exception.GlobalExceptionHandler;
import br.com.freteflow.security.LoginAttemptService;
import br.com.freteflow.security.TokenService;
import br.com.freteflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthControllerLoginAttemptTest {

    private static final String TEST_SECRET = "test-only-jwt-secret-long-enough-for-hmac256-validation";

    @Test
    void repeatedAnonymousFailuresDoNotPreventTheVictimFromLoggingIn() {
        LoginAttemptService attempts = new LoginAttemptService();
        User user = testUser("victim@example.com");
        CountingTokenService tokens = new CountingTokenService();
        AuthController controller = new AuthController(
                authenticationManagerFor(user), mock(UserService.class), tokens, attempts);

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(() -> controller.login(new LoginDTO(user.getEmail(), "wrong")))
                    .isInstanceOf(BadCredentialsException.class);
        }
        assertThat(tokens.generated).hasValue(0);

        var response = controller.login(new LoginDTO(user.getEmail(), "correct"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(tokens.validateToken(response.getBody().token())).isEqualTo(user.getEmail());
        assertThat(tokens.generated).hasValue(1);
    }

    @Test
    void arbitraryEmailSaturationDoesNotRejectLoginRequestsGlobally() {
        LoginAttemptService attempts = new LoginAttemptService();
        for (int index = 0; index < 10_000; index++) {
            attempts.loginFailed("registered-" + index + "@example.com");
        }

        User user = testUser("legitimate@example.com");
        AtomicInteger authenticationCalls = new AtomicInteger();
        AuthenticationManager authenticationManager = authentication -> {
            authenticationCalls.incrementAndGet();
            if (!"correct".equals(authentication.getCredentials())) {
                throw new BadCredentialsException("Bad credentials");
            }
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        };
        CountingTokenService tokens = new CountingTokenService();
        AuthController controller = new AuthController(
                authenticationManager, mock(UserService.class), tokens, attempts);

        assertThatThrownBy(() -> controller.login(new LoginDTO("another-arbitrary@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        var successfulResponse = controller.login(new LoginDTO(user.getEmail(), "correct"));

        assertThat(successfulResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(authenticationCalls).hasValue(2);
        assertThat(tokens.generated).hasValue(1);
    }

    @Test
    void invalidCredentialsRemainRejectedAndNeverGenerateJwt() {
        User user = testUser("invalid@example.com");
        CountingTokenService tokens = new CountingTokenService();
        UserService userService = mock(UserService.class);
        AuthController controller = new AuthController(
                authentication -> { throw new BadCredentialsException("Bad credentials"); },
                userService,
                tokens,
                new LoginAttemptService());

        assertThatThrownBy(() -> controller.login(new LoginDTO(user.getEmail(), "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        assertThat(tokens.generated).hasValue(0);
        verifyNoInteractions(userService);
    }

    @Test
    void publicAuthenticationFailuresHaveTheSameGenericResponse() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");

        ResponseEntity<ErrorResponseDTO> invalidPassword = handler.handleAuthenticationFailure(
                new BadCredentialsException("Bad credentials"), request);
        ResponseEntity<ErrorResponseDTO> disabledAccount = handler.handleAuthenticationFailure(
                new DisabledException("Account disabled"), request);

        assertThat(disabledAccount.getStatusCode()).isEqualTo(invalidPassword.getStatusCode());
        assertThat(disabledAccount.getBody()).isNotNull();
        assertThat(invalidPassword.getBody()).isNotNull();
        assertThat(disabledAccount.getBody().status()).isEqualTo(invalidPassword.getBody().status());
        assertThat(disabledAccount.getBody().error()).isEqualTo(invalidPassword.getBody().error());
        assertThat(disabledAccount.getBody().message()).isEqualTo(invalidPassword.getBody().message());
        assertThat(disabledAccount.getBody().path()).isEqualTo(invalidPassword.getBody().path());
        assertThat(disabledAccount.getBody().message()).isEqualTo("E-mail ou senha inválidos");
    }

    private AuthenticationManager authenticationManagerFor(User user) {
        return authentication -> {
            if (!"correct".equals(authentication.getCredentials())) {
                throw new BadCredentialsException("Bad credentials");
            }
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        };
    }

    private User testUser(String email) {
        return User.builder()
                .name("Usuário de teste")
                .email(email)
                .password("encoded-test-password")
                .role(UserRole.OPERATOR)
                .enabled(true)
                .build();
    }

    private static final class CountingTokenService extends TokenService {
        private final AtomicInteger generated = new AtomicInteger();

        private CountingTokenService() {
            super(TEST_SECRET);
        }

        @Override
        public String generateToken(User user) {
            generated.incrementAndGet();
            return super.generateToken(user);
        }
    }
}
