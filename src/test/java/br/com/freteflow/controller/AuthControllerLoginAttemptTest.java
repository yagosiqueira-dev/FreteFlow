package br.com.freteflow.controller;

import br.com.freteflow.dto.auth.LoginDTO;
import br.com.freteflow.security.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthControllerLoginAttemptTest {

    @Test
    void nonCredentialAuthenticationExceptionReleasesEmptyReservation() {
        LoginAttemptService loginAttemptService = new LoginAttemptService();
        for (int index = 0; index < 10_000 - 1; index++) {
            loginAttemptService.isBlocked("filled-" + index + "@example.com");
        }

        AuthenticationManager authenticationManager = authentication -> {
            throw new DisabledException("Account disabled");
        };
        AuthController controller = new AuthController(authenticationManager, null, null, loginAttemptService);

        assertThatThrownBy(() -> controller.login(new LoginDTO("disabled@example.com", "password")))
                .isInstanceOf(DisabledException.class);

        assertThat(loginAttemptService.isBlocked("next@example.com")).isFalse();
    }

    @Test
    void badCredentialsStillCountTowardFiveAttemptLimit() {
        LoginAttemptService loginAttemptService = new LoginAttemptService();
        AuthenticationManager authenticationManager = authentication -> {
            throw new BadCredentialsException("Bad credentials");
        };
        AuthController controller = new AuthController(authenticationManager, null, null, loginAttemptService);
        LoginDTO credentials = new LoginDTO("invalid@example.com", "wrong-password");

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThatThrownBy(() -> controller.login(credentials))
                    .isInstanceOf(BadCredentialsException.class);
        }

        assertThat(loginAttemptService.isBlocked("invalid@example.com")).isTrue();
    }
}
